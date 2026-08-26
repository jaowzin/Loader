package dev.jaowzin.carromloader.mirror.android.app;


import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.os.IBinder;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BMethod;
import dev.jaowzin.carromloader.bridge.annotation.BParamClass;

@BClassName("android.app.Service")
public interface Service {
    @BMethod
    void attach(Context context,
                @BParamClass(ActivityThread.class) Object thread, String className, IBinder token,
                Application application, Object activityManager);
}
