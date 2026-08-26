package dev.jaowzin.carromloader.runtime.proxy;

import java.util.Locale;

import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;


public class ProxyManifest {
    public static final int FREE_COUNT = 50;

    public static boolean isProxy(String msg) {
        return getBindProvider().equals(msg) || msg.contains("proxy_content_provider_");
    }

    public static String getBindProvider() {
        return CarromRuntimeCore.getHostPkg() + ".carromruntime.SystemCallProvider";
    }

    public static String getProxyAuthorities(int index) {
        return String.format(Locale.CHINA, "%s.proxy_content_provider_%d", CarromRuntimeCore.getHostPkg(), index);
    }

    public static String getProxyPendingActivity(int index) {
        return String.format(Locale.CHINA, "dev.jaowzin.carromloader.runtime.proxy.ProxyPendingActivity$P%d", index);
    }

    public static String getProxyActivity(int index) {
        return String.format(Locale.CHINA, "dev.jaowzin.carromloader.runtime.proxy.ProxyActivity$P%d", index);
    }

    public static String TransparentProxyActivity(int index) {
        return String.format(Locale.CHINA, "dev.jaowzin.carromloader.runtime.proxy.TransparentProxyActivity$P%d", index);
    }

    public static String getProxyService(int index) {
        return String.format(Locale.CHINA, "dev.jaowzin.carromloader.runtime.proxy.ProxyService$P%d", index);
    }

    public static String getProxyJobService(int index) {
        return String.format(Locale.CHINA, "dev.jaowzin.carromloader.runtime.proxy.ProxyJobService$P%d", index);
    }

    public static String getProxyFileProvider() {
        return CarromRuntimeCore.getHostPkg() + ".carromruntime.FileProvider";
    }

    public static String getProxyReceiver() {
        return CarromRuntimeCore.getHostPkg() + ".stub_receiver";
    }

    public static String getProcessName(int bPid) {
        return CarromRuntimeCore.getHostPkg() + ":p" + bPid;
    }
}
