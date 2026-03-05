package com.tamarin.nextstep;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MeiAdapter extends RecyclerView.Adapter<MeiAdapter.MeiViewHolder> {

    private List<Mei> meiList;

    public MeiAdapter(List<Mei> meiList) {
        this.meiList = meiList;
    }

    @NonNull
    @Override
    public MeiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mei, parent, false);
        return new MeiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeiViewHolder holder, int position) {
        Mei mei = meiList.get(position);

        holder.tvNomeEmpresa.setText(mei.getNome());
        holder.tvCnpj.setText(mei.getCnpj());
        holder.tvRisco.setText(mei.getStatusRisco());

        // Cores do Badge
        if (mei.getStatusRisco().equals("Alto")) {
            holder.tvRisco.setTextColor(0xFFD32F2F);
            holder.tvRisco.setBackgroundColor(0xFFFFEBEE);
        } else {
            holder.tvRisco.setTextColor(0xFF2E7D32);
            holder.tvRisco.setBackgroundColor(0xFFE8F5E9);
        }

        // --- NOVIDADE: CLIQUE NO ITEM ---
        holder.itemView.setOnClickListener(v -> {
            // Cria a intenção de ir para a tela de Detalhes
            Intent intent = new Intent(v.getContext(), DetalheMeiActivity.class);

            // "Anexa" o objeto MEI na mala para viagem
            intent.putExtra("mei_selecionado", mei);

            // Inicia a viagem
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return meiList.size();
    }

    static class MeiViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomeEmpresa, tvCnpj, tvRisco;

        public MeiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeEmpresa = itemView.findViewById(R.id.tvNomeEmpresa);
            tvCnpj = itemView.findViewById(R.id.tvCnpj);
            tvRisco = itemView.findViewById(R.id.tvRisco);
        }
    }
}