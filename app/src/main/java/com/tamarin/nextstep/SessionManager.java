package com.tamarin.nextstep;

import android.content.Context;

public class SessionManager {

    // Variáveis estáticas armazenam os dados apenas na memória RAM (Volátil)
    private static String currentToken = null;
    private static String currentUserId = null;

    // Mantemos o método init para não quebrar a chamada lá na MainActivity
    public static void init(Context context) {
        // Como não usamos mais SharedPreferences, este método fica vazio
    }

    // Salva o Token e o ID do Usuário na memória enquanto o app estiver aberto
    public static void saveSession(String token, String userId) {
        currentToken = token;
        currentUserId = userId;
    }

    // Recupera o Token
    public static String getToken() {
        return currentToken;
    }

    // Recupera o ID do Usuário (Para salvar na transação)
    public static String getUserId() {
        return currentUserId;
    }

    // Limpa a sessão (Logout manual ou erro 401)
    public static void clear() {
        currentToken = null;
        currentUserId = null;
    }
}