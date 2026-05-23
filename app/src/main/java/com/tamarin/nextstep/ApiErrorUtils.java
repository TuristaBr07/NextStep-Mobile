package com.tamarin.nextstep;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Utilitário para extrair mensagens amigáveis das respostas de erro do backend.
 */
public final class ApiErrorUtils {

    private static final Gson GSON = new Gson( );

    private ApiErrorUtils() {
    }

    public static String getErrorMessage(Response<?> response, String fallback) {
        if (response == null) {
            return fallback;
        }

        ResponseBody errorBody = response.errorBody();
        if (errorBody == null) {
            return fallback;
        }

        try {
            String json = errorBody.string();
            ApiError error = GSON.fromJson(json, ApiError.class);
            if (error != null && error.mensagem != null && !error.mensagem.trim().isEmpty()) {
                return error.mensagem;
            }
        } catch (IOException | RuntimeException ignored) {
        }

        return fallback;
    }

    private static class ApiError {
        @SerializedName("mensagem")
        String mensagem;
    }
}