package dev.jaowzin.carromloader.runtime;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public final class RuntimeReportStore {
    private static final String FILE_NAME = "controlled-runtime-report.txt";

    private RuntimeReportStore() {
    }

    public static synchronized void write(Context context, String report) throws Exception {
        File target = new File(context.getFilesDir(), FILE_NAME);
        File temp = new File(context.getFilesDir(), FILE_NAME + ".tmp");
        try (FileOutputStream out = new FileOutputStream(temp, false)) {
            out.write(report.getBytes(StandardCharsets.UTF_8));
            out.getFD().sync();
        }
        if (target.exists() && !target.delete()) {
            throw new IllegalStateException("Could not replace old runtime report");
        }
        if (!temp.renameTo(target)) {
            throw new IllegalStateException("Could not publish runtime report");
        }
    }

    public static synchronized String read(Context context) {
        File target = new File(context.getFilesDir(), FILE_NAME);
        if (!target.isFile()) return "No controlled-runtime probe has run yet.";
        try (FileInputStream in = new FileInputStream(target)) {
            byte[] data = new byte[(int) target.length()];
            int offset = 0;
            while (offset < data.length) {
                int read = in.read(data, offset, data.length - offset);
                if (read < 0) break;
                offset += read;
            }
            return new String(data, 0, offset, StandardCharsets.UTF_8);
        } catch (Throwable error) {
            return "Could not read runtime report: " + error;
        }
    }
}
