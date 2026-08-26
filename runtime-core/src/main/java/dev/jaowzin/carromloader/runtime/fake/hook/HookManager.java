package dev.jaowzin.carromloader.runtime.fake.hook;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;
import dev.jaowzin.carromloader.runtime.fake.delegate.AppInstrumentation;

import dev.jaowzin.carromloader.runtime.fake.service.HCallbackProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IAccessibilityManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IAccountManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IActivityClientProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IActivityManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IActivityTaskManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IAlarmManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IAppOpsManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IAppWidgetManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IAttributionSourceProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IAutofillManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.ISensitiveContentProtectionManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.ISettingsSystemProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IConnectivityManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.ISystemSensorManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IContentProviderProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IXiaomiAttributionSourceProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IXiaomiSettingsProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IXiaomiMiuiServicesProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IDnsResolverProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IContextHubServiceProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IDeviceIdentifiersPolicyProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IDevicePolicyManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IDisplayManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IFingerprintManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IGraphicsStatsProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IJobServiceProxy;
import dev.jaowzin.carromloader.runtime.fake.service.ILauncherAppsProxy;
import dev.jaowzin.carromloader.runtime.fake.service.ILocationManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IMediaRouterServiceProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IMediaSessionManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IAudioServiceProxy;
import dev.jaowzin.carromloader.runtime.fake.service.ISensorPrivacyManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.ContentResolverProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IMiuiSecurityManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.SystemLibraryProxy;
import dev.jaowzin.carromloader.runtime.fake.service.ReLinkerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.MediaRecorderProxy;
import dev.jaowzin.carromloader.runtime.fake.service.NetworkPermissionCompat;
import dev.jaowzin.carromloader.runtime.fake.service.AudioRecordProxy;
import dev.jaowzin.carromloader.runtime.fake.service.MediaRecorderClassProxy;
import dev.jaowzin.carromloader.runtime.fake.service.SQLiteDatabaseProxy;
import dev.jaowzin.carromloader.runtime.fake.service.ClassLoaderProxy;
import dev.jaowzin.carromloader.runtime.fake.service.FileSystemProxy;
import dev.jaowzin.carromloader.runtime.fake.service.GmsProxy;
import dev.jaowzin.carromloader.runtime.fake.service.LevelDbProxy;
import dev.jaowzin.carromloader.runtime.fake.service.DeviceIdProxy;
import dev.jaowzin.carromloader.runtime.fake.service.GoogleAccountManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.AuthenticationProxy;
import dev.jaowzin.carromloader.runtime.fake.service.AndroidIdProxy;
import dev.jaowzin.carromloader.runtime.fake.service.AudioPermissionProxy;
import dev.jaowzin.carromloader.runtime.fake.service.NetworkPermissionCompat;

import dev.jaowzin.carromloader.runtime.fake.service.INetworkManagementServiceProxy;
import dev.jaowzin.carromloader.runtime.fake.service.INotificationManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IPackageManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IPermissionManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IPersistentDataBlockServiceProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IPhoneSubInfoProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IPowerManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.ApkAssetsProxy;
import dev.jaowzin.carromloader.runtime.fake.service.ResourcesManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IShortcutManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IStorageManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IStorageStatsManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.ISystemUpdateProxy;
import dev.jaowzin.carromloader.runtime.fake.service.ITelephonyManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.ITelephonyRegistryProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IUserManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IVibratorServiceProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IVpnManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IWifiManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IWifiScannerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.IWindowManagerProxy;
import dev.jaowzin.carromloader.runtime.fake.service.context.ContentServiceStub;
import dev.jaowzin.carromloader.runtime.fake.service.context.RestrictionsManagerStub;
import dev.jaowzin.carromloader.runtime.fake.service.libcore.OsStub;
import dev.jaowzin.carromloader.runtime.utils.Slog;
import dev.jaowzin.carromloader.runtime.utils.compat.BuildCompat;
import dev.jaowzin.carromloader.runtime.fake.service.ISettingsProviderProxy;
import dev.jaowzin.carromloader.runtime.fake.service.FeatureFlagUtilsProxy;
import dev.jaowzin.carromloader.runtime.fake.service.WorkManagerProxy;



