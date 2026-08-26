package dev.jaowzin.carromloader.runtime.fake.service;

import android.content.Context;
import android.os.IBinder;

import java.lang.reflect.Method;

import dev.jaowzin.carromloader.mirror.android.os.BRIVibratorManagerServiceStub;
import dev.jaowzin.carromloader.mirror.android.os.BRServiceManager;
import dev.jaowzin.carromloader.mirror.com.android.internal.os.BRIVibratorServiceStub;
import dev.jaowzin.carromloader.runtime.fake.hook.BinderInvocationStub;
import dev.jaowzin.carromloader.runtime.utils.MethodParameterUtils;
import dev.jaowzin.carromloader.runtime.utils.compat.BuildCompat;


public class IVibratorServiceProxy extends BinderInvocationStub {
    private static String NAME;
    static {
        if (BuildCompat.isS()) {
            NAME = "vibrator_manager";
        } else {
            NAME = Context.VIBRATOR_SERVICE;
        }
    }

    public IVibratorServiceProxy() {
        super(BRServiceManager.get().getService(NAME));
    }

    @Override
    protected Object getWho() {
        IBinder service = BRServiceManager.get().getService(NAME);
        if (BuildCompat.isS()) {
            return BRIVibratorManagerServiceStub.get().asInterface(service);
        }
        return BRIVibratorServiceStub.get().asInterface(service);
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(NAME);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodParameterUtils.replaceFirstUid(args);
        MethodParameterUtils.replaceFirstAppPkg(args);
        return super.invoke(proxy, method, args);
    }
}
