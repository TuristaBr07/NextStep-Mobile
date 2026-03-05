package com.tamarin.nextstep;

import java.io.Serializable;
public class Mei implements Serializable {
    private String nome;
    private String cnpj;
    private double faturamentoAtual;
    private String statusRisco; // "Baixo", "Médio", "Alto"

    // Construtor (Para criar um novo MEI facilmente)
    public Mei(String nome, String cnpj, double faturamentoAtual, String statusRisco) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.faturamentoAtual = faturamentoAtual;
        this.statusRisco = statusRisco;
    }

    // Getters (Para ler os dados depois)
    public String getNome() { return nome; }
    public String getCnpj() { return cnpj; }
    public double getFaturamentoAtual() { return faturamentoAtual; }
    public String getStatusRisco() { return statusRisco; }
}