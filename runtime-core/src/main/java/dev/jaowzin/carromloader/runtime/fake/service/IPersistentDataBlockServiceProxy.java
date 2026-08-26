package dev.jaowzin.carromloader.runtime.fake.service;


import dev.jaowzin.carromloader.mirror.android.os.BRServiceManager;
import dev.jaowzin.carromloader.mirror.android.service.persistentdata.BRIPersistentDataBlockServiceStub;
import dev.jaowzin.carromloader.runtime.fake.hook.BinderInvocationStub;
import dev.jaowzin.carromloader.runtime.fake.service.base.ValueMethodProxy;


public class IPersistentDataBlockServiceProxy extends BinderInvocationStub {

    public static final String NAME = "persistent_data_block";

    public IPersistentDataBlockServiceProxy() {
        super(BRServiceManager.get().getService(NAME));
    }

    @Override
    protected Object getWho() {
        return BRIPersistentDataBlockServiceStub.get().asInterface(BRServiceManager.get().getService(NAME));
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
        addMethodHook(new ValueMethodProxy("write", -1));
        addMethodHook(new ValueMethodProxy("read", new byte[0]));
        addMethodHook(new ValueMethodProxy("wipe", null));
        addMethodHook(new ValueMethodProxy("getDataBlockSize", 0));
        addMethodHook(new ValueMethodProxy("getMaximumDataBlockSize", 0));
        addMethodHook(new ValueMethodProxy("setOemUnlockEnabled", 0));
        addMethodHook(new ValueMethodProxy("getOemUnlockEnabled", false));
    }
}
