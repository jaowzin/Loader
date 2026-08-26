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
constexpr uintptr_t kGetStrikerRva = 0x01A85320;
constexpr uintptr_t kGetAngleRva = 0x01A854A0;
constexpr uintptr_t kGetStrikerSelectorRva = 0x01294FF0;
constexpr uintptr_t kGetAngleSelectorRva = 0x012A01C0;

struct MCPoint {
    double x;
    double y;
};

using GetStrikerFn = MCPoint (*)(void *, void *, int);
using GetAngleFn = double (*)(void *, void *, int);

GetStrikerFn gOriginalGetStriker = nullptr;
GetAngleFn gOriginalGetAngle = nullptr;
std::atomic<int> gStatus{0};
std::atomic<double> gWorldX{NAN};
std::atomic<double> gWorldY{NAN};
std::atomic<double> gAngle{NAN};
std::atomic<int> gPlayer{-1};
std::atomic<int64_t> gPositionNs{0};
std::atomic<int64_t> gAngleNs{0};
std::atomic<uintptr_t> gModuleBase{0};
std::atomic<bool> gCallbackRegistered{false};
std::atomic<bool> gInstalling{false};

int installHooks();

int64_t monotonicNs() {
    timespec ts{};
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return static_cast<int64_t>(ts.tv_sec) * 1000000000LL + ts.tv_nsec;
}

bool isCarromImage(const char *name) {
    return name && *name &&
           (std::strstr(name, "libgame-CARROM") || std::strstr(name, "libcarrom.so"));
}

MCPoint hookGetStriker(void *self, void *selector, int playerId) {
    MCPoint point = gOriginalGetStriker ? gOriginalGetStriker(self, selector, playerId) : MCPoint{0.0, 0.0};
    if (std::isfinite(point.x) && std::isfinite(point.y)) {
        gWorldX.store(point.x, std::memory_order_relaxed);
        gWorldY.store(point.y, std::memory_order_relaxed);
        gPlayer.store(playerId, std::memory_order_relaxed);
        gPositionNs.store(monotonicNs(), std::memory_order_release);
    }
    return point;
}

double hookGetAngle(void *self, void *selector, int playerId) {
    double value = gOriginalGetAngle ? gOriginalGetAngle(self, selector, playerId) : 0.0;
    if (std::isfinite(value)) {
        gAngle.store(value, std::memory_order_relaxed);
        gPlayer.store(playerId, std::memory_order_relaxed);
        gAngleNs.store(monotonicNs(), std::memory_order_release);
    }
    return value;
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

void onImageLoaded(const char *imageName, void *) {
    if (!isCarromImage(imageName)) return;
    __android_log_print(ANDROID_LOG_INFO, kTag, "Carrom native image loaded: %s", imageName);
    gStatus.store(1, std::memory_order_release);
    installHooks();
}

void ensureImageCallbackRegistered() {
#if defined(__aarch64__)
    bool expected = false;
    if (gCallbackRegistered.compare_exchange_strong(expected, true, std::memory_order_acq_rel)) {
        dobby_register_image_load_callback(onImageLoaded);
        __android_log_print(ANDROID_LOG_INFO, kTag, "registered native image load callback");
    }
#endif
}

int installHooks() {
#if !defined(__aarch64__)
    gStatus.store(-1, std::memory_order_release);
    return -1;
#else
    ensureImageCallbackRegistered();
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
        !selectorMatches(base, kGetAngleSelectorRva, "controlsGetAngle:")) {
        __android_log_print(ANDROID_LOG_WARN, kTag, "Carrom build signature mismatch base=%p", reinterpret_cast<void *>(base));
        gStatus.store(-2, std::memory_order_release);
        gInstalling.store(false, std::memory_order_release);
        return -2;
    }

    void *getStriker = reinterpret_cast<void *>(base + kGetStrikerRva);
    void *getAngle = reinterpret_cast<void *>(base + kGetAngleRva);
    int first = DobbyHook(getStriker, reinterpret_cast<void *>(hookGetStriker), reinterpret_cast<void **>(&gOriginalGetStriker));
    int second = DobbyHook(getAngle, reinterpret_cast<void *>(hookGetAngle), reinterpret_cast<void **>(&gOriginalGetAngle));
    if (first != 0 || second != 0 || !gOriginalGetStriker || !gOriginalGetAngle) {
        __android_log_print(ANDROID_LOG_ERROR, kTag, "DobbyHook failed striker=%d angle=%d", first, second);
        gStatus.store(-3, std::memory_order_release);
        gInstalling.store(false, std::memory_order_release);
        return -3;
    }

    __android_log_print(ANDROID_LOG_INFO, kTag, "native aim hooks installed base=%p", reinterpret_cast<void *>(base));
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
    jdouble values[7];
    values[0] = static_cast<jdouble>(gStatus.load(std::memory_order_acquire));
    values[1] = static_cast<jdouble>(gWorldX.load(std::memory_order_relaxed));
    values[2] = static_cast<jdouble>(gWorldY.load(std::memory_order_relaxed));
    values[3] = static_cast<jdouble>(gAngle.load(std::memory_order_relaxed));
    values[4] = static_cast<jdouble>(gPlayer.load(std::memory_order_relaxed));
    values[5] = static_cast<jdouble>(ageMs(gPositionNs.load(std::memory_order_acquire)));
    values[6] = static_cast<jdouble>(ageMs(gAngleNs.load(std::memory_order_acquire)));
    jdoubleArray result = env->NewDoubleArray(7);
    if (!result) return nullptr;
    env->SetDoubleArrayRegion(result, 0, 7, values);
    return result;
}
