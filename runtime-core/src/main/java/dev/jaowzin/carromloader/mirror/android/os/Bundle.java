package dev.jaowzin.carromloader.mirror.android.os;

import android.os.IBinder;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BMethod;

@BClassName("android.os.Bundle")
public interface Bundle {
    @BMethod
    IBinder getIBinder(String String0);

    @BMethod
    void putIBinder(String String0, IBinder IBinder1);
}
