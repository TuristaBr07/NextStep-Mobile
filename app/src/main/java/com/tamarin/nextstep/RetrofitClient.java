package com.tamarin.nextstep;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;

    // NOTA: Mais tarde podemos renomear "SupabaseApi" para "NextStepApi" para ficar mais coerente,
    // mas por agora mantemos o mesmo nome para não quebrar as outras classes.
    private static SupabaseApi api;

    // O endereço mágico que o emulador usa para chegar ao "localhost" do seu computador
    private static final String BASE_URL = "http://10.0.2.2:8081/";

    private RetrofitClient() {
        // Evita instanciação
    }

    public static SupabaseApi getApi() {
        if (api == null) {
            api = getRetrofit().create(SupabaseApi.class);
        }
        return api;
    }

    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(buildHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private static OkHttpClient buildHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();

                        String token = null;
                        try {
                            token = SessionManager.getToken();
                        } catch (Exception ignored) {
                        }

                        // Removemos a apikey do Supabase. Agora apenas dizemos que enviamos JSON.
                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Content-Type", "application/json");

                        // Se o utilizador já fez login e tem o Token JWT, anexa-o ao cabeçalho!
                        if (token != null && !token.trim().isEmpty()) {
                            requestBuilder.header("Authorization", "Bearer " + token);
                        }

                        Response response = chain.proceed(requestBuilder.build());

                        // MÁGICA DA EXPULSÃO AUTOMÁTICA
                        // Se o servidor barrar o acesso (401 Expirado ou 403 Proibido), limpamos a sessão na hora
                        if (response.code() == 401 || response.code() == 403) {
                            try {
                                SessionManager.clear();
                            } catch (Exception ignored) {
                            }
                        }

                        return response;
                    }
                });

        // Mantém o Logging (muito útil para ver os erros no Logcat do Android Studio)
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(logging);

        return builder.build();
    }
}