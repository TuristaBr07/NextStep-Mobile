package com.tamarin.nextstep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;

    private TextView tvSaldo, tvReceita, tvDespesa;
    private ProgressBar pbMeiLimit;

    // Variável do Gráfico
    private com.github.mikephil.charting.charts.LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        rvTransactions = findViewById(R.id.rvTransactions);
        tvSaldo = findViewById(R.id.tvSaldo);
        tvReceita = findViewById(R.id.tvReceita);
        tvDespesa = findViewById(R.id.tvDespesa);
        pbMeiLimit = findViewById(R.id.pbMeiLimit);
        lineChart = findViewById(R.id.lineChartDashboard);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setHasFixedSize(true);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, AddTransactionActivity.class);
                startActivity(intent);
            });
        }

        View ivSettingsBtn = findViewById(R.id.ivSettingsBtn);
        if (ivSettingsBtn != null) {
            ivSettingsBtn.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
                startActivity(intent);
            });
        }

        View ivHistoryBtn = findViewById(R.id.ivHistoryBtn);
        if (ivHistoryBtn != null) {
            ivHistoryBtn.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, TransactionsActivity.class);
                startActivity(intent);
            });
        }

        // NOVO: CÓDIGO DO BOTÃO DE RELATÓRIOS
        View ivReportsBtn = findViewById(R.id.ivReportsBtn);
        if (ivReportsBtn != null) {
            ivReportsBtn.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, ReportsActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTransactions();
    }

    private void fetchTransactions() {
        RetrofitClient.getApi().getTransactions().enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    transactionList = response.body();
                    adapter = new TransactionAdapter(transactionList);
                    rvTransactions.setAdapter(adapter);
                    calculateKPIs();
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(DashboardActivity.this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_LONG).show();
                        SessionManager.clear();
                        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(DashboardActivity.this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Sem conexão com internet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateKPIs() {
        if (transactionList == null) return;

        double totalReceita = 0;
        double totalDespesa = 0;

        for (Transaction tx : transactionList) {
            if (tx.getType() != null && (tx.getType().equalsIgnoreCase("Receita") || tx.getType().equalsIgnoreCase("income"))) {
                totalReceita += tx.getAmount();
            } else {
                totalDespesa += tx.getAmount();
            }
        }

        double saldo = totalReceita - totalDespesa;

        tvSaldo.setText(String.format(Locale.getDefault(), "R$ %.2f", saldo));
        tvReceita.setText(String.format(Locale.getDefault(), "R$ %.2f", totalReceita));
        tvDespesa.setText(String.format(Locale.getDefault(), "R$ %.2f", totalDespesa));

        double limiteMEI = 81000.0;
        int progresso = (int) ((totalReceita / limiteMEI) * 100);
        pbMeiLimit.setProgress(progresso);

        if (progresso > 80) {
            pbMeiLimit.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFFF0000));
        } else {
            pbMeiLimit.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF2E7D32));
        }

        // Chama o método para desenhar o gráfico
        drawChart();
    }

    // --- LÓGICA DO GRÁFICO DE LINHAS ---
    private void drawChart() {
        if (transactionList == null || transactionList.isEmpty() || lineChart == null) {
            if (lineChart != null) lineChart.clear();
            return;
        }

        java.util.List<com.github.mikephil.charting.data.Entry> incomeEntries = new java.util.ArrayList<>();
        java.util.List<com.github.mikephil.charting.data.Entry> expenseEntries = new java.util.ArrayList<>();

        java.util.Map<String, Double> incomeMap = new java.util.TreeMap<>();
        java.util.Map<String, Double> expenseMap = new java.util.TreeMap<>();

        for (Transaction tx : transactionList) {
            String date = tx.getDate();
            double amount = tx.getAmount();

            if (tx.getType() != null && (tx.getType().equalsIgnoreCase("Receita") || tx.getType().equalsIgnoreCase("income"))) {
                incomeMap.put(date, incomeMap.getOrDefault(date, 0.0) + amount);
                if (!expenseMap.containsKey(date)) expenseMap.put(date, 0.0);
            } else {
                expenseMap.put(date, expenseMap.getOrDefault(date, 0.0) + amount);
                if (!incomeMap.containsKey(date)) incomeMap.put(date, 0.0);
            }
        }

        int index = 0;
        final java.util.List<String> xLabels = new java.util.ArrayList<>();

        for (String date : incomeMap.keySet()) {
            xLabels.add(date);
            incomeEntries.add(new com.github.mikephil.charting.data.Entry(index, incomeMap.get(date).floatValue()));
            expenseEntries.add(new com.github.mikephil.charting.data.Entry(index, expenseMap.get(date).floatValue()));
            index++;
        }

        com.github.mikephil.charting.data.LineDataSet incomeSet = new com.github.mikephil.charting.data.LineDataSet(incomeEntries, "Receitas");
        incomeSet.setColor(0xFF2E7D32);
        incomeSet.setCircleColor(0xFF2E7D32);
        incomeSet.setLineWidth(2f);

        com.github.mikephil.charting.data.LineDataSet expenseSet = new com.github.mikephil.charting.data.LineDataSet(expenseEntries, "Despesas");
        expenseSet.setColor(0xFFD32F2F);
        expenseSet.setCircleColor(0xFFD32F2F);
        expenseSet.setLineWidth(2f);

        com.github.mikephil.charting.data.LineData lineData = new com.github.mikephil.charting.data.LineData(incomeSet, expenseSet);

        lineChart.setData(lineData);

        com.github.mikephil.charting.components.XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(xLabels));
        xAxis.setGranularity(1f);

        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.animateX(1000);
        lineChart.invalidate();
    }
}