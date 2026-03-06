package com.tamarin.nextstep;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "NextStepSession";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static SharedPreferences prefs;

    // Inicializa o gerenciador (chamado na MainActivity)
    public static void init(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Salva o Token e o ID do Usuário ao fazer login
    public static void saveSession(String token, String userId) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_ID, userId)
                .apply();
    }

    // Recupera o Token
    public static String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    // Recupera o ID do Usuário (Para salvar na transação)
    public static String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    // Limpa a sessão (Logout)
    public static void clear() {
        prefs.edit().clear().apply();
    }
}