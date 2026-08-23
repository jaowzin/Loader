package dev.jaowzin.carromloader.runtime;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.view.ContextThemeWrapper;

/**
 * Builds the target Application lifecycle and attaches CarromActivity to a controlled
 * Loader-owned process without calling Activity.onCreate(). This is a lifecycle probe,
 * not a game launch.
 */
public final class ActivityAttachProbeService extends Service {
    public static final String ACTION_RUN = "dev.jaowzin.carromloader.runtime.RUN_ACTIVITY_ATTACH";
    public static final String EXTRA_PACKAGE = "target_package";

    private static final String DEFAULT_TARGET = "com.miniclip.carrom";
    private static final String TARGET_ACTIVITY = "com.miniclip.carrom.CarromActivity";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String target = intent != null && intent.getStringExtra(EXTRA_PACKAGE) != null
                ? intent.getStringExtra(EXTRA_PACKAGE)
                : DEFAULT_TARGET;

        // Activity attach is intentionally kept on this process' main thread.
        runProbe(target, startId);
        return START_NOT_STICKY;
    }

    private void runProbe(String target, int startId) {
        long started = System.currentTimeMillis();
        StringBuilder report = new StringBuilder();
        report.append("=== ACTIVITY ATTACH REPORT ===\n");
        report.append("pid=").append(Process.myPid()).append('\n');
        report.append("target=").append(target).append('\n');
        report.append("probeProcess=:activity_probe\n");
        report.append("actualProcess=").append(processName()).append('\n');
        report.append("thread=").append(Thread.currentThread().getName()).append('\n');
        report.append("activityOnCreateCalled=NO\n");
        report.append("checkpoint=SERVICE_STARTED\n");
        report.append("stage=STARTED\n");
        checkpoint(report);

        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            String crash = report.toString()
                    + "checkpoint=UNCAUGHT_EXCEPTION\n"
                    + "crashThread=" + thread.getName() + "\n"
                    + "error=" + error.getClass().getName() + ": " + error.getMessage() + "\n"
                    + "stage=JAVA_CRASH\n"
                    + "=== END ACTIVITY ATTACH REPORT ===\n";
            safeWrite(crash);
        });

        try {
            report.append("checkpoint=BEFORE_CREATE_PACKAGE_CONTEXT\n");
            checkpoint(report);

            Context targetContext = createPackageContext(
                    target,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
            );
            TargetContextWrapper facade = new TargetContextWrapper(this, targetContext);
            ClassLoader loader = targetContext.getClassLoader();

            report.append("targetContext=").append(targetContext.getClass().getName()).append('\n');
            report.append("facadePackage=").append(facade.getPackageName()).append('\n');
            report.append("targetLoader=").append(loader.getClass().getName()).append('\n');
            report.append("checkpoint=PACKAGE_CONTEXT_READY\n");
            report.append("stage=CONTEXT_READY\n");
            checkpoint(report);

            ApplicationInfo appInfo = targetContext.getApplicationInfo();
            String appClassName = appInfo.className;
            if (appClassName == null || appClassName.isEmpty()) {
                appClassName = Application.class.getName();
            }

            Instrumentation instrumentation = new Instrumentation();
            report.append("declaredApplication=").append(appClassName).append('\n');
            report.append("checkpoint=BEFORE_APPLICATION_ATTACH\n");
            checkpoint(report);

            Application application = instrumentation.newApplication(loader, appClassName, facade);
            report.append("applicationInstance=").append(application.getClass().getName()).append('\n');
            report.append("checkpoint=APPLICATION_ATTACHED\n");
            report.append("stage=APPLICATION_ATTACHED\n");
            checkpoint(report);

            report.append("checkpoint=BEFORE_APPLICATION_ONCREATE\n");
            checkpoint(report);
            instrumentation.callApplicationOnCreate(application);
            report.append("checkpoint=APPLICATION_ONCREATE_RETURNED\n");
            report.append("stage=APPLICATION_READY\n");
            checkpoint(report);

            PackageManager pm = getPackageManager();
            ComponentName component = new ComponentName(target, TARGET_ACTIVITY);
            ActivityInfo activityInfo = pm.getActivityInfo(component, PackageManager.GET_META_DATA);
            int themeRes = activityInfo.theme != 0 ? activityInfo.theme : appInfo.theme;
            CharSequence title = activityInfo.loadLabel(pm);
            if (title == null) title = TARGET_ACTIVITY;

            report.append("activityInfoName=").append(activityInfo.name).append('\n');
            report.append("activityInfoProcess=").append(activityInfo.processName).append('\n');
            report.append("activityTheme=0x").append(Integer.toHexString(themeRes)).append('\n');
            report.append("activityTitle=").append(title).append('\n');
            report.append("checkpoint=ACTIVITY_INFO_READY\n");
            report.append("stage=ABOUT_TO_ATTACH_ACTIVITY\n");
            checkpoint(report);

            Context themedContext = themeRes != 0
                    ? new ContextThemeWrapper(facade, themeRes)
                    : facade;
            Intent activityIntent = new Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .setComponent(component)
                    .setPackage(target);
            IBinder token = new Binder();
            Class<?> activityClass = Class.forName(TARGET_ACTIVITY, false, loader);

            report.append("activityClassResolved=").append(activityClass.getName()).append('\n');
            report.append("themedContext=").append(themedContext.getClass().getName()).append('\n');
            report.append("checkpoint=BEFORE_NEW_ACTIVITY\n");
            checkpoint(report);

            Activity activity = instrumentation.newActivity(
                    activityClass,
                    themedContext,
                    token,
                    application,
                    activityIntent,
                    activityInfo,
                    title,
                    null,
                    null,
                    null
            );

            report.append("checkpoint=NEW_ACTIVITY_RETURNED\n");
            report.append("activityInstance=").append(activity.getClass().getName()).append('\n');
            report.append("activityBaseContext=")
                    .append(activity.getBaseContext() == null ? "null" : activity.getBaseContext().getClass().getName())
                    .append('\n');
            report.append("activityPackage=").append(activity.getPackageName()).append('\n');
            report.append("activityApplication=")
                    .append(activity.getApplication() == null ? "null" : activity.getApplication().getClass().getName())
                    .append('\n');
            report.append("activityIntentComponent=")
                    .append(activity.getIntent() == null ? "null" : activity.getIntent().getComponent())
                    .append('\n');
            report.append("activityWindow=")
                    .append(activity.getWindow() == null ? "null" : activity.getWindow().getClass().getName())
                    .append('\n');
            report.append("activityOnCreateCalled=NO\n");
            report.append("targetActivityCodeExecuted=CLASS_INIT_CONSTRUCTOR_ATTACH_ONLY\n");
            report.append("stage=ACTIVITY_ATTACHED\n");
        } catch (Throwable error) {
            report.append("checkpoint=CAUGHT_THROWABLE\n");
            report.append("activityOnCreateCalled=NO\n");
            report.append("stage=ACTIVITY_ATTACH_FAILED\n");
            report.append("error=").append(error.getClass().getName()).append(": ")
                    .append(error.getMessage()).append('\n');
            Throwable cause = error.getCause();
            int depth = 0;
            while (cause != null && depth < 6) {
                report.append("cause").append(depth).append('=')
                        .append(cause.getClass().getName()).append(": ")
                        .append(cause.getMessage()).append('\n');
                cause = cause.getCause();
                depth++;
            }
        }

        report.append("elapsedMs=").append(System.currentTimeMillis() - started).append('\n');
        report.append("=== END ACTIVITY ATTACH REPORT ===\n");
        safeWrite(report.toString());
        stopSelf(startId);
    }

    private String processName() {
        return Build.VERSION.SDK_INT >= 28 ? Application.getProcessName() : "unknown-pre28";
    }

    private void checkpoint(StringBuilder report) {
        safeWrite(report.toString() + "=== REPORT IN PROGRESS ===\n");
    }

    private void safeWrite(String text) {
        try {
            RuntimeReportStore.write(this, text);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
