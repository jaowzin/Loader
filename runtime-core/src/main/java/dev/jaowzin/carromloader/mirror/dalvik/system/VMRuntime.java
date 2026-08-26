package dev.jaowzin.carromloader.mirror.dalvik.system;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BMethod;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("dalvik.system.VMRuntime")
public interface VMRuntime {
    @BStaticMethod
    String getCurrentInstructionSet();

    @BStaticMethod
    Object getRuntime();

    @BStaticMethod
    Boolean is64BitAbi(String String0);

    @BMethod
    Boolean is64Bit();

    @BMethod
    Boolean isJavaDebuggable();

    @BMethod
    void setTargetSdkVersion(int int0);
}
