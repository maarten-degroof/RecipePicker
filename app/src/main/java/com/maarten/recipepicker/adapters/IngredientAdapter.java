package com.maarten.recipepicker.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maarten.recipepicker.R;
import com.maarten.recipepicker.models.Ingredient;

import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.CustomViewHolder> {

    private Activity context;
    private List<Ingredient> ingredientList;

    public IngredientAdapter(Activity context, List<Ingredient> ingredientList){
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
                        .inflate(R.layout.ingredient_list_item_without_remove, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        final Ingredient ingredient = ingredientList.get(position);
        holder.ingredientListTextView.setText(ingredient.toString());
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

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.ingredientListTextView = itemView.findViewById(R.id.ingredientListTextView);
        }
    }
}
