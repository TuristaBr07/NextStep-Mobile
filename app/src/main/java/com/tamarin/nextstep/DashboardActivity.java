package com.tamarin.nextstep;

import android.content.Intent; // IMPORTANTE: Para mudar de tela
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast; // Adicionado para exibir erros se precisar

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton; // IMPORTANTE: O botão redondo

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;

    // Componentes de KPI
    private TextView tvSaldo, tvReceita, tvDespesa;
    private ProgressBar pbMeiLimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Inicializar Componentes de Texto e Lista
        rvTransactions = findViewById(R.id.rvTransactions);
        tvSaldo = findViewById(R.id.tvSaldo);
        tvReceita = findViewById(R.id.tvReceita);
        tvDespesa = findViewById(R.id.tvDespesa);
        pbMeiLimit = findViewById(R.id.pbMeiLimit);

        // 2. Configurar Lista
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setHasFixedSize(true);

        // --- AQUI ESTÁ O CÓDIGO DO BOTÃO QUE FALTAVA ---
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        if (fabAdd != null) { // Verificação de segurança
            fabAdd.setOnClickListener(v -> {
                // Navega para a tela de Adicionar Transação
                Intent intent = new Intent(DashboardActivity.this, AddTransactionActivity.class);
                startActivity(intent);
            });
        }
        // -----------------------------------------------

        // NOTA: Eu removi o fetchTransactions() daqui do onCreate
        // e coloquei no onResume logo abaixo.
    }

    // --- O PULO DO GATO: ATUALIZAR AO VOLTAR ---
    @Override
    protected void onResume() {
        super.onResume();
        // Toda vez que a tela aparecer (ou você voltar da tela de cadastro),
        // ele vai buscar os dados novos no banco.
        fetchTransactions();
    }

    private void fetchTransactions() {
        // Chama a API criada
        RetrofitClient.getApi().getTransactions().enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // SUCESSO: Dados chegaram do banco!
                    transactionList = response.body();

                    // Configura o adaptador com a lista real
                    adapter = new TransactionAdapter(transactionList);
                    rvTransactions.setAdapter(adapter);

                    // Recalcula Saldo e Gráficos
                    calculateKPIs();
                } else {
                    // NOVO: Tratamento de Sessão Expirada (Erro 401)
                    if (response.code() == 401) {
                        Toast.makeText(DashboardActivity.this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_LONG).show();

                        // Limpa o token inválido usando o método exato do seu SessionManager
                        SessionManager.clear();

                        // Redireciona de volta para o Login
                        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Destrói o Dashboard para o usuário não conseguir voltar pelo botão de "Voltar" do celular
                    } else {
                        System.out.println("Erro na resposta: " + response.code());
                        Toast.makeText(DashboardActivity.this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                System.out.println("Falha na conexão: " + t.getMessage());
                Toast.makeText(DashboardActivity.this, "Sem conexão com internet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateKPIs() {
        if (transactionList == null) return;

        double totalReceita = 0;
        double totalDespesa = 0;

        // Soma os valores da lista
        for (Transaction tx : transactionList) {
            // Verifica se o tipo é null antes de comparar para evitar crash
            if (tx.getType() != null && (tx.getType().equalsIgnoreCase("Receita") || tx.getType().equalsIgnoreCase("income"))) {
                totalReceita += tx.getAmount();
            } else {
                totalDespesa += tx.getAmount();
            }
        }

        double saldo = totalReceita - totalDespesa;

        // Atualizar os Textos na Tela
        tvSaldo.setText(String.format(Locale.getDefault(), "R$ %.2f", saldo));
        tvReceita.setText(String.format(Locale.getDefault(), "R$ %.2f", totalReceita));
        tvDespesa.setText(String.format(Locale.getDefault(), "R$ %.2f", totalDespesa));

        // Lógica do Limite MEI (Teto R$ 81.000)
        double limiteMEI = 81000.0;
        int progresso = (int) ((totalReceita / limiteMEI) * 100);
        pbMeiLimit.setProgress(progresso);

        // Mudar cor da barra se estiver perigoso (Visual Extra)
        if (progresso > 80) {
            pbMeiLimit.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFFF0000)); // Vermelho
        } else {
            pbMeiLimit.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF2E7D32)); // Verde
        }
    }
}