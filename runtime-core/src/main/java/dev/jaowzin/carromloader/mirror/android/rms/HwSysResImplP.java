package dev.jaowzin.carromloader.mirror.android.rms;

import java.util.Map;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.rms.HwSysResImpl")
public interface HwSysResImplP {
    @BField
    Map<Integer, java.util.ArrayList<String>> mWhiteListMap();
}
