package dev.jaowzin.carromloader.runtime.fake.service;

import android.content.Context;
import android.os.IBinder;

import dev.jaowzin.carromloader.mirror.android.os.BRServiceManager;
import dev.jaowzin.carromloader.mirror.android.view.BRIGraphicsStatsStub;
import dev.jaowzin.carromloader.runtime.fake.hook.BinderInvocationStub;
import dev.jaowzin.carromloader.runtime.fake.service.base.PkgMethodProxy;


public class IFingerprintManagerProxy extends BinderInvocationStub {
    public IFingerprintManagerProxy() {
        super(BRServiceManager.get().getService(Context.FINGERPRINT_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIGraphicsStatsStub.get().asInterface(BRServiceManager.get().getService(Context.FINGERPRINT_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.FINGERPRINT_SERVICE);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new PkgMethodProxy("isHardwareDetected"));
        addMethodHook(new PkgMethodProxy("hasEnrolledFingerprints"));
        addMethodHook(new PkgMethodProxy("authenticate"));
        addMethodHook(new PkgMethodProxy("cancelAuthentication"));
        addMethodHook(new PkgMethodProxy("getEnrolledFingerprints"));
        addMethodHook(new PkgMethodProxy("getAuthenticatorId"));
    }
}
