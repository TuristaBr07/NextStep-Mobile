package com.tamarin.nextstep;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView rvMeis;
    private MeiAdapter adapter;
    private List<Mei> listaMeis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Inicializar componentes
        rvMeis = findViewById(R.id.rvMeis);
        TextView tvTotalMeis = findViewById(R.id.tvTotalMeis);

        // 2. Configurar a RecyclerView (Performance)
        rvMeis.setLayoutManager(new LinearLayoutManager(this));
        rvMeis.setHasFixedSize(true);

        // 3. Criar dados Fictícios (Mock Data)
        listaMeis = new ArrayList<>();
        listaMeis.add(new Mei("Padaria do João", "12.345.678/0001-90", 78000.00, "Médio"));
        listaMeis.add(new Mei("Maria Manicure", "98.765.432/0001-10", 35000.00, "Baixo"));
        listaMeis.add(new Mei("Oficina do Pedro", "45.678.901/0001-22", 82000.00, "Alto"));
        listaMeis.add(new Mei("Mercadinho da Esquina", "11.222.333/0001-44", 60000.00, "Baixo"));
        listaMeis.add(new Mei("Tech Soluções", "55.444.333/0001-99", 120000.00, "Alto"));

        // 4. Ligar o Adaptador
        adapter = new MeiAdapter(listaMeis);
        rvMeis.setAdapter(adapter);

        // 5. Atualizar o contador no card de resumo
        tvTotalMeis.setText(String.valueOf(listaMeis.size()));
    }
}