package com.tamarin.nextstep;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://wsnrotqjekgelowseqet.supabase.co/"; // Sua URL correta
    // Anon Key (Chave Pública)
    private static final String ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndzbnJvdHFqZWtnZWxvd3NlcWV0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjIxNDQzOTIsImV4cCI6MjA3NzcyMDM5Mn0.6lFmXwZCHiruXkOpBEE24z6k5caxyNpbLMUWJWS1aXE";

    private static Retrofit retrofit = null;

    public static SupabaseApi getApi() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request.Builder builder = chain.request().newBuilder();

                    // 1. A 'apikey' é sempre a ANON (Identifica o App)
                    builder.addHeader("apikey", ANON_KEY);

                    // 2. A 'Authorization' define QUEM é o usuário
                    String userToken = SessionManager.getAuthToken();
                    if (userToken != null) {
                        // Se temos token de usuário (VIP), usamos ele!
                        builder.addHeader("Authorization", "Bearer " + userToken);
                    } else {
                        // Se não, usamos o token de visitante (Anon)
                        builder.addHeader("Authorization", "Bearer " + ANON_KEY);
                    }

                    return chain.proceed(builder.build());
                }
            }).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(SupabaseApi.class);
    }
}