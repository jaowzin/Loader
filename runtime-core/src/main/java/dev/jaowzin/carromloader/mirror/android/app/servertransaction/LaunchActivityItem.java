package dev.jaowzin.carromloader.mirror.android.app.servertransaction;

import android.content.Intent;
import android.content.pm.ActivityInfo;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.app.servertransaction.LaunchActivityItem")
public interface LaunchActivityItem {
    @BField
    ActivityInfo mInfo();

    @BField
    Intent mIntent();
}
