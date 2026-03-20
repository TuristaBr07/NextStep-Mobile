package com.tamarin.nextstep;

import com.google.gson.annotations.SerializedName;

public class Profile {
    private String id;

    // Traduzindo para o Spring Boot entender
    @SerializedName("fullName")
    private String full_name;

    // Traduzindo para o Spring Boot entender
    @SerializedName("companyName")
    private String company_name;

    // Avatar já tem o mesmo nome nos dois, então não precisa de anotação
    private String avatar;

    public Profile(String full_name, String company_name) {
        this.full_name = full_name;
        this.company_name = company_name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return full_name; }
    public void setFullName(String full_name) { this.full_name = full_name; }

    public String getCompanyName() { return company_name; }
    public void setCompanyName(String company_name) { this.company_name = company_name; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}