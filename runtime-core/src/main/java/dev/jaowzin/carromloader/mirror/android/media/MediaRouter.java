package dev.jaowzin.carromloader.mirror.android.media;

import android.os.IInterface;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;
import dev.jaowzin.carromloader.bridge.annotation.BStaticField;

@BClassName("android.media.MediaRouter")
public interface MediaRouter {
    @BStaticField
    Object sStatic();

    @BClassName("android.media.MediaRouter$Static")
    interface StaticKitkat {
        @BField
        IInterface mMediaRouterService();
    }

    @BClassName("android.media.MediaRouter$Static")
    interface Static {
        @BField
        IInterface mAudioService();
    }
}
