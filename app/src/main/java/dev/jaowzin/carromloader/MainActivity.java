package dev.jaowzin.carromloader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import dev.jaowzin.carromloader.engine.CarromGameEngine;
import dev.jaowzin.carromloader.engine.GameEngine;
import dev.jaowzin.carromloader.overlay.GuideOverlayService;
import dev.jaowzin.carromloader.runtime.ControlledRuntimeService;
import dev.jaowzin.carromloader.runtime.RuntimeReportStore;

public final class MainActivity extends Activity {
    private final GameEngine engine = new CarromGameEngine();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView status;
    private TextView runtimeStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(32), dp(24), dp(24));
        root.setBackgroundColor(0xFFF4F4F4);

        TextView title = new TextView(this);
        title.setText("Carrom Loader");
        title.setTextSize(28f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(0xFF111111);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Clean-room CTF companion • controlled runtime phase");
        subtitle.setTextSize(14f);
        subtitle.setTextColor(0xFF555555);
        subtitle.setPadding(0, dp(4), 0, dp(20));
        root.addView(subtitle);

        status = new TextView(this);
        status.setTextSize(15f);
        status.setTextColor(0xFF222222);
        status.setPadding(dp(12), dp(12), dp(12), dp(12));
        status.setBackgroundColor(0xFFE7E7E7);
        root.addView(status, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        root.addView(action("1. Grant overlay permission", v -> requestOverlayPermission()));
        root.addView(action("2. Start trajectory overlay", v -> startOverlay()));
        root.addView(action("3. Open Carrom Pool", v -> launchCarrom()));
        root.addView(action("4. Prepare controlled runtime", v -> prepareControlledRuntime()));
        root.addView(action("Refresh runtime report", v -> refreshRuntimeReport()));
        root.addView(action("Stop overlay", v -> stopService(new Intent(this, GuideOverlayService.class))));

        TextView runtimeTitle = new TextView(this);
        runtimeTitle.setText("Controlled runtime report");
        runtimeTitle.setTypeface(Typeface.DEFAULT_BOLD);
        runtimeTitle.setTextSize(14f);
        runtimeTitle.setTextColor(0xFF222222);
        runtimeTitle.setPadding(0, dp(20), 0, dp(6));
        root.addView(runtimeTitle);

        runtimeStatus = new TextView(this);
        runtimeStatus.setTextSize(11f);
        runtimeStatus.setTextColor(0xFF333333);
        runtimeStatus.setPadding(dp(10), dp(10), dp(10), dp(10));
        runtimeStatus.setBackgroundColor(0xFFFFFFFF);
        runtimeStatus.setTextIsSelectable(true);
        root.addView(runtimeStatus, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView info = new TextView(this);
        info.setText("Phase 2 creates a separate :runtime process and resolves the installed Carrom base APK + splits with an isolated DexClassLoader. The probe only resolves CarromActivity without initializing or launching it; the next lifecycle stage will be built on top of this result.");
        info.setTextSize(13f);
        info.setTextColor(0xFF555555);
        info.setPadding(0, dp(18), 0, 0);
        root.addView(info);

        setContentView(root);
        refreshStatus();
        refreshRuntimeReport();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
        refreshRuntimeReport();
    }

    private Button action(String label, android.view.View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(15f);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52)
        );
        lp.setMargins(0, dp(10), 0, 0);
        button.setLayoutParams(lp);
        return button;
    }

    private void requestOverlayPermission() {
        if (Settings.canDrawOverlays(this)) {
            toast("Overlay permission already granted");
            return;
        }
        Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
        );
        startActivity(intent);
    }

    private void startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
            return;
        }
        Intent intent = new Intent(this, GuideOverlayService.class);
        startForegroundService(intent);
        toast("Guide overlay started");
    }

    private void launchCarrom() {
        if (!engine.isAvailable(this)) {
            toast("Carrom Pool is not installed");
            return;
        }
        if (!engine.launch(this)) {
            toast("Could not launch Carrom Pool");
        }
    }

    private void prepareControlledRuntime() {
        if (!engine.isAvailable(this)) {
            toast("Carrom Pool is not installed");
            return;
        }
        Intent intent = new Intent(this, ControlledRuntimeService.class)
                .setAction(ControlledRuntimeService.ACTION_PREPARE)
                .putExtra(ControlledRuntimeService.EXTRA_PACKAGE, engine.getTargetPackage());
        startService(intent);
        runtimeStatus.setText("Preparing isolated runtime…");
        handler.postDelayed(this::refreshRuntimeReport, 1200L);
        toast("Controlled runtime probe started");
    }

    private void refreshStatus() {
        if (status == null) return;
        boolean installed = engine.isAvailable(this);
        boolean overlay = Settings.canDrawOverlays(this);
        status.setText("Carrom installed: " + (installed ? "YES" : "NO")
                + "\nOverlay permission: " + (overlay ? "YES" : "NO")
                + "\nTarget: " + engine.getTargetPackage());
    }

    private void refreshRuntimeReport() {
        if (runtimeStatus != null) {
            runtimeStatus.setText(RuntimeReportStore.read(this));
        }
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
