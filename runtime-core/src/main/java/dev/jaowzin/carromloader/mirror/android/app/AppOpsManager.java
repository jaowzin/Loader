package dev.jaowzin.carromloader.mirror.android.app;

import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.app.AppOpsManager")
public interface AppOpsManager {
    @BField
    IInterface mService();
}
