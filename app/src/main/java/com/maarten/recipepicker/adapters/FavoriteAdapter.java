package com.maarten.recipepicker.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maarten.recipepicker.R;
import com.maarten.recipepicker.Recipe;
import com.maarten.recipepicker.ViewRecipeActivity;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.CustomViewHolder> {

    private Activity context;
    private List<Recipe> recipeList;
    private static LayoutInflater inflater = null;

    public FavoriteAdapter(Activity context, List<Recipe> recipeList){
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
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        final Recipe recipe = recipeList.get(position);
        holder.recipeTitleTextView.setText(recipe.getTitle());
        holder.recipeIngredientsTextView.setText(recipe.getOrderedIngredientString());

        holder.recipeImageView.setImageBitmap(recipe.getImage());

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

                for (int i = 0; i < recipeList.size(); i++) {
                    Recipe tempRecipe = recipeList.get(i);
                    if(tempRecipe.getFavorite()) {
                        filteredArray.add(tempRecipe);
                    }
                }
                results.count = filteredArray.size();
                results.values = filteredArray;

                return results;
            }
        };

        return filter;
    }

}
