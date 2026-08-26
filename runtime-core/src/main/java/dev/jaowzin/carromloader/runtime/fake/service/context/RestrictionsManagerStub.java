package dev.jaowzin.carromloader.runtime.fake.service.context;

import android.content.Context;

import java.lang.reflect.Method;

import dev.jaowzin.carromloader.mirror.android.content.BRIRestrictionsManagerStub;
import dev.jaowzin.carromloader.mirror.android.os.BRServiceManager;
import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;
import dev.jaowzin.carromloader.runtime.fake.hook.BinderInvocationStub;
import dev.jaowzin.carromloader.runtime.fake.hook.MethodHook;
import dev.jaowzin.carromloader.runtime.fake.hook.ProxyMethod;


public class RestrictionsManagerStub extends BinderInvocationStub {

    public RestrictionsManagerStub() {
        super(BRServiceManager.get().getService(Context.RESTRICTIONS_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIRestrictionsManagerStub.get().asInterface(BRServiceManager.get().getService(Context.RESTRICTIONS_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.RESTRICTIONS_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("getApplicationRestrictions")
    public static class GetApplicationRestrictions extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            args[0] = CarromRuntimeCore.getHostPkg();
            return method.invoke(who, args);
        }
    }
}
