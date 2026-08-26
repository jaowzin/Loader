package dev.jaowzin.carromloader;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Tiny same-UID, cross-process status channel between the virtual Carrom process
 * and the Loader dashboard. A plain file is used instead of SharedPreferences so
 * there is no multi-process cache ambiguity.
 */
final class ModuleStatusStore {
    private static final String FILE_NAME = "carrom_module_status.txt";

    private ModuleStatusStore() {}

    static void write(Context context, String value) {
        if (context == null || value == null) return;
        File dir = context.getFilesDir();
        if (dir == null) return;
        File target = new File(dir, FILE_NAME);
        File temp = new File(dir, FILE_NAME + ".tmp");
        try (FileOutputStream output = new FileOutputStream(temp, false)) {
            output.write(value.getBytes(StandardCharsets.UTF_8));
            output.flush();
            try {
                output.getFD().sync();
            } catch (Throwable ignored) {
            }
        } catch (Throwable ignored) {
            return;
        }

        if (!temp.renameTo(target)) {
            try (FileInputStream input = new FileInputStream(temp);
                 FileOutputStream output = new FileOutputStream(target, false)) {
                byte[] buffer = new byte[2048];
                int read;
                while ((read = input.read(buffer)) > 0) output.write(buffer, 0, read);
                output.flush();
            } catch (Throwable ignored) {
            }
            //noinspection ResultOfMethodCallIgnored
            temp.delete();
        }
    }

    static String read(Context context) {
        if (context == null) return null;
        File dir = context.getFilesDir();
        if (dir == null) return null;
        File file = new File(dir, FILE_NAME);
        if (!file.isFile()) return null;

        try (FileInputStream input = new FileInputStream(file);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[2048];
            int read;
            while ((read = input.read(buffer)) > 0) output.write(buffer, 0, read);
            return output.toString("UTF-8");
        } catch (Throwable ignored) {
            return null;
        }
    }
}
