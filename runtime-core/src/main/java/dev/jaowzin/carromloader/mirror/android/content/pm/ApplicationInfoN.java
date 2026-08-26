package dev.jaowzin.carromloader.mirror.android.content.pm;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.content.pm.ApplicationInfo")
public interface ApplicationInfoN {
    @BField
    String credentialEncryptedDataDir();

    @BField
    String credentialProtectedDataDir();

    @BField
    String deviceEncryptedDataDir();

    @BField
    String deviceProtectedDataDir();
}
