package dev.jaowzin.carromloader.mirror.android.content;

import java.util.List;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.content.IntentFilter")
public interface IntentFilter {
    @BField
    List<String> mActions();

    @BField
    List<String> mCategories();
}
