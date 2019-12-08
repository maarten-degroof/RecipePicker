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

import com.maarten.recipepicker.models.Recipe;
import com.maarten.recipepicker.R;
import com.maarten.recipepicker.ViewRecipeActivity;
import com.maarten.recipepicker.enums.CookTime;
import com.maarten.recipepicker.enums.Difficulty;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.CustomViewHolder> {

    private Activity context;
    private List<Recipe> recipeList;
    private static LayoutInflater inflater = null;
    private int returnCount;

    public FilterAdapter(Activity context, List<Recipe> recipeList){
        this.context = context;
        this.recipeList = recipeList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        returnCount = 0;
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

        if(recipe.getRating() == 0) {
            holder.recipeRatingTextView.setVisibility(View.GONE);
        } else {
            holder.recipeRatingTextView.setText(String.valueOf(recipe.getRating()));
            holder.recipeRatingTextView.setVisibility(View.VISIBLE);
        }

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
    public int getItemCount() {
        return recipeList.size();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

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
                int ratingMin, ratingMax;
                Boolean durationShort, durationMedium, durationLong;
                Boolean difficultyBeginner, difficultyIntermediate, difficultyExpert;

                returnCount = 0;

                try {
                    JSONObject jsonObject = new JSONObject(constraint.toString());
                    filterMin = (int) jsonObject.get("filterMin");
                    filterMax = (int) jsonObject.get("filterMax");
                    ratingMin = (int) jsonObject.get("ratingMin");
                    ratingMax = (int) jsonObject.get("ratingMax");
                    durationShort = (Boolean) jsonObject.get("durationShort");
                    durationMedium = (Boolean) jsonObject.get("durationMedium");
                    durationLong = (Boolean) jsonObject.get("durationLong");
                    difficultyBeginner = (Boolean) jsonObject.get("difficultyBeginner");
                    difficultyIntermediate = (Boolean) jsonObject.get("difficultyIntermediate");
                    difficultyExpert = (Boolean) jsonObject.get("difficultyExpert");


                    /**
                     * Steps to filter:
                     *  -   Create arraylists which contain the objects on which is filtered: Cooktime and Difficulty
                     *      These can contain 0 to all three items
                     *  -   First check if the amount cooked is between requested values
                     *  -   Then check if the rating is between the requested values
                     *  -   Then check if both arrays are empty (which means no filter was selected)
                     *  -   Then check if one of the arrays is empty (only one type of filter was selected)
                     *  -   If it wasn't any of the previous, both arrays are set
                     */
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


                    for (int i = 0; i < recipeList.size(); i++) {
                        Recipe tempRecipe = recipeList.get(i);

                        // check if the times cooked is within the given range
                        if (tempRecipe.getAmountCooked() >= filterMin && tempRecipe.getAmountCooked() <= filterMax) {

                            // check if the rating is within the given range
                            if (tempRecipe.getRating() >= ratingMin && tempRecipe.getRating() <= ratingMax) {

                                // no filters checked -> add recipe
                                if (durationFilterList.isEmpty() && difficultyFilterList.isEmpty()) {
                                    filteredArray.add(tempRecipe);
                                }
                                // no duration; only difficulty
                                else if (durationFilterList.isEmpty()) {
                                    if (difficultyFilterList.contains(tempRecipe.getDifficulty())) {
                                        filteredArray.add(tempRecipe);
                                    }
                                }
                                // no difficulty; only duration
                                else if (difficultyFilterList.isEmpty()) {
                                    if (durationFilterList.contains(tempRecipe.getCookTime())) {
                                        filteredArray.add(tempRecipe);
                                    }
                                }
                                // both filters are set
                                else {
                                    if (durationFilterList.contains(tempRecipe.getCookTime()) && difficultyFilterList.contains(tempRecipe.getDifficulty())) {
                                        filteredArray.add(tempRecipe);
                                    }
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
    }
}
