package dev.jaowzin.carromloader;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;

public final class CarromModuleBridge {
    private static final String TAG = "CarromModuleBridge";
    private static final AtomicReference<String> LAST = new AtomicReference<>("waiting for virtual Carrom");
    private static final AtomicReference<String> BASE = new AtomicReference<>("waiting for virtual Carrom");
    private static final AtomicBoolean MONITORING = new AtomicBoolean(false);
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private static volatile Context statusContext;

    private CarromModuleBridge() {}

    public static void note(String stage, String processName, int userId) {
        String value = stage + " process=" + processName + " user=" + userId;
        LAST.set(value);
        writeStatus(value);
        Log.i(TAG, value);
    }

    public static void onTargetApplicationReady(
            Application application,
            String processName,
            int userId,
            Context hostContext
    ) {
        Context context = hostContext == null ? null : hostContext.getApplicationContext();
        statusContext = context != null ? context : hostContext;

        boolean lines = FeatureSettings.linesEnabled(hostContext);
        boolean bank = FeatureSettings.bankPreviewEnabled(hostContext);
        NativeAimBridge.ensureStarted();
        AimOverlayController.install(application, lines, bank);

        String base = "CarromApplication READY in virtual engine"
                + "\nprocess=" + processName
                + "\nuser=" + userId
                + "\napplication=" + (application == null ? "null" : application.getClass().getName())
                + "\nlines=" + (lines ? "ON" : "OFF")
                + "\nbankPreview=" + (bank ? "ON" : "OFF");
        BASE.set(base);
        publishNativeStatus();
        startNativeMonitor();
    }

    public static String status() {
        Context context = safeRuntimeContext();
        String persisted = ModuleStatusStore.read(context);
        if (persisted != null && !persisted.trim().isEmpty()) return persisted;
        return LAST.get();
    }

    private static void startNativeMonitor() {
        if (!MONITORING.compareAndSet(false, true)) return;
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                publishNativeStatus();
                String summary = NativeAimBridge.summary();
                long delay = summary.contains("HOOKED") ? 650L : 180L;
                HANDLER.postDelayed(this, delay);
            }
        });
    }

    private static void publishNativeStatus() {
        String value = BASE.get()
                + "\n" + NativeAimBridge.summary()
                + "\nautoPlay=NEXT_STAGE";
        LAST.set(value);
        writeStatus(value);
        Log.i(TAG, value.replace('\n', ' '));
    }

    private static void writeStatus(String value) {
        Context context = statusContext;
        if (context == null) context = safeRuntimeContext();
        ModuleStatusStore.write(context, value);
    }

    private static Context safeRuntimeContext() {
        try {
            return CarromRuntimeCore.getContext();
        } catch (Throwable ignored) {
            return null;
        }
    }
}
