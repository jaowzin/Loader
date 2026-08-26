package dev.jaowzin.carromloader.mirror.android.nfc;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.nfc.INfcAdapter")
public interface INfcAdapter {
    @BClassName("android.nfc.INfcAdapter$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
