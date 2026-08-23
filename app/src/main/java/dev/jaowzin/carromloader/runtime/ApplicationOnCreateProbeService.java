package dev.jaowzin.carromloader.runtime;

import android.app.Application;
import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;

/**
 * Executes only the target Application lifecycle in an isolated Loader process.
 * The report is checkpointed before every risky step so native/process crashes
 * still leave a useful last-known stage in the main Loader process.
 */
public final class ApplicationOnCreateProbeService extends Service {
    public static final String ACTION_RUN = "dev.jaowzin.carromloader.runtime.RUN_APPLICATION_ONCREATE";
    public static final String EXTRA_PACKAGE = "target_package";
    private static final String DEFAULT_TARGET = "com.miniclip.carrom";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String target = intent != null && intent.getStringExtra(EXTRA_PACKAGE) != null
                ? intent.getStringExtra(EXTRA_PACKAGE)
                : DEFAULT_TARGET;

        // onStartCommand runs on this process' main thread. Keep the Application lifecycle
        // on the main thread too, matching normal Android startup semantics as closely as possible.
        runProbe(target, startId);
        return START_NOT_STICKY;
    }

    private void runProbe(String target, int startId) {
        long started = System.currentTimeMillis();
        StringBuilder report = new StringBuilder();
        report.append("=== APPLICATION ONCREATE REPORT ===\n");
        report.append("pid=").append(Process.myPid()).append('\n');
        report.append("target=").append(target).append('\n');
        report.append("probeProcess=:oncreate_probe\n");
        report.append("actualProcess=").append(processName()).append('\n');
        report.append("thread=").append(Thread.currentThread().getName()).append('\n');
        report.append("checkpoint=SERVICE_STARTED\n");
        report.append("stage=STARTED\n");
        checkpoint(report);

        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            String crash = report.toString()
                    + "checkpoint=UNCAUGHT_EXCEPTION\n"
                    + "crashThread=" + thread.getName() + "\n"
                    + "error=" + error.getClass().getName() + ": " + error.getMessage() + "\n"
                    + "stage=JAVA_CRASH\n"
                    + "=== END APPLICATION ONCREATE REPORT ===\n";
            safeWrite(crash);
        });

        try {
            report.append("checkpoint=BEFORE_CREATE_PACKAGE_CONTEXT\n");
            checkpoint(report);

            Context targetContext = createPackageContext(
                    target,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
            );
            report.append("targetContext=").append(targetContext.getClass().getName()).append('\n');
            report.append("targetPackage=").append(targetContext.getPackageName()).append('\n');
            report.append("checkpoint=PACKAGE_CONTEXT_READY\n");
            report.append("stage=CONTEXT_READY\n");
            checkpoint(report);

            ApplicationInfo appInfo = targetContext.getApplicationInfo();
            String appClassName = appInfo.className;
            if (appClassName == null || appClassName.isEmpty()) {
                appClassName = Application.class.getName();
            }

            TargetContextWrapper facade = new TargetContextWrapper(this, targetContext);
            ClassLoader loader = targetContext.getClassLoader();
            report.append("facadePackage=").append(facade.getPackageName()).append('\n');
            report.append("targetLoader=").append(loader.getClass().getName()).append('\n');
            report.append("declaredApplication=").append(appClassName).append('\n');
            report.append("checkpoint=BEFORE_NEW_APPLICATION\n");
            report.append("stage=ABOUT_TO_ATTACH\n");
            checkpoint(report);

            Instrumentation instrumentation = new Instrumentation();
            Application application = instrumentation.newApplication(loader, appClassName, facade);
            report.append("applicationInstance=").append(application.getClass().getName()).append('\n');
            report.append("applicationPackage=").append(application.getPackageName()).append('\n');
            report.append("checkpoint=APPLICATION_ATTACHED\n");
            report.append("stage=ATTACHED\n");
            checkpoint(report);

            report.append("checkpoint=BEFORE_ON_CREATE\n");
            report.append("onCreateCalled=ABOUT_TO_CALL\n");
            report.append("stage=ABOUT_TO_CALL_ONCREATE\n");
            checkpoint(report);

            instrumentation.callApplicationOnCreate(application);

            report.append("checkpoint=ON_CREATE_RETURNED\n");
            report.append("onCreateCalled=YES\n");
            report.append("stage=APPLICATION_ONCREATE_COMPLETE\n");
            report.append("applicationBaseContext=")
                    .append(application.getBaseContext() == null ? "null" : application.getBaseContext().getClass().getName())
                    .append('\n');
            report.append("resources=").append(application.getResources().getClass().getName()).append('\n');
            report.append("assets=").append(application.getAssets().getClass().getName()).append('\n');
        } catch (Throwable error) {
            report.append("checkpoint=CAUGHT_THROWABLE\n");
            report.append("stage=APPLICATION_ONCREATE_FAILED\n");
            report.append("error=").append(error.getClass().getName()).append(": ")
                    .append(error.getMessage()).append('\n');
            Throwable cause = error.getCause();
            int depth = 0;
            while (cause != null && depth < 5) {
                report.append("cause").append(depth).append('=')
                        .append(cause.getClass().getName()).append(": ")
                        .append(cause.getMessage()).append('\n');
                cause = cause.getCause();
                depth++;
            }
        }

        report.append("elapsedMs=").append(System.currentTimeMillis() - started).append('\n');
        report.append("=== END APPLICATION ONCREATE REPORT ===\n");
        safeWrite(report.toString());
        stopSelf(startId);
    }

    private String processName() {
        if (Build.VERSION.SDK_INT >= 28) {
            return Application.getProcessName();
        }
        return "unknown-pre28";
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
