package com.maarten.recipepicker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.maarten.recipepicker.adapters.FilterIngredientsResultsAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;
import static com.maarten.recipepicker.MainActivity.recipeList;

public class FilteredIngredientsResultsActivity extends AppCompatActivity {

    private FilterIngredientsResultsAdapter filterIngredientsResultsAdapter;
    private RecyclerView filteredRecyclerView;

    private List<String> ingredientsToIncludeList;
    private List<String> ingredientsNotToIncludeList;

    private MaterialButton addRecipeButton;
    private TextView noRecipesTextView;

    private int amountOfItems;

    private JSONObject filterObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_ingredients_results);

        amountOfItems = 0;

        filteredRecyclerView = findViewById(R.id.filteredRecyclerView);
        filterIngredientsResultsAdapter = new FilterIngredientsResultsAdapter(this, recipeList);
        filteredRecyclerView.setAdapter(filterIngredientsResultsAdapter);
        filteredRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ingredientsToIncludeList = new ArrayList<>();
        ingredientsNotToIncludeList = new ArrayList<>();

        try {
            Intent intent = getIntent();
            filterObject = new JSONObject(intent.getStringExtra("filterObject"));

            JSONArray ingredientsToIncludeJsonArray = filterObject.getJSONArray("ingredientsToIncludeList");
            for (int i=0; i < ingredientsToIncludeJsonArray.length(); i++) {
                ingredientsToIncludeList.add(ingredientsToIncludeJsonArray.getString(i));
            }
            JSONArray ingredientsNotToIncludeJsonArray = filterObject.getJSONArray("ingredientsNotToIncludeList");
            for (int i=0; i < ingredientsNotToIncludeJsonArray.length(); i++) {
                ingredientsNotToIncludeList.add(ingredientsNotToIncludeJsonArray.getString(i));
            }

            amountOfItems = filterIngredientsResultsAdapter.filterAndReturnAmount(filterObject.toString());
        }
        catch (Exception e) {
            Log.e("intentError", e.getMessage());
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Filter Results");
        setSupportActionBar(toolbar);

        // back button pressed
        toolbar.setNavigationOnClickListener(v -> finish());

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // write the text to say for which ingredients you have filtered. FromHtml is used to make the numbers bold
        TextView filteredDescriptionTextView = findViewById(R.id.filteredDescriptionTextField);

        String descriptionString = "";

        if (!ingredientsToIncludeList.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (String ingredient : ingredientsToIncludeList) {
                builder.append(ingredient);
                builder.append(", ");
            }
            // remove the last ", "
            builder.setLength(builder.length() - 2);
            descriptionString += getString(R.string.filtered_recipe_with_ingredients_description, builder.toString());

            if(!ingredientsNotToIncludeList.isEmpty()) {
                descriptionString += getString(R.string.filter_ingredients_and_not_following_ingredients, generateNotToIncludeString());
            }

        } else {
            descriptionString += getString(R.string.filter_ingredients_only_not_included, generateNotToIncludeString());
        }

        filteredDescriptionTextView.setText(Html.fromHtml(descriptionString, FROM_HTML_MODE_LEGACY));

        addRecipeButton = findViewById(R.id.addRecipeButton);
        noRecipesTextView = findViewById(R.id.noFoundRecipesTextView);

        controlNoRecipeElements();
    }

    private String generateNotToIncludeString() {
        StringBuilder builderNotToInclude = new StringBuilder();
        for (String ingredient : ingredientsNotToIncludeList) {
            builderNotToInclude.append(ingredient);
            builderNotToInclude.append(", ");
        }
        // remove the last ", "
        builderNotToInclude.setLength(builderNotToInclude.length() - 2);
        return builderNotToInclude.toString();
    }

    /**
     * This takes care of the 'no found recipes' and 'add recipe' elements when the list is empty
     */
    private void controlNoRecipeElements() {
        if(amountOfItems > 0) {
            addRecipeButton.setVisibility(View.GONE);
            noRecipesTextView.setVisibility(View.GONE);
        } else {
            addRecipeButton.setVisibility(View.VISIBLE);
            noRecipesTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * When activity resumes, update the adapter + filter again to get the correct results, in case you changed something in the recipes
     */
    @Override
    public void onResume() {
        super.onResume();
        filterIngredientsResultsAdapter = new FilterIngredientsResultsAdapter(this, recipeList);
        filteredRecyclerView.setAdapter(filterIngredientsResultsAdapter);
        amountOfItems = filterIngredientsResultsAdapter.filterAndReturnAmount(filterObject.toString());
        Log.d("COUNT", "in onResume: " + amountOfItems);
        controlNoRecipeElements();
    }

    /**
     * Inflates the menu into the toolbar
     *
     * @param menu the menu
     * @return should return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    /**
     * checks if the clicked menu item the home icon is
     *
     * @param item the clicked menu item
     * @return should return true when item found
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_home) {
            goToMainActivity();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Opens the main activity and closes the previous activities
     */
    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     *  opens the AddRecipeActivity
     */
    public void addRecipe(View view) {
        Intent intent = new Intent (this, AddRecipeActivity.class);
        startActivity(intent);
    }

}
