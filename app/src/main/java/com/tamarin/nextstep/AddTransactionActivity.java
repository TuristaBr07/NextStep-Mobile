package com.tamarin.nextstep;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
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
import java.util.Calendar;
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
        setupDateField();
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

        spinnerType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateCategorySpinner();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // No-op
            }
        });
    }

    private void setupDateField() {
        etDate.setFocusable(true);
        etDate.setFocusableInTouchMode(true);
        etDate.setClickable(true);
        etDate.setLongClickable(false);
        etDate.setCursorVisible(false);
        etDate.setKeyListener(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            etDate.setShowSoftInputOnFocus(false);
        }

        etDate.setOnClickListener(v -> showDatePicker());

        etDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && etDate.isEnabled()) {
                showDatePicker();
            }
        });

        etDate.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP
                    && (keyCode == KeyEvent.KEYCODE_ENTER
                    || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                    || keyCode == KeyEvent.KEYCODE_SPACE)) {
                showDatePicker();
                return true;
            }
            return false;
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
                    String formatted = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            dayOfMonth,
                            month + 1,
                            year
                    );
                    etDate.setText(formatted);
                    tilDate.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void loadCategories() {
        RetrofitClient.getApi().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    updateCategorySpinner();
                } else {
                    Toast.makeText(
                            AddTransactionActivity.this,
                            getString(R.string.error_loading_categories),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(
                        AddTransactionActivity.this,
                        getString(R.string.error_connection_loading_categories),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void updateCategorySpinner() {
        String selectedType = spinnerType.getSelectedItem() != null
                ? spinnerType.getSelectedItem().toString()
                : getString(R.string.transaction_type_income);

        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            // TRAVA DE SEGURANÇA: Só adiciona se o tipo bater E o nome não for nulo nem vazio
            if (category.getType() != null && category.getType().equalsIgnoreCase(selectedType)) {
                if (category.getName() != null && !category.getName().trim().isEmpty()) {
                    categoryNames.add(category.getName());
                }
            }
        }

        if (categoryNames.isEmpty()) {
            categoryNames.add(getString(R.string.no_category));
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
            tilDescription.setError(getString(R.string.error_transaction_description_required));
            hasError = true;
        }

        if (TextUtils.isEmpty(amountText)) {
            tilAmount.setError(getString(R.string.error_transaction_amount_required));
            hasError = true;
        } else {
            try {
                double amount = Double.parseDouble(amountText.replace(",", "."));
                if (amount <= 0) {
                    tilAmount.setError(getString(R.string.error_transaction_amount_positive));
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilAmount.setError(getString(R.string.error_transaction_amount_invalid));
                hasError = true;
            }
        }

        if (TextUtils.isEmpty(dateText)) {
            tilDate.setError(getString(R.string.error_transaction_date_required));
            hasError = true;
        } else if (!isValidDate(dateText)) {
            tilDate.setError(getString(R.string.error_transaction_date_format));
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

        RetrofitClient.getApi().createTransaction(transaction).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AddTransactionActivity.this, "Transação salva com sucesso!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddTransactionActivity.this, "Erro ao salvar: " + response.code(), Toast.LENGTH_SHORT).show();
                    setLoading(false);
                }
            }

            @Override
            public void onFailure(Call<Transaction> call, Throwable t) {
                Toast.makeText(AddTransactionActivity.this, "Falha na rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                setLoading(false);
            }
        });
    }

    private void setLoading(boolean loading) {
        isSaving = loading;
        btnSaveTransaction.setEnabled(!loading);
        btnSaveTransaction.setText(
                loading
                        ? getString(R.string.transaction_saving)
                        : getString(R.string.save_transaction)
        );

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