package dev.jaowzin.carromloader.runtime;

import android.app.Application;
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
    private volatile Application targetApplication;

    public TargetContextWrapper(Context host, Context target) {
        super(host);
        if (target == null) throw new IllegalArgumentException("target context == null");
        this.target = target;
    }

    /**
     * Called immediately after Instrumentation.newApplication(). From that point on,
     * getApplicationContext() behaves like the target process instead of leaking the
     * Loader Application context into target libraries.
     */
    public void setTargetApplication(Application application) {
        this.targetApplication = application;
    }

    @Override
    public Context getApplicationContext() {
        Application application = targetApplication;
        return application != null ? application : this;
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
