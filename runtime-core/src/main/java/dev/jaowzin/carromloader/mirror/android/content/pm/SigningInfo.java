package dev.jaowzin.carromloader.mirror.android.content.pm;

import android.content.pm.PackageParser.SigningDetails;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BConstructor;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.content.pm.SigningInfo")
public interface SigningInfo {
    @BConstructor
    android.content.pm.SigningInfo _new(SigningDetails SigningDetails0);

    @BField
    SigningDetails mSigningDetails();
}
