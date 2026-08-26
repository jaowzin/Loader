package dev.jaowzin.carromloader.mirror.android.telephony;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.telephony.NeighboringCellInfo")
public interface NeighboringCellInfo {
    @BField
    int mCid();

    @BField
    int mLac();

    @BField
    int mRssi();
}
