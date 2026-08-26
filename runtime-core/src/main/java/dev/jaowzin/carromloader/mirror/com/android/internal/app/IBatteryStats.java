package dev.jaowzin.carromloader.mirror.com.android.internal.app;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("com.android.internal.app.IBatteryStats")
public interface IBatteryStats {
    @BClassName("com.android.internal.app.IBatteryStats$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
