package dev.jaowzin.carromloader.mirror.com.android.internal.view.inputmethod;

import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.view.inputmethod.InputMethodManager")
public interface InputMethodManager {
    @BField
    IInterface mService();
}
