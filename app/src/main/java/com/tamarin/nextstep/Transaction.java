package com.tamarin.nextstep;

import com.google.gson.annotations.SerializedName;

public class Transaction {

    @SerializedName("id")
    private Long id;

    @SerializedName("description")
    private String description;

    @SerializedName("amount")
    private Double amount;

    @SerializedName("type")
    private String type;

    @SerializedName("category")
    private String category;

    @SerializedName("date")
    private String date;

    @SerializedName("user_id")
    private String userId;

    // --- CONSTRUTORES ---
    public Transaction() {
    }

    // --- GETTERS (Para Ler) E SETTERS (Para Gravar) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}