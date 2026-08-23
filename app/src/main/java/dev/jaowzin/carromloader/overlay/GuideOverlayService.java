package dev.jaowzin.carromloader.overlay;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import dev.jaowzin.carromloader.MainActivity;
import dev.jaowzin.carromloader.engine.CarromGameEngine;

public final class GuideOverlayService extends Service {
    private static final String CHANNEL_ID = "carrom_loader_overlay";
    private static final int NOTIFICATION_ID = 7;

    private WindowManager windowManager;
    private GuideOverlayView guideView;
    private View controllerView;
    private WindowManager.LayoutParams controllerParams;
    private TextView status;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }

        startForeground(NOTIFICATION_ID, buildNotification());
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        showGuide();
        showController();
    }

    private android.app.Notification buildNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Carrom guide overlay",
                NotificationManager.IMPORTANCE_LOW
        );
        manager.createNotificationChannel(channel);

        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(
                this,
                0,
                open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new android.app.Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentTitle("Carrom Loader")
                .setContentText("Guide overlay running")
                .setOngoing(true)
                .setContentIntent(pending)
                .build();
    }

    private void showGuide() {
        guideView = new GuideOverlayView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        windowManager.addView(guideView, params);
    }

    private void showController() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(8);
        root.setPadding(pad, pad, pad, pad);

        GradientDrawable background = new GradientDrawable();
        background.setColor(0xE6222222);
        background.setCornerRadius(dp(12));
        root.setBackground(background);

        TextView drag = new TextView(this);
        drag.setText("CARROM GUIDE  •  drag");
        drag.setTextColor(0xFFFFFFFF);
        drag.setTextSize(13f);
        drag.setPadding(dp(6), dp(4), dp(6), dp(8));
        root.addView(drag);

        status = new TextView(this);
        status.setTextColor(0xFFDDDDDD);
        status.setTextSize(12f);
        root.addView(status);

        LinearLayout rotation = horizontal();
        rotation.addView(button("⟲ 2°", v -> { guideView.rotate(-2f); refreshStatus(); }));
        rotation.addView(button("2° ⟳", v -> { guideView.rotate(2f); refreshStatus(); }));
        root.addView(rotation);

        LinearLayout vertical = horizontal();
        vertical.addView(button("▲", v -> guideView.move(0f, -20f)));
        vertical.addView(button("▼", v -> guideView.move(0f, 20f)));
        root.addView(vertical);

        LinearLayout horizontal = horizontal();
        horizontal.addView(button("◀", v -> guideView.move(-20f, 0f)));
        horizontal.addView(button("▶", v -> guideView.move(20f, 0f)));
        root.addView(horizontal);

        LinearLayout length = horizontal();
        length.addView(button("Line -", v -> { guideView.changeLength(-100f); refreshStatus(); }));
        length.addView(button("Line +", v -> { guideView.changeLength(100f); refreshStatus(); }));
        root.addView(length);

        LinearLayout actions = horizontal();
        actions.addView(button("Open game", v -> new CarromGameEngine().launch(this)));
        actions.addView(button("Close", v -> stopSelf()));
        root.addView(actions);

        controllerParams = new WindowManager.LayoutParams(
                dp(220),
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        controllerParams.gravity = Gravity.TOP | Gravity.START;
        controllerParams.x = dp(12);
        controllerParams.y = dp(100);

        final int[] startX = new int[1];
        final int[] startY = new int[1];
        final float[] touchX = new float[1];
        final float[] touchY = new float[1];
        drag.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startX[0] = controllerParams.x;
                    startY[0] = controllerParams.y;
                    touchX[0] = event.getRawX();
                    touchY[0] = event.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    controllerParams.x = startX[0] + Math.round(event.getRawX() - touchX[0]);
                    controllerParams.y = startY[0] + Math.round(event.getRawY() - touchY[0]);
                    windowManager.updateViewLayout(controllerView, controllerParams);
                    return true;
                default:
                    return true;
            }
        });

        controllerView = root;
        windowManager.addView(controllerView, controllerParams);
        refreshStatus();
    }

    private LinearLayout horizontal() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        return row;
    }

    private Button button(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(11f);
        button.setAllCaps(false);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(42), 1f);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        button.setLayoutParams(lp);
        return button;
    }

    private void refreshStatus() {
        if (status == null || guideView == null) return;
        status.setText(String.format("angle %.0f°  •  line %.0fpx", guideView.getAngleDegrees(), guideView.getTotalLength()));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (windowManager != null) {
            if (controllerView != null) {
                try { windowManager.removeView(controllerView); } catch (Throwable ignored) { }
            }
            if (guideView != null) {
                try { windowManager.removeView(guideView); } catch (Throwable ignored) { }
            }
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
