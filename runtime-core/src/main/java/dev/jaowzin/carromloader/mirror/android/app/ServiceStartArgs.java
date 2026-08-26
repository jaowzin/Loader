package dev.jaowzin.carromloader.mirror.android.app;

import android.content.Intent;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BConstructor;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.app.ServiceStartArgs")
public interface ServiceStartArgs {
    @BConstructor
    ServiceStartArgs _new(boolean boolean0, int int1, int int2, Intent Intent3);

    @BField
    Intent args();

    @BField
    int flags();

    @BField
    int startId();

    @BField
    boolean taskRemoved();
}
