package dev.jaowzin.carromloader.mirror.android.content;

import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticField;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.content.ClipboardManager")
public interface ClipboardManager {
    @BStaticField
    IInterface sService();

    @BStaticMethod
    IInterface getService();
}
