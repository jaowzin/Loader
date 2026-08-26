package dev.jaowzin.carromloader.mirror.android.rms.resource;

import java.util.List;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.rms.resource.ReceiverResource")
public interface ReceiverResourceN {
    @BField
    List<String> mWhiteList();
}
