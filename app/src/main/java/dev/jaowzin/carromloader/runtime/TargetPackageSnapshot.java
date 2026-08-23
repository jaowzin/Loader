package dev.jaowzin.carromloader.runtime;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TargetPackageSnapshot {
    public final String packageName;
    public final long versionCode;
    public final String versionName;
    public final String sourceDir;
    public final String[] splitSourceDirs;
    public final String nativeLibraryDir;
    public final String[] supportedAbis;

    private TargetPackageSnapshot(
            String packageName,
            long versionCode,
            String versionName,
            String sourceDir,
            String[] splitSourceDirs,
            String nativeLibraryDir,
            String[] supportedAbis
    ) {
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.sourceDir = sourceDir;
        this.splitSourceDirs = splitSourceDirs == null ? new String[0] : splitSourceDirs.clone();
        this.nativeLibraryDir = nativeLibraryDir;
        this.supportedAbis = supportedAbis == null ? new String[0] : supportedAbis.clone();
    }

    public static TargetPackageSnapshot capture(Context context, String packageName)
            throws PackageManager.NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageInfo(packageName, 0);
        ApplicationInfo app = info.applicationInfo;
        if (app == null || app.sourceDir == null) {
            throw new IllegalStateException("Target ApplicationInfo/sourceDir unavailable");
        }

        long code = Build.VERSION.SDK_INT >= 28 ? info.getLongVersionCode() : info.versionCode;
        return new TargetPackageSnapshot(
                packageName,
                code,
                info.versionName,
                app.sourceDir,
                app.splitSourceDirs,
                app.nativeLibraryDir,
                Build.SUPPORTED_ABIS
        );
    }

    public String buildDexPath() {
        List<String> paths = new ArrayList<>();
        paths.add(sourceDir);
        paths.addAll(Arrays.asList(splitSourceDirs));
        return String.join(File.pathSeparator, paths);
    }

    public String describe() {
        return "package=" + packageName
                + "\nversion=" + (versionName == null ? "?" : versionName) + " (" + versionCode + ")"
                + "\nbase=" + sourceDir
                + "\nsplits=" + splitSourceDirs.length
                + "\nnativeLibraryDir=" + nativeLibraryDir
                + "\nabis=" + Arrays.toString(supportedAbis);
    }
}
