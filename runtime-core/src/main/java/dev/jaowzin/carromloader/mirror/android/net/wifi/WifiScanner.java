package dev.jaowzin.carromloader.mirror.android.net.wifi;


import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BStaticField;

@BClassName("android.net.wifi.WifiScanner")
public interface WifiScanner {
    @BStaticField
    String GET_AVAILABLE_CHANNELS_EXTRA();
}
