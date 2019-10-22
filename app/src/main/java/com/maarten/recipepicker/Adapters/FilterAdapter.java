package com.maarten.recipepicker.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.maarten.recipepicker.CookTime;
import com.maarten.recipepicker.R;
import com.maarten.recipepicker.Recipe;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FilterAdapter extends BaseAdapter implements Filterable {

    private Activity context;
    private List<Recipe> recipeList;
    private List<Recipe> filteredList;
    private static LayoutInflater inflater = null;

    public FilterAdapter(Activity context, List<Recipe> recipeList){
        this.context = context;
        this.recipeList = recipeList;
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

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

        final Filter filter = new Filter() {

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

                int filterMin, filterMax;
                Boolean durationShort, durationMedium, durationLong;

                try {
                    JSONObject jsonObject = new JSONObject(constraint.toString());
                    filterMin = (int) jsonObject.get("filterMin");
                    filterMax = (int) jsonObject.get("filterMax");
                    durationShort = (Boolean) jsonObject.get("durationShort");
                    durationMedium = (Boolean) jsonObject.get("durationMedium");
                    durationLong = (Boolean) jsonObject.get("durationLong");

                    // perform your search here using the searchConstraint String.

                    for (int i = 0; i < recipeList.size(); i++) {
                        Recipe tempRecipe = recipeList.get(i);
                        if(tempRecipe.getAmountCooked() >= filterMin && tempRecipe.getAmountCooked() <= filterMax) {
                            // you didn't give a duration filter
                            if(!durationShort && !durationMedium && !durationLong) {
                                filteredArray.add(tempRecipe);
                            }
                            // You gave the filter && the recipe has the filter
                            else if (durationShort && tempRecipe.getCookTime() == CookTime.SHORT) {
                                filteredArray.add(tempRecipe);
                            }
                            else if (durationMedium && tempRecipe.getCookTime() == CookTime.MEDIUM) {
                                filteredArray.add(tempRecipe);
                            }
                            else if (durationLong && tempRecipe.getCookTime() == CookTime.LONG) {
                                filteredArray.add(tempRecipe);
                            }

                        }
                    }

                    results.count = filteredArray.size();
                    results.values = filteredArray;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return results;
            }
        };

        return filter;
    }
}