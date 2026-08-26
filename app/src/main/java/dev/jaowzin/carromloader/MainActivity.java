package dev.jaowzin.carromloader;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;
import dev.jaowzin.carromloader.runtime.entity.pm.InstallResult;

public final class MainActivity extends AppCompatActivity {
    private static final String TARGET = "com.miniclip.carrom";
    private static final int USER_ID = 0;

    private static final int BG = Color.rgb(8, 12, 17);
    private static final int CARD = Color.rgb(17, 24, 33);
    private static final int CARD_ALT = Color.rgb(21, 30, 41);
    private static final int STROKE = Color.rgb(40, 53, 68);
    private static final int TEXT = Color.rgb(239, 247, 245);
    private static final int MUTED = Color.rgb(142, 158, 171);
    private static final int ACCENT = Color.rgb(104, 245, 200);
    private static final int WARNING = Color.rgb(255, 198, 92);

    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler statusHandler = new Handler(Looper.getMainLooper());
    private boolean statusPolling;

    private TextView runtimeBadge;
    private TextView runtimeDetail;
    private TextView console;
    private MaterialButton primaryAction;

    private final Runnable statusTicker = new Runnable() {
        @Override
        public void run() {
            if (!statusPolling) return;
            refreshStatus();
            statusHandler.postDelayed(this, 650L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        setContentView(buildContent());
        refreshStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startStatusPolling();
    }

    @Override
    protected void onPause() {
        stopStatusPolling();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopStatusPolling();
        super.onDestroy();
        worker.shutdownNow();
    }

    private void startStatusPolling() {
        statusPolling = true;
        statusHandler.removeCallbacks(statusTicker);
        statusHandler.post(statusTicker);
    }

    private void stopStatusPolling() {
        statusPolling = false;
        statusHandler.removeCallbacks(statusTicker);
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);
    }

    private View buildContent() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        scroll.setClipToPadding(false);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(22), dp(20), dp(34));
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        root.addView(buildHeader());
        root.addView(buildHero());

        root.addView(sectionTitle("ASSIST MODULES", "Configure what loads with virtual Carrom."));
        root.addView(featureCard(
                "Aim Lines",
                "Native game-state trajectory guide. No visual fallback.",
                FeatureSettings.linesEnabled(this),
                true,
                checked -> {
                    FeatureSettings.setLinesEnabled(this, checked);
                    if (!checked) FeatureSettings.setBankPreviewEnabled(this, false);
                    toast(checked ? "Aim Lines enabled" : "Aim Lines disabled");
                }
        ));
        root.addView(featureCard(
                "Bank Preview",
                "Adds reflected guide segments after a rail collision. Requires Aim Lines.",
                FeatureSettings.linesEnabled(this) && FeatureSettings.bankPreviewEnabled(this),
                FeatureSettings.linesEnabled(this),
                checked -> {
                    FeatureSettings.setBankPreviewEnabled(this, checked);
                    toast("Bank Preview updated");
                }
        ));
        root.addView(featureCard(
                "Auto Play",
                "Next module: game-state solver + controlled shot execution.",
                false,
                false,
                checked -> { }
        ));

        root.addView(sectionTitle("RUNTIME", "Manage the isolated Carrom instance."));
        root.addView(buildRuntimeActions());

        root.addView(sectionTitle("CONSOLE", "Live status from the virtual Carrom process."));
        root.addView(buildConsole());

