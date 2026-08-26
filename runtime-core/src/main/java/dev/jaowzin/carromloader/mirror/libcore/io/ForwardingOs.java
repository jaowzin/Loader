package dev.jaowzin.carromloader.mirror.libcore.io;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("libcore.io.ForwardingOs")
public interface ForwardingOs {
    @BField
    Object os();
}
