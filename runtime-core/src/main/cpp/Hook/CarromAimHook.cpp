#include <jni.h>
#include <android/log.h>
#include <link.h>
#include <atomic>
#include <cmath>
#include <cstdint>
#include <cstring>
#include <ctime>

#include "Dobby/dobby.h"

namespace {
constexpr const char *kTag = "carrom_native_aim";

// MatchController fallbacks (Carrom 19.3.0 / module 1473).
constexpr uintptr_t kGetStrikerRva = 0x01A85320;
constexpr uintptr_t kGetAngleRva = 0x01A854A0;
constexpr uintptr_t kForceStrikerRva = 0x01A85288;
constexpr uintptr_t kGetStrikerSelectorRva = 0x01294FF0;
constexpr uintptr_t kGetAngleSelectorRva = 0x012A01C0;
constexpr uintptr_t kForceStrikerSelectorRva = 0x011E518C;

// ControlsLogicLocal is the live input path used while aiming in normal matches.
constexpr uintptr_t kTouchesMovedRva = 0x018ECB5C;
constexpr uintptr_t kLocalGetPowerRva = 0x018EC8D0;
constexpr uintptr_t kLocalGetAngleRva = 0x018EC994;
constexpr uintptr_t kTouchesMovedSelectorRva = 0x011FF2E3;
constexpr uintptr_t kGetPowerSelectorRva = 0x0105DE84;
constexpr uintptr_t kLocalGetAngleSelectorRva = 0x0109018B;

struct MCPoint {
    double x;
    double y;
};

using GetStrikerFn = MCPoint (*)(void *, void *, int);
using GetAngleFn = double (*)(void *, void *, int);
using LocalDoubleGetterFn = double (*)(void *, void *);
using TouchesMovedFn = void (*)(void *, void *, void *, unsigned int);
using ForceStrikerFn = void (*)(void *, void *, int, MCPoint);

GetStrikerFn gOriginalGetStriker = nullptr;
GetAngleFn gOriginalGetAngle = nullptr;
TouchesMovedFn gOriginalTouchesMoved = nullptr;
ForceStrikerFn gOriginalForceStriker = nullptr;
LocalDoubleGetterFn gLocalGetPower = nullptr;
LocalDoubleGetterFn gLocalGetAngle = nullptr;
void *gGetPowerSelector = nullptr;
void *gLocalGetAngleSelector = nullptr;

std::atomic<int> gStatus{0};
std::atomic<double> gWorldX{NAN};
std::atomic<double> gWorldY{NAN};
std::atomic<double> gAngle{NAN};
std::atomic<double> gPower{NAN};
std::atomic<int> gPlayer{-1};
std::atomic<int64_t> gPositionNs{0};
std::atomic<int64_t> gAngleNs{0};
std::atomic<int64_t> gPowerNs{0};
std::atomic<uintptr_t> gModuleBase{0};
std::atomic<bool> gInstalling{false};

int64_t monotonicNs() {
    timespec ts{};
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return static_cast<int64_t>(ts.tv_sec) * 1000000000LL + ts.tv_nsec;
}

bool isCarromImage(const char *name) {
    return name && *name &&
           (std::strstr(name, "libgame-CARROM") || std::strstr(name, "libcarrom.so"));
}

void capturePosition(const MCPoint &point, int playerId) {
    if (!std::isfinite(point.x) || !std::isfinite(point.y)) return;
    gWorldX.store(point.x, std::memory_order_relaxed);
    gWorldY.store(point.y, std::memory_order_relaxed);
    gPlayer.store(playerId, std::memory_order_relaxed);
    gPositionNs.store(monotonicNs(), std::memory_order_release);
}

void captureAngle(double angle) {
    if (!std::isfinite(angle)) return;
    gAngle.store(angle, std::memory_order_relaxed);
    gAngleNs.store(monotonicNs(), std::memory_order_release);
}

void capturePower(double power) {
    if (!std::isfinite(power)) return;
    if (power < 0.0) power = 0.0;
    if (power > 1.0) power = 1.0;
    gPower.store(power, std::memory_order_relaxed);
    gPowerNs.store(monotonicNs(), std::memory_order_release);
}

MCPoint hookGetStriker(void *self, void *selector, int playerId) {
    MCPoint point = gOriginalGetStriker
            ? gOriginalGetStriker(self, selector, playerId)
            : MCPoint{0.0, 0.0};
    capturePosition(point, playerId);
    return point;
}

double hookGetAngle(void *self, void *selector, int playerId) {
    double value = gOriginalGetAngle ? gOriginalGetAngle(self, selector, playerId) : NAN;
    captureAngle(value);
    gPlayer.store(playerId, std::memory_order_relaxed);
    return value;
}

void hookTouchesMoved(void *self, void *selector, void *touches, unsigned int currentNumTouches) {
    if (gOriginalTouchesMoved) {
        gOriginalTouchesMoved(self, selector, touches, currentNumTouches);
    }

    // Read the fields only after the game's own input handler has updated them.
    // getAngle is a direct _angle read and getPower returns the game's normalized 0..1 power.
    if (!self) return;
    if (gLocalGetAngle) {
        captureAngle(gLocalGetAngle(self, gLocalGetAngleSelector));
    }
    if (gLocalGetPower) {
        capturePower(gLocalGetPower(self, gGetPowerSelector));
    }
}

void hookForceStriker(void *self, void *selector, int playerId, MCPoint point) {
    if (gOriginalForceStriker) {
        gOriginalForceStriker(self, selector, playerId, point);
    }
    capturePosition(point, playerId);
}

struct FindContext { uintptr_t base = 0; };

int findModuleCallback(dl_phdr_info *info, size_t, void *data) {
    if (!info || !data) return 0;
    const char *name = info->dlpi_name;
    if (!isCarromImage(name)) return 0;
    static_cast<FindContext *>(data)->base = static_cast<uintptr_t>(info->dlpi_addr);
    return 1;
}

uintptr_t findModuleBase() {
    FindContext context;
    dl_iterate_phdr(findModuleCallback, &context);
    return context.base;
}

bool selectorMatches(uintptr_t base, uintptr_t rva, const char *expected) {
    if (!base || !expected) return false;
    const char *value = reinterpret_cast<const char *>(base + rva);
    return value && std::strcmp(value, expected) == 0;
}

int installHooks() {
#if !defined(__aarch64__)
    gStatus.store(-1, std::memory_order_release);
    return -1;
#else
    if (gStatus.load(std::memory_order_acquire) == 2) return 2;

    bool expected = false;
    if (!gInstalling.compare_exchange_strong(expected, true, std::memory_order_acq_rel)) {
        return gStatus.load(std::memory_order_acquire);
    }

    uintptr_t base = findModuleBase();
    if (!base) {
        gStatus.store(1, std::memory_order_release);
        gInstalling.store(false, std::memory_order_release);
        return 1;
    }
    gModuleBase.store(base, std::memory_order_release);

    if (!selectorMatches(base, kGetStrikerSelectorRva, "getStrikerPosition:") ||
        !selectorMatches(base, kGetAngleSelectorRva, "controlsGetAngle:") ||
        !selectorMatches(base, kTouchesMovedSelectorRva, "touchesMoved:currentNumTouches:")) {
        __android_log_print(ANDROID_LOG_WARN, kTag,
                            "Carrom build signature mismatch base=%p",
                            reinterpret_cast<void *>(base));
        gStatus.store(-2, std::memory_order_release);
        gInstalling.store(false, std::memory_order_release);
        return -2;
    }

    gLocalGetPower = reinterpret_cast<LocalDoubleGetterFn>(base + kLocalGetPowerRva);
    gLocalGetAngle = reinterpret_cast<LocalDoubleGetterFn>(base + kLocalGetAngleRva);
    gGetPowerSelector = reinterpret_cast<void *>(base + kGetPowerSelectorRva);
    gLocalGetAngleSelector = reinterpret_cast<void *>(base + kLocalGetAngleSelectorRva);

    void *getStriker = reinterpret_cast<void *>(base + kGetStrikerRva);
    void *getAngle = reinterpret_cast<void *>(base + kGetAngleRva);
    void *touchesMoved = reinterpret_cast<void *>(base + kTouchesMovedRva);

    int strikerHook = DobbyHook(getStriker,
                               reinterpret_cast<void *>(hookGetStriker),
                               reinterpret_cast<void **>(&gOriginalGetStriker));
    int angleHook = DobbyHook(getAngle,
                             reinterpret_cast<void *>(hookGetAngle),
                             reinterpret_cast<void **>(&gOriginalGetAngle));
    int liveAimHook = DobbyHook(touchesMoved,
                               reinterpret_cast<void *>(hookTouchesMoved),
                               reinterpret_cast<void **>(&gOriginalTouchesMoved));

    if (strikerHook != 0 || angleHook != 0 || liveAimHook != 0 ||
        !gOriginalGetStriker || !gOriginalGetAngle || !gOriginalTouchesMoved) {
        __android_log_print(ANDROID_LOG_ERROR, kTag,
                            "DobbyHook failed striker=%d angle=%d liveAim=%d",
                            strikerHook, angleHook, liveAimHook);
        gStatus.store(-3, std::memory_order_release);
        gInstalling.store(false, std::memory_order_release);
        return -3;
    }

    // Position updates are helpful but not required for the live-angle path. Keep this
    // optional because a stationary striker can legitimately retain one position sample
    // for the whole turn.
    if (selectorMatches(base, kForceStrikerSelectorRva, "forceStrikerPosition:position:")) {
        int positionHook = DobbyHook(reinterpret_cast<void *>(base + kForceStrikerRva),
                                    reinterpret_cast<void *>(hookForceStriker),
                                    reinterpret_cast<void **>(&gOriginalForceStriker));
        if (positionHook != 0 || !gOriginalForceStriker) {
            __android_log_print(ANDROID_LOG_WARN, kTag,
                                "optional forceStriker hook failed=%d", positionHook);
            gOriginalForceStriker = nullptr;
        }
    }

    __android_log_print(ANDROID_LOG_INFO, kTag,
                        "native live aim hooks installed base=%p",
                        reinterpret_cast<void *>(base));
    gStatus.store(2, std::memory_order_release);
    gInstalling.store(false, std::memory_order_release);
    return 2;
#endif
}

double ageMs(int64_t timestampNs) {
    if (timestampNs <= 0) return -1.0;
    int64_t delta = monotonicNs() - timestampNs;
    if (delta < 0) delta = 0;
    return static_cast<double>(delta) / 1000000.0;
}
}  // namespace

