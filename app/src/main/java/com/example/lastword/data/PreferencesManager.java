package com.example.lastword.data;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREF_NAME = "LastWordPrefs";
    private static final String KEY_MUSIC = "music_on";
    private static final String KEY_SOUND = "sound_on";
    private static final String KEY_VIBRATION = "vibration_on";
    private static final String KEY_DARK_MODE = "dark_mode_on";
    private static final String KEY_STATS_MIGRATED = "stats_migrated_v1";
    private static final String KEY_LAST_THANK_YOU = "last_thank_you_idx";

    // Deprecated keys used only for migration
    private static final String KEY_CURRENT_USER = "currentUser";
    private static final String KEY_USERS = "users";

    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isMusicEnabled() {
        return prefs.getBoolean(KEY_MUSIC, true);
    }

    public void setMusicEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_MUSIC, enabled).apply();
    }

    public boolean isSoundEnabled() {
        return prefs.getBoolean(KEY_SOUND, true);
    }

    public void setSoundEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply();
    }

    public boolean isVibrationEnabled() {
        return prefs.getBoolean(KEY_VIBRATION, true);
    }

    public void setVibrationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply();
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, true);
    }

    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public boolean isStatsMigrated() {
        return prefs.getBoolean(KEY_STATS_MIGRATED, false);
    }

    public void setStatsMigrated(boolean migrated) {
        prefs.edit().putBoolean(KEY_STATS_MIGRATED, migrated).apply();
    }

    public int getLastThankYouIndex() {
        return prefs.getInt(KEY_LAST_THANK_YOU, -1);
    }

    public void setLastThankYouIndex(int index) {
        prefs.edit().putInt(KEY_LAST_THANK_YOU, index).apply();
    }

    // Legacy helpers for data migration
    public String getRawUsersJson() {
        return prefs.getString(KEY_USERS, null);
    }

    public String getCurrentUser() {
        return prefs.getString(KEY_CURRENT_USER, null);
    }

    public void clearAuthData() {
        prefs.edit()
                .remove(KEY_CURRENT_USER)
                .remove(KEY_USERS)
                .apply();
    }
}
