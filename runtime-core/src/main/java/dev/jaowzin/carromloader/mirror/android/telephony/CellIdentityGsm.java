package dev.jaowzin.carromloader.mirror.android.telephony;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BConstructor;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.telephony.CellIdentityGsm")
public interface CellIdentityGsm {
    @BConstructor
    CellIdentityGsm _new();

    @BField
    int mCid();

    @BField
    int mLac();

    @BField
    int mMcc();

    @BField
    int mMnc();
}
