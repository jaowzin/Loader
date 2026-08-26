package dev.jaowzin.carromloader;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.entity.pm.InstallResult;

public final class MainActivity extends Activity {
    private static final String TARGET = "com.miniclip.carrom";
    private static final int USER_ID = 0;

    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private TextView status;
    private TextView log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(28));
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView title = new TextView(this);
        title.setText("Carrom Loader");
        title.setTextSize(28f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Carrom Runtime • dual-app engine");
        subtitle.setTextSize(14f);
        subtitle.setPadding(0, dp(4), 0, dp(18));
        root.addView(subtitle);

        status = new TextView(this);
        status.setTextSize(14f);
        status.setPadding(dp(12), dp(12), dp(12), dp(12));
        root.addView(status, matchWrap());

        root.addView(button("1. Clone Carrom into runtime", v -> installVirtual()));
        root.addView(button("2. Launch virtual Carrom", v -> launchVirtual()));
        root.addView(button("3. Remove virtual Carrom", v -> uninstallVirtual()));
        root.addView(button("Refresh runtime status", v -> refreshStatus()));

        TextView logTitle = new TextView(this);
        logTitle.setText("Runtime / Carrom module status");
        logTitle.setTypeface(Typeface.DEFAULT_BOLD);
        logTitle.setPadding(0, dp(20), 0, dp(6));
        root.addView(logTitle);

        log = new TextView(this);
        log.setTextSize(12f);
        log.setTextIsSelectable(true);
        log.setPadding(dp(12), dp(12), dp(12), dp(12));
        log.setMinHeight(dp(150));
        root.addView(log, matchWrap());

        TextView info = new TextView(this);
        info.setText("Carrom Runtime hosts the virtualized game process. "
                + "Carrom-specific trajectory and line modules are kept separately so they can evolve without changing the app container.");
        info.setTextSize(13f);
        info.setPadding(0, dp(18), 0, 0);
        root.addView(info);

        setContentView(scroll);
        refreshStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void installVirtual() {
        if (!isHostCarromInstalled()) {
            toast("Carrom Pool is not installed on this device");
            return;
        }
        setLog("Cloning installed Carrom into runtime user 0…");
        worker.execute(() -> {
            try {
                InstallResult result = BlackBoxCore.get().installPackageAsUser(TARGET, USER_ID);
                String message = result.success
                        ? "INSTALL SUCCESS\npackage=" + result.packageName
                        : "INSTALL FAILED\n" + result.msg;
                runOnUiThread(() -> {
                    setLog(message);
                    refreshStatus();
                });
            } catch (Throwable error) {
                runOnUiThread(() -> setLog("INSTALL ERROR\n" + error));
            }
        });
    }

    private void launchVirtual() {
        setLog("Launching Carrom through Carrom Runtime…");
        worker.execute(() -> {
            try {
                boolean launched = BlackBoxCore.get().launchApk(TARGET, USER_ID);
                runOnUiThread(() -> {
                    setLog("launchApk=" + launched + "\n\n" + CarromModuleBridge.status());
                    if (!launched) toast("Virtual launch returned false");
                });
            } catch (Throwable error) {
                runOnUiThread(() -> setLog("LAUNCH ERROR\n" + error));
            }
        });
    }

    private void uninstallVirtual() {
        worker.execute(() -> {
            try {
                BlackBoxCore.get().uninstallPackageAsUser(TARGET, USER_ID);
                runOnUiThread(() -> {
                    setLog("Virtual Carrom removed");
                    refreshStatus();
                });
            } catch (Throwable error) {
                runOnUiThread(() -> setLog("UNINSTALL ERROR\n" + error));
            }
        });
    }

    private void refreshStatus() {
        boolean hostInstalled = isHostCarromInstalled();
        boolean virtualInstalled = false;
        boolean services = false;
        try {
            virtualInstalled = BlackBoxCore.get().isInstalled(TARGET, USER_ID);
            services = BlackBoxCore.get().areServicesAvailable();
        } catch (Throwable ignored) {
        }
        if (status != null) {
            status.setText("Engine: Carrom Runtime"
                    + "\nRuntime services: " + (services ? "READY" : "STARTING/NOT READY")
                    + "\nCarrom installed on device: " + (hostInstalled ? "YES" : "NO")
                    + "\nCarrom cloned in runtime: " + (virtualInstalled ? "YES" : "NO")
                    + "\nVirtual user: " + USER_ID);
        }
        if (log != null) {
            log.setText(CarromModuleBridge.status());
        }
    }

    private boolean isHostCarromInstalled() {
        try {
            getPackageManager().getPackageInfo(TARGET, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    private Button button(String text, android.view.View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(54)
        );
        lp.setMargins(0, dp(10), 0, 0);
        button.setLayoutParams(lp);
        return button;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void setLog(String text) {
        if (log != null) log.setText(text);
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
