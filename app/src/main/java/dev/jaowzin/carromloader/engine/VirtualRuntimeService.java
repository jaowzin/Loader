package dev.jaowzin.carromloader.engine;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;
import dev.jaowzin.carromloader.carrom.CarromTarget;

public final class VirtualRuntimeService extends Service {
    public static final String ACTION_PREPARE = "dev.jaowzin.carromloader.virtual.PREPARE";
    public static final String EXTRA_PACKAGE = "target_package";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String target = intent != null && intent.getStringExtra(EXTRA_PACKAGE) != null
                ? intent.getStringExtra(EXTRA_PACKAGE)
                : CarromTarget.PACKAGE;
        new Thread(() -> runPrepare(target, startId), "VirtualRuntimePrepare").start();
        return START_NOT_STICKY;
    }

    private void runPrepare(String target, int startId) {
        long started = System.currentTimeMillis();
        StringBuilder report = new StringBuilder();
        report.append("=== CLEAN-ROOM VIRTUAL RUNTIME ===\n");
        report.append("pid=").append(Process.myPid()).append('\n');
        report.append("process=").append(Build.VERSION.SDK_INT >= 28 ? Application.getProcessName() : ":virtual").append('\n');
        report.append("target=").append(target).append('\n');
        report.append("checkpoint=STARTED\n");
        checkpoint(report);

        try {
            VirtualPackage virtualPackage = VirtualAppStore.loadImported(this, target);
            report.append("version=").append(virtualPackage.versionName)
                    .append(" (").append(virtualPackage.versionCode).append(")\n");
            report.append("privateRoot=").append(virtualPackage.rootDir.getAbsolutePath()).append('\n');
            report.append("apkCount=").append(virtualPackage.apkFiles.size()).append('\n');
            for (int i = 0; i < virtualPackage.apkFiles.size(); i++) {
                report.append("apk[").append(i).append("]=")
                        .append(virtualPackage.apkFiles.get(i).getAbsolutePath()).append('\n');
            }
            report.append("checkpoint=PACKAGE_LOADED\n");
            checkpoint(report);

            File base = virtualPackage.baseApk();
            PackageInfo archiveInfo = base == null ? null : getPackageManager().getPackageArchiveInfo(
                    base.getAbsolutePath(), PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            report.append("archivePackage=")
                    .append(archiveInfo == null ? "null" : archiveInfo.packageName).append('\n');

            String abi = selectAbi(virtualPackage);
            report.append("selectedAbi=").append(abi == null ? "NONE" : abi).append('\n');
            if (abi == null) throw new IllegalStateException("No supported native ABI found in imported APKs");

            File nativeDir = virtualPackage.nativeDir();
            deleteRecursive(nativeDir);
            if (!nativeDir.mkdirs()) throw new IllegalStateException("Could not create native directory");
            int nativeCount = extractNativeLibraries(virtualPackage, abi, nativeDir);
            report.append("nativeDir=").append(nativeDir.getAbsolutePath()).append('\n');
            report.append("nativeCount=").append(nativeCount).append('\n');
            File gameLib = findGameLibrary(nativeDir);
            report.append("gameLibrary=").append(gameLib == null ? "NOT_FOUND" : gameLib.getName()).append('\n');
            report.append("checkpoint=NATIVE_READY\n");
            checkpoint(report);

            File optimized = virtualPackage.optimizedDir();
            if (!optimized.exists() && !optimized.mkdirs()) {
                throw new IllegalStateException("Could not create optimized directory");
            }

            DexClassLoader loader = new DexClassLoader(
                    virtualPackage.dexPath(),
                    optimized.getAbsolutePath(),
                    nativeDir.getAbsolutePath(),
                    getClassLoader()
            );
            report.append("classLoader=").append(loader.getClass().getName()).append('\n');
            report.append("dexPath=").append(virtualPackage.dexPath()).append('\n');

            Class<?> appClass = Class.forName(CarromTarget.APPLICATION, false, loader);
            report.append("applicationResolved=").append(appClass.getName()).append('\n');
            Class<?> activityClass = Class.forName(CarromTarget.ACTIVITY, false, loader);
            report.append("activityResolved=").append(activityClass.getName()).append('\n');
            report.append("targetCodeExecuted=NO\n");
            report.append("checkpoint=DEX_READY\n");
            report.append("stage=VIRTUAL_CODE_READY\n");
        } catch (Throwable error) {
            report.append("checkpoint=FAILED\n");
            report.append("stage=VIRTUAL_PREPARE_FAILED\n");
            appendError(report, error);
        }

        report.append("elapsedMs=").append(System.currentTimeMillis() - started).append('\n');
        report.append("=== END VIRTUAL RUNTIME ===\n");
        RuntimeReportStore.write(this, report.toString());
        stopSelf(startId);
    }

    private String selectAbi(VirtualPackage virtualPackage) throws Exception {
        for (String abi : Build.SUPPORTED_ABIS) {
            String prefix = "lib/" + abi + "/";
            for (File apk : virtualPackage.apkFiles) {
                try (ZipFile zip = new ZipFile(apk)) {
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (!entry.isDirectory() && entry.getName().startsWith(prefix)
                                && entry.getName().endsWith(".so")) {
                            return abi;
                        }
                    }
                }
            }
        }
        return null;
    }

    private int extractNativeLibraries(VirtualPackage virtualPackage, String abi, File outputDir) throws Exception {
        String prefix = "lib/" + abi + "/";
        int count = 0;
        byte[] buffer = new byte[1024 * 128];
        for (File apk : virtualPackage.apkFiles) {
            try (ZipFile zip = new ZipFile(apk)) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.isDirectory() || !entry.getName().startsWith(prefix)
                            || !entry.getName().endsWith(".so")) continue;
                    String fileName = entry.getName().substring(prefix.length());
                    if (fileName.contains("/")) continue;
                    File outFile = new File(outputDir, fileName);
                    try (InputStream in = zip.getInputStream(entry);
                         FileOutputStream out = new FileOutputStream(outFile, false)) {
                        int read;
                        while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
                        out.flush();
                    }
                    //noinspection ResultOfMethodCallIgnored
                    outFile.setReadable(true, true);
                    count++;
                }
            }
        }
        return count;
    }

    private File findGameLibrary(File nativeDir) {
        File[] files = nativeDir.listFiles();
        if (files == null) return null;
        for (File file : files) {
            if (file.getName().startsWith(CarromTarget.GAME_LIBRARY_PREFIX)
                    && file.getName().endsWith(".so")) return file;
        }
        return null;
    }

    private void checkpoint(StringBuilder report) {
        RuntimeReportStore.write(this, report.toString() + "=== REPORT IN PROGRESS ===\n");
    }

    private void appendError(StringBuilder report, Throwable error) {
        Throwable current = error;
        int depth = 0;
        while (current != null && depth < 8) {
            report.append(depth == 0 ? "error=" : "cause" + (depth - 1) + "=")
                    .append(current.getClass().getName()).append(": ")
                    .append(current.getMessage()).append('\n');
            current = current.getCause();
            depth++;
        }
    }

    private void deleteRecursive(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) for (File child : children) deleteRecursive(child);
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
