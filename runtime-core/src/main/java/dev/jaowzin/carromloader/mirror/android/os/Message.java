package dev.jaowzin.carromloader.mirror.android.os;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.os.Message")
public interface Message {
    @BStaticMethod
    void updateCheckRecycle(int int0);
}
