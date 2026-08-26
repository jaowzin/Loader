package dev.jaowzin.carromloader.runtime.fake.service;

import static dev.jaowzin.carromloader.runtime.app.BActivityThread.getUid;

import java.lang.reflect.Method;

import dev.jaowzin.carromloader.mirror.android.os.BRINetworkManagementServiceStub;
import dev.jaowzin.carromloader.mirror.android.os.BRServiceManager;
import dev.jaowzin.carromloader.runtime.fake.hook.BinderInvocationStub;
import dev.jaowzin.carromloader.runtime.fake.hook.MethodHook;
import dev.jaowzin.carromloader.runtime.fake.hook.ProxyMethod;
import dev.jaowzin.carromloader.runtime.fake.service.base.UidMethodProxy;
import dev.jaowzin.carromloader.runtime.utils.MethodParameterUtils;
import dev.jaowzin.carromloader.runtime.utils.Slog;


public class INetworkManagementServiceProxy extends BinderInvocationStub {
    public static final String NAME = "network_management";

    public INetworkManagementServiceProxy() {
        super(BRServiceManager.get().getService(NAME));
    }

    @Override
    protected Object getWho() {
        return BRINetworkManagementServiceStub.get().asInterface(BRServiceManager.get().getService(NAME));
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
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new UidMethodProxy("setUidCleartextNetworkPolicy", 0));
        addMethodHook(new UidMethodProxy("setUidMeteredNetworkBlacklist", 0));
        addMethodHook(new UidMethodProxy("setUidMeteredNetworkWhitelist", 0));
    }

    @ProxyMethod("getNetworkStatsUidDetail")
    public static class getNetworkStatsUidDetail extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstUid(args);
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }
}
