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
import com.maarten.recipepicker.models.Recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.maarten.recipepicker.RecipeUtility.changeFirstLetterToCapital;

public class FilterIngredientsResultsAdapter extends RecyclerView.Adapter<FilterIngredientsResultsAdapter.CustomViewHolder> {

    private Activity context;
    private List<Recipe> recipeList;

    private int returnCount;

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

    //@Override
    public Filter getFilter() {

        return new Filter() {

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

                try {
                    // get the ingredientList and convert it back to an array
                    String ingredientListString = constraint.toString();
                    String[] filterIngredientList = ingredientListString.substring(1, ingredientListString.length() - 1).split(", ");

                    // Loop through every recipe. Make copy of ingredientList and remove an item from this list if the recipe has this ingredient.
                    // If list is empty at the end, recipe had all asked ingredients -> add it to the return list
                    for (Recipe tempRecipe : recipeList) {
                        ArrayList<String> filterIngredientListToCheckIn = new ArrayList<>(Arrays.asList(filterIngredientList));

                        int index = 0;

                        while(filterIngredientListToCheckIn.size() > 0 && index < tempRecipe.getIngredientList().size()) {
                            filterIngredientListToCheckIn.remove(changeFirstLetterToCapital(tempRecipe.getIngredientList().get(index).getName()));

                            index++;
                        }
                        if(filterIngredientListToCheckIn.size() == 0) {
                            filteredArray.add(tempRecipe);
                        }
                    }
                    results.count = filteredArray.size();
                    results.values = filteredArray;
                    returnCount = filteredArray.size();
                } catch (Exception e) {
                    Log.e("filterError", e.getMessage());
                }
                return results;
            }
        };
    }
}
