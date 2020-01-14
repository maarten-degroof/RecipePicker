package com.maarten.recipepicker.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.maarten.recipepicker.R;
import com.maarten.recipepicker.RecipePickerApplication;
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
                        .inflate(R.layout.ingredient_list_item_with_checkbox, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final CustomViewHolder holder, final int position) {
        final FilterIngredient ingredient = ingredientList.get(position);

        holder.ingredientCheckBox.setText(ingredient.getName());
        holder.ingredientCheckBox.setChecked(ingredient.isChecked());

        holder.ingredientCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ingredient.toggleIsChecked();
                Toast.makeText(RecipePickerApplication.getAppContext(), "toggled item: " + position + " "+ ingredient.getName() + ingredientList.get(position).isChecked(), Toast.LENGTH_LONG).show();
            }
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


    class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private MaterialCheckBox ingredientCheckBox;
        private View parentView;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.parentView = itemView;
            this.ingredientCheckBox = itemView.findViewById(R.id.ingredientCheckBox);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            if (ingredientList.get(adapterPosition).isChecked()) {
                ingredientCheckBox.setChecked(false);
            }
            else  {
                ingredientCheckBox.setChecked(true);
            }
            ingredientList.get(adapterPosition).toggleIsChecked();
        }
    }
}
