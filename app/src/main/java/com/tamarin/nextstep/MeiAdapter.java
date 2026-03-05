package com.tamarin.nextstep;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MeiAdapter extends RecyclerView.Adapter<MeiAdapter.MeiViewHolder> {

    private List<Mei> meiList;

    // Construtor: Recebe a lista de dados
    public MeiAdapter(List<Mei> meiList) {
        this.meiList = meiList;
    }

    // 1. Cria o visual do cartão (infla o layout)
    @NonNull
    @Override
    public MeiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mei, parent, false);
        return new MeiViewHolder(view);
    }

    // 2. Preenche os dados no cartão
    @Override
    public void onBindViewHolder(@NonNull MeiViewHolder holder, int position) {
        Mei mei = meiList.get(position);

        holder.tvNomeEmpresa.setText(mei.getNome());
        holder.tvCnpj.setText(mei.getCnpj());
        holder.tvRisco.setText(mei.getStatusRisco());

        // Bônus: Mudar cor do badge baseada no risco
        if (mei.getStatusRisco().equals("Alto")) {
            holder.tvRisco.setTextColor(0xFFD32F2F); // Vermelho
            holder.tvRisco.setBackgroundColor(0xFFFFEBEE); // Fundo Vermelho Claro
        } else {
            holder.tvRisco.setTextColor(0xFF2E7D32); // Verde
            holder.tvRisco.setBackgroundColor(0xFFE8F5E9); // Fundo Verde Claro
        }
    }

    // 3. Conta quantos itens tem na lista
    @Override
    public int getItemCount() {
        return meiList.size();
    }

    // A classe que "segura" os componentes da tela
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