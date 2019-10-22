package com.maarten.recipepicker.Adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.maarten.recipepicker.R;
import com.maarten.recipepicker.Recipe;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends BaseAdapter implements Filterable {

    private Activity context;
    private List<Recipe> recipeList;
    private static LayoutInflater inflater = null;

    public FavoriteAdapter(Activity context, List<Recipe> recipeList){
        this.context = context;
        this.recipeList = recipeList;
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

        FavoriteAdapter.this.getFilter().filter("");

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
    public Filter getFilter() {

        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                recipeList  = (List<Recipe>) results.values;
                notifyDataSetChanged();

            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                ArrayList<Recipe> filteredArray = new ArrayList<>();

                // perform your search here using the searchConstraint String.

                //constraint = constraint.toString().toLowerCase();

                for (int i = 0; i < recipeList.size(); i++) {
                    Recipe tempRecipe = recipeList.get(i);
                    if(tempRecipe.getFavorite()) {
                        filteredArray.add(tempRecipe);
                    }
                }

                results.count = filteredArray.size();
                results.values = filteredArray;
                Log.e("VALUE", "count: " + results.count);

                return results;
            }
        };

        return filter;
    }
}