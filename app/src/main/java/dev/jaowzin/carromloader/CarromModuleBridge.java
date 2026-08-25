package dev.jaowzin.carromloader;

import android.app.Application;
import android.util.Log;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Bridge point for our Carrom-specific CTF modules.
 * No anti-cheat/integrity bypass lives here. The first milestone is only proving
 * that Carrom really starts inside the ready-made virtual engine.
 */
public final class CarromModuleBridge {
    private static final String TAG = "CarromModuleBridge";
    private static final AtomicReference<String> LAST = new AtomicReference<>("waiting for virtual Carrom");

    private CarromModuleBridge() {}

    public static void note(String stage, String processName, int userId) {
        String value = stage + " process=" + processName + " user=" + userId;
        LAST.set(value);
        Log.i(TAG, value);
    }

    public static void onTargetApplicationReady(Application application, String processName, int userId) {
        String value = "CarromApplication READY in virtual engine"
                + "\nprocess=" + processName
                + "\nuser=" + userId
                + "\napplication=" + (application == null ? "null" : application.getClass().getName());
        LAST.set(value);
        Log.i(TAG, value.replace('\n', ' '));
    }

    public static String status() {
        return LAST.get();
    }
}
