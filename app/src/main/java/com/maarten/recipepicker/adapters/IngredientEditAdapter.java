package com.maarten.recipepicker.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.maarten.recipepicker.Ingredient;
import com.maarten.recipepicker.R;

import java.util.List;

public class IngredientEditAdapter extends BaseAdapter {

    private Activity context;
    private List<Ingredient> ingredientList;
    private static LayoutInflater inflater = null;

    public IngredientEditAdapter(Activity context, List<Ingredient> ingredientList){
        this.context = context;
        this.ingredientList = ingredientList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return ingredientList.size();
    }

    @Override
    public Ingredient getItem(int position) {
        return ingredientList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        itemView = (itemView == null) ? inflater.inflate(R.layout.ingredient_list_item_with_remove, null): itemView;
        TextView textViewIngredient = itemView.findViewById(R.id.ingredientListTextView);
        Ingredient selectedIngredient = ingredientList.get(position);
        textViewIngredient.setText(selectedIngredient.toString());

        // the remove button
        ImageButton removeButton = itemView.findViewById(R.id.removeIngredientButton);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingredientList.remove(position);
                notifyDataSetChanged();
            }
        });

        return itemView;
    }









}
