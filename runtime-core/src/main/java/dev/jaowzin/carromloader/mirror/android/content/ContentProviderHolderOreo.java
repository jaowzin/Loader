package dev.jaowzin.carromloader.mirror.android.content;

import android.content.pm.ProviderInfo;
import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.app.ContentProviderHolder")
public interface ContentProviderHolderOreo {
    @BField
    ProviderInfo info();

    @BField
    boolean noReleaseNeeded();

    @BField
    IInterface provider();
}
