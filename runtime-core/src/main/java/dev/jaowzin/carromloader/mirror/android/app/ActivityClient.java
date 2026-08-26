package dev.jaowzin.carromloader.mirror.android.app;

import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;


@BClassName("android.app.ActivityClient")
public interface ActivityClient {
    @BField
    Object INTERFACE_SINGLETON();

    @BStaticMethod
    Object getInstance();

    @BStaticMethod
    Object getActivityClientController();

    @BStaticMethod
    Object setActivityClientController(Object iInterface);

    @BClassName("android.app.ActivityClient$ActivityClientControllerSingleton")
    interface ActivityClientControllerSingleton {
        @BField
        IInterface mKnownInstance();
    }
}
