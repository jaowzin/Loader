package dev.jaowzin.carromloader;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;

import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;
import dev.jaowzin.carromloader.runtime.app.configuration.AppLifecycleCallback;
import dev.jaowzin.carromloader.runtime.app.configuration.ClientConfiguration;

public final class CarromLoaderApp extends Application {
    private static final String TAG = "CarromLoaderApp";
    private static final String TARGET = "com.miniclip.carrom";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        CarromRuntimeCore core = CarromRuntimeCore.get();

        try {
            core.closeCodeInit();
        } catch (Throwable error) {
            Log.w(TAG, "closeCodeInit failed", error);
        }

        try {
            core.onBeforeMainApplicationAttach(this, base);
        } catch (Throwable error) {
            Log.w(TAG, "onBeforeMainApplicationAttach failed", error);
        }

        core.doAttachBaseContext(base, new ClientConfiguration() {
            @Override
            public String getHostPackageName() {
                return base.getPackageName();
            }

            @Override
            public boolean isHideRoot() {
                return false;
            }

            @Override
            public boolean isEnableDaemonService() {
                return false;
            }

            @Override
            public boolean isEnableLauncherActivity() {
                return true;
            }

            @Override
            public boolean isUseVpnNetwork() {
                return false;
            }

            @Override
            public boolean isDisableFlagSecure() {
                return false;
            }

            @Override
            public boolean requestInstallPackage(File file, int userId) {
                return false;
            }

            @Override
            public String getLogSenderChatId() {
                return "";
            }
        });

        try {
            core.onAfterMainApplicationAttach(this, base);
        } catch (Throwable error) {
            Log.w(TAG, "onAfterMainApplicationAttach failed", error);
        }

        core.addAppLifecycleCallback(new AppLifecycleCallback() {
            @Override
            public void beforeCreateApplication(String packageName, String processName, Context context, int userId) {
                if (TARGET.equals(packageName)) {
                    CarromModuleBridge.note("beforeCreateApplication", processName, userId);
                }
            }

            @Override
            public void beforeApplicationOnCreate(String packageName, String processName, Application application, int userId) {
                if (TARGET.equals(packageName)) {
                    CarromModuleBridge.note("beforeApplicationOnCreate", processName, userId);
                }
            }

            @Override
            public void afterApplicationOnCreate(String packageName, String processName, Application application, int userId) {
                if (TARGET.equals(packageName)) {
                    CarromModuleBridge.onTargetApplicationReady(
                            application,
                            processName,
                            userId,
                            CarromLoaderApp.this
                    );
                }
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Only the Loader's real main process owns the status socket. Proxy/guest
        // processes publish to it but never attempt to bind the same endpoint.
        String processName = currentProcessName();
        if (getPackageName().equals(processName)) {
            ModuleStatusIpc.startHost(this);
            Log.i(TAG, "status IPC host started in " + processName);
        }

        try {
            CarromRuntimeCore.get().doCreate();
        } catch (Throwable error) {
            Log.e(TAG, "CarromRuntimeCore.doCreate failed", error);
        }
    }

    private String currentProcessName() {
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                String value = Application.getProcessName();
                if (value != null && !value.trim().isEmpty()) return value;
            } catch (Throwable ignored) {
            }
        }

        try (FileInputStream input = new FileInputStream("/proc/self/cmdline")) {
            byte[] buffer = new byte[256];
            int count = input.read(buffer);
            if (count > 0) {
                int end = 0;
                while (end < count && buffer[end] != 0) end++;
                return new String(buffer, 0, end, java.nio.charset.StandardCharsets.UTF_8).trim();
            }
        } catch (Throwable ignored) {
        }
        return "";
    }
}
