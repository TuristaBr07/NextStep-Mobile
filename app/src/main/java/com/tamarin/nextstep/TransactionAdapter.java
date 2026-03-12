package com.tamarin.nextstep;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<Transaction> list;

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

        String description = tx.getDescription() != null && !tx.getDescription().trim().isEmpty()
                ? tx.getDescription()
                : "Transação sem descrição";

        String category = tx.getCategory() != null ? tx.getCategory() : "Sem categoria";
        String date = tx.getDate() != null ? tx.getDate() : "--/--/----";
        Double amount = tx.getAmount() != null ? tx.getAmount() : 0.0;
        String type = tx.getType();

        holder.tvDescription.setText(description);
        holder.tvCategory.setText(category + " • " + date);
        holder.tvAmount.setText(String.format(Locale.getDefault(), "R$ %.2f", amount));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditTransactionActivity.class);
            intent.putExtra("EXTRA_ID", tx.getId());
            intent.putExtra("EXTRA_DESC", tx.getDescription());
            intent.putExtra("EXTRA_AMOUNT", tx.getAmount());
            intent.putExtra("EXTRA_TYPE", tx.getType());
            intent.putExtra("EXTRA_CATEGORY", tx.getCategory());
            v.getContext().startActivity(intent);
        });

        boolean isExpense = type != null && (
                type.equalsIgnoreCase("expense") ||
                        type.equalsIgnoreCase("Despesa") ||
                        type.equalsIgnoreCase("Saída")
        );

        if (isExpense) {
            holder.tvAmount.setTextColor(Color.parseColor("#C62828"));
            holder.ivIcon.setImageResource(R.drawable.ic_expense);
            holder.ivIcon.setColorFilter(Color.parseColor("#C62828"));
            holder.iconContainer.setBackgroundResource(R.drawable.bg_soft_pill);
            holder.ivIcon.setContentDescription("Ícone de despesa");
        } else {
            holder.tvAmount.setTextColor(Color.parseColor("#2E7D32"));
            holder.ivIcon.setImageResource(R.drawable.ic_income);
            holder.ivIcon.setColorFilter(Color.parseColor("#2E7D32"));
            holder.iconContainer.setBackgroundResource(R.drawable.bg_soft_pill);
            holder.ivIcon.setContentDescription("Ícone de receita");
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvCategory, tvAmount;
        ImageView ivIcon;
        LinearLayout iconContainer;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            iconContainer = itemView.findViewById(R.id.iconContainer);
        }
    }
}