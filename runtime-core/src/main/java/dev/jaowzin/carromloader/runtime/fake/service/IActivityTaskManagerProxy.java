package dev.jaowzin.carromloader.runtime.fake.service;

import android.app.ActivityManager;

import java.lang.reflect.Method;

import dev.jaowzin.carromloader.mirror.android.app.BRActivityTaskManager;
import dev.jaowzin.carromloader.mirror.android.app.BRIActivityTaskManagerStub;
import dev.jaowzin.carromloader.mirror.android.os.BRServiceManager;
import dev.jaowzin.carromloader.mirror.android.util.BRSingleton;
import dev.jaowzin.carromloader.runtime.fake.hook.BinderInvocationStub;
import dev.jaowzin.carromloader.runtime.fake.hook.MethodHook;
import dev.jaowzin.carromloader.runtime.fake.hook.ProxyMethod;
import dev.jaowzin.carromloader.runtime.fake.hook.ScanClass;
import dev.jaowzin.carromloader.runtime.utils.compat.TaskDescriptionCompat;


@ScanClass(ActivityManagerCommonProxy.class)
public class IActivityTaskManagerProxy extends BinderInvocationStub {
    public static final String TAG = "ActivityTaskManager";

    public IActivityTaskManagerProxy() {
        super(BRServiceManager.get().getService("activity_task"));
    }

    @Override
    protected Object getWho() {
        return BRIActivityTaskManagerStub.get().asInterface(BRServiceManager.get().getService("activity_task"));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("activity_task");
        BRActivityTaskManager.get().getService();
        Object o = BRActivityTaskManager.get().IActivityTaskManagerSingleton();
        BRSingleton.get(o)._set_mInstance(BRIActivityTaskManagerStub.get().asInterface(this));
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    
    @ProxyMethod("setTaskDescription")
    public static class SetTaskDescription extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            ActivityManager.TaskDescription td = (ActivityManager.TaskDescription) args[1];
            args[1] = TaskDescriptionCompat.fix(td);
            return method.invoke(who, args);
        }
    }
}
