package dev.jaowzin.carromloader.mirror.android.app.usage;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BConstructor;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.app.usage.StorageStats")
public interface StorageStats {
    @BConstructor
    StorageStats _new();

    @BField
    long cacheBytes();

    @BField
    long codeBytes();

    @BField
    long dataBytes();
}
