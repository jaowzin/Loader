package dev.jaowzin.carromloader.mirror.android.content.pm;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;
import dev.jaowzin.carromloader.bridge.annotation.BMethod;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.content.pm.PackageManager")
public interface PackageManager {
    @BStaticMethod
    void disableApplicationInfoCache();
}
