package com.tamarin.nextstep;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Se a palavra 'BuildConfig' ficar vermelha, não se assuste.
// Vamos resolver isso no passo "Rebuild" logo abaixo.
import com.tamarin.nextstep.BuildConfig;

public class RetrofitClient {

    // AGORA PEGANDO DO SEGREDO (Cofre)
    // O Gradle injetou essas variáveis automaticamente
    private static final String BASE_URL = BuildConfig.SUPABASE_URL;
    private static final String ANON_KEY = BuildConfig.SUPABASE_KEY;

    private static Retrofit retrofit = null;

    public static SupabaseApi getApi() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request.Builder builder = chain.request().newBuilder();

                    // 1. Identifica o App (Chave Pública)
                    builder.addHeader("apikey", ANON_KEY);

                    // 2. Identifica o Usuário (Token de Sessão ou Anon)
                    String userToken = SessionManager.getAuthToken();
                    if (userToken != null) {
                        builder.addHeader("Authorization", "Bearer " + userToken);
                    } else {
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