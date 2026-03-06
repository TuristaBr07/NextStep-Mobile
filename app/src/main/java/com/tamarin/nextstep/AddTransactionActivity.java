package com.tamarin.nextstep;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTransactionActivity extends AppCompatActivity {

    private TextInputEditText etDescription, etAmount, etDate;
    private Spinner spinnerCategory;
    private RadioGroup rgType;
    private Button btnSave;

    // Listas separadas para organizar o Spinner
    private List<Category> allCategories = new ArrayList<>();
    private List<Category> incomeCategories = new ArrayList<>();
    private List<Category> expenseCategories = new ArrayList<>();

    private final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // 1. Ligar Componentes
        etDescription = findViewById(R.id.etDescription);
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        rgType = findViewById(R.id.rgType);
        btnSave = findViewById(R.id.btnSave);

        // 2. Configurar Data
        setupDatePicker();

        // 3. Buscar Categorias e Configurar o filtro dinâmico
        loadCategories();

        // 4. Ouvinte: Quando trocar Entrada/Saída, atualiza o Spinner
        rgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateSpinner(); // Mágica acontece aqui
            }
        });

        // 5. Salvar
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void setupDatePicker() {
        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        etDate.setOnClickListener(v -> new DatePickerDialog(AddTransactionActivity.this, date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("pt", "BR"));
        etDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void loadCategories() {
        SupabaseApi api = RetrofitClient.getApi();
        Call<List<Category>> call = api.getCategories();

        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allCategories = response.body();

                    // Limpa as listas
                    incomeCategories.clear();
                    expenseCategories.clear();

                    // Mágica: Filtro A Prova de Falhas
                    for (Category c : allCategories) {
                        String type = c.getType();

                        // Se o tipo for nulo, adiciona em TUDO para garantir que aparece
                        if (type == null) {
                            incomeCategories.add(c);
                            expenseCategories.add(c);
                            continue;
                        }

                        // Aceita inglês (income) OU português (Receita)
                        if (type.equalsIgnoreCase("income") || type.equalsIgnoreCase("Receita")) {
                            incomeCategories.add(c);
                        }
                        // Aceita inglês (expense) OU português (Despesa)
                        else if (type.equalsIgnoreCase("expense") || type.equalsIgnoreCase("Despesa")) {
                            expenseCategories.add(c);
                        }
                        // Se for um tipo estranho, adiciona em ambos por segurança
                        else {
                            incomeCategories.add(c);
                            expenseCategories.add(c);
                        }
                    }

                    // Log de depuração (aparece no Logcat) para sabermos o que aconteceu
                    System.out.println("Categorias carregadas: " + allCategories.size());
                    System.out.println("Entradas: " + incomeCategories.size());
                    System.out.println("Saídas: " + expenseCategories.size());

                    // Verifica se as listas ficaram vazias
                    if (incomeCategories.isEmpty() && expenseCategories.isEmpty()) {
                        Toast.makeText(AddTransactionActivity.this, "Aviso: Nenhuma categoria encontrada no banco!", Toast.LENGTH_LONG).show();
                    }

                    // Atualiza o Spinner com a seleção atual
                    updateSpinner();

                } else {
                    Toast.makeText(AddTransactionActivity.this, "Erro ao carregar categorias: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(AddTransactionActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método que troca as opções do Spinner
    private void updateSpinner() {
        int selectedId = rgType.getCheckedRadioButtonId();
        List<Category> filteredList;

        if (selectedId == R.id.rbIncome) {
            filteredList = incomeCategories;
        } else {
            filteredList = expenseCategories;
        }

        ArrayAdapter<Category> adapter = new ArrayAdapter<>(AddTransactionActivity.this,
                android.R.layout.simple_spinner_item, filteredList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void saveTransaction() {
        String description = etDescription.getText().toString();
        String amountStr = etAmount.getText().toString();

        if (description.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        // CORREÇÃO CRÍTICA: Lógica segura baseada no ID do botão, não no texto
        int selectedId = rgType.getCheckedRadioButtonId();
        String type;
        if (selectedId == R.id.rbIncome) {
            type = "income"; // Receita
        } else {
            type = "expense"; // Despesa
        }

        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        if (selectedCategory == null) {
            Toast.makeText(this, "Selecione uma categoria válida", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateDb = dbFormat.format(myCalendar.getTime());

        Transaction t = new Transaction();
        t.setDescription(description);
        t.setAmount(amount);
        t.setType(type); // Agora vai certo!
        t.setCategory(selectedCategory.getName()); // Salva o nome
        // Dica: Se no futuro o Back-end pedir o ID da categoria, use: t.setCategoryId(selectedCategory.getId());
        t.setDate(dateDb);
        t.setUserId(SessionManager.getUserId());

        SupabaseApi api = RetrofitClient.getApi();
        Call<List<Transaction>> call = api.createTransaction(t);

        call.enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddTransactionActivity.this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddTransactionActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Toast.makeText(AddTransactionActivity.this, "Falha ao salvar: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}