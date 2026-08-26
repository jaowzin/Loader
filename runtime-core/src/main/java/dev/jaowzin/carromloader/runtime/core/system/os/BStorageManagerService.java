package dev.jaowzin.carromloader.runtime.core.system.os;

import android.net.Uri;
import android.os.Process;
import android.os.RemoteException;
import android.os.storage.StorageVolume;

import java.io.File;

import dev.jaowzin.carromloader.mirror.android.os.storage.BRStorageManager;
import dev.jaowzin.carromloader.mirror.android.os.storage.BRStorageVolume;
import dev.jaowzin.carromloader.runtime.CarromRuntimeCore;
import dev.jaowzin.carromloader.runtime.core.env.BEnvironment;
import dev.jaowzin.carromloader.runtime.core.system.ISystemService;
import dev.jaowzin.carromloader.runtime.core.system.user.BUserHandle;
import dev.jaowzin.carromloader.runtime.fake.provider.FileProvider;
import dev.jaowzin.carromloader.runtime.proxy.ProxyManifest;
import dev.jaowzin.carromloader.runtime.utils.compat.BuildCompat;


public class BStorageManagerService extends IBStorageManagerService.Stub implements ISystemService {
    private static final BStorageManagerService sService = new BStorageManagerService();

    public static BStorageManagerService get() {
        return sService;
    }

    public BStorageManagerService() {
    }

    @Override
    public StorageVolume[] getVolumeList(int uid, String packageName, int flags, int userId) throws RemoteException {
        if (BRStorageManager.get().getVolumeList(0, 0) == null) {
            return null;
        }
        try {
            StorageVolume[] storageVolumes = BRStorageManager.get().getVolumeList(BUserHandle.getUserId(Process.myUid()), 0);
            if (storageVolumes == null)
                return null;
            for (StorageVolume storageVolume : storageVolumes) {
                BRStorageVolume.get(storageVolume)._set_mPath(BEnvironment.getExternalUserDir(userId));
                if (BuildCompat.isPie()) {
                    BRStorageVolume.get(storageVolume)._set_mInternalPath(BEnvironment.getExternalUserDir(userId));
                }
            }
            return storageVolumes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Uri getUriForFile(String file) throws RemoteException {
        return FileProvider.getUriForFile(CarromRuntimeCore.getContext(), ProxyManifest.getProxyFileProvider(), new File(file));
    }

    @Override
    public void systemReady() {

    }
}
