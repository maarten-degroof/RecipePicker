package com.maarten.recipepicker.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.maarten.recipepicker.R;
import com.maarten.recipepicker.Recipe;

import java.util.List;

public class RecipeAdapter extends BaseAdapter {

    private Activity context;
    private List<Recipe> recipeList;
    private static LayoutInflater inflater = null;

    public RecipeAdapter(Activity context, List<Recipe> recipeList){
        this.context = context;
        this.recipeList = recipeList;
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return recipeList.size();
    }

    @Override
    public Recipe getItem(int position) {
        return recipeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        itemView = (itemView == null) ? inflater.inflate(R.layout.list_item, null): itemView;
        TextView textViewTitle = itemView.findViewById(R.id.textViewTitle);
        Recipe selectedRecipe = recipeList.get(position);
        textViewTitle.setText(selectedRecipe.getTitle());
        return itemView;
    }









}
