package com.maarten.recipepicker.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.maarten.recipepicker.R;
import com.maarten.recipepicker.models.FilterIngredient;

import java.util.List;

public class FilterIngredientsAdapter extends RecyclerView.Adapter<FilterIngredientsAdapter.CustomViewHolder> {

    private Activity context;
    private List<FilterIngredient> ingredientList;

    public FilterIngredientsAdapter(Activity context, List<FilterIngredient> ingredientList){
        this.context = context;
        this.ingredientList = ingredientList;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomViewHolder(
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.filter_ingredient_list_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final CustomViewHolder holder, final int position) {
        final FilterIngredient ingredient = ingredientList.get(position);

        holder.ingredientTextView.setText(ingredient.getName());

        holder.showIngredientState(position);

        holder.cardView.setOnClickListener(view -> {
            ingredient.setNextState();
            holder.showIngredientState(position);
        });
    }

    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView ingredientTextView;
        private MaterialCardView cardView;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            this.ingredientTextView = itemView.findViewById(R.id.ingredientTextView);
            this.cardView = itemView.findViewById(R.id.filterIngredientCardView);
        }

        private void showIngredientState(int position) {
            switch (ingredientList.get(position).getState()) {
                case -1:
                    this.cardView.setCardBackgroundColor(Color.parseColor("#FFD50000"));
                    this.ingredientTextView.setTextColor(Color.WHITE);
                    return;

                case 1:
                    this.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primaryLightColor));
                    this.ingredientTextView.setTextColor(Color.WHITE);
                    return;

                default:
                    this.cardView.setCardBackgroundColor(Color.WHITE);
                    this.ingredientTextView.setTextColor(Color.BLACK);
            }
        }
    }
}
