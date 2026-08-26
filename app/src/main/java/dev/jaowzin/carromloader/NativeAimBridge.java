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
        final int playerId;
        final double positionAgeMs;
        final double angleAgeMs;

        State(int status, double worldX, double worldY, double angle,
              int playerId, double positionAgeMs, double angleAgeMs) {
            this.status = status;
            this.worldX = worldX;
            this.worldY = worldY;
            this.angle = angle;
            this.playerId = playerId;
            this.positionAgeMs = positionAgeMs;
            this.angleAgeMs = angleAgeMs;
        }

        boolean hooked() { return status == 2; }

        boolean positionFresh() {
            return hooked() && Double.isFinite(worldX) && Double.isFinite(worldY)
                    && positionAgeMs >= 0.0 && positionAgeMs < 1200.0;
        }

        boolean angleFresh() {
            return hooked() && Double.isFinite(angle)
                    && angleAgeMs >= 0.0 && angleAgeMs < 900.0;
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
        if (!ensureLibrary()) return new State(-10, Double.NaN, Double.NaN, Double.NaN, -1, -1, -1);
        try {
            nativeStart();
            double[] values = nativeSnapshot();
            if (values == null || values.length < 7) {
                return new State(-11, Double.NaN, Double.NaN, Double.NaN, -1, -1, -1);
            }
            return new State((int) values[0], values[1], values[2], values[3],
                    (int) values[4], values[5], values[6]);
        } catch (Throwable error) {
            Log.w(TAG, "nativeSnapshot failed", error);
            return new State(-12, Double.NaN, Double.NaN, Double.NaN, -1, -1, -1);
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
        return "nativeAim=" + status
                + " world=(" + fmt(state.worldX) + "," + fmt(state.worldY) + ")"
                + " angle=" + fmt(state.angle)
                + " player=" + state.playerId
                + " age=" + Math.round(Math.max(state.positionAgeMs, state.angleAgeMs)) + "ms";
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
