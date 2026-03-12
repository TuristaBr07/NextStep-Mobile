package com.tamarin.nextstep;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "nextstep_session";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";

    private static SharedPreferences prefs;
    private static String currentToken;
    private static String currentUserId;

    private SessionManager() {
        // Evita instanciação
    }

    public static void init(Context context) {
        if (prefs == null) {
            prefs = context.getApplicationContext()
                    .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

            currentToken = prefs.getString(KEY_TOKEN, null);
            currentUserId = prefs.getString(KEY_USER_ID, null);
        }
    }

    public static void saveSession(String token, String userId) {
        ensureInitialized();

        currentToken = token;
        currentUserId = userId;

        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_ID, userId)
                .apply();
    }

    public static String getToken() {
        ensureInitialized();
        return currentToken;
    }

    public static String getUserId() {
        ensureInitialized();
        return currentUserId;
    }

    public static boolean hasSession() {
        ensureInitialized();
        return currentToken != null && !currentToken.trim().isEmpty();
    }

    public static void clear() {
        ensureInitialized();

        currentToken = null;
        currentUserId = null;

        prefs.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_USER_ID)
                .apply();
    }

    private static void ensureInitialized() {
        if (prefs == null) {
            throw new IllegalStateException("SessionManager não foi inicializado. Chame SessionManager.init(context) antes de usar.");
        }
    }
}