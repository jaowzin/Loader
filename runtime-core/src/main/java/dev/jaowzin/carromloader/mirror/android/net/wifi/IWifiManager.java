package dev.jaowzin.carromloader.mirror.android.net.wifi;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.net.wifi.IWifiManager")
public interface IWifiManager {
    @BClassName("android.net.wifi.IWifiManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
