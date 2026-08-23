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
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.view.ContextThemeWrapper;

/**
 * Rebuilds the target Application + Activity shell in an isolated Loader process,
 * then calls only CarromActivity.onCreate(). Checkpoints are persisted before every
 * risky lifecycle transition so a Java/native process crash leaves the last stage.
 */
public final class ActivityOnCreateProbeService extends Service {
    public static final String ACTION_RUN = "dev.jaowzin.carromloader.runtime.RUN_ACTIVITY_ONCREATE";
    public static final String EXTRA_PACKAGE = "target_package";

    private static final String DEFAULT_TARGET = "com.miniclip.carrom";
    private static final String TARGET_ACTIVITY = "com.miniclip.carrom.CarromActivity";
    private static final int KNOWN_FAILING_RESOURCE = 0x7f080093;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String target = intent != null && intent.getStringExtra(EXTRA_PACKAGE) != null
                ? intent.getStringExtra(EXTRA_PACKAGE)
                : DEFAULT_TARGET;
        runProbe(target, startId);
        return START_NOT_STICKY;
    }

    private void runProbe(String target, int startId) {
        long started = System.currentTimeMillis();
        StringBuilder report = new StringBuilder();
        report.append("=== ACTIVITY ONCREATE REPORT ===\n");
        report.append("pid=").append(Process.myPid()).append('\n');
        report.append("target=").append(target).append('\n');
        report.append("probeProcess=:activity_oncreate_probe\n");
        report.append("actualProcess=").append(processName()).append('\n');
        report.append("thread=").append(Thread.currentThread().getName()).append('\n');
        report.append("activityOnCreateCalled=NO\n");
        report.append("knownResourceProbe=0x").append(Integer.toHexString(KNOWN_FAILING_RESOURCE)).append('\n');
        report.append("checkpoint=SERVICE_STARTED\n");
        report.append("stage=STARTED\n");
        checkpoint(report);

        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            StringBuilder crash = new StringBuilder(report);
            crash.append("checkpoint=UNCAUGHT_EXCEPTION\n");
            crash.append("crashThread=").append(thread.getName()).append('\n');
            appendThrowable(crash, "uncaught", error, 24);
            crash.append("stage=JAVA_CRASH\n");
            crash.append("=== END ACTIVITY ONCREATE REPORT ===\n");
            safeWrite(crash.toString());
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
            report.append("targetCodePath=").append(facade.getPackageCodePath()).append('\n');
            report.append("targetResourcePath=").append(facade.getPackageResourcePath()).append('\n');
            report.append("checkpoint=PACKAGE_CONTEXT_READY\n");
            report.append("stage=CONTEXT_READY\n");
            checkpoint(report);

            probeResource(report, "targetContext", targetContext, KNOWN_FAILING_RESOURCE);
            probeResource(report, "facadeBeforeApp", facade, KNOWN_FAILING_RESOURCE);
            checkpoint(report);

            ApplicationInfo appInfo = targetContext.getApplicationInfo();
            String appClassName = appInfo.className;
            if (appClassName == null || appClassName.isEmpty()) appClassName = Application.class.getName();

            Instrumentation instrumentation = new Instrumentation();
            report.append("declaredApplication=").append(appClassName).append('\n');
            report.append("checkpoint=BEFORE_APPLICATION_ATTACH\n");
            checkpoint(report);

            Application application = instrumentation.newApplication(loader, appClassName, facade);
            facade.setTargetApplication(application);
            report.append("applicationInstance=").append(application.getClass().getName()).append('\n');
            report.append("applicationContext=")
                    .append(application.getApplicationContext() == null ? "null" : application.getApplicationContext().getClass().getName())
                    .append('\n');
            report.append("checkpoint=APPLICATION_ATTACHED\n");
            checkpoint(report);

            probeResource(report, "application", application, KNOWN_FAILING_RESOURCE);
            probeResource(report, "facadeAfterApp", facade, KNOWN_FAILING_RESOURCE);
            checkpoint(report);

            report.append("checkpoint=BEFORE_APPLICATION_ONCREATE\n");
            checkpoint(report);
            instrumentation.callApplicationOnCreate(application);
            report.append("checkpoint=APPLICATION_ONCREATE_RETURNED\n");
            report.append("stage=APPLICATION_READY\n");
            checkpoint(report);

            PackageManager pm = facade.getPackageManager();
            ComponentName component = new ComponentName(target, TARGET_ACTIVITY);
            ActivityInfo activityInfo = pm.getActivityInfo(component, PackageManager.GET_META_DATA);
            int themeRes = activityInfo.theme != 0 ? activityInfo.theme : appInfo.theme;
            CharSequence title = activityInfo.loadLabel(pm);
            if (title == null) title = TARGET_ACTIVITY;

            report.append("activityInfoName=").append(activityInfo.name).append('\n');
            report.append("activityTheme=0x").append(Integer.toHexString(themeRes)).append('\n');
            report.append("activityTitle=").append(title).append('\n');
            report.append("checkpoint=ACTIVITY_INFO_READY\n");
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
            report.append("activityApplicationContext=")
                    .append(activity.getApplicationContext() == null ? "null" : activity.getApplicationContext().getClass().getName())
                    .append('\n');
            report.append("activityResources=").append(activity.getResources().getClass().getName()).append('\n');
            report.append("activityWindow=")
                    .append(activity.getWindow() == null ? "null" : activity.getWindow().getClass().getName())
                    .append('\n');
            report.append("stage=ACTIVITY_ATTACHED\n");
            checkpoint(report);

            probeResource(report, "themedContext", themedContext, KNOWN_FAILING_RESOURCE);
            probeResource(report, "activity", activity, KNOWN_FAILING_RESOURCE);
            report.append("checkpoint=RESOURCE_PREFLIGHT_COMPLETE\n");
            checkpoint(report);

            report.append("checkpoint=BEFORE_ACTIVITY_ONCREATE\n");
            report.append("activityOnCreateCalled=ABOUT_TO_CALL\n");
            report.append("stage=ABOUT_TO_CALL_ACTIVITY_ONCREATE\n");
            checkpoint(report);

            instrumentation.callActivityOnCreate(activity, null);

            report.append("checkpoint=ACTIVITY_ONCREATE_RETURNED\n");
            report.append("activityOnCreateCalled=YES\n");
            report.append("stage=ACTIVITY_ONCREATE_COMPLETE\n");
            report.append("activityFinishing=").append(activity.isFinishing()).append('\n');
            report.append("activityChangingConfigurations=").append(activity.isChangingConfigurations()).append('\n');
            report.append("windowAfterOnCreate=")
                    .append(activity.getWindow() == null ? "null" : activity.getWindow().getClass().getName())
                    .append('\n');
        } catch (Throwable error) {
            report.append("checkpoint=CAUGHT_THROWABLE\n");
            report.append("stage=ACTIVITY_ONCREATE_FAILED\n");
            appendThrowable(report, "error", error, 30);
            Throwable cause = error.getCause();
            int depth = 0;
            while (cause != null && depth < 8) {
                appendThrowable(report, "cause" + depth, cause, 12);
                cause = cause.getCause();
                depth++;
            }
        }

        report.append("elapsedMs=").append(System.currentTimeMillis() - started).append('\n');
        report.append("=== END ACTIVITY ONCREATE REPORT ===\n");
        safeWrite(report.toString());
        stopSelf(startId);
    }

    private void probeResource(StringBuilder report, String label, Context context, int id) {
        report.append("resourceProbe.").append(label).append(".context=")
                .append(context == null ? "null" : context.getClass().getName()).append('\n');
        if (context == null) {
            report.append("resourceProbe.").append(label).append(".result=NULL_CONTEXT\n");
            return;
        }
        try {
            Resources resources = context.getResources();
            String name = resources.getResourceName(id);
            Drawable drawable = resources.getDrawable(id, context.getTheme());
            report.append("resourceProbe.").append(label).append(".resources=")
                    .append(resources.getClass().getName()).append('\n');
            report.append("resourceProbe.").append(label).append(".name=").append(name).append('\n');
            report.append("resourceProbe.").append(label).append(".drawable=")
                    .append(drawable == null ? "null" : drawable.getClass().getName()).append('\n');
            report.append("resourceProbe.").append(label).append(".result=OK\n");
        } catch (Throwable error) {
            report.append("resourceProbe.").append(label).append(".result=FAILED\n");
            report.append("resourceProbe.").append(label).append(".error=")
                    .append(error.getClass().getName()).append(": ").append(error.getMessage()).append('\n');
        }
    }

    private void appendThrowable(StringBuilder report, String prefix, Throwable error, int frameLimit) {
        report.append(prefix).append('=').append(error.getClass().getName()).append(": ")
                .append(error.getMessage()).append('\n');
        StackTraceElement[] frames = error.getStackTrace();
        int count = Math.min(frameLimit, frames == null ? 0 : frames.length);
        for (int i = 0; i < count; i++) {
            report.append(prefix).append(".stack").append(i).append('=')
                    .append(frames[i].toString()).append('\n');
        }
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
