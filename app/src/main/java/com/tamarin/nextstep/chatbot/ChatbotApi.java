package com.tamarin.nextstep.chatbot;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatbotApi {

    @POST("chat")
    Call<ChatbotResponse> sendMessage(@Body ChatbotRequest request);
}