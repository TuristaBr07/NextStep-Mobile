package com.tamarin.nextstep;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CategorySettingsAdapter extends RecyclerView.Adapter<CategorySettingsAdapter.ViewHolder> {

    private List<Category> categories;
    private OnCategoryDeleteListener deleteListener;

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
        holder.tvCatType.setText(cat.getType());

        // Define a cor baseada no tipo para ficar bonito
        if ("Receita".equalsIgnoreCase(cat.getType())) {
            holder.tvCatType.setTextColor(0xFF2E7D32); // Verde
        } else {
            holder.tvCatType.setTextColor(0xFFD32F2F); // Vermelho
        }

        holder.ivDeleteCat.setOnClickListener(v -> deleteListener.onDeleteClick(cat));
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
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