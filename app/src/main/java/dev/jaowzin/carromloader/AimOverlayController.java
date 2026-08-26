package dev.jaowzin.carromloader;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.appcompat.view.WindowCallbackWrapper;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

final class AimOverlayController implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "carrom_loader_aim_overlay";
    private static final AtomicBoolean INSTALLED = new AtomicBoolean(false);
    private static volatile AimOverlayController ACTIVE;

    private final Set<Activity> resumed = Collections.newSetFromMap(new WeakHashMap<>());
    private volatile boolean enabled;
    private volatile boolean bankPreview;

    private AimOverlayController(boolean enabled, boolean bankPreview) {
        this.enabled = enabled;
        this.bankPreview = bankPreview;
    }

    static void install(Application targetApplication, boolean linesEnabled, boolean bankPreview) {
        if (targetApplication == null) return;
        NativeAimBridge.ensureStarted();
        if (!INSTALLED.compareAndSet(false, true)) {
            updateFeatures(linesEnabled, bankPreview);
            return;
        }
        AimOverlayController controller = new AimOverlayController(linesEnabled, bankPreview);
        ACTIVE = controller;
        targetApplication.registerActivityLifecycleCallbacks(controller);
    }

    static void updateFeatures(boolean linesEnabled, boolean bankPreview) {
        AimOverlayController controller = ACTIVE;
        if (controller == null) return;
        controller.applyFeatures(linesEnabled, bankPreview);
    }

    private void applyFeatures(boolean linesEnabled, boolean newBankPreview) {
        boolean bankChanged = bankPreview != newBankPreview;
        enabled = linesEnabled;
        bankPreview = newBankPreview;

        Activity[] activities = resumed.toArray(new Activity[0]);
        for (Activity activity : activities) {
            if (activity == null) continue;
            if (!enabled) {
                detach(activity);
            } else if (bankChanged) {
                detach(activity);
                attach(activity);
            } else {
                attach(activity);
            }
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        resumed.add(activity);
        NativeAimBridge.ensureStarted();
        if (enabled) attach(activity); else detach(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        resumed.remove(activity);
    }

    private void attach(Activity activity) {
        if (!enabled || activity == null) return;
        Window window = activity.getWindow();
        if (window == null) return;
        View decor = window.getDecorView();
        if (!(decor instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) decor;

        TrajectoryOverlayView overlay = null;
        View existing = group.findViewWithTag(TAG);
        if (existing instanceof TrajectoryOverlayView) overlay = (TrajectoryOverlayView) existing;
        if (overlay == null) {
            overlay = new TrajectoryOverlayView(activity, bankPreview);
            overlay.setTag(TAG);
            group.addView(overlay, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
        }
        overlay.startVision(window);

        Window.Callback callback = window.getCallback();
        if (!(callback instanceof TouchObserverCallback)) {
            window.setCallback(new TouchObserverCallback(callback, overlay));
        }
    }

    private void detach(Activity activity) {
        if (activity == null) return;
        Window window = activity.getWindow();
        if (window == null) return;

        Window.Callback callback = window.getCallback();
        if (callback instanceof TouchObserverCallback) {
            window.setCallback(((TouchObserverCallback) callback).wrapped());
        }

        View decor = window.getDecorView();
        if (decor instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) decor;
            View overlay = group.findViewWithTag(TAG);
            if (overlay instanceof TrajectoryOverlayView) {
                ((TrajectoryOverlayView) overlay).stopVision();
                group.removeView(overlay);
            }
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        resumed.remove(activity);
        detach(activity);
    }

    @Override public void onActivityCreated(Activity activity, Bundle state) {}
    @Override public void onActivityStarted(Activity activity) {}
    @Override public void onActivityStopped(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) {}

    private static final class TouchObserverCallback extends WindowCallbackWrapper {
        private final Window.Callback original;
        private final TrajectoryOverlayView overlay;

        TouchObserverCallback(Window.Callback original, TrajectoryOverlayView overlay) {
            super(original);
            this.original = original;
            this.overlay = overlay;
        }

        Window.Callback wrapped() { return original; }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            overlay.observeTouch(event);
            return super.dispatchTouchEvent(event);
        }
    }
}
