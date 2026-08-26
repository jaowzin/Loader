package dev.jaowzin.carromloader.mirror.android.rms.resource;

import java.util.Map;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.rms.resource.ReceiverResource")
public interface ReceiverResourceO {
    @BField
    Map<Integer, java.util.List<String>> mWhiteListMap();
}
