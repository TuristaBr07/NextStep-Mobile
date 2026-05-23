package com.tamarin.nextstep;

import com.google.gson.annotations.SerializedName;

/**
 * Payload de cadastro compatível com o AuthDTO do backend:
 * public record AuthDTO(String email, String senha) { }
 */
public class SignUpRequest {

    private String email;

    @SerializedName("senha")
    private String password;

    public SignUpRequest(String email, String password, String fullName) {
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