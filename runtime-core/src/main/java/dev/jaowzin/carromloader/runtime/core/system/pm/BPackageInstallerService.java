package dev.jaowzin.carromloader.runtime.core.system.pm;

import java.util.ArrayList;
import java.util.List;

import dev.jaowzin.carromloader.runtime.core.system.ISystemService;
import dev.jaowzin.carromloader.runtime.core.system.pm.installer.CopyExecutor;
import dev.jaowzin.carromloader.runtime.core.system.pm.installer.CreatePackageExecutor;
import dev.jaowzin.carromloader.runtime.core.system.pm.installer.CreateUserExecutor;
import dev.jaowzin.carromloader.runtime.core.system.pm.installer.Executor;
import dev.jaowzin.carromloader.runtime.core.system.pm.installer.RemoveAppExecutor;
import dev.jaowzin.carromloader.runtime.core.system.pm.installer.RemoveUserExecutor;
import dev.jaowzin.carromloader.runtime.entity.pm.InstallOption;
import dev.jaowzin.carromloader.runtime.utils.Slog;


public class BPackageInstallerService extends IBPackageInstallerService.Stub implements ISystemService {
    private static final BPackageInstallerService sService = new BPackageInstallerService();

    public static BPackageInstallerService get() {
        return sService;
    }

    public static final String TAG = "BPackageInstallerService";

    @Override
    public int installPackageAsUser(BPackageSettings ps, int userId) {
        List<Executor> executors = new ArrayList<>();
        
        executors.add(new CreateUserExecutor());
        
        executors.add(new CreatePackageExecutor());
        
        executors.add(new CopyExecutor());
        InstallOption option = ps.installOption;
        for (Executor executor : executors) {
            int exec = executor.exec(ps, option, userId);
            Slog.d(TAG, "installPackageAsUser: " + executor.getClass().getSimpleName() + " exec: " + exec);
            if (exec != 0) {
                return exec;
            }
        }
        return 0;
    }

    @Override
    public int uninstallPackageAsUser(BPackageSettings ps, boolean removeApp, int userId) {
        List<Executor> executors = new ArrayList<>();
        if (removeApp) {
            
            executors.add(new RemoveAppExecutor());
        }
        
        executors.add(new RemoveUserExecutor());
        InstallOption option = ps.installOption;
        for (Executor executor : executors) {
            int exec = executor.exec(ps, option, userId);
            Slog.d(TAG, "uninstallPackageAsUser: " + executor.getClass().getSimpleName() + " exec: " + exec);
            if (exec != 0) {
                return exec;
            }
        }
        return 0;
    }

    @Override
    public int clearPackage(BPackageSettings ps, int userId) {
        List<Executor> executors = new ArrayList<>();
        
        executors.add(new RemoveUserExecutor());
        
        executors.add(new CreateUserExecutor());
        InstallOption option = ps.installOption;
        for (Executor executor : executors) {
            int exec = executor.exec(ps, option, userId);
            Slog.d(TAG, "uninstallPackageAsUser: " + executor.getClass().getSimpleName() + " exec: " + exec);
            if (exec != 0) {
                return exec;
            }
        }
        return 0;
    }

    @Override
    public int updatePackage(BPackageSettings ps) {
        List<Executor> executors = new ArrayList<>();
        executors.add(new CreatePackageExecutor());
        executors.add(new CopyExecutor());
        InstallOption option = ps.installOption;
        for (Executor executor : executors) {
            int exec = executor.exec(ps, option, -1);
            if (exec != 0) {
                return exec;
            }
        }
        return 0;
    }

    @Override
    public void systemReady() {

    }
}
