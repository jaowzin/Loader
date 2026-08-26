package dev.jaowzin.carromloader.mirror.android.widget;

import android.content.pm.ApplicationInfo;

import java.util.ArrayList;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.widget.RemoteViews")
public interface RemoteViews {
    @BField
    ArrayList<Object> mActions();

    @BField
    ApplicationInfo mApplication();

    @BField
    String mPackage();
}
