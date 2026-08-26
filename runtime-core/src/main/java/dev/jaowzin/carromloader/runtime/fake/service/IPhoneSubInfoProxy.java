package dev.jaowzin.carromloader.runtime.fake.service;

import java.lang.reflect.Method;

import dev.jaowzin.carromloader.mirror.android.telephony.BRTelephonyManager;
import dev.jaowzin.carromloader.runtime.fake.hook.ClassInvocationStub;
import dev.jaowzin.carromloader.runtime.fake.hook.MethodHook;
import dev.jaowzin.carromloader.runtime.fake.hook.ProxyMethod;
import dev.jaowzin.carromloader.runtime.utils.MethodParameterUtils;


public class IPhoneSubInfoProxy extends ClassInvocationStub {
    public static final String TAG = "IPhoneSubInfoProxy";

    public IPhoneSubInfoProxy() {
        if (BRTelephonyManager.get()._check_sServiceHandleCacheEnabled() != null) {
            BRTelephonyManager.get()._set_sServiceHandleCacheEnabled(true);
        }
        if (BRTelephonyManager.get()._check_getSubscriberInfoService() != null) {
            BRTelephonyManager.get().getSubscriberInfoService();
        }
    }

    @Override
    protected Object getWho() {
        return BRTelephonyManager.get().sIPhoneSubInfo();
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        BRTelephonyManager.get()._set_sIPhoneSubInfo(proxyInvocation);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodParameterUtils.replaceFirstAppPkg(args);
        return super.invoke(proxy, method, args);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }


    @ProxyMethod("getLine1NumberForSubscriber")
    public static class getLine1NumberForSubscriber extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return null;
        }
    }
}
