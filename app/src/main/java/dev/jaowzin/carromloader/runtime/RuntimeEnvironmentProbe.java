package dev.jaowzin.carromloader.runtime;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

public final class RuntimeEnvironmentProbe {
    private RuntimeEnvironmentProbe() {
    }

    public static String probe(Context host, String target, String activityClass) throws Exception {
        StringBuilder out = new StringBuilder();
        PackageManager pm = host.getPackageManager();

        Context targetContext = host.createPackageContext(
                target,
                Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
        );
        ApplicationInfo appInfo = targetContext.getApplicationInfo();
        Resources resources = targetContext.getResources();
        AssetManager assets = targetContext.getAssets();
        ClassLoader packageLoader = targetContext.getClassLoader();

        ActivityInfo activityInfo = pm.getActivityInfo(
                new ComponentName(target, activityClass),
                PackageManager.GET_META_DATA
        );

        CharSequence appLabel = pm.getApplicationLabel(appInfo);
        Class<?> resolved = Class.forName(activityClass, false, packageLoader);

        out.append("contextPackage=").append(targetContext.getPackageName()).append('\n');
        out.append("contextClass=").append(targetContext.getClass().getName()).append('\n');
        out.append("contextClassLoader=").append(packageLoader.getClass().getName()).append('\n');
        out.append("resourcesClass=").append(resources.getClass().getName()).append('\n');
        out.append("assetsClass=").append(assets.getClass().getName()).append('\n');
        out.append("applicationLabel=").append(appLabel == null ? "null" : appLabel).append('\n');
        out.append("targetSdk=").append(appInfo.targetSdkVersion).append('\n');
        out.append("applicationClass=").append(appInfo.className == null ? "<default>" : appInfo.className).append('\n');
        out.append("applicationProcess=").append(appInfo.processName).append('\n');
        out.append("activityName=").append(activityInfo.name).append('\n');
        out.append("activityProcess=").append(activityInfo.processName).append('\n');
        out.append("activityExported=").append(activityInfo.exported).append('\n');
        out.append("activityTheme=0x").append(Integer.toHexString(activityInfo.getThemeResource())).append('\n');
        out.append("packageLoaderResolved=").append(resolved.getName()).append('\n');
        out.append("resourcesReady=YES\n");
        out.append("assetsReady=YES\n");
        out.append("packageContextReady=YES\n");
        return out.toString();
    }
}
