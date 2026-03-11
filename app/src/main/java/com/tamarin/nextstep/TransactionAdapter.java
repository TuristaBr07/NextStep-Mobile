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

        // Define os textos básicos
        holder.tvDescription.setText(tx.getDescription());
        holder.tvCategory.setText(tx.getCategory() + " • " + tx.getDate());

        // Formatação de dinheiro (R$)
        holder.tvAmount.setText(String.format(Locale.getDefault(), "R$ %.2f", tx.getAmount()));

        // --- CORREÇÃO DA LÓGICA DE COR ---
        String type = tx.getType();

        // --- TORNAR A LINHA CLICÁVEL ---
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), EditTransactionActivity.class);

            // Passar os dados da transação para o ecrã de edição
            intent.putExtra("EXTRA_ID", tx.getId());
            intent.putExtra("EXTRA_DESC", tx.getDescription());
            intent.putExtra("EXTRA_AMOUNT", tx.getAmount());
            intent.putExtra("EXTRA_TYPE", tx.getType());
            intent.putExtra("EXTRA_CATEGORY", tx.getCategory());

            v.getContext().startActivity(intent);
        });

        // Verifica se é DESPESA aceitando Inglês (expense) e Português (Despesa/Saída)
        boolean isExpense = type != null && (
                type.equalsIgnoreCase("expense") ||
                        type.equalsIgnoreCase("Despesa") ||
                        type.equalsIgnoreCase("Saída")
        );

        if (isExpense) {
            // É Saída: Vermelho e Seta para Baixo
            holder.tvAmount.setTextColor(Color.parseColor("#D32F2F")); // Vermelho
            holder.ivIcon.setImageResource(android.R.drawable.arrow_down_float);
            holder.ivIcon.setColorFilter(Color.parseColor("#D32F2F"));
        } else {
            // É Entrada: Verde e Seta para Cima
            holder.tvAmount.setTextColor(Color.parseColor("#2E7D32")); // Verde
            holder.ivIcon.setImageResource(android.R.drawable.arrow_up_float);
            holder.ivIcon.setColorFilter(Color.parseColor("#2E7D32"));
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0; // Proteção extra contra lista nula
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