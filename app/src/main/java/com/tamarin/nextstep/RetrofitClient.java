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
    private static SupabaseApi api;

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
                    .baseUrl(getBaseUrl())
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

                        Request.Builder requestBuilder = original.newBuilder()
                                .header("apikey", BuildConfig.SUPABASE_KEY)
                                .header("Content-Type", "application/json");

                        if (token != null && !token.trim().isEmpty()) {
                            requestBuilder.header("Authorization", "Bearer " + token);
                        } else {
                            requestBuilder.header("Authorization", "Bearer " + BuildConfig.SUPABASE_KEY);
                        }

                        return chain.proceed(requestBuilder.build());
                    }
                });

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }

        return builder.build();
    }

    private static String getBaseUrl() {
        String url = BuildConfig.SUPABASE_URL;
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("SUPABASE_URL não foi configurada.");
        }

        if (!url.endsWith("/")) {
            url += "/";
        }

        return url;
    }
}