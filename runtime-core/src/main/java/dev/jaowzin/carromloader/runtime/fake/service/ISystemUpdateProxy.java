package dev.jaowzin.carromloader.runtime.fake.service;


import dev.jaowzin.carromloader.mirror.android.os.BRServiceManager;
import dev.jaowzin.carromloader.mirror.android.view.BRIAutoFillManagerStub;
import dev.jaowzin.carromloader.runtime.fake.hook.BinderInvocationStub;


public class ISystemUpdateProxy extends BinderInvocationStub {
    public ISystemUpdateProxy() {
        super(BRServiceManager.get().getService("system_update"));
    }

    @Override
    protected Object getWho() {
        return BRIAutoFillManagerStub.get().asInterface(BRServiceManager.get().getService("system_update"));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("system_update");
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
