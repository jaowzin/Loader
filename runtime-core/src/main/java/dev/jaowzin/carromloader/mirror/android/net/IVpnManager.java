package dev.jaowzin.carromloader.mirror.android.net;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;


@BClassName("android.net.IVpnManager")
public interface IVpnManager {

    @BClassName("android.net.IVpnManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
