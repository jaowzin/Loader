package dev.jaowzin.carromloader.mirror.android.location;

import android.util.ArrayMap;

import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BField;

@BClassName("android.location.LocationManager")
public interface LocationManagerQ {
    @BField
    ArrayMap mGnssNmeaListeners();

    @BField
    ArrayMap mGnssStatusListeners();

    @BField
    ArrayMap mGpsNmeaListeners();

    @BField
    ArrayMap mGpsStatusListeners();

    @BField
    ArrayMap mListeners();
}
