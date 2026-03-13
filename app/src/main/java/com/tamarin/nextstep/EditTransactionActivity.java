package com.tamarin.nextstep;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTransactionActivity extends AppCompatActivity {

    private TextInputEditText etDescription, etAmount, etDate;
    private TextInputLayout tilDescription, tilAmount, tilDate;
    private RadioButton rbIncome, rbExpense;
    private Spinner spinnerCategory;
    private Button btnSave, btnDelete;

    private Long transactionId;
    private boolean isProcessing = false;
    private List<Category> categories = new ArrayList<>();
    private String initialType = "Receita";
    private String initialCategory = "";
    private String initialDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);

        etDescription = findViewById(R.id.etDescription);
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        tilDescription = findViewById(R.id.tilDescription);
        tilAmount = findViewById(R.id.tilAmount);
        tilDate = findViewById(R.id.tilDate);
        rbIncome = findViewById(R.id.rbIncome);
        rbExpense = findViewById(R.id.rbExpense);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        setupDateField();
        readExtras();
        loadCategories();

        rbIncome.setOnClickListener(v -> updateCategorySpinner(getSelectedType()));
        rbExpense.setOnClickListener(v -> updateCategorySpinner(getSelectedType()));

        btnSave.setOnClickListener(v -> {
            if (!isProcessing) {
                validateAndSave();
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (!isProcessing) {
                confirmDelete();
            }
        });
    }

    private void setupDateField() {
        etDate.setFocusable(false);
        etDate.setClickable(true);
        etDate.setLongClickable(false);
        etDate.setOnClickListener(v -> showDatePicker());
        etDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePicker();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        String currentValue = etDate.getText() != null ? etDate.getText().toString().trim() : "";
        if (!currentValue.isEmpty()) {
            try {
                Date parsed = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(currentValue);
                if (parsed != null) {
                    calendar.setTime(parsed);
                }
            } catch (ParseException ignored) {
            }
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String formatted = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    etDate.setText(formatted);
                    tilDate.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void readExtras() {
        if (getIntent() != null) {
            transactionId = (Long) getIntent().getSerializableExtra("EXTRA_ID");
            String desc = getIntent().getStringExtra("EXTRA_DESC");
            Double amount = (Double) getIntent().getSerializableExtra("EXTRA_AMOUNT");
            String type = getIntent().getStringExtra("EXTRA_TYPE");
            String category = getIntent().getStringExtra("EXTRA_CATEGORY");
            String date = getIntent().getStringExtra("EXTRA_DATE");

            initialType = type != null ? type : "Receita";
            initialCategory = category != null ? category : "";
            initialDate = formatDateForDisplay(date);

            etDescription.setText(desc != null ? desc : "");
            etAmount.setText(amount != null ? String.valueOf(amount).replace(".", ",") : "");
            etDate.setText(initialDate);

            if (initialType.equalsIgnoreCase("Despesa")
                    || initialType.equalsIgnoreCase("Saída")
                    || initialType.equalsIgnoreCase("expense")) {
                rbExpense.setChecked(true);
            } else {
                rbIncome.setChecked(true);
            }
        }
    }

    private void loadCategories() {
        RetrofitClient.getApi().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    updateCategorySpinner(getSelectedType());
                } else {
                    Toast.makeText(EditTransactionActivity.this, "Erro ao carregar categorias.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(EditTransactionActivity.this, "Falha ao carregar categorias.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCategorySpinner(String selectedType) {
        List<String> categoryNames = new ArrayList<>();

        for (Category category : categories) {
            if (category.getType() != null && category.getType().equalsIgnoreCase(selectedType)) {
                categoryNames.add(category.getName());
            }
        }

        if (categoryNames.isEmpty()) {
            categoryNames.add("Sem categoria");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        int selectedIndex = categoryNames.indexOf(initialCategory);
        if (selectedIndex >= 0) {
            spinnerCategory.setSelection(selectedIndex);
        }
    }

    private String getSelectedType() {
        return rbExpense.isChecked() ? "Despesa" : "Receita";
    }

    private void validateAndSave() {
        clearErrors();

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

        saveTransaction(description, amountText, dateText);
    }

    private void saveTransaction(String description, String amountText, String dateText) {
        if (transactionId == null) {
            Toast.makeText(this, "ID da transação inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        setProcessing(true, true);

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(Double.parseDouble(amountText.replace(",", ".")));
        transaction.setType(getSelectedType());
        transaction.setCategory(spinnerCategory.getSelectedItem() != null
                ? spinnerCategory.getSelectedItem().toString()
                : "Sem categoria");
        transaction.setDate(formatDateToApi(dateText));

        RetrofitClient.getApi().updateTransaction("eq." + transactionId, transaction).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                setProcessing(false, true);

                if (response.isSuccessful()) {
                    Toast.makeText(EditTransactionActivity.this, "Transação atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (response.code() == 401) {
                    Toast.makeText(EditTransactionActivity.this, "Sessão expirada.", Toast.LENGTH_SHORT).show();
                    SessionManager.clear();
                    finish();
                } else {
                    Toast.makeText(EditTransactionActivity.this, "Erro ao atualizar transação.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                setProcessing(false, true);
                Toast.makeText(EditTransactionActivity.this, "Falha de conexão.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Excluir transação")
                .setMessage("Deseja realmente excluir esta transação?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> deleteTransaction())
                .show();
    }

    private void deleteTransaction() {
        if (transactionId == null) {
            Toast.makeText(this, "ID da transação inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        setProcessing(true, false);

        RetrofitClient.getApi().deleteTransaction("eq." + transactionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                setProcessing(false, false);

                if (response.isSuccessful()) {
                    Toast.makeText(EditTransactionActivity.this, "Transação excluída com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (response.code() == 401) {
                    Toast.makeText(EditTransactionActivity.this, "Sessão expirada.", Toast.LENGTH_SHORT).show();
                    SessionManager.clear();
                    finish();
                } else {
                    Toast.makeText(EditTransactionActivity.this, "Erro ao excluir transação.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                setProcessing(false, false);
                Toast.makeText(EditTransactionActivity.this, "Falha de conexão.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setProcessing(boolean processing, boolean savingMode) {
        isProcessing = processing;

        etDescription.setEnabled(!processing);
        etAmount.setEnabled(!processing);
        etDate.setEnabled(!processing);
        rbIncome.setEnabled(!processing);
        rbExpense.setEnabled(!processing);
        spinnerCategory.setEnabled(!processing);
        btnSave.setEnabled(!processing);
        btnDelete.setEnabled(!processing);

        if (!processing) {
            btnSave.setText("Salvar alterações");
            btnDelete.setText("Excluir transação");
            return;
        }

        if (savingMode) {
            btnSave.setText("Salvando...");
            btnDelete.setText("Excluir transação");
        } else {
            btnSave.setText("Salvar alterações");
            btnDelete.setText("Excluindo...");
        }
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

    private String formatDateForDisplay(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return "";
        }

        String[] patterns = new String[]{
                "yyyy-MM-dd",
                "dd/MM/yyyy",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat input = new SimpleDateFormat(pattern, Locale.getDefault());
                input.setLenient(false);
                Date parsed = input.parse(rawDate);
                if (parsed != null) {
                    return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(parsed);
                }
            } catch (ParseException ignored) {
            }
        }

        return rawDate;
    }
}