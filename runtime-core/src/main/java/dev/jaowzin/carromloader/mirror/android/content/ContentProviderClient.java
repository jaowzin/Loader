package dev.jaowzin.carromloader.mirror.android.content;

import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.content.ContentProviderClient")
public interface ContentProviderClient {
    @BField
    IInterface mContentProvider();
}
