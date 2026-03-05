package com.tamarin.nextstep;

public class LoginResponse {
    // O Supabase manda um JSON com "access_token"
    private String access_token;

    public String getAccessToken() {
        return access_token;
    }
}