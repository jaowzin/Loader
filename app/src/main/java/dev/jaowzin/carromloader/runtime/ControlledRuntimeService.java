package dev.jaowzin.carromloader.runtime;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;

import java.io.File;

import dalvik.system.DexClassLoader;

public final class ControlledRuntimeService extends Service {
    public static final String ACTION_PREPARE = "dev.jaowzin.carromloader.runtime.PREPARE";
    public static final String EXTRA_PACKAGE = "target_package";
    public static final String DEFAULT_TARGET = "com.miniclip.carrom";
    private static final String PROBE_CLASS = "com.miniclip.carrom.CarromActivity";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String target = intent != null && intent.getStringExtra(EXTRA_PACKAGE) != null
                ? intent.getStringExtra(EXTRA_PACKAGE)
                : DEFAULT_TARGET;

        new Thread(() -> runProbe(target, startId), "ControlledRuntimeProbe").start();
        return START_NOT_STICKY;
    }

    private void runProbe(String target, int startId) {
        long started = System.currentTimeMillis();
        StringBuilder report = new StringBuilder();
        report.append("=== CONTROLLED RUNTIME REPORT ===\n");
        report.append("pid=").append(Process.myPid()).append('\n');
        report.append("target=").append(target).append('\n');

        try {
            TargetPackageSnapshot snapshot = TargetPackageSnapshot.capture(this, target);
            report.append(snapshot.describe()).append('\n');

            File optimized = new File(getCodeCacheDir(), "runtime-dex");
            if (!optimized.isDirectory() && !optimized.mkdirs()) {
                throw new IllegalStateException("Could not create runtime-dex directory");
            }

            String dexPath = snapshot.buildDexPath();
            DexClassLoader loader = new DexClassLoader(
                    dexPath,
                    optimized.getAbsolutePath(),
                    snapshot.nativeLibraryDir,
                    ClassLoader.getSystemClassLoader()
            );

            report.append("dexPathEntries=").append(1 + snapshot.splitSourceDirs.length).append('\n');
            report.append("classLoader=").append(loader.getClass().getName()).append('\n');

            Class<?> probe = Class.forName(PROBE_CLASS, false, loader);
            report.append("probeClass=").append(probe.getName()).append('\n');
            Class<?> parent = probe.getSuperclass();
            report.append("probeSuperclass=").append(parent == null ? "null" : parent.getName()).append('\n');
            report.append("classResolved=YES\n");
            report.append("stage=DEX_READY\n");
        } catch (Throwable error) {
            report.append("classResolved=NO\n");
            report.append("stage=FAILED\n");
            report.append("error=").append(error.getClass().getName()).append(": ")
                    .append(error.getMessage()).append('\n');
        }

        report.append("elapsedMs=").append(System.currentTimeMillis() - started).append('\n');
        report.append("=== END REPORT ===\n");

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
