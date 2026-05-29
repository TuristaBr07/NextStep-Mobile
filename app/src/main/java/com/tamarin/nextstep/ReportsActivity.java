package com.tamarin.nextstep;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity extends AppCompatActivity {

    private static final int MAX_VISIBLE_CATEGORIES = 5;
    private static final Locale LOCALE_PT_BR = new Locale("pt", "BR");

    private MaterialButtonToggleGroup toggleGroupReportType;
    private PieChart pieChart;
    private LinearLayout layoutCategorySummary;
    private TextView tvSummaryEmpty;

    private List<CategoryReport> reportData = new ArrayList<>();

    private final java.util.List<retrofit2.Call<?>> pendingCalls = new java.util.ArrayList<>();

    private <T> retrofit2.Call<T> track(retrofit2.Call<T> call) {
        pendingCalls.add(call);
        return call;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (retrofit2.Call<?> c : pendingCalls) {
            c.cancel();
        }
        pendingCalls.clear();
    }

    private final int[] chartColors = new int[]{
            0xFF2E7D32,
            0xFF1565C0,
            0xFFF9A825,
            0xFFD32F2F,
            0xFF8E24AA,
            0xFF00897B
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        toggleGroupReportType = findViewById(R.id.toggleGroupReportType);
        pieChart = findViewById(R.id.pieChartReports);
        layoutCategorySummary = findViewById(R.id.layoutCategorySummary);
        tvSummaryEmpty = findViewById(R.id.tvSummaryEmpty);

        setupChart();

        toggleGroupReportType.check(R.id.btnReportAll);
        toggleGroupReportType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                processChartData();
            }
        });

        loadReportData();
    }

    private String getSelectedTypeFilter() {
        int checkedId = toggleGroupReportType.getCheckedButtonId();
        if (checkedId == R.id.btnReportIncome) {
            return getString(R.string.transaction_type_income);
        } else if (checkedId == R.id.btnReportExpense) {
            return getString(R.string.transaction_type_expense);
        }
        return "Todos";
    }

    private void setupChart() {
        int textPrimary = ContextCompat.getColor(this, R.color.ns_text_primary);
        int textSecondary = ContextCompat.getColor(this, R.color.ns_text_secondary);
        int surface = ContextCompat.getColor(this, R.color.ns_surface);

        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(surface);
        pieChart.setTransparentCircleColor(surface);
        pieChart.setTransparentCircleAlpha(70);
        pieChart.setTransparentCircleRadius(58f);
        pieChart.setHoleRadius(54f);
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(textPrimary);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setDrawEntryLabels(false);
        pieChart.setExtraTopOffset(8f);
        pieChart.setExtraBottomOffset(12f);
        pieChart.setExtraLeftOffset(8f);
        pieChart.setExtraRightOffset(8f);
        pieChart.setMinAngleForSlices(4f);
        pieChart.setNoDataText(getString(R.string.reports_pie_no_data));
        pieChart.setNoDataTextColor(textSecondary);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setWordWrapEnabled(true);
        legend.setTextColor(textSecondary);
        legend.setTextSize(12f);
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setFormSize(10f);
        legend.setXEntrySpace(8f);
        legend.setYEntrySpace(6f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        pieChart.animateY(800);
    }

    private void loadReportData() {
        track(RetrofitClient.getApi().getCategoryReports()).enqueue(new Callback<List<CategoryReport>>() {
            @Override
            public void onResponse(Call<List<CategoryReport>> call, Response<List<CategoryReport>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    reportData = response.body();
                    processChartData();
                } else if (response.code() == 401) {
                    Toast.makeText(ReportsActivity.this, getString(R.string.error_session_expired), Toast.LENGTH_SHORT).show();
                    SessionManager.clear();
                    Intent intent = new Intent(ReportsActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ReportsActivity.this, getString(R.string.error_load_reports), Toast.LENGTH_SHORT).show();
                    showEmptySummary();
                }
            }

            @Override
            public void onFailure(Call<List<CategoryReport>> call, Throwable t) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) return;
                Toast.makeText(ReportsActivity.this, getString(R.string.error_connection_generic), Toast.LENGTH_SHORT).show();
                showEmptySummary();
            }
        });
    }

    private void processChartData() {
        if (pieChart == null) return;

        if (reportData == null || reportData.isEmpty()) {
            pieChart.clear();
            pieChart.setCenterText(getString(R.string.reports_center_no_data));
            pieChart.invalidate();
            showEmptySummary();
            return;
        }

        String typeFilter = getSelectedTypeFilter();

        HashMap<String, Float> categoryTotals = new HashMap<>();

        for (CategoryReport cr : reportData) {
            String type = cr.getType() != null ? cr.getType() : "";
            String category = cr.getCategory() != null && !cr.getCategory().trim().isEmpty()
                    ? cr.getCategory()
                    : "Outros";
            float amount = (float) cr.getTotal();

            boolean matchType = typeFilter.equals("Todos") || type.equalsIgnoreCase(typeFilter);

            if (matchType && amount > 0f) {
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + amount);
            }
        }

        if (categoryTotals.isEmpty()) {
            pieChart.clear();
            pieChart.setCenterText(buildCenterText(typeFilter));
            pieChart.invalidate();
            showEmptySummary();
            return;
        }

        List<PieEntry> rawEntries = new ArrayList<>();
        float grandTotal = 0f;

        for (String category : categoryTotals.keySet()) {
            Float value = categoryTotals.get(category);
            if (value != null && value > 0f) {
                rawEntries.add(new PieEntry(value, category));
                grandTotal += value;
            }
        }

        if (rawEntries.isEmpty()) {
            pieChart.clear();
            pieChart.setCenterText(buildCenterText(typeFilter));
            pieChart.invalidate();
            showEmptySummary();
            return;
        }

        Collections.sort(rawEntries, new Comparator<PieEntry>() {
            @Override
            public int compare(PieEntry o1, PieEntry o2) {
                return Float.compare(o2.getValue(), o1.getValue());
            }
        });

        List<PieEntry> finalEntries = new ArrayList<>();
        float othersTotal = 0f;

        for (int i = 0; i < rawEntries.size(); i++) {
            if (i < MAX_VISIBLE_CATEGORIES) {
                finalEntries.add(rawEntries.get(i));
            } else {
                othersTotal += rawEntries.get(i).getValue();
            }
        }

        if (othersTotal > 0f) {
            finalEntries.add(new PieEntry(othersTotal, "Outros"));
        }

        PieDataSet dataSet = new PieDataSet(finalEntries, "");
        dataSet.setSliceSpace(4f);
        dataSet.setSelectionShift(8f);
        dataSet.setColors(chartColors);
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        pieChart.setData(data);
        pieChart.setCenterText(buildCenterText(typeFilter));
        pieChart.highlightValues(null);
        pieChart.invalidate();

        renderCategorySummary(finalEntries, grandTotal);
    }

    private void renderCategorySummary(List<PieEntry> entries, float grandTotal) {
        if (layoutCategorySummary == null) return;

        layoutCategorySummary.removeAllViews();

        if (entries == null || entries.isEmpty() || grandTotal <= 0f) {
            showEmptySummary();
            return;
        }

        if (tvSummaryEmpty != null) {
            tvSummaryEmpty.setVisibility(View.GONE);
        }

        for (int i = 0; i < entries.size(); i++) {
            PieEntry entry = entries.get(i);
            int color = chartColors[i % chartColors.length];
            layoutCategorySummary.addView(createSummaryRow(entry, grandTotal, color, i + 1));
        }
    }

    private View createSummaryRow(PieEntry entry, float grandTotal, int color, int position) {
        int textPrimary = ContextCompat.getColor(this, R.color.ns_text_primary);
        int textSecondary = ContextCompat.getColor(this, R.color.ns_text_secondary);
        int surfaceSoft = ContextCompat.getColor(this, R.color.ns_surface_soft);
        int border = ContextCompat.getColor(this, R.color.ns_border);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if (position > 1) {
            cardParams.topMargin = dp(10);
        }
        card.setLayoutParams(cardParams);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(surfaceSoft);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), border);
        card.setBackground(bg);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        View colorDot = new View(this);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(12), dp(12));
        dotParams.rightMargin = dp(10);
        dotParams.topMargin = dp(3);
        colorDot.setLayoutParams(dotParams);

        GradientDrawable dotBg = new GradientDrawable();
        dotBg.setColor(color);
        dotBg.setShape(GradientDrawable.OVAL);
        colorDot.setBackground(dotBg);

        LinearLayout textBlock = new LinearLayout(this);
        textBlock.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textBlockParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        textBlock.setLayoutParams(textBlockParams);

        TextView tvCategory = new TextView(this);
        tvCategory.setText(position + ". " + entry.getLabel());
        tvCategory.setTextColor(textPrimary);
        tvCategory.setTextSize(15);
        tvCategory.setTypeface(tvCategory.getTypeface(), android.graphics.Typeface.BOLD);

        float percent = (entry.getValue() / grandTotal) * 100f;
        TextView tvPercent = new TextView(this);
        tvPercent.setText(String.format(Locale.getDefault(), "%.1f%% do total", percent));
        tvPercent.setTextColor(textSecondary);
        tvPercent.setTextSize(12);

        textBlock.addView(tvCategory);
        textBlock.addView(tvPercent);

        TextView tvAmount = new TextView(this);
        tvAmount.setText(formatCurrency(entry.getValue()));
        tvAmount.setTextColor(textPrimary);
        tvAmount.setTextSize(14);
        tvAmount.setTypeface(tvAmount.getTypeface(), android.graphics.Typeface.BOLD);

        topRow.addView(colorDot);
        topRow.addView(textBlock);
        topRow.addView(tvAmount);

        card.addView(topRow);
        return card;
    }

    private void showEmptySummary() {
        if (layoutCategorySummary != null) {
            layoutCategorySummary.removeAllViews();
        }
        if (tvSummaryEmpty != null) {
            tvSummaryEmpty.setVisibility(View.VISIBLE);
        }
    }

    private String buildCenterText(String typeFilter) {
        if (getString(R.string.transaction_type_income).equals(typeFilter)) {
            return getString(R.string.reports_center_income);
        } else if (getString(R.string.transaction_type_expense).equals(typeFilter)) {
            return getString(R.string.reports_center_expense);
        }
        return getString(R.string.reports_center_total);
    }

    private String formatCurrency(float value) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(LOCALE_PT_BR);
        return currencyFormat.format(value);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}