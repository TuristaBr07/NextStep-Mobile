package com.tamarin.nextstep;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTransactionActivity extends AppCompatActivity {

    private TextInputEditText etDescription, etAmount;
    private Spinner spinnerCategory;
    private RadioGroup rgType;
    private Button btnSave, btnDelete;

    private Long transactionId;
    private String originalCategory;

    private List<Category> currentCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);

        // 1. Ligar componentes da interface
        etDescription = findViewById(R.id.etDescription);
        etAmount = findViewById(R.id.etAmount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        rgType = findViewById(R.id.rgType);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        // 2. Receber os dados da transação em que o utilizador clicou
        transactionId = getIntent().getLongExtra("EXTRA_ID", -1);
        String desc = getIntent().getStringExtra("EXTRA_DESC");
        double amount = getIntent().getDoubleExtra("EXTRA_AMOUNT", 0.0);
        String type = getIntent().getStringExtra("EXTRA_TYPE");
        originalCategory = getIntent().getStringExtra("EXTRA_CATEGORY");

        // Prevenção de erro: se não passar o ID, fecha o ecrã
        if (transactionId == -1) {
            Toast.makeText(this, "Erro ao carregar transação", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3. Preencher os campos no ecrã com os dados recebidos
        etDescription.setText(desc);
        etAmount.setText(String.valueOf(amount));

        if (type != null && (type.equalsIgnoreCase("income") || type.equalsIgnoreCase("Receita"))) {
            rgType.check(R.id.rbIncome);
        } else {
            rgType.check(R.id.rbExpense);
        }

        // 4. Carregar categorias para preencher o Spinner
        loadCategories();

        // Atualizar categorias caso o utilizador mude de Entrada para Saída
        rgType.setOnCheckedChangeListener((group, checkedId) -> loadCategories());

        // 5. Configurar os cliques dos Botões
        btnSave.setOnClickListener(v -> updateTransaction());
        btnDelete.setOnClickListener(v -> deleteTransaction());
    }

    private void loadCategories() {
        SupabaseApi api = RetrofitClient.getApi();
        api.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentCategories.clear();
                    int selectedId = rgType.getCheckedRadioButtonId();

                    // Filtra as categorias conforme o botão selecionado
                    for (Category c : response.body()) {
                        String cType = c.getType();
                        if (cType == null) {
                            currentCategories.add(c);
                        } else if (selectedId == R.id.rbIncome && (cType.equalsIgnoreCase("income") || cType.equalsIgnoreCase("Receita"))) {
                            currentCategories.add(c);
                        } else if (selectedId == R.id.rbExpense && (cType.equalsIgnoreCase("expense") || cType.equalsIgnoreCase("Despesa") || cType.equalsIgnoreCase("Saída"))) {
                            currentCategories.add(c);
                        }
                    }

                    ArrayAdapter<Category> adapter = new ArrayAdapter<>(EditTransactionActivity.this,
                            android.R.layout.simple_spinner_item, currentCategories);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(adapter);

                    // Tenta selecionar automaticamente a categoria original que veio da base de dados
                    if (originalCategory != null) {
                        for (int i = 0; i < currentCategories.size(); i++) {
                            if (currentCategories.get(i).getName().equalsIgnoreCase(originalCategory)) {
                                spinnerCategory.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(EditTransactionActivity.this, "Erro ao carregar categorias", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTransaction() {
        String description = etDescription.getText().toString();
        String amountStr = etAmount.getText().toString();

        if (description.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String type = (rgType.getCheckedRadioButtonId() == R.id.rbIncome) ? "income" : "expense";

        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        String categoryName = (selectedCategory != null) ? selectedCategory.getName() : originalCategory;

        String dateDb = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        Transaction t = new Transaction();
        t.setDescription(description);
        t.setAmount(amount);
        t.setType(type);
        t.setCategory(categoryName);
        t.setDate(dateDb);
        t.setUserId(SessionManager.getUserId());

        // O Supabase exige este formato "eq.ID" para encontrar a linha certa e atualizar
        String queryId = "eq." + transactionId;

        RetrofitClient.getApi().updateTransaction(queryId, t).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditTransactionActivity.this, "Atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish(); // Fecha e regressa ao Dashboard
                } else {
                    Toast.makeText(EditTransactionActivity.this, "Erro ao atualizar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Toast.makeText(EditTransactionActivity.this, "Falha de ligação", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteTransaction() {
        String queryId = "eq." + transactionId;

        RetrofitClient.getApi().deleteTransaction(queryId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditTransactionActivity.this, "Transação apagada!", Toast.LENGTH_SHORT).show();
                    finish(); // Fecha e regressa ao Dashboard
                } else {
                    Toast.makeText(EditTransactionActivity.this, "Erro ao apagar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditTransactionActivity.this, "Falha de ligação", Toast.LENGTH_SHORT).show();
            }
        });
    }
}