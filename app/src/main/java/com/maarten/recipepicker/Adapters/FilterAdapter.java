package com.maarten.recipepicker.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maarten.recipepicker.CookTime;
import com.maarten.recipepicker.R;
import com.maarten.recipepicker.Recipe;
import com.maarten.recipepicker.ViewRecipeActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.CustomViewHolder> {

    private Activity context;
    private List<Recipe> recipeList;
    private static LayoutInflater inflater = null;

    public FilterAdapter(Activity context, List<Recipe> recipeList){
        this.context = context;
        this.recipeList = recipeList;
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

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
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        final Recipe recipe = recipeList.get(position);
        holder.recipeTitleTextView.setText(recipe.getTitle());

        if(recipe.getImagePath() != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(recipe.getImagePath());
            holder.recipeImageView.setImageBitmap(bitmap);
        } else {
            holder.recipeImageView.setImageResource(R.drawable.no_image_available);
        }

        holder.parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewRecipeActivity.class);
                intent.putExtra("Recipe", recipe);
                context.startActivity(intent);
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
        private ImageView recipeImageView;
        private View parentView;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.parentView = itemView;
            this.recipeTitleTextView = itemView.findViewById(R.id.recipeTitleTextView);
            this.recipeImageView = itemView.findViewById(R.id.recipeImageView);

        }
    }

    //@Override
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