package dev.jaowzin.carromloader.mirror.android.content.pm;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;
import dev.jaowzin.carromloader.bridge.annotation.BMethod;

@BClassName("android.content.pm.ApplicationInfo")
public interface ApplicationInfoP {
    @BField
    String[] splitClassLoaderNames();

    @BMethod
    void setHiddenApiEnforcementPolicy(int int0);
}
