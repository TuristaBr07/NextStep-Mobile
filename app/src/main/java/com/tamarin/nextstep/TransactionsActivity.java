package com.tamarin.nextstep;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsActivity extends AppCompatActivity {

    private TextInputLayout tilSearch;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilterType;
    private ChipGroup chipGroupPeriod;
    private AutoCompleteTextView actvFilterCategory;
    private RecyclerView rvAllTransactions;
    private LinearLayout layoutEmptyState;
    private TextView tvResultsCount;
    private SwipeRefreshLayout swipeRefreshTransactions;
    private MaterialButton btnExportCsv;

    private TransactionAdapter adapter;
    private List<Transaction> allTransactions = new ArrayList<>();
    private final List<Transaction> filteredTransactions = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        tilSearch = findViewById(R.id.tilSearch);
        etSearch = findViewById(R.id.etSearch);
        chipGroupFilterType = findViewById(R.id.chipGroupFilterType);
        chipGroupPeriod = findViewById(R.id.chipGroupPeriod);
        actvFilterCategory = findViewById(R.id.actvFilterCategory);
        btnExportCsv = findViewById(R.id.btnExportCsv);
        rvAllTransactions = findViewById(R.id.rvAllTransactions);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvResultsCount = findViewById(R.id.tvResultsCount);
        swipeRefreshTransactions = findViewById(R.id.swipeRefreshTransactions);

        swipeRefreshTransactions.setColorSchemeColors(
                ContextCompat.getColor(this, R.color.ns_primary)
        );
        swipeRefreshTransactions.setOnRefreshListener(() -> {
            loadTransactions();
            loadCategories();
        });

        rvAllTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvAllTransactions.setHasFixedSize(true);

        adapter = new TransactionAdapter(filteredTransactions);
        rvAllTransactions.setAdapter(adapter);

        setupFilters();
        loadCategories();

        if (btnExportCsv != null) {
            btnExportCsv.setOnClickListener(v -> exportCsv());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions();
    }

    private void loadTransactions() {
        track(RetrofitClient.getApi().getTransactions()).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (isFinishing() || isDestroyed()) return;
                swipeRefreshTransactions.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    allTransactions = response.body();
                    applyFilters();
                } else if (response.code() == 401) {
                    Toast.makeText(TransactionsActivity.this, getString(R.string.error_session_expired), Toast.LENGTH_SHORT).show();
                    SessionManager.clear();
                    finish();
                } else {
                    Toast.makeText(TransactionsActivity.this, getString(R.string.error_load_transactions), Toast.LENGTH_SHORT).show();
                    allTransactions.clear();
                    filteredTransactions.clear();
                    adapter.notifyDataSetChanged();
                    updateResultsInfo();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) return;
                swipeRefreshTransactions.setRefreshing(false);
                Toast.makeText(TransactionsActivity.this, getString(R.string.error_connection_generic), Toast.LENGTH_SHORT).show();
                allTransactions.clear();
                filteredTransactions.clear();
                adapter.notifyDataSetChanged();
                updateResultsInfo();
                updateEmptyState();
            }
        });
    }

    private void loadCategories() {
        track(RetrofitClient.getApi().getCategories()).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();

                    List<String> categoryNames = new ArrayList<>();
                    categoryNames.add(getString(R.string.filter_category_all));

                    for (Category cat : categories) {
                        if (cat.getName() != null && !cat.getName().trim().isEmpty()) {
                            categoryNames.add(cat.getName());
                        }
                    }

                    ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                            TransactionsActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            categoryNames
                    );
                    actvFilterCategory.setAdapter(catAdapter);
                    actvFilterCategory.setText(getString(R.string.filter_category_all), false);
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) return;
                Toast.makeText(TransactionsActivity.this, getString(R.string.error_loading_categories), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFilters() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        chipGroupFilterType.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters());
        if (chipGroupPeriod != null) {
            chipGroupPeriod.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters());
        }
        actvFilterCategory.setOnItemClickListener((parent, view, position, id) -> applyFilters());
    }

    private void applyFilters() {
        String textFilter = (etSearch != null && etSearch.getText() != null)
                ? etSearch.getText().toString().toLowerCase().trim()
                : "";

        int checkedChipId = chipGroupFilterType.getCheckedChipId();
        String typeFilter;
        if (checkedChipId == R.id.chipFilterIncome) {
            typeFilter = getString(R.string.transaction_type_income);
        } else if (checkedChipId == R.id.chipFilterExpense) {
            typeFilter = getString(R.string.transaction_type_expense);
        } else {
            typeFilter = "Todos";
        }

        String categoryFilter = (actvFilterCategory.getText() != null)
                ? actvFilterCategory.getText().toString().trim()
                : "Todas";
        if (categoryFilter.isEmpty()) categoryFilter = "Todas";

        int periodChipId = chipGroupPeriod != null ? chipGroupPeriod.getCheckedChipId() : -1;
        long periodStart = 0;
        if (periodChipId == R.id.chipPeriodWeek) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0);
            c.add(Calendar.DAY_OF_YEAR, -7);
            periodStart = c.getTimeInMillis();
        } else if (periodChipId == R.id.chipPeriodMonth) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0);
            periodStart = c.getTimeInMillis();
        }

        filteredTransactions.clear();

        for (Transaction tx : allTransactions) {
            String description = tx.getDescription() != null ? tx.getDescription().toLowerCase() : "";
            String category = tx.getCategory() != null ? tx.getCategory() : "";
            String type = tx.getType() != null ? tx.getType() : "";

            boolean matchText = textFilter.isEmpty() || description.contains(textFilter);
            boolean matchType = typeFilter.equals("Todos") || type.equalsIgnoreCase(typeFilter);
            boolean matchCategory = categoryFilter.equals("Todas") || category.equalsIgnoreCase(categoryFilter);

            boolean matchPeriod = true;
            if (periodStart > 0) {
                long txMs = parseDateToMillis(tx.getDate());
                matchPeriod = txMs >= periodStart;
            }

            if (matchText && matchType && matchCategory && matchPeriod) {
                filteredTransactions.add(tx);
            }
        }

        sortTransactionsByDateDesc(filteredTransactions);

        adapter.notifyDataSetChanged();
        updateResultsInfo();
        updateEmptyState();
    }

    private void sortTransactionsByDateDesc(List<Transaction> transactions) {
        Collections.sort(transactions, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                long d1 = parseDateToMillis(t1 != null ? t1.getDate() : null);
                long d2 = parseDateToMillis(t2 != null ? t2.getDate() : null);
                return Long.compare(d2, d1);
            }
        });
    }

    private long parseDateToMillis(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return Long.MIN_VALUE;
        }

        String[] patterns = new String[]{
                "yyyy-MM-dd",
                "dd/MM/yyyy",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, java.util.Locale.getDefault());
                sdf.setLenient(false);
                Date date = sdf.parse(rawDate);
                if (date != null) {
                    return date.getTime();
                }
            } catch (ParseException ignored) {
            }
        }

        return Long.MIN_VALUE;
    }

    private void updateResultsInfo() {
        if (tvResultsCount == null) return;

        int count = filteredTransactions.size();
        if (count == 0) {
            tvResultsCount.setText(getString(R.string.transactions_empty_title));
        } else if (count == 1) {
            tvResultsCount.setText(getString(R.string.transactions_count_single));
        } else {
            tvResultsCount.setText(getString(R.string.transactions_count_plural, count));
        }
    }

    private void exportCsv() {
        if (filteredTransactions.isEmpty()) {
            Toast.makeText(this, getString(R.string.transactions_export_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder csv = new StringBuilder("Data,Descrição,Tipo,Categoria,Valor (R$)\n");
        for (Transaction tx : filteredTransactions) {
            String date = tx.getDate() != null ? tx.getDate() : "";
            String desc = tx.getDescription() != null ? tx.getDescription().replace(",", ";") : "";
            String type = tx.getType() != null ? tx.getType() : "";
            String cat = tx.getCategory() != null ? tx.getCategory().replace(",", ";") : "";
            String amount = tx.getAmount() != null
                    ? String.format(java.util.Locale.getDefault(), "%.2f", tx.getAmount())
                    : "0.00";
            csv.append(date).append(",")
               .append(desc).append(",")
               .append(type).append(",")
               .append(cat).append(",")
               .append(amount).append("\n");
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Transações NextStep");
        shareIntent.putExtra(Intent.EXTRA_TEXT, csv.toString());
        startActivity(Intent.createChooser(shareIntent, getString(R.string.transactions_export_share)));
    }

    private void updateEmptyState() {
        if (filteredTransactions.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvAllTransactions.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvAllTransactions.setVisibility(View.VISIBLE);
        }
    }
}
