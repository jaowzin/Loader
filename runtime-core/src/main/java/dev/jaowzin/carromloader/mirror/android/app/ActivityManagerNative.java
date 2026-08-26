package dev.jaowzin.carromloader.mirror.android.app;

import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticField;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.app.ActivityManagerNative")
public interface ActivityManagerNative {
    @BStaticField
    Object gDefault();

    @BStaticMethod
    IInterface getDefault();
}
