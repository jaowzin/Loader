package dev.jaowzin.carromloader.runtime.core.system.pm.installer;

import dev.jaowzin.carromloader.runtime.core.env.BEnvironment;
import dev.jaowzin.carromloader.runtime.core.system.pm.BPackageSettings;
import dev.jaowzin.carromloader.runtime.entity.pm.InstallOption;
import dev.jaowzin.carromloader.runtime.utils.FileUtils;


public class RemoveUserExecutor implements Executor {

    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        String packageName = ps.pkg.packageName;
        
        FileUtils.deleteDir(BEnvironment.getDataDir(packageName, userId));
        FileUtils.deleteDir(BEnvironment.getDeDataDir(packageName, userId));
        FileUtils.deleteDir(BEnvironment.getExternalDataDir(packageName, userId));
        return 0;
    }
}