extern "C" JNIEXPORT jint JNICALL
Java_dev_jaowzin_carromloader_NativeAimBridge_nativeStart(JNIEnv *, jclass) {
    return static_cast<jint>(installHooks());
}

extern "C" JNIEXPORT jdoubleArray JNICALL
Java_dev_jaowzin_carromloader_NativeAimBridge_nativeSnapshot(JNIEnv *env, jclass) {
    jdouble values[9];
    values[0] = static_cast<jdouble>(gStatus.load(std::memory_order_acquire));
    values[1] = static_cast<jdouble>(gWorldX.load(std::memory_order_relaxed));
    values[2] = static_cast<jdouble>(gWorldY.load(std::memory_order_relaxed));
    values[3] = static_cast<jdouble>(gAngle.load(std::memory_order_relaxed));
    values[4] = static_cast<jdouble>(gPower.load(std::memory_order_relaxed));
    values[5] = static_cast<jdouble>(gPlayer.load(std::memory_order_relaxed));
    values[6] = static_cast<jdouble>(ageMs(gPositionNs.load(std::memory_order_acquire)));
    values[7] = static_cast<jdouble>(ageMs(gAngleNs.load(std::memory_order_acquire)));
    values[8] = static_cast<jdouble>(ageMs(gPowerNs.load(std::memory_order_acquire)));

    jdoubleArray result = env->NewDoubleArray(9);
    if (!result) return nullptr;
    env->SetDoubleArrayRegion(result, 0, 9, values);
    return result;
}
