package dev.jaowzin.carromloader.runtime.fake.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import java.lang.reflect.Method;

import dev.jaowzin.carromloader.mirror.android.app.usage.BRIStorageStatsManagerStub;
import dev.jaowzin.carromloader.mirror.android.os.BRServiceManager;
import dev.jaowzin.carromloader.runtime.fake.hook.BinderInvocationStub;
import dev.jaowzin.carromloader.runtime.utils.MethodParameterUtils;


@TargetApi(Build.VERSION_CODES.O)
public class IStorageStatsManagerProxy extends BinderInvocationStub {

    public IStorageStatsManagerProxy() {
        super(BRServiceManager.get().getService(Context.STORAGE_STATS_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIStorageStatsManagerStub.get().asInterface(BRServiceManager.get().getService(Context.STORAGE_STATS_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.STORAGE_STATS_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodParameterUtils.replaceFirstAppPkg(args);
        return super.invoke(proxy, method, args);
    }
}
