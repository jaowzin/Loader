package dev.jaowzin.carromloader.mirror.android.renderscript;

import java.io.File;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.renderscript.RenderScriptCacheDir")
public interface RenderScriptCacheDir {
    @BStaticMethod
    void setupDiskCache(File File0);
}
