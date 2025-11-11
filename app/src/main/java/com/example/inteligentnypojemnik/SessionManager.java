package com.example.inteligentnypojemnik;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String SHARED_PREF_NAME = "app_session";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

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

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}