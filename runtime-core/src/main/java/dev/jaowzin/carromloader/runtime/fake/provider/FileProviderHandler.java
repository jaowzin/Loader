package dev.jaowzin.carromloader.runtime.fake.provider;

import android.content.Context;
import android.content.pm.ProviderInfo;
import android.net.Uri;

import java.io.File;
import java.util.List;

import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;
import dev.jaowzin.carromloader.runtime.app.BActivityThread;
import dev.jaowzin.carromloader.runtime.utils.compat.BuildCompat;


public class FileProviderHandler {

    public static Uri convertFileUri(Context context, Uri uri) {
        if (BuildCompat.isN()) {
            File file = convertFile(context, uri);
            if (file == null)
                return null;
            return CarromRuntimeCore.getBStorageManager().getUriForFile(file.getAbsolutePath());
        }
        return uri;
    }

    public static File convertFile(Context context, Uri uri) {
        List<ProviderInfo> providers = BActivityThread.getProviders();
        for (ProviderInfo provider : providers) {
            try {
                File fileForUri = FileProvider.getFileForUri(context, provider.authority, uri);
                if (fileForUri != null && fileForUri.exists()) {
                    return fileForUri;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
