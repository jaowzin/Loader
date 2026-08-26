package dev.jaowzin.carromloader.runtime.core.system.pm.installer;

import dev.jaowzin.carromloader.runtime.core.env.BEnvironment;
import dev.jaowzin.carromloader.runtime.core.system.pm.BPackageSettings;
import dev.jaowzin.carromloader.runtime.entity.pm.InstallOption;
import dev.jaowzin.carromloader.runtime.utils.FileUtils;


public class CreateUserExecutor implements Executor {

    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        String packageName = ps.pkg.packageName;
        FileUtils.deleteDir(BEnvironment.getDataLibDir(packageName, userId));

        
        FileUtils.mkdirs(BEnvironment.getDataDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDataCacheDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDataFilesDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDataDatabasesDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDeDataDir(packageName, userId));








        return 0;
    }
}
