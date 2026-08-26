package dev.jaowzin.carromloader.mirror.android.location;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;
import dev.jaowzin.carromloader.bridge.annotation.BMethod;

@BClassName("android.location.LocationRequest")
public interface LocationRequestL {
    @BField
    boolean mHideFromAppOps();

    @BField
    String mProvider();

    @BField
    Object mWorkSource();

    @BMethod
    String getProvider();
}
