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
import com.maarten.recipepicker.enums.CookTime;
import com.maarten.recipepicker.enums.Difficulty;
import com.maarten.recipepicker.models.Recipe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.CustomViewHolder> {

    private Activity context;
    private List<Recipe> recipeList;
    private int returnCount;

    public FilterAdapter(Activity context, List<Recipe> recipeList){
        this.context = context;
        this.recipeList = recipeList;
        returnCount = 0;
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
                ArrayList<Recipe> filteredArray;

                int filterMin, filterMax;
                int ratingMin, ratingMax;
                boolean durationShort, durationMedium, durationLong;
                boolean difficultyBeginner, difficultyIntermediate, difficultyExpert;
                boolean recipeShouldHaveAllCategories;
                Set<String> categorySet = new TreeSet<>();

                returnCount = 0;

                try {
                    JSONObject jsonObject = new JSONObject(constraint.toString());
                    filterMin = jsonObject.getInt("filterMin");
                    filterMax = jsonObject.getInt("filterMax");
                    ratingMin = jsonObject.getInt("ratingMin");
                    ratingMax = jsonObject.getInt("ratingMax");

                    durationShort = jsonObject.getBoolean("durationShort");
                    durationMedium = jsonObject.getBoolean("durationMedium");
                    durationLong = jsonObject.getBoolean("durationLong");

                    difficultyBeginner = jsonObject.getBoolean("difficultyBeginner");
                    difficultyIntermediate = jsonObject.getBoolean("difficultyIntermediate");
                    difficultyExpert = jsonObject.getBoolean("difficultyExpert");

                    recipeShouldHaveAllCategories = jsonObject.getBoolean("shouldFilterAllCategories");

                    JSONArray categoryJsonArray = jsonObject.getJSONArray("categories");
                    for (int i=0; i < categoryJsonArray.length(); i++) {
                        categorySet.add(categoryJsonArray.getString(i));
                    }


                    // Steps to filter:
                    // - Create arrayLists which contain the objects on which is filtered: CookTime and Difficulty
                    // - These can contain 0 to all three items
                    // - First check if the amount cooked is between requested values
                    // - Then check if the rating is between the requested values
                    // - Then check if both arrays are empty (which means no filter was selected)
                    // - Then check if one of the arrays is empty (only one type of filter was selected)
                    // - If it wasn't any of the previous, both arrays are set
                    // - Lastly, check for the categories

                    ArrayList<CookTime> durationFilterList = new ArrayList<>();
                    if(durationShort) {
                        durationFilterList.add(CookTime.SHORT);
                    }
                    if(durationMedium) {
                        durationFilterList.add(CookTime.MEDIUM);
                    }
                    if(durationLong) {
                        durationFilterList.add(CookTime.LONG);
                    }

                    ArrayList<Difficulty> difficultyFilterList = new ArrayList<>();
                    if(difficultyBeginner) {
                        difficultyFilterList.add(Difficulty.BEGINNER);
                    }
                    if(difficultyIntermediate) {
                        difficultyFilterList.add(Difficulty.INTERMEDIATE);
                    }
                    if(difficultyExpert) {
                        difficultyFilterList.add(Difficulty.EXPERT);
                    }

                    List<Recipe> partFilterList = new ArrayList<>();

                    for (Recipe recipe : recipeList) {
                        // Check if the times cooked is within the given range
                        if (recipe.getAmountCooked() >= filterMin && recipe.getAmountCooked() <= filterMax) {

                            // Check if the rating is within the given range
                            if (recipe.getRating() >= ratingMin && recipe.getRating() <= ratingMax) {

                                // No filters checked -> add recipe
                                if (durationFilterList.isEmpty() && difficultyFilterList.isEmpty()) {
                                    partFilterList.add(recipe);
                                }
                                // No duration; only difficulty
                                else if (durationFilterList.isEmpty()) {
                                    if (difficultyFilterList.contains(recipe.getDifficulty())) {
                                        partFilterList.add(recipe);
                                    }
                                }
                                // No difficulty; only duration
                                else if (difficultyFilterList.isEmpty()) {
                                    if (durationFilterList.contains(recipe.getCookTime())) {
                                        partFilterList.add(recipe);
                                    }
                                }
                                // Both filters are set
                                else {
                                    if (durationFilterList.contains(recipe.getCookTime()) && difficultyFilterList.contains(recipe.getDifficulty())) {
                                        partFilterList.add(recipe);
                                    }
                                }
                            }
                        }
                    }

                    // Part two of the filtering: filter on the categories
                    if (categorySet.isEmpty()) {
                        filteredArray = new ArrayList<>(partFilterList);
                    } else {
                        filteredArray = new ArrayList<>();
                        for (Recipe recipe : partFilterList) {
                            if (recipeShouldHaveAllCategories) {
                                if (recipe.getCategories().containsAll(categorySet)) {
                                    filteredArray.add(recipe);
                                }
                            }
                            else {
                                if (listContainsItem(recipe.getCategories(), categorySet)) {
                                    filteredArray.add(recipe);
                                }
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

    /**
     * Checks if the checkList contains an item of the testList
     * @param checkList the list of Strings to check against
     * @param testList the list of String to use to check
     * @return returns true if an item is found
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
