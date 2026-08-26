package dev.jaowzin.carromloader.runtime.core.system.pm.installer;

import dev.jaowzin.carromloader.runtime.core.system.pm.BPackageSettings;
import dev.jaowzin.carromloader.runtime.entity.pm.InstallOption;


public interface Executor {
    public static final String TAG = "InstallExecutor";

    int exec(BPackageSettings ps, InstallOption option, int userId);
}
