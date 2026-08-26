package dev.jaowzin.carromloader;

import android.content.Context;
import android.content.SharedPreferences;

public final class FeatureSettings {
    private static final String PREFS = "carrom_loader_features";
    private static final String KEY_LINES = "lines_enabled";
    private static final String KEY_BANK = "bank_preview";
    private static final String KEY_AUTOPLAY = "autoplay_enabled";

    private FeatureSettings() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean linesEnabled(Context context) {
        return prefs(context).getBoolean(KEY_LINES, false);
    }

    public static void setLinesEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_LINES, enabled).apply();
    }

    public static boolean bankPreviewEnabled(Context context) {
        return prefs(context).getBoolean(KEY_BANK, true);
    }

    public static void setBankPreviewEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_BANK, enabled).apply();
    }

    public static boolean autoPlayEnabled(Context context) {
        return prefs(context).getBoolean(KEY_AUTOPLAY, false);
    }

    public static void setAutoPlayEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_AUTOPLAY, enabled).apply();
    }
}
