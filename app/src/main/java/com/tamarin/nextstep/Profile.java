package com.tamarin.nextstep;

public class Profile {
    private String id;
    private String full_name;
    private String company_name;

    public Profile(String full_name, String company_name) {
        this.full_name = full_name;
        this.company_name = company_name;
    }

    public String getId() { return id; }
    public String getFullName() { return full_name; }
    public String getCompanyName() { return company_name; }
}