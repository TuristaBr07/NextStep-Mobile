package com.tamarin.nextstep;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> list;

    public TransactionAdapter(List<Transaction> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction tx = list.get(position);

        holder.tvDescription.setText(tx.getDescription());
        holder.tvCategory.setText(tx.getCategory() + " • " + tx.getDate());

        // Formatação de dinheiro (R$)
        holder.tvAmount.setText(String.format(Locale.getDefault(), "R$ %.2f", tx.getAmount()));

        // Lógica Visual: Receita (Verde) vs Despesa (Vermelho)
        if ("Despesa".equalsIgnoreCase(tx.getType())) {
            holder.tvAmount.setTextColor(Color.parseColor("#D32F2F")); // Vermelho
            holder.ivIcon.setImageResource(android.R.drawable.arrow_down_float);
            holder.ivIcon.setColorFilter(Color.parseColor("#D32F2F"));
        } else {
            holder.tvAmount.setTextColor(Color.parseColor("#2E7D32")); // Verde
            holder.ivIcon.setImageResource(android.R.drawable.arrow_up_float);
            holder.ivIcon.setColorFilter(Color.parseColor("#2E7D32"));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvCategory, tvAmount;
        ImageView ivIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}