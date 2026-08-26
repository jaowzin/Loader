package dev.jaowzin.carromloader;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.concurrent.atomic.AtomicReference;

public final class CarromModuleBridge {
    private static final String TAG = "CarromModuleBridge";
    private static final AtomicReference<String> LAST = new AtomicReference<>("waiting for virtual Carrom");

    private CarromModuleBridge() {}

    public static void note(String stage, String processName, int userId) {
        String value = stage + " process=" + processName + " user=" + userId;
        LAST.set(value);
        Log.i(TAG, value);
    }

    public static void onTargetApplicationReady(
            Application application,
            String processName,
            int userId,
            Context hostContext
    ) {
        boolean lines = FeatureSettings.linesEnabled(hostContext);
        boolean bank = FeatureSettings.bankPreviewEnabled(hostContext);
        NativeAimBridge.ensureStarted();
        AimOverlayController.install(application, lines, bank);

        String value = "CarromApplication READY in virtual engine"
                + "\nprocess=" + processName
                + "\nuser=" + userId
                + "\napplication=" + (application == null ? "null" : application.getClass().getName())
                + "\nlines=" + (lines ? "ON" : "OFF")
                + "\nbankPreview=" + (bank ? "ON" : "OFF")
                + "\n" + NativeAimBridge.summary()
                + "\nautoPlay=NEXT_STAGE";
        LAST.set(value);
        Log.i(TAG, value.replace('\n', ' '));
    }

    public static String status() {
        return LAST.get() + "\n" + NativeAimBridge.summary();
    }
}
