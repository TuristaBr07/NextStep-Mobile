package com.tamarin.nextstep;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;
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

    private MaterialButtonToggleGroup toggleGroupType;
    private MaterialButtonToggleGroup toggleGroupStatus;
    private AutoCompleteTextView actvCategory;
    private TextInputEditText etDescription, etAmount, etDate;
    private TextInputLayout tilCategory, tilDescription, tilAmount, tilDate;
    private Button btnSaveTransaction;

    private List<Category> categories = new ArrayList<>();
    private boolean isSaving = false;

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
        setContentView(R.layout.activity_add_transaction);

        toggleGroupType = findViewById(R.id.toggleGroupType);
        toggleGroupStatus = findViewById(R.id.toggleGroupStatus);
        actvCategory = findViewById(R.id.actvCategory);
        etDescription = findViewById(R.id.etDescription);
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        tilCategory = findViewById(R.id.tilCategory);
        tilDescription = findViewById(R.id.tilDescription);
        tilAmount = findViewById(R.id.tilAmount);
        tilDate = findViewById(R.id.tilDate);
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction);

        setupTypeToggle();
        setupStatusToggle();
        setupDateField();
        applyPresetType();
        loadCategories();

        btnSaveTransaction.setOnClickListener(v -> {
            if (!isSaving) {
                validateAndSave();
            }
        });
    }

    private void setupTypeToggle() {
        toggleGroupType.check(R.id.btnTypeIncome);
        toggleGroupType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                updateCategoryDropdown();
            }
        });
    }

    private void applyPresetType() {
        String preset = getIntent().getStringExtra("preset_type");
        if ("expense".equals(preset)) {
            toggleGroupType.check(R.id.btnTypeExpense);
        } else {
            toggleGroupType.check(R.id.btnTypeIncome);
        }
    }

    private void setupStatusToggle() {
        toggleGroupStatus.check(R.id.btnStatusPaid);
    }

    private String getSelectedStatus() {
        return toggleGroupStatus.getCheckedButtonId() == R.id.btnStatusPending
                ? "PENDENTE"
                : "PAGO";
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
        track(RetrofitClient.getApi().getCategories()).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    updateCategoryDropdown();
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
                if (call.isCanceled() || isFinishing() || isDestroyed()) return;
                Toast.makeText(
                        AddTransactionActivity.this,
                        getString(R.string.error_connection_loading_categories),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void updateCategoryDropdown() {
        String selectedType = getSelectedType();

        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            if (category.getType() != null && category.getType().equalsIgnoreCase(selectedType)) {
                if (category.getName() != null && !category.getName().trim().isEmpty()) {
                    categoryNames.add(category.getName());
                }
            }
        }

        if (categoryNames.isEmpty()) {
            categoryNames.add(getString(R.string.no_category));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categoryNames
        );
        actvCategory.setAdapter(adapter);
        actvCategory.setText(categoryNames.get(0), false);
        tilCategory.setError(null);
    }

    private String getSelectedType() {
        return toggleGroupType.getCheckedButtonId() == R.id.btnTypeExpense
                ? getString(R.string.transaction_type_expense)
                : getString(R.string.transaction_type_income);
    }

    private void validateAndSave() {
        clearErrors();

        String type = getSelectedType();
        String category = actvCategory.getText() != null ? actvCategory.getText().toString().trim() : "";
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

        saveTransaction(type, category, description, amountText, dateText, getSelectedStatus());
    }

    private void saveTransaction(String type, String category, String description, String amountText, String dateText, String status) {
        setLoading(true);

        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setDescription(description);
        transaction.setAmount(Double.parseDouble(amountText.replace(",", ".")));
        transaction.setDate(formatDateToApi(dateText));
        transaction.setStatus(status);
        transaction.setUserId(SessionManager.getUserId());

        track(RetrofitClient.getApi().createTransaction(transaction)).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AddTransactionActivity.this, getString(R.string.transaction_created_success), Toast.LENGTH_SHORT).show();
                    DashboardActivity.forceRefresh();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddTransactionActivity.this, getString(R.string.error_save_transaction_code, response.code()), Toast.LENGTH_SHORT).show();
                    setLoading(false);
                }
            }

            @Override
            public void onFailure(Call<Transaction> call, Throwable t) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) return;
                Toast.makeText(AddTransactionActivity.this, getString(R.string.error_network_save, t.getMessage()), Toast.LENGTH_SHORT).show();
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

        for (int i = 0; i < toggleGroupType.getChildCount(); i++) {
            toggleGroupType.getChildAt(i).setEnabled(!loading);
        }
        for (int i = 0; i < toggleGroupStatus.getChildCount(); i++) {
            toggleGroupStatus.getChildAt(i).setEnabled(!loading);
        }
        actvCategory.setEnabled(!loading);
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