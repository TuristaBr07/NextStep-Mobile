package com.tamarin.nextstep;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        // 1. Inicializar Componentes
        rvTransactions = findViewById(R.id.rvTransactions);
        tvSaldo = findViewById(R.id.tvSaldo);
        tvReceita = findViewById(R.id.tvReceita);
        tvDespesa = findViewById(R.id.tvDespesa);
        pbMeiLimit = findViewById(R.id.pbMeiLimit);

        // 2. Configurar Lista
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setHasFixedSize(true);

        // 3. Carregar Dados
        fetchTransactions();

    }

    private void fetchTransactions() {
        // Chama a API criada no passo anterior
        RetrofitClient.getApi().getTransactions().enqueue(new retrofit2.Callback<List<Transaction>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Transaction>> call, retrofit2.Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // SUCESSO: Dados chegaram do banco!
                    transactionList = response.body();

                    // Configura o adaptador com a lista real
                    adapter = new TransactionAdapter(transactionList);
                    rvTransactions.setAdapter(adapter);

                    // Recalcula Saldo e Gráficos
                    calculateKPIs();
                } else {
                    System.out.println("Erro na resposta: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Transaction>> call, Throwable t) {
                System.out.println("Falha na conexão: " + t.getMessage());
                // Dica: Se cair aqui, verifique sua internet ou a URL no RetrofitClient
            }
        });
    }

    private void calculateKPIs() {
        double totalReceita = 0;
        double totalDespesa = 0;

        // Soma os valores da lista
        for (Transaction tx : transactionList) {
            if ("Receita".equalsIgnoreCase(tx.getType())) {
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