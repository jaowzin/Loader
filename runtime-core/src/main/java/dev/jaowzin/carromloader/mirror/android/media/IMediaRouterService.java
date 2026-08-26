package dev.jaowzin.carromloader.mirror.android.media;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.media.IMediaRouterService")
public interface IMediaRouterService {
    @BClassName("android.media.IMediaRouterService$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
