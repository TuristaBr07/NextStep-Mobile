package com.tamarin.nextstep.chatbot;

import com.google.gson.annotations.SerializedName;

public class ChatbotResponse {

    @SerializedName("reply")
    private String reply;

    @SerializedName("message")
    private String message;

    @SerializedName("response")
    private String response;

    public String getReply() {
        return reply;
    }

    public String getMessage() {
        return message;
    }

    public String getResponse() {
        return response;
    }

    public String extractBestReply() {
        if (reply != null && !reply.trim().isEmpty()) {
            return reply.trim();
        }

        if (message != null && !message.trim().isEmpty()) {
            return message.trim();
        }

        if (response != null && !response.trim().isEmpty()) {
            return response.trim();
        }

        return null;
    }
}