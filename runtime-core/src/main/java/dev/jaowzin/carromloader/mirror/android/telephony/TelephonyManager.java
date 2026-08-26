package dev.jaowzin.carromloader.mirror.android.telephony;

import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticField;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;


@BClassName("android.telephony.TelephonyManager")
public interface TelephonyManager {

    @BStaticMethod
    Object getSubscriberInfoService();

    @BStaticField
    boolean sServiceHandleCacheEnabled();

    @BStaticField
    IInterface sIPhoneSubInfo();
}
