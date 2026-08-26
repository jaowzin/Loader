package dev.jaowzin.carromloader.mirror.android.app.servertransaction;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BMethod;

@BClassName("android.app.servertransaction.ActivityLifecycleItem")
public interface ActivityLifecycleItem {
    @BMethod
    Integer getTargetState();
}
