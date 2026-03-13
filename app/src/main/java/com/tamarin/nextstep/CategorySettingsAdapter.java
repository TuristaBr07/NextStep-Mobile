package com.tamarin.nextstep;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategorySettingsAdapter extends RecyclerView.Adapter<CategorySettingsAdapter.ViewHolder> {

    private final List<Category> categories;
    private final OnCategoryDeleteListener deleteListener;

    public interface OnCategoryDeleteListener {
        void onDeleteClick(Category category);
    }

    public CategorySettingsAdapter(List<Category> categories, OnCategoryDeleteListener deleteListener) {
        this.categories = categories;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category cat = categories.get(position);

        holder.tvCatName.setText(cat.getName());

        boolean isIncome = "Receita".equalsIgnoreCase(cat.getType());
        int accentColor = ContextCompat.getColor(
                holder.itemView.getContext(),
                isIncome ? R.color.ns_success : R.color.ns_error
        );

        holder.tvCatType.setText(isIncome ? "Receita" : "Despesa");
        holder.tvCatType.setTextColor(accentColor);

        GradientDrawable pill = new GradientDrawable();
        pill.setCornerRadius(999f);
        pill.setColor(adjustAlpha(accentColor, 0.12f));
        pill.setStroke(1, adjustAlpha(accentColor, 0.35f));
        holder.tvCatType.setBackground(pill);
        holder.tvCatType.setPadding(dp(holder, 10), dp(holder, 4), dp(holder, 10), dp(holder, 4));

        holder.ivDeleteCat.setOnClickListener(v -> deleteListener.onDeleteClick(cat));
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(android.graphics.Color.alpha(color) * factor);
        int red = android.graphics.Color.red(color);
        int green = android.graphics.Color.green(color);
        int blue = android.graphics.Color.blue(color);
        return android.graphics.Color.argb(alpha, red, green, blue);
    }

    private int dp(ViewHolder holder, int value) {
        return Math.round(value * holder.itemView.getResources().getDisplayMetrics().density);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCatName, tvCatType;
        ImageView ivDeleteCat;

        public ViewHolder(View itemView) {
            super(itemView);
            tvCatName = itemView.findViewById(R.id.tvCatName);
            tvCatType = itemView.findViewById(R.id.tvCatType);
            ivDeleteCat = itemView.findViewById(R.id.ivDeleteCat);
        }
    }
}