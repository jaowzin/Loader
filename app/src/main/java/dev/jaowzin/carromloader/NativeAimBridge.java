package dev.jaowzin.carromloader;

import android.util.Log;

final class NativeAimBridge {
    private static final String TAG = "carrom_native_aim";
    private static volatile boolean loadAttempted;
    private static volatile boolean libraryLoaded;

    static final class State {
        final int status;
        final double worldX;
        final double worldY;
        final double angle;
        final double power;
        final int playerId;
        final double positionAgeMs;
        final double angleAgeMs;
        final double powerAgeMs;

        State(int status, double worldX, double worldY, double angle, double power,
              int playerId, double positionAgeMs, double angleAgeMs, double powerAgeMs) {
            this.status = status;
            this.worldX = worldX;
            this.worldY = worldY;
            this.angle = angle;
            this.power = power;
            this.playerId = playerId;
            this.positionAgeMs = positionAgeMs;
            this.angleAgeMs = angleAgeMs;
            this.powerAgeMs = powerAgeMs;
        }

        boolean hooked() {
            return status == 2;
        }

        boolean positionUsable() {
            return hooked()
                    && Double.isFinite(worldX)
                    && Double.isFinite(worldY)
                    && positionAgeMs >= 0.0;
        }

        boolean angleUsable() {
            return hooked() && Double.isFinite(angle) && angleAgeMs >= 0.0;
        }

        boolean powerUsable() {
            return hooked()
                    && Double.isFinite(power)
                    && power >= 0.0
                    && power <= 1.0
                    && powerAgeMs >= 0.0;
        }
    }

    private NativeAimBridge() {}

    static boolean ensureStarted() {
        if (!ensureLibrary()) return false;
        try {
            return nativeStart() == 2;
        } catch (Throwable error) {
            Log.w(TAG, "nativeStart failed", error);
            return false;
        }
    }

    static State read() {
        if (!ensureLibrary()) return empty(-10);
        try {
            nativeStart();
            double[] values = nativeSnapshot();
            if (values == null || values.length < 9) {
                return empty(-11);
            }
            return new State(
                    (int) values[0],
                    values[1],
                    values[2],
                    values[3],
                    values[4],
                    (int) values[5],
                    values[6],
                    values[7],
                    values[8]
            );
        } catch (Throwable error) {
            Log.w(TAG, "nativeSnapshot failed", error);
            return empty(-12);
        }
    }

    static String summary() {
        State state = read();
        String status;
        switch (state.status) {
            case 2: status = "HOOKED"; break;
            case 1: status = "WAITING_LIB"; break;
            case 0: status = "IDLE"; break;
            case -1: status = "UNSUPPORTED_ABI"; break;
            case -2: status = "BUILD_MISMATCH"; break;
            case -3: status = "HOOK_FAILED"; break;
            default: status = "ERROR(" + state.status + ")"; break;
        }
        if (!state.hooked()) return "nativeAim=" + status;

        long aimAge = Math.round(Math.max(state.angleAgeMs, state.powerAgeMs));
        return "nativeAim=" + status
                + " world=(" + fmt(state.worldX) + "," + fmt(state.worldY) + ")"
                + " angle=" + fmt(state.angle)
                + " power=" + fmt(state.power)
                + " player=" + state.playerId
                + " agePos=" + Math.round(state.positionAgeMs) + "ms"
                + " ageAim=" + aimAge + "ms";
    }

    private static State empty(int status) {
        return new State(
                status,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                -1,
                -1,
                -1,
                -1
        );
    }

    private static synchronized boolean ensureLibrary() {
        if (libraryLoaded) return true;
        if (loadAttempted) return false;
        loadAttempted = true;
        try {
            System.loadLibrary("carromruntime");
            libraryLoaded = true;
            return true;
        } catch (Throwable error) {
            Log.w(TAG, "carromruntime load failed", error);
            return false;
        }
    }

    private static String fmt(double value) {
        if (!Double.isFinite(value)) return "nan";
        return String.format(java.util.Locale.US, "%.4f", value);
    }

    private static native int nativeStart();
    private static native double[] nativeSnapshot();
}
