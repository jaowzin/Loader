package dev.jaowzin.carromloader.runtime.fake.service;

import android.content.Context;

import java.lang.reflect.Method;

import dev.jaowzin.carromloader.mirror.android.media.session.BRISessionManagerStub;
import dev.jaowzin.carromloader.mirror.android.os.BRServiceManager;
import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;
import dev.jaowzin.carromloader.runtime.fake.hook.BinderInvocationStub;
import dev.jaowzin.carromloader.runtime.fake.hook.MethodHook;
import dev.jaowzin.carromloader.runtime.fake.hook.ProxyMethod;


public class IMediaSessionManagerProxy extends BinderInvocationStub {

    public IMediaSessionManagerProxy() {
        super(BRServiceManager.get().getService(Context.MEDIA_SESSION_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRISessionManagerStub.get().asInterface(BRServiceManager.get().getService(Context.MEDIA_SESSION_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.MEDIA_SESSION_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("createSession")
    public static class CreateSession extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0 && args[0] instanceof String) {
                args[0] = CarromRuntimeCore.getHostPkg();
            }
            return method.invoke(who, args);
        }
    }
}
