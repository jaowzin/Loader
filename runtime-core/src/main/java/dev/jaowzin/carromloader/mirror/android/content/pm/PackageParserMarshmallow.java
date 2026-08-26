package dev.jaowzin.carromloader.mirror.android.content.pm;

import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;

import java.io.File;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BConstructor;
import dev.jaowzin.carromloader.bridge.annotation.BMethod;

@BClassName("android.content.pm.PackageParser")
public interface PackageParserMarshmallow {
    @BConstructor
    PackageParser _new();
















    @BMethod
    void collectCertificates(Package p, int flags);

    @BMethod
    Package parsePackage(File File0, int int1);
}
