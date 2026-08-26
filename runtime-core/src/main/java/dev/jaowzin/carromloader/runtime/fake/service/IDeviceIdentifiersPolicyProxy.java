package dev.jaowzin.carromloader.runtime.fake.service;


import java.lang.reflect.Method;

import dev.jaowzin.carromloader.mirror.android.os.BRIDeviceIdentifiersPolicyServiceStub;
import dev.jaowzin.carromloader.mirror.android.os.BRServiceManager;
import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;
import dev.jaowzin.carromloader.runtime.fake.hook.BinderInvocationStub;
import dev.jaowzin.carromloader.runtime.fake.hook.MethodHook;
import dev.jaowzin.carromloader.runtime.fake.hook.ProxyMethod;
import dev.jaowzin.carromloader.runtime.utils.Md5Utils;


public class IDeviceIdentifiersPolicyProxy extends BinderInvocationStub {

    public IDeviceIdentifiersPolicyProxy() {
        super(BRServiceManager.get().getService("device_identifiers"));
    }

    @Override
    protected Object getWho() {
        return BRIDeviceIdentifiersPolicyServiceStub.get().asInterface(BRServiceManager.get().getService("device_identifiers"));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("device_identifiers");
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("getSerialForPackage")
    public static class x extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {


            return Md5Utils.md5(CarromRuntimeCore.getHostPkg());
        }
    }
}
