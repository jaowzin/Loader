package dev.jaowzin.carromloader.mirror.android.service.notification;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.service.notification.StatusBarNotification")
public interface StatusBarNotification {
    @BField
    Integer id();

    @BField
    String opPkg();

    @BField
    String pkg();

    @BField
    String tag();
}
