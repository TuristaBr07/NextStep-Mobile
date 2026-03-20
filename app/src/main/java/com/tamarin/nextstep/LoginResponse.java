package com.tamarin.nextstep;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    // 1. Mudamos para "token" (como o Spring Boot envia)
    @SerializedName("token")
    private String accessToken;

    // 2. Lemos o ID e o email diretamente da raiz do JSON do Spring Boot
    @SerializedName("idUsuario")
    private String idUsuario;

    @SerializedName("email")
    private String email;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    // 3. Truque: Mantemos o método getUser() intacto para não quebrar o resto do seu App!
    // Ele simplesmente pega os campos soltos acima e monta o objeto User na hora.
    public User getUser() {
        User user = new User();
        user.setId(idUsuario);
        user.setEmail(email);
        return user;
    }
}