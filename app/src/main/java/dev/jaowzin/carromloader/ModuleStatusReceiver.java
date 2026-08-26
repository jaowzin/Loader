package dev.jaowzin.carromloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Receives status from the virtual Carrom process and persists it in the Loader process. */
public final class ModuleStatusReceiver extends BroadcastReceiver {
    public static final String ACTION = "dev.jaowzin.carromloader.MODULE_STATUS";
    public static final String EXTRA_VALUE = "value";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || !ACTION.equals(intent.getAction())) return;
        String value = intent.getStringExtra(EXTRA_VALUE);
        if (value == null || value.trim().isEmpty()) return;
        ModuleStatusStore.write(context.getApplicationContext(), value);
    }
}
