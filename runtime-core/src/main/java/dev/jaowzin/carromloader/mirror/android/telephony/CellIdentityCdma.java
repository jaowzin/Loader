package dev.jaowzin.carromloader.mirror.android.telephony;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BConstructor;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.telephony.CellIdentityCdma")
public interface CellIdentityCdma {
    @BConstructor
    CellIdentityCdma _new();

    @BField
    int mBasestationId();

    @BField
    int mNetworkId();

    @BField
    int mSystemId();
}
