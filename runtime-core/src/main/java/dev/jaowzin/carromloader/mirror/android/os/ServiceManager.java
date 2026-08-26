package dev.jaowzin.carromloader.mirror.android.os;

import android.os.IBinder;
import android.os.IInterface;

import java.util.Map;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticField;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.os.ServiceManager")
public interface ServiceManager {
    @BStaticField
    Map<String, IBinder> sCache();

    @BStaticField
    IInterface sServiceManager();

    @BStaticMethod
    void addService(String String0, IBinder IBinder1);

    @BStaticMethod
    IBinder checkService();

    @BStaticMethod
    IInterface getIServiceManager();

    @BStaticMethod
    IBinder getService(String name);

    @BStaticMethod
    String[] listServices();
}
