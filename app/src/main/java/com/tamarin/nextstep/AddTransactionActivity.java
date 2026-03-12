package com.tamarin.nextstep;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// IMPORTAÇÃO DO TEXT INPUT DO MATERIAL DESIGN
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTransactionActivity extends AppCompatActivity {

    private Spinner spinnerType, spinnerCategory;
    private TextInputEditText etDescription, etAmount, etDate;
    private Button btnSaveTransaction;
    private List<Category> userCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        spinnerType = findViewById(R.id.spinnerType);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        etDescription = findViewById(R.id.etDescription);
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction);

        setupTypeSpinner();
        loadCategories();

        btnSaveTransaction.setOnClickListener(v -> saveTransaction());
    }

    private void setupTypeSpinner() {
        String[] types = {"Receita", "Despesa"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCategorySpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadCategories() {
        RetrofitClient.getApi().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userCategories = response.body();
                    updateCategorySpinner();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(AddTransactionActivity.this, "Erro ao carregar categorias", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCategorySpinner() {
        if (spinnerType.getSelectedItem() == null) return;

        String selectedType = spinnerType.getSelectedItem().toString();
        List<String> filteredCategories = new ArrayList<>();

        for (Category cat : userCategories) {
            if (cat.getType() != null && cat.getType().equalsIgnoreCase(selectedType)) {
                filteredCategories.add(cat.getName());
            }
        }

        if (filteredCategories.isEmpty()) {
            filteredCategories.add("Sem categorias");
        }

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filteredCategories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);
    }

    private void saveTransaction() {
        if (spinnerType.getSelectedItem() == null || spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(this, "Aguarde o carregamento das categorias", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = spinnerType.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        String date = etDate.getText() != null ? etDate.getText().toString().trim() : "";

        if (description.isEmpty() || amountStr.isEmpty() || date.isEmpty() || category.equals("Sem categorias")) {
            Toast.makeText(this, "Preencha todos os campos corretamente", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr.replace(",", "."));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Transaction newTransaction = new Transaction();
        newTransaction.setType(type);
        newTransaction.setCategory(category);
        newTransaction.setDescription(description);
        newTransaction.setAmount(amount);
        newTransaction.setDate(date);

        // Injetar o ID do utilizador logado para não dar erro no banco!
        newTransaction.setUserId(SessionManager.getUserId());

// Correção: Agora esperamos uma List<Transaction> como resposta
        RetrofitClient.getApi().createTransaction(newTransaction).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddTransactionActivity.this, "Transação salva!", Toast.LENGTH_SHORT).show();
                    finish(); // Fecha a tela e volta pro Dashboard
                } else {
                    Toast.makeText(AddTransactionActivity.this, "Erro ao salvar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Toast.makeText(AddTransactionActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }
}