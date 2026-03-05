package com.tamarin.nextstep;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DetalheMeiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_mei);

        // 1. Receber o objeto que veio da outra tela
        Mei mei = (Mei) getIntent().getSerializableExtra("mei_selecionado");

        // 2. Vincular componentes
        TextView tvNome = findViewById(R.id.tvDetalheNome);
        TextView tvCnpj = findViewById(R.id.tvDetalheCnpj);
        TextView tvRisco = findViewById(R.id.tvDetalheRisco);
        TextView tvFaturamento = findViewById(R.id.tvDetalheFaturamento);
        Button btnVoltar = findViewById(R.id.btnVoltar);

        // 3. Preencher a tela com os dados recebidos
        if (mei != null) {
            tvNome.setText(mei.getNome());
            tvCnpj.setText("CNPJ: " + mei.getCnpj());
            tvRisco.setText(mei.getStatusRisco());
            tvFaturamento.setText("Faturamento: R$ " + mei.getFaturamentoAtual());
        }

        // 4. Botão Voltar
        btnVoltar.setOnClickListener(v -> finish());
    }
}