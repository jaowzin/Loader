package dev.jaowzin.carromloader;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.jaowzin.carromloader.carrom.CarromTarget;
import dev.jaowzin.carromloader.engine.RuntimeReportStore;
import dev.jaowzin.carromloader.engine.VirtualAppStore;
import dev.jaowzin.carromloader.engine.VirtualPackage;
import dev.jaowzin.carromloader.engine.VirtualRuntimeService;

public final class MainActivity extends Activity {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView status;
    private TextView report;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(30), dp(22), dp(30));
        root.setBackgroundColor(0xFFF4F4F4);
        scrollView.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText("Carrom Loader v2");
        title.setTextSize(28f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(0xFF111111);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Clean-room dual-app engine foundation");
        subtitle.setTextSize(14f);
        subtitle.setTextColor(0xFF555555);
        subtitle.setPadding(0, dp(4), 0, dp(18));
        root.addView(subtitle);

        status = new TextView(this);
        status.setTextSize(14f);
        status.setTextColor(0xFF222222);
        status.setPadding(dp(12), dp(12), dp(12), dp(12));
        status.setBackgroundColor(0xFFE6E6E6);
        root.addView(status, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(action("1. Import Carrom into virtual store", v -> importCarrom()));
        root.addView(action("2. Prepare isolated virtual runtime", v -> prepareRuntime()));
        root.addView(action("3. Clear imported virtual package", v -> clearImported()));
        root.addView(action("Refresh report", v -> {
            refreshReport();
            scrollToReport();
        }));
        root.addView(action("Copy report", v -> copyReport()));

        TextView reportTitle = new TextView(this);
        reportTitle.setText("Virtual engine report");
        reportTitle.setTypeface(Typeface.DEFAULT_BOLD);
        reportTitle.setTextSize(14f);
        reportTitle.setTextColor(0xFF222222);
        reportTitle.setPadding(0, dp(20), 0, dp(6));
        root.addView(reportTitle);

        report = new TextView(this);
        report.setTextSize(11f);
        report.setTextColor(0xFF333333);
        report.setPadding(dp(10), dp(10), dp(10), dp(10));
        report.setBackgroundColor(0xFFFFFFFF);
        report.setTextIsSelectable(true);
        report.setMinHeight(dp(220));
        root.addView(report, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView info = new TextView(this);
        info.setText("v2.0 imports the installed Carrom base APK and every split into Loader-private storage, extracts the matching native ABI, and resolves Carrom code through a DexClassLoader whose dex/native paths point only at the imported copy. It does not claim full Activity virtualization yet; lifecycle/resources hooks come next.");
        info.setTextSize(13f);
        info.setTextColor(0xFF555555);
        info.setPadding(0, dp(18), 0, 0);
        root.addView(info);

        setContentView(scrollView);
        refreshStatus();
        refreshReport();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
        refreshReport();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        worker.shutdownNow();
    }

    private Button action(String text, android.view.View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(15f);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
        lp.setMargins(0, dp(9), 0, 0);
        button.setLayoutParams(lp);
        return button;
    }

    private void importCarrom() {
        if (!isCarromInstalled()) {
            toast("Carrom Pool is not installed");
            return;
        }
        report.setText("Importing base APK + splits into Loader private storage…\nThis can take a while because the game is large.");
        scrollToReport();
        worker.submit(() -> {
            long started = System.currentTimeMillis();
            try {
                VirtualPackage imported = VirtualAppStore.importInstalled(this, CarromTarget.PACKAGE);
                String text = "=== VIRTUAL PACKAGE IMPORT ===\n"
                        + "package=" + imported.packageName + "\n"
                        + "version=" + imported.versionName + " (" + imported.versionCode + ")\n"
                        + "privateRoot=" + imported.rootDir.getAbsolutePath() + "\n"
                        + "apkCount=" + imported.apkFiles.size() + "\n"
                        + "dexPath=" + imported.dexPath() + "\n"
                        + "installedSourceUsedAfterImport=NO\n"
                        + "stage=PACKAGE_IMPORTED\n"
                        + "elapsedMs=" + (System.currentTimeMillis() - started) + "\n"
                        + "=== END IMPORT ===\n";
                RuntimeReportStore.write(this, text);
                runOnUiThread(() -> {
                    refreshStatus();
                    refreshReport();
                    scrollToReport();
                    toast("Carrom imported into virtual store");
                });
            } catch (Throwable error) {
                RuntimeReportStore.write(this,
                        "=== VIRTUAL PACKAGE IMPORT ===\n"
                                + "stage=IMPORT_FAILED\n"
                                + "error=" + error.getClass().getName() + ": " + error.getMessage() + "\n"
                                + "=== END IMPORT ===\n");
                runOnUiThread(() -> {
                    refreshReport();
                    scrollToReport();
                    toast("Import failed; check report");
                });
            }
        });
    }

    private void prepareRuntime() {
        if (!VirtualAppStore.isImported(this, CarromTarget.PACKAGE)) {
            toast("Import Carrom first");
            return;
        }
        Intent intent = new Intent(this, VirtualRuntimeService.class)
                .setAction(VirtualRuntimeService.ACTION_PREPARE)
                .putExtra(VirtualRuntimeService.EXTRA_PACKAGE, CarromTarget.PACKAGE);
        startService(intent);
        report.setText("Preparing :virtual runtime from the imported APK copies…");
        scrollToReport();
        long[] delays = {300L, 900L, 1800L, 3500L, 6000L, 10000L};
        for (long delay : delays) {
            handler.postDelayed(() -> {
                refreshReport();
                scrollToReport();
            }, delay);
        }
    }

    private void clearImported() {
        worker.submit(() -> {
            VirtualAppStore.clear(this, CarromTarget.PACKAGE);
            RuntimeReportStore.clear(this);
            runOnUiThread(() -> {
                refreshStatus();
                refreshReport();
                toast("Virtual package cleared");
            });
        });
    }

    private boolean isCarromInstalled() {
        try {
            getPackageManager().getApplicationInfo(CarromTarget.PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException error) {
            return false;
        }
    }

    private void refreshStatus() {
        if (status == null) return;
        boolean installed = isCarromInstalled();
        boolean imported = VirtualAppStore.isImported(this, CarromTarget.PACKAGE);
        StringBuilder text = new StringBuilder();
        text.append("Installed Carrom: ").append(installed ? "YES" : "NO");
        text.append("\nImported virtual copy: ").append(imported ? "YES" : "NO");
        if (imported) {
            try {
                VirtualPackage pkg = VirtualAppStore.loadImported(this, CarromTarget.PACKAGE);
                text.append("\nVersion: ").append(pkg.versionName).append(" (").append(pkg.versionCode).append(')');
                text.append("\nAPK files: ").append(pkg.apkFiles.size());
            } catch (Throwable ignored) {
            }
        }
        status.setText(text.toString());
    }

    private void refreshReport() {
        if (report != null) report.setText(RuntimeReportStore.read(this));
    }

    private void copyReport() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Carrom Loader v2 report", RuntimeReportStore.read(this)));
            toast("Report copied");
        }
    }

    private void scrollToReport() {
        if (scrollView == null || report == null) return;
        scrollView.post(() -> scrollView.smoothScrollTo(0, report.getTop()));
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
