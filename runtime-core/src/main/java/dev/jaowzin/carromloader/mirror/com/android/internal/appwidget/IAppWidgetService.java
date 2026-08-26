package dev.jaowzin.carromloader.mirror.com.android.internal.appwidget;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("com.android.internal.appwidget.IAppWidgetService")
public interface IAppWidgetService {
    @BClassName("com.android.internal.appwidget.IAppWidgetService$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
