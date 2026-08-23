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
        new Thread(() -> runAttachProbe(target, startId), "TargetApplicationAttachProbe").start();
        return START_NOT_STICKY;
    }

    private void runAttachProbe(String target, int startId) {
        long started = System.currentTimeMillis();
        StringBuilder report = new StringBuilder();
        report.append("=== APPLICATION ATTACH REPORT ===\n");
        report.append("pid=").append(Process.myPid()).append('\n');
        report.append("target=").append(target).append('\n');
        report.append("probeProcess=").append(getPackageName()).append(":app_probe\n");

        try {
            Context targetContext = createPackageContext(
                    target,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
            );
            ApplicationInfo appInfo = targetContext.getApplicationInfo();
            String appClassName = appInfo.className;
            if (appClassName == null || appClassName.isEmpty()) {
                appClassName = Application.class.getName();
            }

            TargetContextWrapper facade = new TargetContextWrapper(this, targetContext);
            ClassLoader loader = targetContext.getClassLoader();

            report.append("targetContext=").append(targetContext.getClass().getName()).append('\n');
            report.append("facadeContext=").append(facade.getClass().getName()).append('\n');
            report.append("facadePackage=").append(facade.getPackageName()).append('\n');
            report.append("targetLoader=").append(loader.getClass().getName()).append('\n');
            report.append("declaredApplication=").append(appClassName).append('\n');
            report.append("onCreateCalled=NO\n");
            report.append("targetCodeExecuted=CLASS_INIT_CONSTRUCTOR_ATTACH_ONLY\n");

            Instrumentation instrumentation = new Instrumentation();
            Application application = Instrumentation.newApplication(loader, appClassName, facade);

            report.append("applicationInstance=").append(application.getClass().getName()).append('\n');
            report.append("applicationBaseContext=")
                    .append(application.getBaseContext() == null ? "null" : application.getBaseContext().getClass().getName())
                    .append('\n');
            report.append("applicationPackage=").append(application.getPackageName()).append('\n');
            report.append("applicationLoader=").append(application.getClassLoader().getClass().getName()).append('\n');
            report.append("resources=").append(application.getResources().getClass().getName()).append('\n');
            report.append("assets=").append(application.getAssets().getClass().getName()).append('\n');
            report.append("applicationInfoPackage=").append(application.getApplicationInfo().packageName).append('\n');
            report.append("applicationInfoSource=").append(application.getApplicationInfo().sourceDir).append('\n');
            report.append("constructorAndAttach=YES\n");
            report.append("stage=APPLICATION_ATTACHED\n");
        } catch (Throwable error) {
            report.append("constructorAndAttach=NO\n");
            report.append("onCreateCalled=NO\n");
            report.append("stage=APPLICATION_ATTACH_FAILED\n");
            report.append("error=").append(error.getClass().getName()).append(": ")
                    .append(error.getMessage()).append('\n');
        }

        report.append("elapsedMs=").append(System.currentTimeMillis() - started).append('\n');
        report.append("=== END APPLICATION ATTACH REPORT ===\n");

        try {
            RuntimeReportStore.write(this, report.toString());
        } catch (Throwable ignored) {
        }
        stopSelf(startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
