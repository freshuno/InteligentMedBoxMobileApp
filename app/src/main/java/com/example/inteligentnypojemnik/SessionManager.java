package com.example.inteligentnypojemnik;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String SHARED_PREF_NAME = "app_session";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_DEFAULT_ROLE_PREFIX = "default_role";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveTokens(String token, String refresh) {
        editor.putString(KEY_ACCESS_TOKEN, token);
        editor.putString(KEY_REFRESH_TOKEN, refresh);
        editor.apply();
    }

    public String getAuthToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public void saveUsername(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "Użytkownik");
    }

    public void saveDefaultRole(String role) {
        String currentUser = getUsername();
        if (currentUser == null || currentUser.isEmpty()) return;
        String userSpecificKey = KEY_DEFAULT_ROLE_PREFIX + currentUser;
        editor.putString(userSpecificKey, role);
        editor.apply();
    }

    public String getDefaultRole() {
        // Pobieramy aktualnie zalogowanego użytkownika
        String currentUser = getUsername();

        // Jeśli nie ma usera (np. wylogowany), nie ma też jego preferencji
        if (currentUser == null || currentUser.isEmpty()) return null;

        // Odtwarzamy ten sam unikalny klucz
        String userSpecificKey = KEY_DEFAULT_ROLE_PREFIX + currentUser;

        return prefs.getString(userSpecificKey, null);
    }

    public void clearSession() {
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.remove(KEY_USERNAME);
        editor.apply();
    }
}