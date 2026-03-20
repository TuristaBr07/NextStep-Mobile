package com.tamarin.nextstep;

import com.google.gson.annotations.SerializedName;

public class TransactionSummary {

    @SerializedName("receitas")
    private double receitas;

    @SerializedName("despesas")
    private double despesas;

    @SerializedName("saldo")
    private double saldo;

    public double getReceitas() { return receitas; }
    public void setReceitas(double receitas) { this.receitas = receitas; }

    public double getDespesas() { return despesas; }
    public void setDespesas(double despesas) { this.despesas = despesas; }

    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }
}