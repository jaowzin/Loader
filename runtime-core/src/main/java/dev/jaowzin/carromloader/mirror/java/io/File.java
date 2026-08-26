package dev.jaowzin.carromloader.mirror.java.io;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticField;

@BClassName("java.io.File")
public interface File {
    @BStaticField
    Object fs();
}
