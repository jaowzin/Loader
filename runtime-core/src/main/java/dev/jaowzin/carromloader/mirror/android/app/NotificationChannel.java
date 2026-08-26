package dev.jaowzin.carromloader.mirror.android.app;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.app.NotificationChannel")
public interface NotificationChannel {
    @BField
    String mId();
}
