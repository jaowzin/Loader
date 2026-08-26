package dev.jaowzin.carromloader.mirror.android.os;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.os.Process")
public interface Process {
    @BStaticMethod
    void setArgV0(String String0);
}
