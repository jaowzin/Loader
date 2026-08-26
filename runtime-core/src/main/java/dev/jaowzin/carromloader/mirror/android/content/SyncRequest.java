package dev.jaowzin.carromloader.mirror.android.content;

import android.accounts.Account;
import android.os.Bundle;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.content.SyncRequest")
public interface SyncRequest {
    @BField
    Account mAccountToSync();

    @BField
    String mAuthority();

    @BField
    Bundle mExtras();

    @BField
    boolean mIsPeriodic();

    @BField
    long mSyncFlexTimeSecs();

    @BField
    long mSyncRunTimeSecs();
}
