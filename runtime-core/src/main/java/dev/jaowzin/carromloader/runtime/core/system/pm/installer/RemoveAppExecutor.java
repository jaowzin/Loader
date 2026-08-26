package dev.jaowzin.carromloader.runtime.core.system.pm.installer;

import dev.jaowzin.carromloader.runtime.core.env.BEnvironment;
import dev.jaowzin.carromloader.runtime.core.system.pm.BPackageSettings;
import dev.jaowzin.carromloader.runtime.entity.pm.InstallOption;
import dev.jaowzin.carromloader.runtime.utils.FileUtils;


public class RemoveAppExecutor implements Executor {
    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        FileUtils.deleteDir(BEnvironment.getAppDir(ps.pkg.packageName));
        return 0;
    }
}
