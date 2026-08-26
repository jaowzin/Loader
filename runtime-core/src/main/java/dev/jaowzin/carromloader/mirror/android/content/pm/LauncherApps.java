package dev.jaowzin.carromloader.mirror.android.content.pm;

import android.content.pm.PackageManager;
import android.os.IInterface;
import android.os.UserManager;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.content.pm.LauncherApps")
public interface LauncherApps {
    @BField
    PackageManager mPm();

    @BField
    IInterface mService();

    @BField
    UserManager mUserManager();
}
