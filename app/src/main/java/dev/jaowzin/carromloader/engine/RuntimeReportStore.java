package dev.jaowzin.carromloader.engine;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public final class RuntimeReportStore {
    private static final String FILE = "virtual-runtime-report.txt";

    private RuntimeReportStore() {}

    public static void write(Context context, String text) {
        try {
            File target = new File(context.getNoBackupFilesDir(), FILE);
            File temp = new File(context.getNoBackupFilesDir(), FILE + ".tmp");
            try (FileOutputStream out = new FileOutputStream(temp, false)) {
                out.write(text.getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
            if (target.exists() && !target.delete()) return;
            //noinspection ResultOfMethodCallIgnored
            temp.renameTo(target);
        } catch (Throwable ignored) {
        }
    }

    public static String read(Context context) {
        File target = new File(context.getNoBackupFilesDir(), FILE);
        if (!target.isFile()) return "No virtual runtime report yet.";
        try (FileInputStream in = new FileInputStream(target)) {
            byte[] bytes = new byte[(int) target.length()];
            int offset = 0;
            while (offset < bytes.length) {
                int read = in.read(bytes, offset, bytes.length - offset);
                if (read < 0) break;
                offset += read;
            }
            return new String(bytes, 0, offset, StandardCharsets.UTF_8);
        } catch (Throwable error) {
            return "Could not read runtime report: " + error;
        }
    }

    public static void clear(Context context) {
        //noinspection ResultOfMethodCallIgnored
        new File(context.getNoBackupFilesDir(), FILE).delete();
        //noinspection ResultOfMethodCallIgnored
        new File(context.getNoBackupFilesDir(), FILE + ".tmp").delete();
    }
}
