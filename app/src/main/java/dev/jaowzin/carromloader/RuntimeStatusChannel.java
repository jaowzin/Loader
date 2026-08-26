package dev.jaowzin.carromloader;

import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;

import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;
import dev.jaowzin.carromloader.runtime.core.system.ServiceManager;
import dev.jaowzin.carromloader.runtime.core.system.status.CarromStatusService;

/** Direct status transport over the runtime's own central Binder service. */
final class RuntimeStatusChannel {
    private static final String TAG = "CarromStatusBinder";

    private RuntimeStatusChannel() {}

    static boolean publish(String value) {
        if (value == null || value.trim().isEmpty()) return false;
        IBinder binder = resolve();
        if (binder == null) return false;

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(CarromStatusService.DESCRIPTOR);
            data.writeString(value);
            boolean ok = binder.transact(CarromStatusService.TRANSACTION_SET, data, reply, 0);
            if (!ok) return false;
            reply.readException();
            return true;
        } catch (Throwable error) {
            Log.w(TAG, "publish failed", error);
            return false;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    static String read() {
        IBinder binder = resolve();
        if (binder == null) return null;

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(CarromStatusService.DESCRIPTOR);
            boolean ok = binder.transact(CarromStatusService.TRANSACTION_GET, data, reply, 0);
            if (!ok) return null;
            reply.readException();
            return reply.readString();
        } catch (Throwable error) {
            Log.w(TAG, "read failed", error);
            return null;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    private static IBinder resolve() {
        try {
            IBinder binder = CarromRuntimeCore.get().getService(ServiceManager.CARROM_STATUS);
            return binder != null && binder.isBinderAlive() ? binder : null;
        } catch (Throwable error) {
            Log.w(TAG, "resolve failed", error);
            return null;
        }
    }
}
