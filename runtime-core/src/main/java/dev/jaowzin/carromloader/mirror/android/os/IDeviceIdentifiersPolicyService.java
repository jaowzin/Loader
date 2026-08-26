package dev.jaowzin.carromloader.mirror.android.os;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.os.IDeviceIdentifiersPolicyService")
public interface IDeviceIdentifiersPolicyService {
    @BClassName("android.os.IDeviceIdentifiersPolicyService$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
