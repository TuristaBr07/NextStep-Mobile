package com.tamarin.nextstep;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsActivity extends AppCompatActivity {

    private EditText etSearch;
    private Spinner spinnerFilterType;
    private Spinner spinnerFilterCategory;
    private RecyclerView rvAllTransactions;
    private LinearLayout layoutEmptyState;
    private TextView tvResultsCount;

    private TransactionAdapter adapter;
    private List<Transaction> allTransactions = new ArrayList<>();
    private final List<Transaction> filteredTransactions = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        etSearch = findViewById(R.id.etSearch);
        spinnerFilterType = findViewById(R.id.spinnerFilterType);
        spinnerFilterCategory = findViewById(R.id.spinnerFilterCategory);
        rvAllTransactions = findViewById(R.id.rvAllTransactions);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvResultsCount = findViewById(R.id.tvResultsCount);

        rvAllTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvAllTransactions.setHasFixedSize(true);

        adapter = new TransactionAdapter(filteredTransactions);
        rvAllTransactions.setAdapter(adapter);

        setupFilters();
        loadCategories();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions();
    }

    private void loadTransactions() {
        RetrofitClient.getApi().getTransactions().enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allTransactions = response.body();
                    applyFilters();
                } else if (response.code() == 401) {
                    Toast.makeText(TransactionsActivity.this, "Sessão expirada.", Toast.LENGTH_SHORT).show();
                    SessionManager.clear();
                    finish();
                } else {
                    Toast.makeText(TransactionsActivity.this, "Erro ao carregar transações.", Toast.LENGTH_SHORT).show();
                    allTransactions.clear();
                    filteredTransactions.clear();
                    adapter.notifyDataSetChanged();
                    updateResultsInfo();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Toast.makeText(TransactionsActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
                allTransactions.clear();
                filteredTransactions.clear();
                adapter.notifyDataSetChanged();
                updateResultsInfo();
                updateEmptyState();
            }
        });
    }

    private void loadCategories() {
        RetrofitClient.getApi().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();

                    List<String> categoryNames = new ArrayList<>();
                    categoryNames.add("Todas");

                    for (Category cat : categories) {
                        if (cat.getName() != null && !cat.getName().trim().isEmpty()) {
                            categoryNames.add(cat.getName());
                        }
                    }

                    android.widget.ArrayAdapter<String> catAdapter = new android.widget.ArrayAdapter<>(
                            TransactionsActivity.this,
                            android.R.layout.simple_spinner_item,
                            categoryNames
                    );
                    catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerFilterCategory.setAdapter(catAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(TransactionsActivity.this, "Erro ao carregar categorias.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFilters() {
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

        spinnerFilterType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void applyFilters() {
        String textFilter = etSearch.getText() != null
                ? etSearch.getText().toString().toLowerCase().trim()
                : "";

        String typeFilter = spinnerFilterType.getSelectedItem() != null
                ? spinnerFilterType.getSelectedItem().toString()
                : "Todos";

        String categoryFilter = spinnerFilterCategory.getSelectedItem() != null
                ? spinnerFilterCategory.getSelectedItem().toString()
                : "Todas";

        filteredTransactions.clear();

        for (Transaction tx : allTransactions) {
            String description = tx.getDescription() != null ? tx.getDescription().toLowerCase() : "";
            String category = tx.getCategory() != null ? tx.getCategory() : "";
            String type = tx.getType() != null ? tx.getType() : "";

            boolean matchText = textFilter.isEmpty() || description.contains(textFilter);
            boolean matchType = typeFilter.equals("Todos") || type.equalsIgnoreCase(typeFilter);
            boolean matchCategory = categoryFilter.equals("Todas") || category.equalsIgnoreCase(categoryFilter);

            if (matchText && matchType && matchCategory) {
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
            tvResultsCount.setText("Nenhuma transação encontrada");
        } else if (count == 1) {
            tvResultsCount.setText("1 transação encontrada");
        } else {
            tvResultsCount.setText(count + " transações encontradas");
        }
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