package dev.jaowzin.carromloader.mirror.android.content.pm;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.content.pm.ApplicationInfo")
public interface ApplicationInfoL {
    @BField
    String primaryCpuAbi();

    @BField
    Integer privateFlags();

    @BField
    String scanPublicSourceDir();

    @BField
    String scanSourceDir();

    @BField
    String secondaryCpuAbi();

    @BField
    String secondaryNativeLibraryDir();

    @BField
    String[] splitPublicSourceDirs();

    @BField
    String[] splitSourceDirs();
}
