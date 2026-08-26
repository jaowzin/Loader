package dev.jaowzin.carromloader.mirror.android.view;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BMethod;

@BClassName("android.view.DisplayAdjustments")
public interface DisplayAdjustments {
    @BMethod
    void setCompatibilityInfo();
}
