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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private static final double LIMITE_MEI = 81000.0;

    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();

    private TextView tvSaldo;
    private TextView tvReceita;
    private TextView tvDespesa;
    private TextView tvHeader;
    private TextView tvSubHeader;
    private TextView tvMeiProgressValue;

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
        tvMeiProgressValue = findViewById(R.id.tvMeiProgressValue);
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
        setupChartAppearance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchSummary(); // Agora buscamos a matemática do servidor
        fetchTransactions(); // Buscamos a lista para o gráfico e histórico
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

    private void fetchSummary() {
        RetrofitClient.getApi().getTransactionSummary().enqueue(new Callback<TransactionSummary>() {
            @Override
            public void onResponse(Call<TransactionSummary> call, Response<TransactionSummary> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateKPIs(response.body());
                }
            }

            @Override
            public void onFailure(Call<TransactionSummary> call, Throwable t) {
                // Falha silenciosa: se falhar, os valores ficam zerados
            }
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
                    drawChart(); // Desenhamos o gráfico com a lista
                } else if (response.code() == 401) {
                    Toast.makeText(DashboardActivity.this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_LONG).show();
                    SessionManager.clear();
                    Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Sem conexão com internet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateKPIs(TransactionSummary summary) {
        double totalReceita = summary.getReceitas();
        double totalDespesa = summary.getDespesas();
        double saldo = summary.getSaldo();

        tvSaldo.setText(formatCurrency(saldo));
        tvReceita.setText(formatCurrency(totalReceita));
        tvDespesa.setText(formatCurrency(totalDespesa));

        int progresso = (int) ((totalReceita / LIMITE_MEI) * 100);
        if (progresso < 0) progresso = 0;
        if (progresso > 100) progresso = 100;

        pbMeiLimit.setProgress(progresso);
        pbMeiLimit.setProgressBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.ns_border))
        );

        int progressColor;
        if (progresso >= 81) {
            progressColor = ContextCompat.getColor(this, R.color.ns_error);
        } else if (progresso >= 60) {
            progressColor = 0xFFF9A825;
        } else {
            progressColor = ContextCompat.getColor(this, R.color.ns_success);
        }
        pbMeiLimit.setProgressTintList(ColorStateList.valueOf(progressColor));

        if (tvMeiProgressValue != null) {
            tvMeiProgressValue.setText(
                    String.format(
                            Locale.getDefault(),
                            "%d%% do limite • %s de %s",
                            progresso,
                            formatCurrency(totalReceita),
                            formatCurrency(LIMITE_MEI)
                    )
            );
        }
    }

    private void setupChartAppearance() {
        if (lineChart == null) return;

        int textPrimary = ContextCompat.getColor(this, R.color.ns_text_primary);
        int textSecondary = ContextCompat.getColor(this, R.color.ns_text_secondary);
        int border = ContextCompat.getColor(this, R.color.ns_border);

        lineChart.setNoDataText("Sem dados para exibir no gráfico.");
        lineChart.setNoDataTextColor(textSecondary);
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setPinchZoom(false);
        lineChart.setScaleEnabled(false);
        lineChart.setExtraBottomOffset(8f);
        lineChart.setExtraLeftOffset(4f);
        lineChart.setExtraRightOffset(8f);

        Legend legend = lineChart.getLegend();
        legend.setTextColor(textSecondary);
        legend.setForm(Legend.LegendForm.LINE);
        legend.setWordWrapEnabled(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(textSecondary);
        xAxis.setGridColor(border);
        xAxis.setAxisLineColor(border);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-35f);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(textSecondary);
        leftAxis.setGridColor(border);
        leftAxis.setAxisLineColor(border);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new CurrencyAxisFormatter());

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
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
            String rawDate = tx.getDate();
            String dateKey = rawDate == null ? "Sem data" : rawDate;

            double amount = tx.getAmount();

            if (tx.getType() != null && (tx.getType().equalsIgnoreCase("Receita") || tx.getType().equalsIgnoreCase("income"))) {
                incomeMap.put(dateKey, incomeMap.getOrDefault(dateKey, 0.0) + amount);
                if (!expenseMap.containsKey(dateKey)) {
                    expenseMap.put(dateKey, 0.0);
                }
            } else {
                expenseMap.put(dateKey, expenseMap.getOrDefault(dateKey, 0.0) + amount);
                if (!incomeMap.containsKey(dateKey)) {
                    incomeMap.put(dateKey, 0.0);
                }
            }
        }

        int index = 0;
        List<String> xLabels = new ArrayList<>();

        for (String date : incomeMap.keySet()) {
            xLabels.add(formatChartDate(date));
            incomeEntries.add(new Entry(index, incomeMap.get(date).floatValue()));
            expenseEntries.add(new Entry(index, expenseMap.getOrDefault(date, 0.0).floatValue()));
            index++;
        }

        LineDataSet incomeSet = new LineDataSet(incomeEntries, "Receitas");
        incomeSet.setColor(ContextCompat.getColor(this, R.color.ns_success));
        incomeSet.setCircleColor(ContextCompat.getColor(this, R.color.ns_success));
        incomeSet.setLineWidth(2.4f);
        incomeSet.setCircleRadius(3.5f);
        incomeSet.setDrawValues(false);
        incomeSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        LineDataSet expenseSet = new LineDataSet(expenseEntries, "Despesas");
        expenseSet.setColor(ContextCompat.getColor(this, R.color.ns_error));
        expenseSet.setCircleColor(ContextCompat.getColor(this, R.color.ns_error));
        expenseSet.setLineWidth(2.4f);
        expenseSet.setCircleRadius(3.5f);
        expenseSet.setDrawValues(false);
        expenseSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        LineData lineData = new LineData(incomeSet, expenseSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setLabelCount(Math.min(xLabels.size(), 6), true);

        lineChart.animateX(700);
        lineChart.invalidate();
    }

    private void fetchUserProfile() {
        String userId = SessionManager.getUserId();
        if (userId == null || userId.isEmpty()) return;

        RetrofitClient.getApi().getProfile(userId).enqueue(new Callback<List<Profile>>() {
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
            }
        });
    }

    private String formatCurrency(double value) {
        return String.format(Locale.getDefault(), "R$ %.2f", value);
    }

    private String formatChartDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty() || rawDate.equalsIgnoreCase("Sem data")) {
            return "Sem data";
        }

        List<String> inputPatterns = new ArrayList<>();
        inputPatterns.add("yyyy-MM-dd");
        inputPatterns.add("dd/MM/yyyy");
        inputPatterns.add("yyyy-MM-dd'T'HH:mm:ss");
        inputPatterns.add("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        for (String pattern : inputPatterns) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(pattern, Locale.getDefault());
                inputFormat.setLenient(false);
                Date parsed = inputFormat.parse(rawDate);
                if (parsed != null) {
                    return new SimpleDateFormat("dd/MM", Locale.getDefault()).format(parsed);
                }
            } catch (ParseException ignored) {
            }
        }

        return rawDate;
    }

    private Bitmap decodeBase64ToBitmap(String b64) {
        try {
            byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    private static class CurrencyAxisFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
            if (value >= 1000f) {
                return String.format(Locale.getDefault(), "R$ %.0fk", value / 1000f);
            }
            return String.format(Locale.getDefault(), "R$ %.0f", value);
        }
    }
}