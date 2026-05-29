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

public final class RetrofitClient {

    private static final String BASE_URL = "https://back-endnextstep-production.up.railway.app/";

    private static Retrofit retrofit;
    private static NextStepApi api;

    private RetrofitClient() {
    }

    public static synchronized NextStepApi getApi() {
        if (api == null) {
            api = getRetrofit().create(NextStepApi.class);
        }
        return api;
    }

    private static synchronized Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(createHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private static OkHttpClient createHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // Loga o corpo completo apenas em builds de desenvolvimento.
        // Em produção fica desligado para não vazar token JWT e dados pessoais no logcat.
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor())
                .addInterceptor(logging)
                .build();
    }

    private static class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request.Builder builder = original.newBuilder()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json");

            String token = SessionManager.getToken();
            if (token != null && !token.trim().isEmpty()) {
                builder.header("Authorization", "Bearer " + token.trim());
            }

            Response response = chain.proceed(builder.build());

            if (response.code() == 401 || response.code() == 403) {
                SessionManager.clear();
            }

            return response;
        }
    }
}
