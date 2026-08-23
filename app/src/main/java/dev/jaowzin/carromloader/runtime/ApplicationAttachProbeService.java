package dev.jaowzin.carromloader.runtime;

import android.app.Application;
import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.os.Process;

public final class ApplicationAttachProbeService extends Service {
    public static final String ACTION_ATTACH = "dev.jaowzin.carromloader.runtime.ATTACH_APPLICATION";
    public static final String EXTRA_PACKAGE = "target_package";
    private static final String DEFAULT_TARGET = "com.miniclip.carrom";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String target = intent != null && intent.getStringExtra(EXTRA_PACKAGE) != null
                ? intent.getStringExtra(EXTRA_PACKAGE)
                : DEFAULT_TARGET;

        // Write something before the worker thread starts. If :app_probe dies very early,
        // the main Loader process can still show that the service was actually entered.
        String early = "=== APPLICATION ATTACH REPORT ===\n"
                + "pid=" + Process.myPid() + "\n"
                + "target=" + target + "\n"
                + "stage=SERVICE_STARTED\n"
                + "checkpoint=ON_START_COMMAND\n"
                + "=== REPORT IN PROGRESS ===\n";
        safeWrite(early);

        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            String crash = early
                    + "checkpoint=UNCAUGHT_EXCEPTION\n"
                    + "thread=" + thread.getName() + "\n"
                    + "error=" + error.getClass().getName() + ": " + error.getMessage() + "\n"
                    + "stage=JAVA_CRASH\n"
                    + "=== END APPLICATION ATTACH REPORT ===\n";
            safeWrite(crash);
        });

        new Thread(() -> runAttachProbe(target, startId), "TargetApplicationAttachProbe").start();
        return START_NOT_STICKY;
    }

    private void runAttachProbe(String target, int startId) {
        long started = System.currentTimeMillis();
        StringBuilder report = new StringBuilder();
        report.append("=== APPLICATION ATTACH REPORT ===\n");
        report.append("pid=").append(Process.myPid()).append('\n');
        report.append("target=").append(target).append('\n');
        report.append("hostPackage=").append(getPackageName()).append('\n');
        report.append("probeProcess=:app_probe\n");
        report.append("onCreateCalled=NO\n");
        report.append("checkpoint=WORKER_STARTED\n");
        report.append("stage=STARTED\n");
        checkpoint(report);

        try {
            report.append("checkpoint=BEFORE_CREATE_PACKAGE_CONTEXT\n");
            checkpoint(report);

            Context targetContext = createPackageContext(
                    target,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
            );
            report.append("targetContext=").append(targetContext.getClass().getName()).append('\n');
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

            report.append("facadeContext=").append(facade.getClass().getName()).append('\n');
            report.append("facadePackage=").append(facade.getPackageName()).append('\n');
            report.append("targetLoader=").append(loader.getClass().getName()).append('\n');
            report.append("declaredApplication=").append(appClassName).append('\n');
            report.append("targetCodeExecuted=NO\n");
            report.append("checkpoint=BEFORE_NEW_APPLICATION\n");
            report.append("stage=ABOUT_TO_INSTANTIATE_APPLICATION\n");
            checkpoint(report);

            Instrumentation instrumentation = new Instrumentation();
            Application application = instrumentation.newApplication(loader, appClassName, facade);

            // If the process survives newApplication(), class initialization, constructor and
            // Application.attach() all completed. Persist this immediately before touching any
            // additional target-derived getters.
            report.append("applicationInstance=").append(application.getClass().getName()).append('\n');
            report.append("constructorAndAttach=YES\n");
            report.append("targetCodeExecuted=CLASS_INIT_CONSTRUCTOR_ATTACH_ONLY\n");
            report.append("checkpoint=NEW_APPLICATION_RETURNED\n");
            report.append("stage=APPLICATION_ATTACHED\n");
            checkpoint(report);

            report.append("applicationBaseContext=")
                    .append(application.getBaseContext() == null ? "null" : application.getBaseContext().getClass().getName())
                    .append('\n');
            report.append("applicationPackage=").append(application.getPackageName()).append('\n');
            report.append("applicationLoader=").append(application.getClassLoader().getClass().getName()).append('\n');
            report.append("resources=").append(application.getResources().getClass().getName()).append('\n');
            report.append("assets=").append(application.getAssets().getClass().getName()).append('\n');
            report.append("applicationInfoPackage=").append(application.getApplicationInfo().packageName).append('\n');
            report.append("applicationInfoSource=").append(application.getApplicationInfo().sourceDir).append('\n');
            report.append("checkpoint=POST_ATTACH_GETTERS_OK\n");
            report.append("stage=APPLICATION_ATTACHED_COMPLETE\n");
        } catch (Throwable error) {
            report.append("constructorAndAttach=NO\n");
            report.append("onCreateCalled=NO\n");
            report.append("checkpoint=CAUGHT_THROWABLE\n");
            report.append("stage=APPLICATION_ATTACH_FAILED\n");
            report.append("error=").append(error.getClass().getName()).append(": ")
                    .append(error.getMessage()).append('\n');
        }

        report.append("elapsedMs=").append(System.currentTimeMillis() - started).append('\n');
        report.append("=== END APPLICATION ATTACH REPORT ===\n");
        safeWrite(report.toString());
        stopSelf(startId);
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