public class HookManager {
    public static final String TAG = "HookManager";

    private static final HookManager sHookManager = new HookManager();

    private final Map<Class<?>, IInjectHook> mInjectors = new HashMap<>();

    public static HookManager get() {
        return sHookManager;
    }

    public void init() {
        if (CarromRuntimeCore.get().isBlackProcess() || CarromRuntimeCore.get().isServerProcess()) {
            addInjector(new IDisplayManagerProxy());
            addInjector(new OsStub());
            addInjector(new IActivityManagerProxy());
            addInjector(new IPackageManagerProxy());
            addInjector(new ITelephonyManagerProxy());
            addInjector(new HCallbackProxy());
            addInjector(new IAppOpsManagerProxy());
            addInjector(new INotificationManagerProxy());
            addInjector(new IAlarmManagerProxy());
            addInjector(new IAppWidgetManagerProxy());
            addInjector(new ContentServiceStub());
            addInjector(new IWindowManagerProxy());
            addInjector(new IUserManagerProxy());
            addInjector(new RestrictionsManagerStub());
            addInjector(new IMediaSessionManagerProxy());
            addInjector(new IAudioServiceProxy());
            addInjector(new ISensorPrivacyManagerProxy());
            addInjector(new ContentResolverProxy());
            addInjector(new SystemLibraryProxy());
            addInjector(new ReLinkerProxy());
            addInjector(new WorkManagerProxy());
            addInjector(new MediaRecorderProxy());
            addInjector(new AudioRecordProxy());
            addInjector(new IMiuiSecurityManagerProxy());
            addInjector(new ISettingsProviderProxy());
            addInjector(new FeatureFlagUtilsProxy());
            addInjector(new MediaRecorderClassProxy());
            addInjector(new SQLiteDatabaseProxy());
            addInjector(new ClassLoaderProxy());
            addInjector(new FileSystemProxy());
            addInjector(new GmsProxy());
            addInjector(new LevelDbProxy());
            addInjector(new DeviceIdProxy());
            addInjector(new GoogleAccountManagerProxy());
            addInjector(new AuthenticationProxy());
            addInjector(new AndroidIdProxy());
            addInjector(new AudioPermissionProxy());
            addInjector(new ILocationManagerProxy());
            addInjector(new IStorageManagerProxy());
            addInjector(new ILauncherAppsProxy());
            addInjector(new IJobServiceProxy());
            addInjector(new IAccessibilityManagerProxy());
            addInjector(new ITelephonyRegistryProxy());
            addInjector(new IDevicePolicyManagerProxy());
            addInjector(new IAccountManagerProxy());
            addInjector(new NetworkPermissionCompat());
            addInjector(new IConnectivityManagerProxy());
            addInjector(new IDnsResolverProxy());
                    addInjector(new IAttributionSourceProxy());
        addInjector(new IContentProviderProxy());
        addInjector(new ISettingsSystemProxy());
        addInjector(new ISystemSensorManagerProxy());
        
        
        addInjector(new IXiaomiAttributionSourceProxy());
        addInjector(new IXiaomiSettingsProxy());
        addInjector(new IXiaomiMiuiServicesProxy());
            addInjector(new IPhoneSubInfoProxy());
            addInjector(new IMediaRouterServiceProxy());
            addInjector(new IPowerManagerProxy());
            addInjector(new IContextHubServiceProxy());
            
            addInjector(new IVibratorServiceProxy());
            addInjector(new IPersistentDataBlockServiceProxy());
            addInjector(AppInstrumentation.get());
            
            addInjector(new IWifiManagerProxy());
            addInjector(new IWifiScannerProxy());
            addInjector(new ApkAssetsProxy());
            addInjector(new ResourcesManagerProxy());
            
            if (BuildCompat.isS()) {
                addInjector(new IActivityClientProxy(null));
                addInjector(new IVpnManagerProxy());
            }
            
            if (BuildCompat.isS()) {
                addInjector(new ISensitiveContentProtectionManagerProxy());
            }
            
            if (BuildCompat.isR()) {
                addInjector(new IPermissionManagerProxy());
            }
            
            if (BuildCompat.isQ()) {
                addInjector(new IActivityTaskManagerProxy());
            }
            
            if (BuildCompat.isPie()) {
                addInjector(new ISystemUpdateProxy());
            }
            
            if (BuildCompat.isOreo()) {
                addInjector(new IAutofillManagerProxy());
                addInjector(new IDeviceIdentifiersPolicyProxy());
                addInjector(new IStorageStatsManagerProxy());
            }
            
            if (BuildCompat.isN_MR1()) {
                addInjector(new IShortcutManagerProxy());
            }
            
            if (BuildCompat.isN()) {
                addInjector(new INetworkManagementServiceProxy());
            }
            
            if (BuildCompat.isM()) {
                addInjector(new IFingerprintManagerProxy());
                addInjector(new IGraphicsStatsProxy());
            }
            
            if (BuildCompat.isL()) {
                addInjector(new IJobServiceProxy());
            }
        }
        injectAll();
    }

