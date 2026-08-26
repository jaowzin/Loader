package dev.jaowzin.carromloader.runtime.core;

import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;


public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    public static void create() {
        new CrashHandler();
    }

    public CrashHandler() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (CarromRuntimeCore.get().getExceptionHandler() != null) {
            CarromRuntimeCore.get().getExceptionHandler().uncaughtException(t, e);
        }
        mDefaultHandler.uncaughtException(t, e);
    }
}
