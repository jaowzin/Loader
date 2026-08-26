package dev.jaowzin.carromloader.mirror.android.content.pm;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BConstructor;
import dev.jaowzin.carromloader.bridge.annotation.BStaticField;

@BClassName("android.content.pm.UserInfo")
public interface UserInfo {
    @BConstructor
    Object _new(int id, String name, int flags);

    @BStaticField
    int FLAG_PRIMARY();
}
