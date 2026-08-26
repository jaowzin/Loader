package dev.jaowzin.carromloader.mirror.android.app.servertransaction;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.app.servertransaction.TopResumedActivityChangeItem")
public interface TopResumedActivityChangeItem {
    @BField
    Boolean mOnTop();
}
