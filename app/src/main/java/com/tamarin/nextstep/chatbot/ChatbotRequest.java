package com.tamarin.nextstep.chatbot;

public class ChatbotRequest {
    private final String message;
    private final String userId;
    private final String platform;

    public ChatbotRequest(String message, String userId, String platform) {
        this.message = message;
        this.userId = userId;
        this.platform = platform;
    }

    public String getMessage() {
        return message;
    }

    public String getUserId() {
        return userId;
    }

    public String getPlatform() {
        return platform;
    }
}