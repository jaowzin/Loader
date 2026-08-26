package dev.jaowzin.carromloader.mirror.android.app;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BMethod;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.app.IApplicationThread")
public interface IApplicationThreadOreo {
    @BMethod
    void scheduleServiceArgs();

    @BClassName("android.app.IApplicationThread$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
