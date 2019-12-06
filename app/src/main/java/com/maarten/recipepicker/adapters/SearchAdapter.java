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

import com.maarten.recipepicker.Models.Ingredient;
import com.maarten.recipepicker.R;
import com.maarten.recipepicker.Models.Recipe;
import com.maarten.recipepicker.ViewRecipeActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.CustomViewHolder> {

    private Activity context;
    private List<Recipe> recipeList;
    private static LayoutInflater inflater = null;
    private static int returnCount = 0;

    public SearchAdapter(Activity context, List<Recipe> recipeList){
        this.context = context;
        this.recipeList = recipeList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomViewHolder(
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.list_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final CustomViewHolder holder, int position) {
        final Recipe recipe = recipeList.get(position);
        holder.recipeTitleTextView.setText(recipe.getTitle());
        holder.recipeIngredientsTextView.setText(recipe.getOrderedIngredientString());

        holder.recipeImageView.setImageBitmap(recipe.getImage());

        holder.parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewRecipeActivity.class);
                intent.putExtra("Recipe", recipe);
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(context, holder.recipeImageView, "recipeImage");
                context.startActivity(intent, options.toBundle());
            }
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

    class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView recipeTitleTextView;
        private TextView recipeIngredientsTextView;
        private ImageView recipeImageView;
        private View parentView;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.parentView = itemView;
            this.recipeTitleTextView = itemView.findViewById(R.id.recipeTitleTextView);
            this.recipeImageView = itemView.findViewById(R.id.recipeImageView);
            this.recipeIngredientsTextView = itemView.findViewById(R.id.recipeIngredientsTextView);
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

                try {
                    String searchString = constraint.toString().toLowerCase();

                    // checks if part of the title is the same as the searchstring
                    // if that fails checks each ingredient

                    for (int i = 0; i < recipeList.size(); i++) {
                        Recipe tempRecipe = recipeList.get(i);

                        if(tempRecipe.getTitle().toLowerCase().contains(searchString)) {
                            filteredArray.add(tempRecipe);
                        } else {
                            // if the title didn't match -> check each ingredient
                            for (Ingredient ingredient : tempRecipe.getIngredientList()) {
                                if(ingredient.getName().toLowerCase().contains(searchString)) {
                                    filteredArray.add(tempRecipe);
                                }
                            }
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
        return filter;
    }
}
