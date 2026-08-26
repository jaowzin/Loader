package dev.jaowzin.carromloader.runtime.fake.service;

import android.os.IInterface;

import java.lang.reflect.Method;

import dev.jaowzin.carromloader.mirror.android.hardware.display.BRDisplayManagerGlobal;
import dev.jaowzin.carromloader.runtime.fake.hook.ClassInvocationStub;
import dev.jaowzin.carromloader.runtime.fake.hook.MethodHook;
import dev.jaowzin.carromloader.runtime.fake.hook.ProxyMethod;
import dev.jaowzin.carromloader.runtime.fake.service.base.PkgMethodProxy;
import dev.jaowzin.carromloader.runtime.utils.MethodParameterUtils;


public class IDisplayManagerProxy extends ClassInvocationStub {

    public IDisplayManagerProxy() {
    }

    @Override
    protected Object getWho() {
        return BRDisplayManagerGlobal.get(BRDisplayManagerGlobal.get().getInstance()).mDm();
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        Object dmg = BRDisplayManagerGlobal.get().getInstance();
        BRDisplayManagerGlobal.get(dmg)._set_mDm(getProxyInvocation());
    }

    @Override
    public boolean isBadEnv() {
        Object dmg = BRDisplayManagerGlobal.get().getInstance();
        IInterface mDm = BRDisplayManagerGlobal.get(dmg).mDm();
        return mDm != getProxyInvocation();
    }


    @ProxyMethod("createVirtualDisplay")
    public static class CreateVirtualDisplay extends MethodHook {

        @Override
        protected String getMethodName() {
            return "createVirtualDisplay";
        }

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }
}
