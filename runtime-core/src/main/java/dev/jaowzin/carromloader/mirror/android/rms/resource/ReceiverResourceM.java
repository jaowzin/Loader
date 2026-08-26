package dev.jaowzin.carromloader.mirror.android.rms.resource;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.rms.resource.ReceiverResource")
public interface ReceiverResourceM {
    @BField
    String[] mWhiteList();
}
