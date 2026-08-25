package dev.jaowzin.carromloader;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.File;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.configuration.AppLifecycleCallback;
import top.niunaijun.blackbox.app.configuration.ClientConfiguration;

public final class CarromLoaderApp extends Application {
    private static final String TAG = "CarromLoaderApp";
    private static final String TARGET = "com.miniclip.carrom";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        BlackBoxCore core = BlackBoxCore.get();

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
                // Do not forward Loader/Carrom logs to third-party channels.
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
                    CarromModuleBridge.onTargetApplicationReady(application, processName, userId);
                }
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            BlackBoxCore.get().doCreate();
        } catch (Throwable error) {
            Log.e(TAG, "BlackBoxCore.doCreate failed", error);
        }
    }
}
