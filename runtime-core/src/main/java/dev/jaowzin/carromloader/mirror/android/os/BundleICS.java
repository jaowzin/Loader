package dev.jaowzin.carromloader.mirror.android.os;

import android.os.Parcel;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.os.Bundle")
public interface BundleICS {
    @BField
    Parcel mParcelledData();
}
