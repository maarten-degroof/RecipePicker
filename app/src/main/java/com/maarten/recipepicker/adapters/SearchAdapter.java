package com.maarten.recipepicker.adapters;

import android.app.Activity;
import android.content.Context;
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

import com.maarten.recipepicker.models.Ingredient;
import com.maarten.recipepicker.models.Instruction;
import com.maarten.recipepicker.models.Recipe;
import com.maarten.recipepicker.R;
import com.maarten.recipepicker.ViewRecipeActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.CustomViewHolder> {

    private Activity context;
    private List<Recipe> recipeList;

    private static int returnCount = 0;

    public SearchAdapter(Activity context, List<Recipe> recipeList){
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
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
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
     * runs the filter, pauses for 20 milliseconds and then returns the amount of items which succeeded the filter
     *
     * @param filterString - the json string on which will be filtered
     * @return - an int saying the amount of items that are shown
     */
    public int filterAndReturnAmount(String filterString) {
        getFilter().filter(filterString);

        try {
            TimeUnit.MILLISECONDS.sleep(20);
        } catch (Exception e) {
            Log.e("SleepError", e.getMessage());
        }
        Log.d("COUNT", "returning: "+returnCount);
        return returnCount;
    }

    public Filter getFilter() {
        return new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                recipeList = (List<Recipe>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                ArrayList<Recipe> filteredArray;

                boolean searchTitle = true;
                boolean searchIngredients = true;
                boolean searchInstructions = true;
                boolean searchComments = false;
                boolean searchOnlyFavorites = false;
                boolean recipeShouldHaveAllCategories;
                Set<String> categorySet = new TreeSet<>();

                try {

                    JSONObject jsonObject = new JSONObject(constraint.toString());
                    final String searchString = jsonObject.getString("searchString").toLowerCase();
                    searchTitle = jsonObject.getBoolean("searchTitle");
                    searchIngredients = jsonObject.getBoolean("searchIngredients");
                    searchInstructions = jsonObject.getBoolean("searchInstructions");
                    searchComments = jsonObject.getBoolean("searchComments");
                    searchOnlyFavorites = jsonObject.getBoolean("searchOnlyFavorites");

                    recipeShouldHaveAllCategories = jsonObject.getBoolean("shouldFilterAllCategories");

                    JSONArray categoryJsonArray = jsonObject.getJSONArray("categories");
                    for (int i = 0; i < categoryJsonArray.length(); i++) {
                        categorySet.add(categoryJsonArray.getString(i));
                    }

                    // checks if part of the title is the same as the searchstring
                    // if that fails checks each ingredient
                    // if that fails check each instruction
                    // if that fails check the comments
                    // then check if all the found recipes have the required categories
                    List<Recipe> tempSearchedList = new ArrayList<>();

                    for (Recipe recipe : recipeList) {
                        boolean shouldSearchRecipe = true;

                        // if should only search for favorites and this one isn't a favorite, just skip all the checks
                        if (searchOnlyFavorites && !recipe.getFavorite()) {
                            shouldSearchRecipe = false;
                        }

                        if (shouldSearchRecipe && searchTitle) {
                            if (recipe.getTitle().toLowerCase().contains(searchString)) {
                                tempSearchedList.add(recipe);
                                shouldSearchRecipe = false;
                            }
                        }
                        if (shouldSearchRecipe && searchIngredients) {
                            if (recipe.getIngredientList().stream().anyMatch(ingredient -> ingredient.getName().toLowerCase().contains(searchString))) {
                                tempSearchedList.add(recipe);
                                shouldSearchRecipe = false;
                            }
                        }
                        if (shouldSearchRecipe && searchInstructions) {
                            if (recipe.getInstructionList().stream().anyMatch(instruction -> instruction.getDescription().toLowerCase().contains(searchString))) {
                                tempSearchedList.add(recipe);
                                shouldSearchRecipe = false;
                            }
                        }
                        if (shouldSearchRecipe && searchComments) {
                            if (recipe.getComments().toLowerCase().contains(searchString)) {
                                tempSearchedList.add(recipe);
                                shouldSearchRecipe = false;
                            }
                        }
                    }

                    // Part two: filter on the categories
                    if (categorySet.isEmpty()) {
                        filteredArray = new ArrayList<>(tempSearchedList);
                    } else {
                        filteredArray = new ArrayList<>();
                        for (Recipe recipe : tempSearchedList) {
                            if (recipeShouldHaveAllCategories) {
                                if (recipe.getCategories().containsAll(categorySet)) {
                                    filteredArray.add(recipe);
                                }
                            } else {
                                if (listContainsItem(recipe.getCategories(), categorySet)) {
                                    filteredArray.add(recipe);
                                }
                            }
                        }
                    }

                    results.count = filteredArray.size();
                    results.values = filteredArray;
                    returnCount = filteredArray.size();
                }
                catch(Exception e){
                    Log.e("filterError", e.getMessage());
                }
                return results;
            }

        };
    }

    /**
     * Checks if the checkList contains an item of the testList
     *
     * @param checkList - the list of Strings to check against
     * @param testList - the list of String to use to check
     * @return - returns true if an item is found
     */
    private boolean listContainsItem(Set<String> checkList, Set<String> testList) {
        for (String item : testList) {
            if (checkList.contains(item)) {
                return true;
            }
        }
        return false;
    }
}
