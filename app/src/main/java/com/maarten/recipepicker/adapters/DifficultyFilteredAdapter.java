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
import com.maarten.recipepicker.enums.Difficulty;
import com.maarten.recipepicker.models.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DifficultyFilteredAdapter extends RecyclerView.Adapter<DifficultyFilteredAdapter.CustomViewHolder> {

    private Activity context;
    private List<Recipe> recipeList;

    private int returnCount;

    public DifficultyFilteredAdapter(Activity context, List<Recipe> recipeList){
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
     * @param difficulty the difficulty to filter on
     * @return an int saying the amount of items that are shown
     */
    public int filterAndReturnAmount(String difficulty) {
        getFilter().filter(difficulty);

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
                ArrayList<Recipe> filteredArray = new ArrayList<>();
                returnCount = 0;

                Difficulty difficulty;

                switch(constraint.toString()) {
                    case "BEGINNER":
                        difficulty = Difficulty.BEGINNER;
                        break;
                    case "EXPERT":
                        difficulty = Difficulty.EXPERT;
                        break;
                    default:
                        difficulty = Difficulty.INTERMEDIATE;
                }

                for (Recipe tempRecipe : recipeList) {
                    if(tempRecipe.getDifficulty() == difficulty){
                        filteredArray.add(tempRecipe);
                    }
                }
                results.count = filteredArray.size();
                results.values = filteredArray;
                returnCount = results.count;

                return results;
            }
        };
    }
}
