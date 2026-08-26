package dev.jaowzin.carromloader.runtime.utils.compat;

import android.os.IBinder;
import android.os.IInterface;

import dev.jaowzin.carromloader.mirror.android.app.BRApplicationThreadNative;
import dev.jaowzin.carromloader.mirror.android.app.BRIApplicationThreadOreoStub;

public class ApplicationThreadCompat {

    public static IInterface asInterface(IBinder binder) {
        if (BuildCompat.isOreo()) {
            return BRIApplicationThreadOreoStub.get().asInterface(binder);
        }
        return BRApplicationThreadNative.get().asInterface(binder);
    }
}
