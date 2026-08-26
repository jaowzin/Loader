package dev.jaowzin.carromloader.runtime.fake.service.base;

import java.lang.reflect.Method;

import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;
import dev.jaowzin.carromloader.runtime.app.BActivityThread;
import dev.jaowzin.carromloader.runtime.fake.hook.MethodHook;


public class UidMethodProxy extends MethodHook {
    private final int index;
    private final String name;

    public UidMethodProxy(String name, int index) {
        this.index = index;
        this.name = name;
    }

    @Override
    protected String getMethodName() {
        return name;
    }

    @Override
    protected Object hook(Object who, Method method, Object[] args) throws Throwable {
        int uid = (int) args[index];
        if (uid == BActivityThread.getBUid()) {
            args[index] = CarromRuntimeCore.getHostUid();
        }
        return method.invoke(who, args);
    }
}
