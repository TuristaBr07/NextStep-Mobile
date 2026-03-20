package com.tamarin.nextstep;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    private String email;

    // Traduzimos "password" para "senha" no JSON do Login também!
    @SerializedName("senha")
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}