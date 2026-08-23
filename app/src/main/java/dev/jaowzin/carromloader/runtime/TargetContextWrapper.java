package dev.jaowzin.carromloader.runtime;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

/**
 * Clean-room context facade for the controlled runtime. It delegates code/resources
 * and package metadata to the target package context while keeping lifecycle ownership
 * in Loader. Private-data redirection is intentionally not implemented yet.
 */
public final class TargetContextWrapper extends ContextWrapper {
    private final Context target;

    public TargetContextWrapper(Context host, Context target) {
        super(host);
        if (target == null) throw new IllegalArgumentException("target context == null");
        this.target = target;
    }

    @Override
    public String getPackageName() {
        return target.getPackageName();
    }

    @Override
    public ClassLoader getClassLoader() {
        return target.getClassLoader();
    }

    @Override
    public Resources getResources() {
        return target.getResources();
    }

    @Override
    public AssetManager getAssets() {
        return target.getAssets();
    }

    @Override
    public Resources.Theme getTheme() {
        return target.getTheme();
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return target.getApplicationInfo();
    }

    @Override
    public PackageManager getPackageManager() {
        return target.getPackageManager();
    }

    @Override
    public String getPackageCodePath() {
        return target.getPackageCodePath();
    }

    @Override
    public String getPackageResourcePath() {
        return target.getPackageResourcePath();
    }

    public Context getTargetContext() {
        return target;
    }
}
