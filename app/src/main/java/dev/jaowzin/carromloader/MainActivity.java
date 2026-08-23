package dev.jaowzin.carromloader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import dev.jaowzin.carromloader.engine.CarromGameEngine;
import dev.jaowzin.carromloader.engine.GameEngine;
import dev.jaowzin.carromloader.overlay.GuideOverlayService;

public final class MainActivity extends Activity {
    private final GameEngine engine = new CarromGameEngine();
    private TextView status;

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
        subtitle.setText("Clean-room CTF companion • com.miniclip.carrom");
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
        root.addView(action("Stop overlay", v -> stopService(new Intent(this, GuideOverlayService.class))));

        TextView info = new TextView(this);
        info.setText("Current engine\n" + engine.getDescription()
                + "\n\nThe guide is rendered by this app in a separate Android overlay. It does not patch or replace the installed Carrom APK.");
        info.setTextSize(13f);
        info.setTextColor(0xFF555555);
        info.setPadding(0, dp(22), 0, 0);
        root.addView(info);

        setContentView(root);
        refreshStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
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

    private void refreshStatus() {
        if (status == null) return;
        boolean installed = engine.isAvailable(this);
        boolean overlay = Settings.canDrawOverlays(this);
        status.setText("Carrom installed: " + (installed ? "YES" : "NO")
                + "\nOverlay permission: " + (overlay ? "YES" : "NO")
                + "\nTarget: " + engine.getTargetPackage());
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
