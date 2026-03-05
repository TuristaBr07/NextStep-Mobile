package com.tamarin.nextstep;

import java.io.Serializable;

public class Transaction implements Serializable {
    private Long id;            // ID único (importante para o banco)
    private String description; // Ex: "Venda de Produto"
    private double amount;      // Valor monetário
    private String type;        // "Receita" ou "Despesa"
    private String category;    // Categoria da transação
    private String date;        // Data formato YYYY-MM-DD

    // Construtor vazio (Necessário para o futuro uso com Retrofit/Gson)
    public Transaction() {}

    public Transaction(Long id, String description, double amount, String type, String category, String date) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
    }

    // Getters
    public Long getId() { return id; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public String getDate() { return date; }
}