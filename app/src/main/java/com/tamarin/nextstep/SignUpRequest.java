package com.tamarin.nextstep;

import com.google.gson.annotations.SerializedName;

public class SignUpRequest {
    private String email;
    private String password;
    private UserData data;

    public SignUpRequest(String email, String password, String fullName) {
        this.email = email;
        this.password = password;
        this.data = new UserData(fullName);
    }

    public static class UserData {
        @SerializedName("full_name")
        private String fullName;

        public UserData(String fullName) {
            this.fullName = fullName;
        }
    }
}