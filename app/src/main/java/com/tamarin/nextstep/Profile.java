package com.tamarin.nextstep;

public class Profile {
    private String id;
    private String full_name;
    private String company_name;

    // NOVA VARIÁVEL
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

    // NOVOS GETTERS E SETTERS PARA A FOTO
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}