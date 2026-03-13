package com.tamarin.nextstep;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTransactionActivity extends AppCompatActivity {

    private Spinner spinnerType, spinnerCategory;
    private TextInputEditText etDescription, etAmount, etDate;
    private TextInputLayout tilDescription, tilAmount, tilDate;
    private Button btnSaveTransaction;

    private List<Category> categories = new ArrayList<>();
    private boolean isSaving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        spinnerType = findViewById(R.id.spinnerType);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        etDescription = findViewById(R.id.etDescription);
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        tilDescription = findViewById(R.id.tilDescription);
        tilAmount = findViewById(R.id.tilAmount);
        tilDate = findViewById(R.id.tilDate);
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction);

        setupTypeSpinner();
        loadCategories();

        btnSaveTransaction.setOnClickListener(v -> {
            if (!isSaving) {
                validateAndSave();
            }
        });
    }

    private void setupTypeSpinner() {
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.transaction_types,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
    }

    private void loadCategories() {
        RetrofitClient.getApi().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    updateCategorySpinner();
                } else {
                    Toast.makeText(AddTransactionActivity.this, "Erro ao carregar categorias.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(AddTransactionActivity.this, "Falha ao carregar categorias.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCategorySpinner() {
        String selectedType = spinnerType.getSelectedItem() != null
                ? spinnerType.getSelectedItem().toString()
                : "Receita";

        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            if (category.getType() != null && category.getType().equalsIgnoreCase(selectedType)) {
                categoryNames.add(category.getName());
            }
        }

        if (categoryNames.isEmpty()) {
            categoryNames.add("Sem categoria");
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryNames
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        spinnerType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                updateCategorySpinnerWithoutLoop();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void updateCategorySpinnerWithoutLoop() {
        String selectedType = spinnerType.getSelectedItem() != null
                ? spinnerType.getSelectedItem().toString()
                : "Receita";

        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            if (category.getType() != null && category.getType().equalsIgnoreCase(selectedType)) {
                categoryNames.add(category.getName());
            }
        }

        if (categoryNames.isEmpty()) {
            categoryNames.add("Sem categoria");
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryNames
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void validateAndSave() {
        clearErrors();

        String type = spinnerType.getSelectedItem() != null ? spinnerType.getSelectedItem().toString() : "";
        String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String amountText = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        String dateText = etDate.getText() != null ? etDate.getText().toString().trim() : "";

        boolean hasError = false;

        if (TextUtils.isEmpty(description)) {
            tilDescription.setError("Informe uma descrição");
            hasError = true;
        }

        if (TextUtils.isEmpty(amountText)) {
            tilAmount.setError("Informe o valor");
            hasError = true;
        } else {
            try {
                double amount = Double.parseDouble(amountText.replace(",", "."));
                if (amount <= 0) {
                    tilAmount.setError("O valor deve ser maior que zero");
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilAmount.setError("Valor inválido");
                hasError = true;
            }
        }

        if (TextUtils.isEmpty(dateText)) {
            tilDate.setError("Informe a data");
            hasError = true;
        } else if (!isValidDate(dateText)) {
            tilDate.setError("Use o formato DD/MM/AAAA");
            hasError = true;
        }

        if (hasError) {
            return;
        }

        saveTransaction(type, category, description, amountText, dateText);
    }

    private void saveTransaction(String type, String category, String description, String amountText, String dateText) {
        setLoading(true);

        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setDescription(description);
        transaction.setAmount(Double.parseDouble(amountText.replace(",", ".")));
        transaction.setDate(formatDateToApi(dateText));
        transaction.setUserId(SessionManager.getUserId());

        RetrofitClient.getApi().createTransaction(transaction).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                setLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(AddTransactionActivity.this, "Transação criada com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (response.code() == 401) {
                    Toast.makeText(AddTransactionActivity.this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
                    SessionManager.clear();
                    finish();
                } else {
                    Toast.makeText(AddTransactionActivity.this, "Erro ao salvar transação.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(AddTransactionActivity.this, "Falha de conexão.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        isSaving = loading;
        btnSaveTransaction.setEnabled(!loading);
        btnSaveTransaction.setText(loading ? "Salvando..." : "Salvar transação");

        spinnerType.setEnabled(!loading);
        spinnerCategory.setEnabled(!loading);
        etDescription.setEnabled(!loading);
        etAmount.setEnabled(!loading);
        etDate.setEnabled(!loading);
    }

    private void clearErrors() {
        tilDescription.setError(null);
        tilAmount.setError(null);
        tilDate.setError(null);
    }

    private boolean isValidDate(String value) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(value);
            return date != null;
        } catch (ParseException e) {
            return false;
        }
    }

    private String formatDateToApi(String value) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = input.parse(value);
            return date != null ? output.format(date) : value;
        } catch (Exception e) {
            return value;
        }
    }
}