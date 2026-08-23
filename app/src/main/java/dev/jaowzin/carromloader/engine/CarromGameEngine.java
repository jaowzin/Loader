package dev.jaowzin.carromloader.engine;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public final class CarromGameEngine implements GameEngine {
    public static final String PACKAGE_NAME = "com.miniclip.carrom";

    @Override
    public boolean isAvailable(Context context) {
        try {
            context.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    @Override
    public boolean launch(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(PACKAGE_NAME);
        if (intent == null) return false;
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return true;
    }

    @Override
    public String getTargetPackage() {
        return PACKAGE_NAME;
    }

    @Override
    public String getDescription() {
        return "External clean-room engine: launches the original Carrom Pool process and keeps the guide overlay separate.";
    }
}
