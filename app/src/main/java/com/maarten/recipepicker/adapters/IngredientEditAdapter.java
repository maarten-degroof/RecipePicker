package com.maarten.recipepicker.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maarten.recipepicker.models.Ingredient;
import com.maarten.recipepicker.R;

import java.util.List;

public class IngredientEditAdapter extends RecyclerView.Adapter<IngredientEditAdapter.CustomViewHolder> {

    private Activity context;
    private List<Ingredient> ingredientList;

    public IngredientEditAdapter(Activity context, List<Ingredient> ingredientList){
        this.context = context;
        this.ingredientList = ingredientList;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomViewHolder(
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.ingredient_list_item_with_remove, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {
        final Ingredient ingredient = ingredientList.get(position);
        holder.ingredientListTextView.setText(ingredient.toString());

        holder.removeIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingredientList.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    static class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView ingredientListTextView;
        private ImageButton removeIngredientButton;
        private View parentView;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.parentView = itemView;
            this.ingredientListTextView = itemView.findViewById(R.id.ingredientListTextView);
            this.removeIngredientButton = itemView.findViewById(R.id.removeIngredientButton);
        }
    }
}
