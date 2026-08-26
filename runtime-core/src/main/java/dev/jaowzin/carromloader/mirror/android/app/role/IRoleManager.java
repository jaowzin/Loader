package dev.jaowzin.carromloader.mirror.android.app.role;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.app.role.IRoleManager")
public interface IRoleManager {
    @BClassName("android.app.role.IRoleManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
