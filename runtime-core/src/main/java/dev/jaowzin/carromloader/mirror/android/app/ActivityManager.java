package dev.jaowzin.carromloader.mirror.android.app;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticField;

@BClassName("android.app.ActivityManager")
public interface ActivityManager {
    @BStaticField
    int START_INTENT_NOT_RESOLVED();

    @BStaticField
    int START_NOT_CURRENT_USER_ACTIVITY();

    @BStaticField
    int START_SUCCESS();

    @BStaticField
    int START_TASK_TO_FRONT();
}
