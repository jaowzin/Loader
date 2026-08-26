package dev.jaowzin.carromloader.runtime.core.system;

import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;
import dev.jaowzin.carromloader.runtime.core.system.accounts.BAccountManagerService;
import dev.jaowzin.carromloader.runtime.core.system.am.BActivityManagerService;
import dev.jaowzin.carromloader.runtime.core.system.am.BJobManagerService;
import dev.jaowzin.carromloader.runtime.core.system.location.BLocationManagerService;
import dev.jaowzin.carromloader.runtime.core.system.notification.BNotificationManagerService;
import dev.jaowzin.carromloader.runtime.core.system.os.BStorageManagerService;
import dev.jaowzin.carromloader.runtime.core.system.pm.BPackageManagerService;
import dev.jaowzin.carromloader.runtime.core.system.status.CarromStatusService;
import dev.jaowzin.carromloader.runtime.core.system.user.BUserManagerService;

public class ServiceManager {
    private static ServiceManager sServiceManager = null;
    public static final String ACTIVITY_MANAGER = "activity_manager";
    public static final String JOB_MANAGER = "job_manager";
    public static final String PACKAGE_MANAGER = "package_manager";
    public static final String STORAGE_MANAGER = "storage_manager";
    public static final String USER_MANAGER = "user_manager";
    public static final String ACCOUNT_MANAGER = "account_manager";
    public static final String LOCATION_MANAGER = "location_manager";
    public static final String NOTIFICATION_MANAGER = "notification_manager";
    public static final String CARROM_STATUS = "carrom_status";

    private final Map<String, IBinder> mCaches = new HashMap<>();

    public static ServiceManager get() {
        if (sServiceManager == null) {
            synchronized (ServiceManager.class) {
                if (sServiceManager == null) {
                    sServiceManager = new ServiceManager();
                }
            }
        }
        return sServiceManager;
    }

    public static IBinder getService(String name) {
        return get().getServiceInternal(name);
    }

    private ServiceManager() {
        mCaches.put(ACTIVITY_MANAGER, BActivityManagerService.get());
        mCaches.put(JOB_MANAGER, BJobManagerService.get());
        mCaches.put(PACKAGE_MANAGER, BPackageManagerService.get());
        mCaches.put(STORAGE_MANAGER, BStorageManagerService.get());
        mCaches.put(USER_MANAGER, BUserManagerService.get());
        mCaches.put(ACCOUNT_MANAGER, BAccountManagerService.get());
        mCaches.put(LOCATION_MANAGER, BLocationManagerService.get());
        mCaches.put(NOTIFICATION_MANAGER, BNotificationManagerService.get());
        mCaches.put(CARROM_STATUS, CarromStatusService.get());
    }

    public IBinder getServiceInternal(String name) {
        return mCaches.get(name);
    }

    public static void initBlackManager() {
        CarromRuntimeCore.get().getService(ACTIVITY_MANAGER);
        CarromRuntimeCore.get().getService(JOB_MANAGER);
        CarromRuntimeCore.get().getService(PACKAGE_MANAGER);
        CarromRuntimeCore.get().getService(STORAGE_MANAGER);
        CarromRuntimeCore.get().getService(USER_MANAGER);
        CarromRuntimeCore.get().getService(ACCOUNT_MANAGER);
        CarromRuntimeCore.get().getService(LOCATION_MANAGER);
        CarromRuntimeCore.get().getService(NOTIFICATION_MANAGER);
        CarromRuntimeCore.get().getService(CARROM_STATUS);
    }
}
