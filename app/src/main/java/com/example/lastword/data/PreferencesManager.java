package com.example.lastword.data;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PreferencesManager {
    private static final String PREF_NAME = "LastWordPrefs";
    private static final String KEY_CURRENT_USER = "currentUser";
    private static final String KEY_USERS = "users";
    private static final String KEY_MUSIC = "music_on";
    private static final String KEY_SOUND = "sound_on";
    private static final String KEY_VIBRATION = "vibration_on";
    private static final String KEY_DARK_MODE = "dark_mode_on";

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

    public String getCurrentUser() {
        return prefs.getString(KEY_CURRENT_USER, null);
    }

    public void setCurrentUser(String username) {
        prefs.edit().putString(KEY_CURRENT_USER, username).apply();
    }

    public void logout() {
        prefs.edit().remove(KEY_CURRENT_USER).apply();
    }

    public boolean registerUser(String username, String email, String password) {
        try {
            String usersStr = prefs.getString(KEY_USERS, "[]");
            JSONArray array = new JSONArray(usersStr);
            for (int i = 0; i < array.length(); i++) {
                if (array.getJSONObject(i).getString("username").equalsIgnoreCase(username)) {
                    return false;
                }
            }
            JSONObject user = new JSONObject();
            user.put("username", username);
            user.put("email", email);
            user.put("password", password);
            user.put("played", 0);
            user.put("wins", 0);
            user.put("losses", 0);
            user.put("highScore", 0);
            user.put("streak", 0);
            user.put("achievements", new JSONArray());
            array.put(user);
            prefs.edit().putString(KEY_USERS, array.toString()).apply();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loginUser(String username, String password) {
        try {
            String usersStr = prefs.getString(KEY_USERS, "[]");
            JSONArray array = new JSONArray(usersStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject user = array.getJSONObject(i);
                if (user.getString("username").equalsIgnoreCase(username) && user.getString("password").equals(password)) {
                    setCurrentUser(user.getString("username"));
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public JSONObject getUserData(String username) {
        if (username == null) return null;
        try {
            String usersStr = prefs.getString(KEY_USERS, "[]");
            JSONArray array = new JSONArray(usersStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject user = array.getJSONObject(i);
                if (user.getString("username").equalsIgnoreCase(username)) {
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateStats(String username, int score, boolean won, String difficulty) {
        try {
            String usersStr = prefs.getString(KEY_USERS, "[]");
            JSONArray array = new JSONArray(usersStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject user = array.getJSONObject(i);
                if (user.getString("username").equalsIgnoreCase(username)) {
                    int played = user.optInt("played", 0) + 1;
                    int wins = user.optInt("wins", 0) + (won ? 1 : 0);
                    int losses = user.optInt("losses", 0) + (won ? 0 : 1);
                    int currentStreak = won ? (user.optInt("streak", 0) + 1) : 0;
                    int highScore = user.optInt("highScore", 0);
                    if (score > highScore) {
                        highScore = score;
                    }

                    user.put("played", played);
                    user.put("wins", wins);
                    user.put("losses", losses);
                    user.put("streak", currentStreak);
                    user.put("highScore", highScore);

                    if ("EASY".equalsIgnoreCase(difficulty)) {
                        user.put("animals_" + (won ? "saved" : "killed"), user.optInt("animals_" + (won ? "saved" : "killed"), 0) + 1);
                    } else if ("INTERMEDIATE".equalsIgnoreCase(difficulty)) {
                        user.put("adults_" + (won ? "saved" : "killed"), user.optInt("adults_" + (won ? "saved" : "killed"), 0) + 1);
                    } else if ("HARD".equalsIgnoreCase(difficulty)) {
                        user.put("children_" + (won ? "saved" : "killed"), user.optInt("children_" + (won ? "saved" : "killed"), 0) + 1);
                    }

                    JSONArray achs = user.optJSONArray("achievements");
                    if (achs == null) achs = new JSONArray();
                    List<String> achList = new ArrayList<>();
                    for (int j = 0; j < achs.length(); j++) {
                        achList.add(achs.getString(j));
                    }

                    if (won && !achList.contains("SAVIOR")) {
                        achList.add("SAVIOR");
                    }
                    if (won && score >= 500 && !achList.contains("HEROIC_ECHO")) {
                        achList.add("HEROIC_ECHO");
                    }
                    if (currentStreak >= 3 && !achList.contains("SOUL_PROTECTOR")) {
                        achList.add("SOUL_PROTECTOR");
                    }

                    JSONArray newAchs = new JSONArray();
                    for (String a : achList) {
                        newAchs.put(a);
                    }
                    user.put("achievements", newAchs);

                    array.put(i, user);
                    break;
                }
            }
            prefs.edit().putString(KEY_USERS, array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONArray getAllUsersSortedByScore() {
        try {
            String usersStr = prefs.getString(KEY_USERS, "[]");
            JSONArray array = new JSONArray(usersStr);
            ArrayList<JSONObject> list = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getJSONObject(i));
            }
            Collections.sort(list, (o1, o2) -> {
                int score1 = o1.optInt("highScore", 0);
                int score2 = o2.optInt("highScore", 0);
                return Integer.compare(score2, score1);
            });
            JSONArray sorted = new JSONArray();
            for (JSONObject u : list) {
                sorted.put(u);
            }
            return sorted;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }
}
