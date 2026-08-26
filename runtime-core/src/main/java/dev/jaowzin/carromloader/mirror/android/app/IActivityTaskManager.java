package dev.jaowzin.carromloader.mirror.android.app;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.app.IActivityTaskManager")
public interface IActivityTaskManager {
    @BClassName("android.app.IActivityTaskManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
