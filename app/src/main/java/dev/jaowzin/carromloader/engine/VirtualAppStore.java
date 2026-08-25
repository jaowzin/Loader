package dev.jaowzin.carromloader.engine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public final class VirtualAppStore {
    private static final String ROOT = "virtual_apps";
    private static final String META = "package.properties";

    private VirtualAppStore() {}

    public static VirtualPackage importInstalled(Context context, String packageName) throws Exception {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
        PackageInfo pi = pm.getPackageInfo(packageName, 0);

        File storeRoot = new File(context.getNoBackupFilesDir(), ROOT);
        if (!storeRoot.exists() && !storeRoot.mkdirs()) {
            throw new IOException("Could not create virtual store");
        }

        File finalDir = new File(storeRoot, packageName);
        File stageDir = new File(storeRoot, packageName + ".staging");
        deleteRecursive(stageDir);
        if (!stageDir.mkdirs()) throw new IOException("Could not create staging directory");

        File apkDir = new File(stageDir, "apks");
        if (!apkDir.mkdirs()) throw new IOException("Could not create APK directory");

        List<File> copied = new ArrayList<>();
        File base = new File(apkDir, "base.apk");
        copy(new File(ai.sourceDir), base);
        copied.add(base);

        if (ai.splitSourceDirs != null) {
            for (int i = 0; i < ai.splitSourceDirs.length; i++) {
                String splitName = ai.splitNames != null && i < ai.splitNames.length
                        ? ai.splitNames[i]
                        : "split" + i;
                File out = new File(apkDir,
                        String.format("split-%02d-%s.apk", i, sanitize(splitName)));
                copy(new File(ai.splitSourceDirs[i]), out);
                copied.add(out);
            }
        }

        String versionName = pi.versionName == null ? "unknown" : pi.versionName;
        long versionCode = Build.VERSION.SDK_INT >= 28 ? pi.getLongVersionCode() : pi.versionCode;

        Properties meta = new Properties();
        meta.setProperty("packageName", packageName);
        meta.setProperty("versionName", versionName);
        meta.setProperty("versionCode", Long.toString(versionCode));
        meta.setProperty("apkCount", Integer.toString(copied.size()));
        meta.setProperty("sourceBase", ai.sourceDir == null ? "" : ai.sourceDir);
        meta.setProperty("importedAt", Long.toString(System.currentTimeMillis()));
        try (OutputStream output = new FileOutputStream(new File(stageDir, META))) {
            meta.store(output, "Carrom Loader clean-room virtual package");
        }

        deleteRecursive(finalDir);
        if (!stageDir.renameTo(finalDir)) {
            throw new IOException("Could not publish virtual package");
        }
        return loadImported(context, packageName);
    }

    public static VirtualPackage loadImported(Context context, String packageName) throws Exception {
        File root = new File(new File(context.getNoBackupFilesDir(), ROOT), packageName);
        File metaFile = new File(root, META);
        File apkDir = new File(root, "apks");
        if (!metaFile.isFile() || !apkDir.isDirectory()) {
            throw new IOException("Virtual package is not imported");
        }

        Properties meta = new Properties();
        try (InputStream input = new FileInputStream(metaFile)) {
            meta.load(input);
        }

        File[] files = apkDir.listFiles((dir, name) -> name.endsWith(".apk"));
        if (files == null || files.length == 0) throw new IOException("No imported APK files");
        Arrays.sort(files, Comparator.comparing(file -> file.getName().equals("base.apk") ? "" : file.getName()));

        return new VirtualPackage(
                meta.getProperty("packageName", packageName),
                meta.getProperty("versionName", "unknown"),
                Long.parseLong(meta.getProperty("versionCode", "0")),
                root,
                new ArrayList<>(Arrays.asList(files))
        );
    }

    public static boolean isImported(Context context, String packageName) {
        try {
            loadImported(context, packageName);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void clear(Context context, String packageName) {
        File root = new File(new File(context.getNoBackupFilesDir(), ROOT), packageName);
        deleteRecursive(root);
    }

    private static String sanitize(String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static void copy(File source, File target) throws IOException {
        if (!source.isFile()) throw new IOException("Missing source: " + source);
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[1024 * 256];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            out.flush();
        }
    }

    private static void deleteRecursive(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) for (File child : children) deleteRecursive(child);
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }
}
