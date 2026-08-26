package dev.jaowzin.carromloader.mirror.android.content;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;


@BClassName("android.content.AttributionSourceState")
public interface AttributionSourceState {
    @BField
    String packageName();

    @BField
    int uid();
}
