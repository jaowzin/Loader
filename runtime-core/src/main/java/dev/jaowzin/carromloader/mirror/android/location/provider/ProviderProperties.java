package dev.jaowzin.carromloader.mirror.android.location.provider;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.location.provider.ProviderProperties")
public interface ProviderProperties {
    @BField
    boolean mHasNetworkRequirement();

    @BField
    boolean mHasSatelliteRequirement();

    @BField
    boolean mHasCellRequirement();

    @BField
    boolean mHasMonetaryCost();

    @BField
    boolean mHasAltitudeSupport();

    @BField
    boolean mHasSpeedSupport();

    @BField
    boolean mHasBearingSupport();
}
