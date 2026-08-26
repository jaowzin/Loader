package dev.jaowzin.carromloader.runtime.utils.compat;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.*;

import dev.jaowzin.carromloader.mirror.android.app.BRContextImpl;
import dev.jaowzin.carromloader.mirror.android.app.BRContextImplKitkat;
import dev.jaowzin.carromloader.mirror.android.content.AttributionSourceStateContext;
import dev.jaowzin.carromloader.mirror.android.content.BRAttributionSource;
import dev.jaowzin.carromloader.mirror.android.content.BRAttributionSourceState;
import dev.jaowzin.carromloader.mirror.android.content.BRContentResolver;
import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;
import dev.jaowzin.carromloader.runtime.app.BActivityThread;
import dev.jaowzin.carromloader.runtime.utils.Slog;


public class ContextCompat {
    public static final String TAG = "ContextCompat";

    public static void fixAttributionSourceState(Object obj, int uid) {
        Object mAttributionSourceState;
        if (obj != null && BRAttributionSource.get(obj)._check_mAttributionSourceState() != null) {
            mAttributionSourceState = BRAttributionSource.get(obj).mAttributionSourceState();

            AttributionSourceStateContext attributionSourceStateContext = BRAttributionSourceState.get(mAttributionSourceState);
            attributionSourceStateContext._set_packageName(CarromRuntimeCore.getHostPkg());
            attributionSourceStateContext._set_uid(uid);
            fixAttributionSourceState(BRAttributionSource.get(obj).getNext(), uid);
        }
    }

    public static void fix(Context context) {
        try {
            
            if (context == null) {
                Slog.w(TAG, "Context is null, skipping ContextCompat.fix");
                return;
            }
            
            int deep = 0;
            while (context instanceof ContextWrapper) {
                context = ((ContextWrapper) context).getBaseContext();
                deep++;
                if (deep >= 10) {
                    return;
                }
            }
            
            
            if (context == null) {
                Slog.w(TAG, "Base context is null after unwrapping, skipping ContextCompat.fix");
                return;
            }
            
            BRContextImpl.get(context)._set_mPackageManager(null);
            try {
                context.getPackageManager();
            } catch (Throwable e) {
                e.printStackTrace();
            }

            BRContextImpl.get(context)._set_mBasePackageName(CarromRuntimeCore.getHostPkg());
            BRContextImplKitkat.get(context)._set_mOpPackageName(CarromRuntimeCore.getHostPkg());
            
            try {
                BRContentResolver.get(context.getContentResolver())._set_mPackageName(CarromRuntimeCore.getHostPkg());
            } catch (Exception e) {
                Slog.w(TAG, "Failed to fix content resolver: " + e.getMessage());
            }

            if (BuildCompat.isS()) {
                try {
                    
                    
                    fixAttributionSourceState(BRContextImpl.get(context).getAttributionSource(), CarromRuntimeCore.getHostUid());
                } catch (Exception e) {
                    Slog.w(TAG, "Failed to fix attribution source state: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Slog.e(TAG, "Error in ContextCompat.fix: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
