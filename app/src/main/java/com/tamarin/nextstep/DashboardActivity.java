package com.tamarin.nextstep;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();

    private TextView tvSaldo;
    private TextView tvReceita;
    private TextView tvDespesa;
    private TextView tvHeader;
    private TextView tvSubHeader;

    private ImageView ivLogo;
    private ProgressBar pbMeiLimit;
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        rvTransactions = findViewById(R.id.rvTransactions);
        tvSaldo = findViewById(R.id.tvSaldo);
        tvReceita = findViewById(R.id.tvReceita);
        tvDespesa = findViewById(R.id.tvDespesa);
        tvHeader = findViewById(R.id.tvHeader);
        tvSubHeader = findViewById(R.id.tvSubHeader);
        ivLogo = findViewById(R.id.ivLogo);
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

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTransactions();
        fetchUserProfile();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(DashboardActivity.this, TransactionsActivity.class));
                return false;
            } else if (itemId == R.id.nav_reports) {
                startActivity(new Intent(DashboardActivity.this, ReportsActivity.class));
                return false;
            } else if (itemId == R.id.nav_chatbot) {
                startActivity(new Intent(DashboardActivity.this, ChatbotActivity.class));
                return false;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
                return false;
            }

            return false;
        });
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
                        Toast.makeText(
                                DashboardActivity.this,
                                "Sessão expirada. Faça login novamente.",
                                Toast.LENGTH_LONG
                        ).show();

                        SessionManager.clear();

                        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(
                                DashboardActivity.this,
                                "Erro ao carregar dados",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Toast.makeText(
                        DashboardActivity.this,
                        "Sem conexão com internet",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void calculateKPIs() {
        if (transactionList == null) return;

        double totalReceita = 0.0;
        double totalDespesa = 0.0;

        for (Transaction tx : transactionList) {
            String type = tx.getType();

            if (type != null && (type.equalsIgnoreCase("Receita") || type.equalsIgnoreCase("income"))) {
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
        if (progresso < 0) progresso = 0;
        if (progresso > 100) progresso = 100;

        pbMeiLimit.setProgress(progresso);

        if (progresso > 80) {
            pbMeiLimit.setProgressTintList(ColorStateList.valueOf(0xFFD32F2F));
        } else {
            pbMeiLimit.setProgressTintList(ColorStateList.valueOf(0xFF2E7D32));
        }

        drawChart();
    }

    private void drawChart() {
        if (transactionList == null || transactionList.isEmpty() || lineChart == null) {
            if (lineChart != null) {
                lineChart.clear();
            }
            return;
        }

        List<Entry> incomeEntries = new ArrayList<>();
        List<Entry> expenseEntries = new ArrayList<>();

        Map<String, Double> incomeMap = new TreeMap<>();
        Map<String, Double> expenseMap = new TreeMap<>();

        for (Transaction tx : transactionList) {
            String date = tx.getDate();
            if (date == null) {
                date = "Sem data";
            }

            double amount = tx.getAmount();

            if (tx.getType() != null && (tx.getType().equalsIgnoreCase("Receita") || tx.getType().equalsIgnoreCase("income"))) {
                incomeMap.put(date, incomeMap.getOrDefault(date, 0.0) + amount);
                if (!expenseMap.containsKey(date)) {
                    expenseMap.put(date, 0.0);
                }
            } else {
                expenseMap.put(date, expenseMap.getOrDefault(date, 0.0) + amount);
                if (!incomeMap.containsKey(date)) {
                    incomeMap.put(date, 0.0);
                }
            }
        }

        int index = 0;
        List<String> xLabels = new ArrayList<>();

        for (String date : incomeMap.keySet()) {
            xLabels.add(date);
            incomeEntries.add(new Entry(index, incomeMap.get(date).floatValue()));
            expenseEntries.add(new Entry(index, expenseMap.getOrDefault(date, 0.0).floatValue()));
            index++;
        }

        LineDataSet incomeSet = new LineDataSet(incomeEntries, "Receitas");
        incomeSet.setColor(0xFF2E7D32);
        incomeSet.setCircleColor(0xFF2E7D32);
        incomeSet.setLineWidth(2f);

        LineDataSet expenseSet = new LineDataSet(expenseEntries, "Despesas");
        expenseSet.setColor(0xFFD32F2F);
        expenseSet.setCircleColor(0xFFD32F2F);
        expenseSet.setLineWidth(2f);

        LineData lineData = new LineData(incomeSet, expenseSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setGranularity(1f);

        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    private void fetchUserProfile() {
        String userId = SessionManager.getUserId();
        if (userId == null || userId.isEmpty()) return;

        RetrofitClient.getApi().getProfile("eq." + userId).enqueue(new Callback<List<Profile>>() {
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Profile p = response.body().get(0);

                    if (p.getFullName() != null && !p.getFullName().isEmpty()) {
                        String firstName = p.getFullName().split(" ")[0];
                        tvHeader.setText("Olá, " + firstName + "!");
                    }

                    if (p.getCompanyName() != null && !p.getCompanyName().isEmpty()) {
                        tvSubHeader.setText("Visão financeira de " + p.getCompanyName());
                    } else {
                        tvSubHeader.setText("Visão financeira do seu negócio");
                    }

                    if (p.getAvatar() != null && !p.getAvatar().isEmpty()) {
                        Bitmap bitmap = decodeBase64ToBitmap(p.getAvatar());
                        if (bitmap != null) {
                            ivLogo.setImageBitmap(bitmap);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {
                // Falha silenciosa para não impactar a experiência
            }
        });
    }

    private Bitmap decodeBase64ToBitmap(String b64) {
        try {
            byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
        } catch (Exception e) {
            return null;
        }
    }
}