package com.tamarin.nextstep;

public class ChatMessage {
    private String text;
    private boolean fromUser;

    public ChatMessage() {
        // Necessário para serialização/desserialização local com Gson
    }

    public ChatMessage(String text, boolean fromUser) {
        this.text = text;
        this.fromUser = fromUser;
    }

    public String getText() {
        return text;
    }

    public boolean isFromUser() {
        return fromUser;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setFromUser(boolean fromUser) {
        this.fromUser = fromUser;
    }
}