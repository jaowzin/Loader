package dev.jaowzin.carromloader.mirror.android.media;

import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticField;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.media.AudioManager")
public interface AudioManager {
    @BStaticField
    IInterface sService();

    @BStaticMethod
    void getService();
}
