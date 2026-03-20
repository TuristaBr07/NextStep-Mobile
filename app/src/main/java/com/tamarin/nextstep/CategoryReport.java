package com.tamarin.nextstep;

import com.google.gson.annotations.SerializedName;

public class CategoryReport {

    @SerializedName("category")
    private String category;

    @SerializedName("type")
    private String type;

    @SerializedName("total")
    private double total;

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}