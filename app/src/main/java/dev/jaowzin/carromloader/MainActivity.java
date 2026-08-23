package dev.jaowzin.carromloader;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import dev.jaowzin.carromloader.engine.CarromGameEngine;
import dev.jaowzin.carromloader.engine.GameEngine;
import dev.jaowzin.carromloader.overlay.GuideOverlayService;
import dev.jaowzin.carromloader.runtime.ActivityAttachProbeService;
import dev.jaowzin.carromloader.runtime.ApplicationAttachProbeService;
import dev.jaowzin.carromloader.runtime.ApplicationOnCreateProbeService;
import dev.jaowzin.carromloader.runtime.ControlledRuntimeService;
import dev.jaowzin.carromloader.runtime.RuntimeHostActivity;
import dev.jaowzin.carromloader.runtime.RuntimeReportStore;

public final class MainActivity extends Activity {
    private final GameEngine engine = new CarromGameEngine();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView status;
    private TextView runtimeStatus;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(32), dp(24), dp(32));
        root.setBackgroundColor(0xFFF4F4F4);
        scrollView.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView title = new TextView(this);
        title.setText("Carrom Loader");
        title.setTextSize(28f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(0xFF111111);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Clean-room CTF companion • controlled runtime diagnostics");
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
        root.addView(action("5. Open runtime host shell", v -> openRuntimeHost()));
        root.addView(action("6. Attach CarromApplication (no onCreate)", v -> attachTargetApplication()));
        root.addView(action("7. Run CarromApplication.onCreate", v -> runTargetApplicationOnCreate()));
        root.addView(action("8. Attach CarromActivity (no onCreate)", v -> runTargetActivityAttach()));
        root.addView(action("Refresh runtime report", v -> {
            refreshRuntimeReport();
            scrollToReport();
        }));
        root.addView(action("Copy runtime report", v -> copyRuntimeReport()));
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
        runtimeStatus.setMinHeight(dp(180));
        root.addView(runtimeStatus, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView info = new TextView(this);
        info.setText("Phase 6 rebuilds the already-passing CarromApplication lifecycle inside :activity_probe, then creates and attaches CarromActivity with its ActivityInfo, theme, Intent, token and Application. CarromActivity.onCreate() is intentionally not called yet.");
        info.setTextSize(13f);
        info.setTextColor(0xFF555555);
        info.setPadding(0, dp(18), 0, 0);
        root.addView(info);

        setContentView(scrollView);
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
        scrollToReport();
        handler.postDelayed(() -> { refreshRuntimeReport(); scrollToReport(); }, 1200L);
        toast("Controlled runtime probe started");
    }

    private void openRuntimeHost() {
        if (!engine.isAvailable(this)) {
            toast("Carrom Pool is not installed");
            return;
        }
        startActivity(new Intent(this, RuntimeHostActivity.class));
    }

    private void attachTargetApplication() {
        if (!engine.isAvailable(this)) {
            toast("Carrom Pool is not installed");
            return;
        }
        Intent intent = new Intent(this, ApplicationAttachProbeService.class)
                .setAction(ApplicationAttachProbeService.ACTION_ATTACH)
                .putExtra(ApplicationAttachProbeService.EXTRA_PACKAGE, engine.getTargetPackage());
        startService(intent);
        runtimeStatus.setText("Starting :app_probe and waiting for first checkpoint…");
        scrollToReport();
        scheduleReportRefreshes();
        toast("Application attach probe started");
    }

    private void runTargetApplicationOnCreate() {
        if (!engine.isAvailable(this)) {
            toast("Carrom Pool is not installed");
            return;
        }
        Intent intent = new Intent(this, ApplicationOnCreateProbeService.class)
                .setAction(ApplicationOnCreateProbeService.ACTION_RUN)
                .putExtra(ApplicationOnCreateProbeService.EXTRA_PACKAGE, engine.getTargetPackage());
        startService(intent);
        runtimeStatus.setText("Running CarromApplication.onCreate in isolated :oncreate_probe…");
        scrollToReport();
        scheduleReportRefreshes();
        toast("Application onCreate probe started");
    }

    private void runTargetActivityAttach() {
        if (!engine.isAvailable(this)) {
            toast("Carrom Pool is not installed");
            return;
        }
        Intent intent = new Intent(this, ActivityAttachProbeService.class)
                .setAction(ActivityAttachProbeService.ACTION_RUN)
                .putExtra(ActivityAttachProbeService.EXTRA_PACKAGE, engine.getTargetPackage());
        startService(intent);
        runtimeStatus.setText("Attaching CarromActivity in isolated :activity_probe…");
        scrollToReport();
        scheduleReportRefreshes();
        toast("Activity attach probe started");
    }

    private void scheduleReportRefreshes() {
        long[] delays = {150L, 350L, 700L, 1500L, 3000L, 5000L, 8000L};
        for (long delay : delays) {
            handler.postDelayed(() -> {
                refreshRuntimeReport();
                scrollToReport();
            }, delay);
        }
    }

    private void copyRuntimeReport() {
        String report = RuntimeReportStore.read(this);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Carrom Loader runtime report", report));
            toast("Runtime report copied");
        }
    }

    private void scrollToReport() {
        if (scrollView == null || runtimeStatus == null) return;
        scrollView.post(() -> scrollView.smoothScrollTo(0, runtimeStatus.getTop()));
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
