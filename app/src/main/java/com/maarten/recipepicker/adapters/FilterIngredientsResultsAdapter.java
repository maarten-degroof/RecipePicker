package com.maarten.recipepicker.adapters;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.maarten.recipepicker.R;
import com.maarten.recipepicker.ViewRecipeActivity;
import com.maarten.recipepicker.models.Ingredient;
import com.maarten.recipepicker.models.Recipe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FilterIngredientsResultsAdapter extends RecyclerView.Adapter<FilterIngredientsResultsAdapter.CustomViewHolder> {

    private Activity context;
    private List<Recipe> recipeList;

    private int returnCount = 0;

    public FilterIngredientsResultsAdapter(Activity context, List<Recipe> recipeList){
        this.context = context;
        this.recipeList = recipeList;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomViewHolder(
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.recipe_list_item_card, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final CustomViewHolder holder, int position) {
        final Recipe recipe = recipeList.get(position);
        holder.recipeTitleTextView.setText(recipe.getTitle());
        holder.recipeIngredientsTextView.setText(recipe.getOrderedIngredientString());

        if(recipe.getRating() == 0) {
            holder.recipeRatingTextView.setVisibility(View.GONE);
        } else {
            holder.recipeRatingTextView.setText(String.valueOf(recipe.getRating()));
            holder.recipeRatingTextView.setVisibility(View.VISIBLE);
        }

        holder.recipeImageView.setImageBitmap(recipe.getImage());

        holder.parentView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewRecipeActivity.class);
            intent.putExtra("Recipe", recipe);
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(context, holder.recipeImageView, "recipeImage");
            context.startActivity(intent, options.toBundle());
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    static class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView recipeTitleTextView;
        private TextView recipeIngredientsTextView;
        private TextView recipeRatingTextView;
        private ImageView recipeImageView;
        private View parentView;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.parentView = itemView;
            this.recipeTitleTextView = itemView.findViewById(R.id.recipeTitleTextView);
            this.recipeImageView = itemView.findViewById(R.id.recipeImageView);
            this.recipeIngredientsTextView = itemView.findViewById(R.id.recipeIngredientsTextView);
            this.recipeRatingTextView = itemView.findViewById(R.id.recipeRatingTextView);
        }
    }

    /**
     * Runs the filter, pauses for 20 milliseconds and then returns the amount of items which succeeded the filter
     * @param filterString the json string on which will be filtered
     * @return an int saying the amount of items that are shown
     */
    public int filterAndReturnAmount(String filterString) {
        getFilter().filter(filterString);

        try {
            TimeUnit.MILLISECONDS.sleep(20);
        } catch (Exception e) {
            Log.e("SleepError", "" + e.getMessage());
        }
        return returnCount;
    }

    /**
     * Converts a list of ingredients into a string list of the ingredient names
     * @param ingredientList the list to convert
     * @return returns a List<String> of all the ingredient names
     */
    private List<String> ingredientNameToList(List<Ingredient> ingredientList) {
        List<String> ingredientNameList = new ArrayList<>();

        for (Ingredient ingredient : ingredientList) {
            ingredientNameList.add(ingredient.getName());
        }
        return ingredientNameList;
    }

    public Filter getFilter() {

        return new Filter() {

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
                returnCount = 0;

                List<String> ingredientsToIncludeList = new ArrayList<>();
                List<String> ingredientsNotToIncludeList = new ArrayList<>();

                try {
                    JSONObject jsonObject = new JSONObject(constraint.toString());

                    JSONArray ingredientsToIncludeJsonArray = jsonObject.getJSONArray("ingredientsToIncludeList");
                    for (int i=0; i < ingredientsToIncludeJsonArray.length(); i++) {
                        ingredientsToIncludeList.add(ingredientsToIncludeJsonArray.getString(i));
                    }
                    JSONArray ingredientsNotToIncludeJsonArray = jsonObject.getJSONArray("ingredientsNotToIncludeList");
                    for (int i=0; i < ingredientsNotToIncludeJsonArray.length(); i++) {
                        ingredientsNotToIncludeList.add(ingredientsNotToIncludeJsonArray.getString(i));
                    }

                } catch (Exception e) {
                    Log.e("JsonError-Adapter", "" + e.getMessage());
                }

                try {

                    for (Recipe recipe : recipeList) {

                        List<String> ingredientList = ingredientNameToList(recipe.getIngredientList());

                        if(ingredientList.containsAll(ingredientsToIncludeList)) {
                            if (Collections.disjoint(ingredientList, ingredientsNotToIncludeList)) {
                                filteredArray.add(recipe);
                            }
                        }
                    }

                    results.count = filteredArray.size();
                    results.values = filteredArray;
                    returnCount = filteredArray.size();
                } catch (Exception e) {
                    Log.e("filterError", "" + e.getMessage());
                }
                return results;
            }
        };
    }
}
