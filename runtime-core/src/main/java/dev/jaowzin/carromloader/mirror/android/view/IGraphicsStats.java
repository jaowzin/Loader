package dev.jaowzin.carromloader.mirror.android.view;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.view.IGraphicsStats")
public interface IGraphicsStats {
    @BClassName("android.view.IGraphicsStats$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
