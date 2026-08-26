package dev.jaowzin.carromloader;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class CarromModuleBridge {
    private static final String TAG = "CarromModuleBridge";
    private static final String WAITING = "waiting for virtual Carrom";
    private static final AtomicReference<String> LAST = new AtomicReference<>(WAITING);
    private static final AtomicReference<String> BASE = new AtomicReference<>(WAITING);
    private static final AtomicBoolean MONITORING = new AtomicBoolean(false);
    private static final AtomicBoolean FEATURE_RECEIVER_REGISTERED = new AtomicBoolean(false);
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
        registerFeatureReceiver(statusContext);

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
        String remote = RuntimeStatusChannel.read();
        if (remote != null && !remote.trim().isEmpty()) return remote;
        return LAST.get();
    }

    private static void registerFeatureReceiver(Context context) {
        if (context == null || !FEATURE_RECEIVER_REGISTERED.compareAndSet(false, true)) return;
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ignored, Intent intent) {
                if (intent == null || !FeatureSettings.ACTION_FEATURES_CHANGED.equals(intent.getAction())) return;
                boolean lines = intent.getBooleanExtra(FeatureSettings.EXTRA_LINES, false);
                boolean bank = intent.getBooleanExtra(FeatureSettings.EXTRA_BANK, false);
                AimOverlayController.updateFeatures(lines, bank);
                String current = BASE.get();
                int marker = current.indexOf("\nlines=");
                if (marker >= 0) current = current.substring(0, marker);
                BASE.set(current
                        + "\nlines=" + (lines ? "ON" : "OFF")
                        + "\nbankPreview=" + (bank ? "ON" : "OFF"));
                publishNativeStatus();
            }
        };
        IntentFilter filter = new IntentFilter(FeatureSettings.ACTION_FEATURES_CHANGED);
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                context.registerReceiver(receiver, filter);
            }
        } catch (Throwable error) {
            FEATURE_RECEIVER_REGISTERED.set(false);
            Log.w(TAG, "feature receiver registration failed", error);
        }
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
        if (value == null || value.trim().isEmpty()) return;
        RuntimeStatusChannel.publish(value);
    }
}
