package com.tamarin.nextstep;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private static final Locale LOCALE_PT_BR = new Locale("pt", "BR");

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

        String category = tx.getCategory() != null && !tx.getCategory().trim().isEmpty()
                ? tx.getCategory()
                : "Sem categoria";

        String date = formatDate(tx.getDate());
        Double amount = tx.getAmount() != null ? tx.getAmount() : 0.0;
        String type = tx.getType();

        holder.tvDescription.setText(description);
        holder.tvCategory.setText(category + " • " + date);
        holder.tvAmount.setText(formatCurrency(amount));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditTransactionActivity.class);
            intent.putExtra("EXTRA_ID", tx.getId());
            intent.putExtra("EXTRA_DESC", tx.getDescription());
            intent.putExtra("EXTRA_AMOUNT", tx.getAmount());
            intent.putExtra("EXTRA_TYPE", tx.getType());
            intent.putExtra("EXTRA_CATEGORY", tx.getCategory());
            intent.putExtra("EXTRA_DATE", tx.getDate());
            v.getContext().startActivity(intent);
        });

        boolean isExpense = type != null && (
                type.equalsIgnoreCase("expense") ||
                        type.equalsIgnoreCase("Despesa") ||
                        type.equalsIgnoreCase("Saída")
        );

        int amountColor = ContextCompat.getColor(
                holder.itemView.getContext(),
                isExpense ? R.color.ns_error : R.color.ns_success
        );

        holder.tvAmount.setTextColor(amountColor);
        holder.ivIcon.setImageResource(isExpense ? R.drawable.ic_expense : R.drawable.ic_income);
        holder.ivIcon.setColorFilter(amountColor);
        holder.ivIcon.setContentDescription(isExpense ? "Ícone de despesa" : "Ícone de receita");

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(adjustAlpha(amountColor, 0.12f));
        holder.iconContainer.setBackground(bg);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    private String formatCurrency(double value) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(LOCALE_PT_BR);
        return currencyFormat.format(value);
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return "--/--/----";
        }

        String[] patterns = new String[]{
                "yyyy-MM-dd",
                "dd/MM/yyyy",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat input = new SimpleDateFormat(pattern, Locale.getDefault());
                input.setLenient(false);
                Date parsed = input.parse(rawDate);
                if (parsed != null) {
                    return new SimpleDateFormat("dd/MM/yyyy", LOCALE_PT_BR).format(parsed);
                }
            } catch (ParseException ignored) {
            }
        }

        return rawDate;
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(android.graphics.Color.alpha(color) * factor);
        int red = android.graphics.Color.red(color);
        int green = android.graphics.Color.green(color);
        int blue = android.graphics.Color.blue(color);
        return android.graphics.Color.argb(alpha, red, green, blue);
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