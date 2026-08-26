package dev.jaowzin.carromloader.runtime.fake.hook;

import java.lang.reflect.Method;

import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;


public abstract class MethodHook {
    protected String getMethodName() {
        return null;
    }

    protected Object afterHook(Object result) throws Throwable {
        return result;
    }

    protected Object beforeHook(Object who, Method method, Object[] args) throws Throwable {
        return null;
    }

    protected abstract Object hook(Object who, Method method, Object[] args) throws Throwable;

    protected boolean isEnable() {
        return CarromRuntimeCore.get().isBlackProcess();
    }
}
