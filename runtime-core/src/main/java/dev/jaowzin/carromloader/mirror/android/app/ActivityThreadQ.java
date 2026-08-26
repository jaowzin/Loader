package dev.jaowzin.carromloader.mirror.android.app;

import android.os.IBinder;

import java.util.List;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BMethod;

@BClassName("android.app.ActivityThread")
public interface ActivityThreadQ {
    @BMethod
    void handleNewIntent(IBinder IBinder0, List List1);
}
