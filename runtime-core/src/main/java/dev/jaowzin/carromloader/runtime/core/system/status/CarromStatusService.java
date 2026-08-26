package dev.jaowzin.carromloader.runtime.core.system.status;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Tiny Binder service hosted by the runtime server process.
 * Both the Loader UI process and virtual app processes resolve this same binder
 * through the runtime ServiceManager, so it is not affected by guest filesystem,
 * broadcasts, sockets or component virtualization.
 */
public final class CarromStatusService extends Binder {
    public static final String DESCRIPTOR = "dev.jaowzin.carromloader.runtime.ICarromStatus";
    public static final int TRANSACTION_SET = IBinder.FIRST_CALL_TRANSACTION;
    public static final int TRANSACTION_GET = IBinder.FIRST_CALL_TRANSACTION + 1;

    private static final CarromStatusService INSTANCE = new CarromStatusService();
    private static final String WAITING = "waiting for virtual Carrom";

    private final AtomicReference<String> latest = new AtomicReference<>(WAITING);

    private CarromStatusService() {
        attachInterface(null, DESCRIPTOR);
    }

    public static CarromStatusService get() {
        return INSTANCE;
    }

    @Override
    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == INTERFACE_TRANSACTION) {
            if (reply != null) reply.writeString(DESCRIPTOR);
            return true;
        }

        if (code == TRANSACTION_SET) {
            data.enforceInterface(DESCRIPTOR);
            String value = data.readString();
            if (value != null && !value.trim().isEmpty()) latest.set(value);
            if (reply != null) reply.writeNoException();
            return true;
        }

        if (code == TRANSACTION_GET) {
            data.enforceInterface(DESCRIPTOR);
            if (reply != null) {
                reply.writeNoException();
                reply.writeString(latest.get());
            }
            return true;
        }

        return super.onTransact(code, data, reply, flags);
    }
}
