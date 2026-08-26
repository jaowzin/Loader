package dev.jaowzin.carromloader.mirror.com.android.internal.telephony;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("com.android.internal.telephony.ITelephony")
public interface ITelephony {
    @BClassName("com.android.internal.telephony.ITelephony$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
