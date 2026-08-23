package dev.jaowzin.carromloader.engine;

import android.content.Context;

public interface GameEngine {
    boolean isAvailable(Context context);
    boolean launch(Context context);
    String getTargetPackage();
    String getDescription();
}
