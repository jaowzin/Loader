package dev.jaowzin.carromloader.mirror.android.content;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.content.ContentProviderNative")
public interface ContentProviderNative {
    @BStaticMethod
    IInterface asInterface(IBinder IBinder0);
}
