package dev.jaowzin.carromloader.mirror.android.media;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.media.IAudioService")
public interface IAudioService {
    @BClassName("android.media.IAudioService$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
