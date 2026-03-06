package com.tamarin.nextstep;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static SupabaseApi getApi() {
        if (retrofit == null) {

            // Configura o "Carteiro" (OkHttp) para colocar os selos (Headers) em todas as cartas
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();

                    // 1. Pega o Token usando o NOME NOVO que criamos no SessionManager
                    String token = SessionManager.getToken();

                    Request.Builder builder = original.newBuilder()
                            .header("apikey", BuildConfig.SUPABASE_KEY) // Chave Pública (Sempre vai)
                            .header("Content-Type", "application/json");

                    // 2. Decide qual crachá usar
                    if (token != null) {
                        // Se o usuário já logou, usa o crachá dele (Token JWT)
                        builder.header("Authorization", "Bearer " + token);
                    } else {
                        // Se não logou (tela de login), usa o crachá de visitante (Chave Pública)
                        builder.header("Authorization", "Bearer " + BuildConfig.SUPABASE_KEY);
                    }

                    Request request = builder.build();
                    return chain.proceed(request);
                }
            }).build();

            // Constrói o Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.SUPABASE_URL) // URL que vem do local.properties
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(SupabaseApi.class);
    }
}