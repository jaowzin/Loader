package dev.jaowzin.carromloader.mirror.android.app;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;

@BClassName("android.app.IWallpaperManager")
public interface IWallpaperManager {
    @BClassName("android.app.IWallpaperManager$Stub")
    interface Stub {
        @BStaticMethod
        IInterface asInterface(IBinder IBinder0);
    }
}
