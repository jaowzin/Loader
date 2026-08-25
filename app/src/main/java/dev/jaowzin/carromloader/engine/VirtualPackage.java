package dev.jaowzin.carromloader.engine;

import java.io.File;
import java.util.Collections;
import java.util.List;

public final class VirtualPackage {
    public final String packageName;
    public final String versionName;
    public final long versionCode;
    public final File rootDir;
    public final List<File> apkFiles;

    public VirtualPackage(String packageName, String versionName, long versionCode,
                          File rootDir, List<File> apkFiles) {
        this.packageName = packageName;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.rootDir = rootDir;
        this.apkFiles = Collections.unmodifiableList(apkFiles);
    }

    public File baseApk() {
        return apkFiles.isEmpty() ? null : apkFiles.get(0);
    }

    public File nativeDir() {
        return new File(rootDir, "native");
    }

    public File optimizedDir() {
        return new File(rootDir, "oat");
    }

    public String dexPath() {
        StringBuilder out = new StringBuilder();
        for (File apk : apkFiles) {
            if (out.length() > 0) out.append(File.pathSeparatorChar);
            out.append(apk.getAbsolutePath());
        }
        return out.toString();
    }
}
