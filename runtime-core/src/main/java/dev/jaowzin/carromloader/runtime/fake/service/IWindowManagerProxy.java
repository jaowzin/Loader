package dev.jaowzin.carromloader.runtime.fake.service;

import android.os.IInterface;

import java.lang.reflect.Method;

import dev.jaowzin.carromloader.mirror.android.os.BRServiceManager;
import dev.jaowzin.carromloader.mirror.android.view.BRIWindowManagerStub;
import dev.jaowzin.carromloader.mirror.android.view.BRWindowManagerGlobal;
import dev.jaowzin.carromloader.runtime.fake.hook.BinderInvocationStub;
import dev.jaowzin.carromloader.runtime.fake.hook.MethodHook;
import dev.jaowzin.carromloader.runtime.fake.hook.ProxyMethod;


public class IWindowManagerProxy extends BinderInvocationStub {
    public static final String TAG = "WindowManagerStub";

    public IWindowManagerProxy() {
        super(BRServiceManager.get().getService("window"));
    }

    @Override
    protected Object getWho() {
        return BRIWindowManagerStub.get().asInterface(BRServiceManager.get().getService("window"));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("window");
        BRWindowManagerGlobal.get()._set_sWindowManagerService(null);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("openSession")
    public static class OpenSession extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            IInterface session = (IInterface) method.invoke(who, args);
            IWindowSessionProxy IWindowSessionProxy = new IWindowSessionProxy(session);
            IWindowSessionProxy.injectHook();
            return IWindowSessionProxy.getProxyInvocation();
        }
    }
}
