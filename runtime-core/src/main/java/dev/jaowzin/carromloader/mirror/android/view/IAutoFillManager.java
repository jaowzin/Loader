package dev.jaowzin.carromloader.mirror.android.view;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.view.autofill.IAutoFillManager")
public interface IAutoFillManager {
    @BClassName("android.view.autofill.IAutoFillManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