        TextView footer = text("Carrom Loader • Runtime v2", 12f, MUTED, false);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(24), 0, dp(4));
        root.addView(footer, matchWrap());

        return scroll;
    }

    private View buildHeader() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dp(18));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams copyLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        TextView eyebrow = text("CARROM LAB", 11f, ACCENT, true);
        eyebrow.setLetterSpacing(0.18f);
        copy.addView(eyebrow);

        TextView title = text("Carrom Loader", 30f, TEXT, true);
        title.setPadding(0, dp(2), 0, 0);
        copy.addView(title);
        row.addView(copy, copyLp);

        TextView mark = text("CL", 15f, BG, true);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(roundRect(ACCENT, dp(16)));
        row.addView(mark, new LinearLayout.LayoutParams(dp(48), dp(48)));
        return row;
    }

    private View buildHero() {
        MaterialCardView card = card(CARD_ALT, 24);
        LinearLayout body = vertical(dp(18));
        body.setPadding(dp(20), dp(20), dp(20), dp(20));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout copy = vertical(0);
        LinearLayout.LayoutParams copyLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        TextView small = text("VIRTUAL GAME", 11f, MUTED, true);
        small.setLetterSpacing(0.12f);
        copy.addView(small);
        TextView name = text("Carrom Disc Pool", 22f, TEXT, true);
        name.setPadding(0, dp(3), 0, 0);
        copy.addView(name);
        top.addView(copy, copyLp);

        runtimeBadge = text("CHECKING", 10f, WARNING, true);
        runtimeBadge.setGravity(Gravity.CENTER);
        runtimeBadge.setPadding(dp(11), dp(7), dp(11), dp(7));
        runtimeBadge.setBackground(roundRect(Color.rgb(46, 39, 24), dp(99)));
        top.addView(runtimeBadge);
        body.addView(top);

        runtimeDetail = text("Checking runtime…", 13f, MUTED, false);
        runtimeDetail.setPadding(0, dp(12), 0, dp(16));
        body.addView(runtimeDetail);

        primaryAction = new MaterialButton(this);
        primaryAction.setText("OPEN CARROM");
        primaryAction.setAllCaps(false);
        primaryAction.setTextSize(15f);
        primaryAction.setTypeface(Typeface.DEFAULT_BOLD);
        primaryAction.setTextColor(BG);
        primaryAction.setBackgroundTintList(ColorStateList.valueOf(ACCENT));
        primaryAction.setCornerRadius(dp(17));
        primaryAction.setInsetTop(0);
        primaryAction.setInsetBottom(0);
        primaryAction.setOnClickListener(v -> setupAndLaunch());
        body.addView(primaryAction, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
        ));

        card.addView(body);
        LinearLayout.LayoutParams lp = matchWrap();
        lp.setMargins(0, 0, 0, dp(8));
        card.setLayoutParams(lp);
        return card;
    }

    private View featureCard(
            String title,
            String subtitle,
            boolean checked,
            boolean enabled,
            ToggleListener listener
    ) {
        MaterialCardView card = card(CARD, 20);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), dp(16), dp(14), dp(16));

        LinearLayout copy = vertical(0);
        LinearLayout.LayoutParams copyLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        TextView titleView = text(title, 16f, enabled ? TEXT : MUTED, true);
        copy.addView(titleView);
        TextView subtitleView = text(subtitle, 12.5f, MUTED, false);
        subtitleView.setPadding(0, dp(4), dp(10), 0);
        subtitleView.setLineSpacing(0f, 1.08f);
        copy.addView(subtitleView);
        row.addView(copy, copyLp);

        SwitchMaterial toggle = new SwitchMaterial(this);
        toggle.setChecked(checked);
        toggle.setEnabled(enabled);
        toggle.setOnCheckedChangeListener((button, value) -> listener.onChanged(value));
        row.addView(toggle);

        card.addView(row);
        LinearLayout.LayoutParams lp = matchWrap();
        lp.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(lp);
        return card;
    }

    private View buildRuntimeActions() {
        MaterialCardView card = card(CARD, 20);
        LinearLayout body = vertical(dp(10));
        body.setPadding(dp(14), dp(14), dp(14), dp(14));

        MaterialButton refresh = secondaryButton("Refresh status");
        refresh.setOnClickListener(v -> refreshStatus());
        body.addView(refresh, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50)));

        MaterialButton reset = secondaryButton("Reset virtual Carrom");
        reset.setTextColor(Color.rgb(255, 151, 151));
        reset.setOnClickListener(v -> confirmReset());
        LinearLayout.LayoutParams resetLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
        resetLp.setMargins(0, dp(8), 0, 0);
        body.addView(reset, resetLp);

        card.addView(body);
        return card;
    }

    private View buildConsole() {
        MaterialCardView card = card(Color.rgb(10, 15, 21), 18);
        console = text(CarromModuleBridge.status(), 12f, Color.rgb(171, 194, 188), false);
        console.setTypeface(Typeface.MONOSPACE);
        console.setTextIsSelectable(true);
        console.setPadding(dp(16), dp(15), dp(16), dp(15));
        console.setMinHeight(dp(110));
        card.addView(console);
        return card;
    }

    private View sectionTitle(String title, String subtitle) {
        LinearLayout box = vertical(0);
        box.setPadding(0, dp(20), 0, dp(10));
        TextView t = text(title, 11f, MUTED, true);
        t.setLetterSpacing(0.14f);
        box.addView(t);
        TextView s = text(subtitle, 12.5f, MUTED, false);
        s.setPadding(0, dp(4), 0, 0);
        box.addView(s);
        return box;
    }

    private void setupAndLaunch() {
        if (!isHostCarromInstalled()) {
            toast("Install Carrom Disc Pool on the device first");
            return;
        }
        primaryAction.setEnabled(false);
        primaryAction.setText("PREPARING…");
        setConsole("Preparing virtual Carrom…");

        worker.execute(() -> {
            try {
                CarromRuntimeCore core = CarromRuntimeCore.get();
                if (!core.isInstalled(TARGET, USER_ID)) {
                    InstallResult result = core.installPackageAsUser(TARGET, USER_ID);
                    if (!result.success) {
                        runOnUiThread(() -> {
                            setConsole("INSTALL FAILED\n" + result.msg);
                            primaryAction.setEnabled(true);
                            refreshStatus();
                        });
                        return;
                    }
                }

                boolean launched = core.launchApk(TARGET, USER_ID);
                runOnUiThread(() -> {
                    primaryAction.setEnabled(true);
                    refreshStatus();
                    if (!launched) toast("Virtual launch returned false");
                });
            } catch (Throwable error) {
                runOnUiThread(() -> {
                    setConsole("LAUNCH ERROR\n" + error);
                    primaryAction.setEnabled(true);
                });
            }
        });
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("Reset virtual Carrom?")
                .setMessage("This removes the isolated runtime copy. The original installed game is untouched.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reset", (dialog, which) -> resetVirtual())
                .show();
    }

    private void resetVirtual() {
        setConsole("Resetting virtual Carrom…");
        worker.execute(() -> {
            try {
                CarromRuntimeCore.get().uninstallPackageAsUser(TARGET, USER_ID);
                runOnUiThread(() -> {
                    setConsole("Virtual Carrom removed.\nTap OPEN CARROM to create a clean instance.");
                    refreshStatus();
                });
            } catch (Throwable error) {
                runOnUiThread(() -> setConsole("RESET ERROR\n" + error));
            }
        });
    }

    private void refreshStatus() {
        boolean hostInstalled = isHostCarromInstalled();
        boolean virtualInstalled = false;
        boolean services = false;
        try {
            CarromRuntimeCore core = CarromRuntimeCore.get();
            virtualInstalled = core.isInstalled(TARGET, USER_ID);
            services = core.areServicesAvailable();
        } catch (Throwable ignored) {
        }

        if (runtimeBadge != null) {
            runtimeBadge.setText(services ? "RUNTIME ONLINE" : "STARTING");
            runtimeBadge.setTextColor(services ? ACCENT : WARNING);
            runtimeBadge.setBackground(roundRect(
                    services ? Color.rgb(21, 48, 42) : Color.rgb(46, 39, 24),
                    dp(99)
            ));
        }
        if (runtimeDetail != null) {
            runtimeDetail.setText(
                    (hostInstalled ? "Device game detected" : "Device game not found")
                            + "  •  "
                            + (virtualInstalled ? "Virtual instance ready" : "Virtual instance not prepared")
            );
        }
        if (primaryAction != null && primaryAction.isEnabled()) {
            primaryAction.setText(virtualInstalled ? "OPEN CARROM" : "SET UP & OPEN CARROM");
        }
        if (console != null) {
            String status = CarromModuleBridge.status();
            if (status == null || status.trim().isEmpty()) status = "waiting for virtual Carrom";
            console.setText(status);
        }
    }

    private boolean isHostCarromInstalled() {
        try {
            getPackageManager().getPackageInfo(TARGET, 0);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private MaterialCardView card(int color, int radiusDp) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(color);
        card.setRadius(dp(radiusDp));
        card.setCardElevation(0f);
        card.setStrokeColor(STROKE);
        card.setStrokeWidth(dp(1));
        return card;
    }

    private MaterialButton secondaryButton(String label) {
        MaterialButton button = new MaterialButton(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(TEXT);
        button.setTextSize(14f);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        button.setStrokeColor(ColorStateList.valueOf(STROKE));
        button.setStrokeWidth(dp(1));
        button.setCornerRadius(dp(15));
        button.setInsetTop(0);
        button.setInsetBottom(0);
        return button;
    }

    private LinearLayout vertical(int spacingIgnored) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT_BOLD);
        return view;
    }

    private android.graphics.drawable.GradientDrawable roundRect(int color, int radius) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void setConsole(String value) {
        if (console != null) console.setText(value);
    }

    private void toast(String value) {
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private interface ToggleListener {
        void onChanged(boolean checked);
    }
}
