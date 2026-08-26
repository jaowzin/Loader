package dev.jaowzin.carromloader.runtime.core.system;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;
import dev.jaowzin.carromloader.runtime.core.env.AppSystemEnv;
import dev.jaowzin.carromloader.runtime.core.env.BEnvironment;
import dev.jaowzin.carromloader.runtime.core.system.accounts.BAccountManagerService;
import dev.jaowzin.carromloader.runtime.core.system.am.BActivityManagerService;
import dev.jaowzin.carromloader.runtime.core.system.am.BJobManagerService;
import dev.jaowzin.carromloader.runtime.core.system.location.BLocationManagerService;
import dev.jaowzin.carromloader.runtime.core.system.notification.BNotificationManagerService;
import dev.jaowzin.carromloader.runtime.core.system.os.BStorageManagerService;
import dev.jaowzin.carromloader.runtime.core.system.pm.BPackageInstallerService;
import dev.jaowzin.carromloader.runtime.core.system.pm.BPackageManagerService;

import dev.jaowzin.carromloader.runtime.core.system.user.BUserHandle;
import dev.jaowzin.carromloader.runtime.core.system.user.BUserManagerService;
import dev.jaowzin.carromloader.runtime.entity.pm.InstallOption;
import dev.jaowzin.carromloader.runtime.utils.FileUtils;

import dev.jaowzin.carromloader.runtime.core.system.JarManager;


public class CarromRuntimeSystem {
    private static CarromRuntimeSystem sCarromRuntimeSystem;
    private final List<ISystemService> mServices = new ArrayList<>();
    private final static AtomicBoolean isStartup = new AtomicBoolean(false);

    public static CarromRuntimeSystem getSystem() {
        if (sCarromRuntimeSystem == null) {
            synchronized (CarromRuntimeSystem.class) {
                if (sCarromRuntimeSystem == null) {
                    sCarromRuntimeSystem = new CarromRuntimeSystem();
                }
            }
        }
        return sCarromRuntimeSystem;
    }

    public void startup() {
        if (isStartup.getAndSet(true))
            return;
        BEnvironment.load();

        mServices.add(BPackageManagerService.get());
        mServices.add(BUserManagerService.get());
        mServices.add(BActivityManagerService.get());
        mServices.add(BJobManagerService.get());
        mServices.add(BStorageManagerService.get());
        mServices.add(BPackageInstallerService.get());

        mServices.add(BProcessManagerService.get());
        mServices.add(BAccountManagerService.get());
        mServices.add(BLocationManagerService.get());
        mServices.add(BNotificationManagerService.get());

        for (ISystemService service : mServices) {
            service.systemReady();
        }

        List<String> preInstallPackages = AppSystemEnv.getPreInstallPackages();
        for (String preInstallPackage : preInstallPackages) {
            try {
                if (!BPackageManagerService.get().isInstalled(preInstallPackage, BUserHandle.USER_ALL)) {
                    PackageInfo packageInfo = CarromRuntimeCore.getPackageManager().getPackageInfo(preInstallPackage, 0);
                    BPackageManagerService.get().installPackageAsUser(packageInfo.applicationInfo.sourceDir, InstallOption.installBySystem(), BUserHandle.USER_ALL);
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        
        JarManager.getInstance().initializeAsync();
        
        
     
    }
}
