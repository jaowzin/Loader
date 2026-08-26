package dev.jaowzin.carromloader.runtime.fake.delegate;

import android.net.Uri;
import android.os.Build;
import android.os.IInterface;
import android.util.ArrayMap;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import dev.jaowzin.carromloader.mirror.android.app.BRActivityThread;
import dev.jaowzin.carromloader.mirror.android.app.BRActivityThreadProviderClientRecordP;
import dev.jaowzin.carromloader.mirror.android.app.BRIActivityManagerContentProviderHolder;
import dev.jaowzin.carromloader.mirror.android.content.BRContentProviderHolderOreo;
import dev.jaowzin.carromloader.mirror.android.providers.BRSettingsContentProviderHolder;
import dev.jaowzin.carromloader.mirror.android.providers.BRSettingsGlobal;
import dev.jaowzin.carromloader.mirror.android.providers.BRSettingsNameValueCache;
import dev.jaowzin.carromloader.mirror.android.providers.BRSettingsNameValueCacheOreo;
import dev.jaowzin.carromloader.mirror.android.providers.BRSettingsSecure;
import dev.jaowzin.carromloader.mirror.android.providers.BRSettingsSystem;
import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;
import dev.jaowzin.carromloader.runtime.fake.service.context.providers.ContentProviderStub;
import dev.jaowzin.carromloader.runtime.fake.service.context.providers.SystemProviderStub;
import dev.jaowzin.carromloader.runtime.utils.compat.BuildCompat;


public class ContentProviderDelegate {
    public static final String TAG = "ContentProviderDelegate";
    private static Set<String> sInjected = new HashSet<>();

    public static void update(Object holder, String auth) {
        IInterface iInterface;
        if (BuildCompat.isOreo()) {
            iInterface = BRContentProviderHolderOreo.get(holder).provider();
        } else {
            iInterface = BRIActivityManagerContentProviderHolder.get(holder).provider();
        }

        if (iInterface instanceof Proxy)
            return;
        IInterface bContentProvider;
        switch (auth) {
            case "media":
            case "telephony":
            case "settings":
                bContentProvider = new SystemProviderStub().wrapper(iInterface, CarromRuntimeCore.getHostPkg());
                break;
            default:
                bContentProvider = new ContentProviderStub().wrapper(iInterface, CarromRuntimeCore.getHostPkg());
                break;
        }
        if (BuildCompat.isOreo()) {
            BRContentProviderHolderOreo.get(holder)._set_provider(bContentProvider);
        } else {
            BRIActivityManagerContentProviderHolder.get(holder)._set_provider(bContentProvider);
        }
    }

    public static void init() {
        clearSettingProvider();

        CarromRuntimeCore.getContext().getContentResolver().call(Uri.parse("content://settings"), "", null, null);
        Object activityThread = CarromRuntimeCore.mainThread();
        ArrayMap<Object, Object> map = (ArrayMap<Object, Object>) BRActivityThread.get(activityThread).mProviderMap();

        for (Object value : map.values()) {
            String[] mNames = BRActivityThreadProviderClientRecordP.get(value).mNames();
            if (mNames == null || mNames.length <= 0) {
                continue;
            }
            String providerName = mNames[0];
            if (!sInjected.contains(providerName)) {
                sInjected.add(providerName);
                final IInterface iInterface = BRActivityThreadProviderClientRecordP.get(value).mProvider();
                BRActivityThreadProviderClientRecordP.get(value)._set_mProvider(new ContentProviderStub().wrapper(iInterface, CarromRuntimeCore.getHostPkg()));
                BRActivityThreadProviderClientRecordP.get(value)._set_mNames(new String[]{providerName});
            }
        }
    }

    public static void clearSettingProvider() {
        Object cache;
        cache = BRSettingsSystem.get().sNameValueCache();
        if (cache != null) {
            clearContentProvider(cache);
        }
        cache = BRSettingsSecure.get().sNameValueCache();
        if (cache != null) {
            clearContentProvider(cache);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && BRSettingsGlobal.getRealClass() != null) {
            cache = BRSettingsGlobal.get().sNameValueCache();
            if (cache != null) {
                clearContentProvider(cache);
            }
        }
    }

    private static void clearContentProvider(Object cache) {
        if (BuildCompat.isOreo()) {
            Object holder = BRSettingsNameValueCacheOreo.get(cache).mProviderHolder();
            if (holder != null) {
                BRSettingsContentProviderHolder.get(holder)._set_mContentProvider(null);
            }
        } else {
            BRSettingsNameValueCache.get(cache)._set_mContentProvider(null);
        }
    }
}
