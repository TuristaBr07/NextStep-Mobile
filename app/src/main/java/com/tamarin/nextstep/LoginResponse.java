package com.tamarin.nextstep;

import com.google.gson.annotations.SerializedName;

/**
 * Resposta do endpoint POST /auth/login.
 *
 * O backend retorna: token, idUsuario e email. O método getUser() foi mantido
 * para preservar compatibilidade com telas que já esperavam um objeto User.
 */
public class LoginResponse {

    @SerializedName("token")
    private String accessToken;

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

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User getUser() {
        User user = new User();
        user.setId(idUsuario);
        user.setEmail(email);
        return user;
    }
}