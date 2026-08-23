package dev.jaowzin.carromloader.runtime;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.view.ContextThemeWrapper;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Host-owned Activity used only to validate target Context/Resources/Theme wiring.
 * It does not instantiate or invoke Carrom classes/lifecycle.
 */
public final class RuntimeHostActivity extends Activity {
    private static final String TARGET = "com.miniclip.carrom";
    private static final String TARGET_ACTIVITY = "com.miniclip.carrom.CarromActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StringBuilder diagnostic = new StringBuilder();
        diagnostic.append("Runtime Host Shell\n\n");
        diagnostic.append("pid=").append(Process.myPid()).append('\n');

        try {
            Context targetContext = createPackageContext(
                    TARGET,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
            );
            TargetContextWrapper facade = new TargetContextWrapper(this, targetContext);

            PackageManager pm = getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(TARGET, 0);
            ActivityInfo activityInfo = pm.getActivityInfo(
                    new ComponentName(TARGET, TARGET_ACTIVITY),
                    0
            );

            Context themedTarget = activityInfo.theme != 0
                    ? new ContextThemeWrapper(facade, activityInfo.theme)
                    : facade;

            Class<?> applicationClass = Class.forName(appInfo.className, false, facade.getClassLoader());
            Class<?> activityClass = Class.forName(TARGET_ACTIVITY, false, facade.getClassLoader());

            diagnostic.append("targetContext=").append(targetContext.getClass().getName()).append('\n');
            diagnostic.append("facadePackage=").append(facade.getPackageName()).append('\n');
            diagnostic.append("facadeLoader=").append(facade.getClassLoader().getClass().getName()).append('\n');
            diagnostic.append("resources=").append(facade.getResources().getClass().getName()).append('\n');
            diagnostic.append("assets=").append(facade.getAssets().getClass().getName()).append('\n');
            diagnostic.append("activityTheme=0x").append(Integer.toHexString(activityInfo.theme)).append('\n');
            diagnostic.append("themedContext=").append(themedTarget.getClass().getName()).append('\n');
            diagnostic.append("applicationClass=").append(applicationClass.getName()).append('\n');
            diagnostic.append("activityClass=").append(activityClass.getName()).append('\n');
            diagnostic.append("targetCodeExecuted=NO\n");
            diagnostic.append("stage=HOST_SHELL_READY\n");

            LinearLayout root = new LinearLayout(themedTarget);
            root.setOrientation(LinearLayout.VERTICAL);
            int pad = Math.round(22 * getResources().getDisplayMetrics().density);
            root.setPadding(pad, pad, pad, pad);

            TextView view = new TextView(themedTarget);
            view.setText(diagnostic.toString());
            view.setTextSize(15f);
            root.addView(view, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            setContentView(root);
        } catch (Throwable error) {
            diagnostic.append("stage=HOST_SHELL_FAILED\n");
            diagnostic.append("error=").append(error.getClass().getName()).append(": ")
                    .append(error.getMessage()).append('\n');

            TextView view = new TextView(this);
            view.setText(diagnostic.toString());
            int pad = Math.round(22 * getResources().getDisplayMetrics().density);
            view.setPadding(pad, pad, pad, pad);
            setContentView(view);
        }

        try {
            RuntimeReportStore.write(this,
                    "=== RUNTIME HOST REPORT ===\n" + diagnostic + "=== END HOST REPORT ===\n");
        } catch (Throwable ignored) {
        }
    }
}
