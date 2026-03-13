package com.tamarin.nextstep.chatbot;

import com.tamarin.nextstep.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatbotRetrofitClient {

    private static Retrofit retrofit;
    private static ChatbotApi api;

    private ChatbotRetrofitClient() {
        // Evita instanciação
    }

    public static ChatbotApi getApi() {
        if (!isConfigured()) {
            return null;
        }

        if (api == null) {
            api = getRetrofit().create(ChatbotApi.class);
        }

        return api;
    }

    public static boolean isConfigured() {
        return BuildConfig.CHATBOT_BASE_URL != null
                && !BuildConfig.CHATBOT_BASE_URL.trim().isEmpty();
    }

    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(ensureTrailingSlash(BuildConfig.CHATBOT_BASE_URL))
                    .client(buildHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    private static OkHttpClient buildHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS);

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }

        return builder.build();
    }

    private static String ensureTrailingSlash(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl;
        }
        return baseUrl + "/";
    }
}