package dev.jaowzin.carromloader.mirror.android.os.health;

import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.os.health.SystemHealthManager")
public interface SystemHealthManager {
    @BField
    IInterface mBatteryStats();
}
