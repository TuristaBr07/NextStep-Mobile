package com.tamarin.nextstep;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gerenciador central da sessão autenticada.
 *
 * Ele guarda o JWT retornado pelo backend Spring Boot e o idUsuario usado nas
 * chamadas de perfil. A inicialização acontece em NextStepApp, mas os métodos
 * são defensivos para evitar travamentos durante testes unitários ou chamadas
 * muito cedo no ciclo de vida do app.
 */
public final class SessionManager {

    private static final String PREF_NAME = "nextstep_session";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";

    private static SharedPreferences preferences;
    private static String currentToken;
    private static String currentUserId;
    private static String currentEmail;

    private SessionManager() {
    }

    public static synchronized void init(Context context) {
        if (context == null) {
            return;
        }

        if (preferences == null) {
            preferences = context.getApplicationContext()
                    .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            currentToken = preferences.getString(KEY_TOKEN, null);
            currentUserId = preferences.getString(KEY_USER_ID, null);
            currentEmail = preferences.getString(KEY_EMAIL, null);
        }
    }

    public static synchronized void saveSession(String token, String userId) {
        saveSession(token, userId, null);
    }

    public static synchronized void saveSession(String token, String userId, String email) {
        currentToken = token;
        currentUserId = userId;
        currentEmail = email;

        if (preferences != null) {
            preferences.edit()
                    .putString(KEY_TOKEN, token)
                    .putString(KEY_USER_ID, userId)
                    .putString(KEY_EMAIL, email)
                    .apply();
        }
    }

    public static synchronized String getToken() {
        if (currentToken == null && preferences != null) {
            currentToken = preferences.getString(KEY_TOKEN, null);
        }
        return currentToken;
    }

    public static synchronized String getUserId() {
        if (currentUserId == null && preferences != null) {
            currentUserId = preferences.getString(KEY_USER_ID, null);
        }
        return currentUserId;
    }

    public static synchronized String getEmail() {
        if (currentEmail == null && preferences != null) {
            currentEmail = preferences.getString(KEY_EMAIL, null);
        }
        return currentEmail;
    }

    public static synchronized boolean hasSession() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public static synchronized void clear() {
        currentToken = null;
        currentUserId = null;
        currentEmail = null;

        if (preferences != null) {
            preferences.edit().clear().apply();
        }
    }
}