    public void checkEnv(Class<?> clazz) {
        IInjectHook iInjectHook = mInjectors.get(clazz);
        if (iInjectHook != null && iInjectHook.isBadEnv()) {
            Log.d(TAG, "checkEnv: " + clazz.getSimpleName() + " is bad env");
            iInjectHook.injectHook();
        }
    }

    public void checkAll() {
        for (Class<?> aClass : mInjectors.keySet()) {
            IInjectHook iInjectHook = mInjectors.get(aClass);
            if (iInjectHook != null && iInjectHook.isBadEnv()) {
                Log.d(TAG, "checkEnv: " + aClass.getSimpleName() + " is bad env");
                iInjectHook.injectHook();
            }
        }
    }

    void addInjector(IInjectHook injectHook) {
        mInjectors.put(injectHook.getClass(), injectHook);
    }

    void injectAll() {
        for (IInjectHook value : mInjectors.values()) {
            try {
                Slog.d(TAG, "hook: " + value);
                value.injectHook();
            } catch (Exception e) {
                Slog.d(TAG, "hook error: " + value);
                
                handleHookError(value, e);
            }
        }
    }

    
    private void handleHookError(IInjectHook hook, Exception e) {
        String hookName = hook.getClass().getSimpleName();
        
        
        Slog.e(TAG, "Hook failed: " + hookName + " - " + e.getMessage(), e);
        
        
        if (hookName.contains("ActivityManager") || 
            hookName.contains("PackageManager") ||
            hookName.contains("WebView") ||
            hookName.contains("ContentProvider")) {
            
            Slog.w(TAG, "Critical hook failed: " + hookName + ", attempting recovery");
            
            try {
                
                if (hook.isBadEnv()) {
                    Slog.d(TAG, "Attempting to recover hook: " + hookName);
                    hook.injectHook();
                }
            } catch (Exception recoveryException) {
                Slog.e(TAG, "Hook recovery failed: " + hookName, recoveryException);
            }
        }
    }

    
    public boolean areCriticalHooksInstalled() {
        String[] criticalHooks = {
            "IActivityManagerProxy",
            "IPackageManagerProxy",
            "IContentProviderProxy"
        };
        
        for (String hookName : criticalHooks) {
            boolean found = false;
            for (Class<?> hookClass : mInjectors.keySet()) {
                if (hookClass.getSimpleName().equals(hookName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Slog.w(TAG, "Critical hook missing: " + hookName);
                return false;
            }
        }
        
        Slog.d(TAG, "All critical hooks are installed");
        return true;
    }

    
    public void reinitializeHooks() {
        Slog.d(TAG, "Reinitializing all hooks");
        
        
        mInjectors.clear();
        
        
        init();
        
        Slog.d(TAG, "Hook reinitialization completed");
    }
}
