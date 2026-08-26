package dev.jaowzin.carromloader.mirror.java.lang;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("java.lang.ThreadGroup")
public interface ThreadGroupN {
    @BField
    java.lang.ThreadGroup[] groups();

    @BField
    Integer ngroups();

    @BField
    java.lang.ThreadGroup parent();
}
