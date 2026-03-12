package com.tamarin.nextstep;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity extends AppCompatActivity {

    private Spinner spinnerReportType;
    private PieChart pieChart;
    private List<Transaction> allTransactions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        spinnerReportType = findViewById(R.id.spinnerReportType);
        pieChart = findViewById(R.id.pieChartReports);

        setupChart();

        spinnerReportType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                processChartData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadTransactions();
    }

    private void setupChart() {
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setCenterTextSize(16f);
        pieChart.getLegend().setWordWrapEnabled(true);
        pieChart.animateY(1000); // Animação de entrada
    }

    private void loadTransactions() {
        RetrofitClient.getApi().getTransactions().enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allTransactions = response.body();
                    processChartData();
                } else if (response.code() == 401) {
                    Toast.makeText(ReportsActivity.this, "Sessão expirada.", Toast.LENGTH_SHORT).show();
                    SessionManager.clear();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Toast.makeText(ReportsActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processChartData() {
        if (allTransactions == null || allTransactions.isEmpty()) return;

        String typeFilter = spinnerReportType.getSelectedItem().toString();
        Map<String, Float> categoryTotals = new HashMap<>();

        for (Transaction tx : allTransactions) {
            String type = tx.getType() != null ? tx.getType() : "";
            String category = tx.getCategory() != null ? tx.getCategory() : "Outros";
            float amount = tx.getAmount() != null ? tx.getAmount().floatValue() : 0f;

            boolean matchType = typeFilter.equals("Todos") || type.equalsIgnoreCase(typeFilter);

            if (matchType) {
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + amount);
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            }
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            return;
        }

        // Configuração das fatias da tarte
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Paleta de cores pronta
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // Atualiza o texto central com base no filtro
        if(typeFilter.equals("Receita")) {
            pieChart.setCenterText("Receitas por\nCategoria");
        } else if (typeFilter.equals("Despesa")) {
            pieChart.setCenterText("Despesas por\nCategoria");
        } else {
            pieChart.setCenterText("Total por\nCategoria");
        }

        pieChart.invalidate();
    }
}