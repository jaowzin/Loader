package dev.jaowzin.carromloader;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.appcompat.view.WindowCallbackWrapper;

import java.util.concurrent.atomic.AtomicBoolean;

final class AimOverlayController implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "carrom_loader_aim_overlay";
    private static final AtomicBoolean INSTALLED = new AtomicBoolean(false);

    private final boolean bankPreview;

    private AimOverlayController(boolean bankPreview) {
        this.bankPreview = bankPreview;
    }

    static void install(Application targetApplication, boolean linesEnabled, boolean bankPreview) {
        if (targetApplication == null || !linesEnabled) return;
        if (!INSTALLED.compareAndSet(false, true)) return;
        targetApplication.registerActivityLifecycleCallbacks(new AimOverlayController(bankPreview));
    }

    @Override
    public void onActivityResumed(Activity activity) {
        attach(activity);
    }

    private void attach(Activity activity) {
        Window window = activity.getWindow();
        if (window == null) return;
        View decor = window.getDecorView();
        if (!(decor instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) decor;

        TrajectoryOverlayView overlay = null;
        View existing = group.findViewWithTag(TAG);
        if (existing instanceof TrajectoryOverlayView) {
            overlay = (TrajectoryOverlayView) existing;
        }
        if (overlay == null) {
            overlay = new TrajectoryOverlayView(activity, bankPreview);
            overlay.setTag(TAG);
            group.addView(overlay, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
        }

        Window.Callback callback = window.getCallback();
        if (!(callback instanceof TouchObserverCallback)) {
            window.setCallback(new TouchObserverCallback(callback, overlay));
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Window window = activity.getWindow();
        if (window == null) return;
        Window.Callback callback = window.getCallback();
        if (callback instanceof TouchObserverCallback) {
            window.setCallback(((TouchObserverCallback) callback).wrapped());
        }
        View decor = window.getDecorView();
        if (decor instanceof ViewGroup) {
            View overlay = ((ViewGroup) decor).findViewWithTag(TAG);
            if (overlay != null) ((ViewGroup) decor).removeView(overlay);
        }
    }

    @Override public void onActivityCreated(Activity activity, Bundle state) {}
    @Override public void onActivityStarted(Activity activity) {}
    @Override public void onActivityPaused(Activity activity) {}
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

        Window.Callback wrapped() {
            return original;
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            overlay.observeTouch(event);
            return super.dispatchTouchEvent(event);
        }
    }
}
