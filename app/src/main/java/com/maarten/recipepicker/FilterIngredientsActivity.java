package com.maarten.recipepicker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.maarten.recipepicker.adapters.FilterIngredientsAdapter;
import com.maarten.recipepicker.models.FilterIngredient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Opens the Activity to filter on ingredients.
 * Will list all ingredients. If none, will show a text to add an ingredient.
 */

public class FilterIngredientsActivity extends AppCompatActivity {

    private List<FilterIngredient> ingredientList;
    private FilterIngredientsAdapter filterIngredientsAdapter;
    private RecyclerView filterIngredientsRecyclerView;

    private MaterialButton addRecipeButton;
    private TextView addRecipeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_ingredients);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Filter on ingredients");
        setSupportActionBar(toolbar);

        // Back button pressed
        toolbar.setNavigationOnClickListener(v -> finish());

        // This takes care of the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        addRecipeTextView = findViewById(R.id.noRecipesYetTextView);
        addRecipeButton = findViewById(R.id.addRecipeButton);
        filterIngredientsRecyclerView = findViewById(R.id.filterIngredientsRecyclerView);

        createIngredientListAndRecyclerView();
    }

    /**
     * Creates the list of ingredients, and connects them to the adapter.
     * Also takes care of the RecyclerView
     */
     private void createIngredientListAndRecyclerView() {
         List<String> ingredientNameList = RecipeUtility.generateIngredientList();
         ingredientList = new ArrayList<>();
         for (String name : ingredientNameList) {
             ingredientList.add(new FilterIngredient(name));
         }

         // Shows the 'add recipe' button and text when there aren't any ingredients
         showAddRecipeScreen(ingredientList.isEmpty());

         filterIngredientsAdapter = new FilterIngredientsAdapter(this, ingredientList);
         filterIngredientsRecyclerView.setAdapter(filterIngredientsAdapter);
         filterIngredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
     }

    @Override
    protected void onResume() {
        super.onResume();
        createIngredientListAndRecyclerView();
    }

    /**
     * This takes care of the 'no recipes' and 'add recipe' elements when the list is empty
     * @param shouldShow boolean, if true means there are no ingredients and it should show the 'add recipe' button
     */
    private void showAddRecipeScreen(Boolean shouldShow) {
        if (shouldShow) {
            addRecipeButton.setVisibility(View.VISIBLE);
            addRecipeTextView.setVisibility(View.VISIBLE);
            filterIngredientsRecyclerView.setVisibility(View.GONE);
        } else {
            addRecipeButton.setVisibility(View.GONE);
            addRecipeTextView.setVisibility(View.GONE);
            filterIngredientsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Goes back to the previous activity (FilterActivity)
     * @param view The button 'use filters instead'
     */
    public void goBackToFilters(View view) {
        finish();
    }

    /**
     * Converts the selected ingredients and opens the FilteredIngredientsResultsActivity
     * Shows a toast if no ingredients were selected
     * @param view The 'Filter your cookbook' button
     */
    public void viewFilterResults(View view) {
        ArrayList<String> ingredientsToIncludeList = new ArrayList<>();
        ArrayList<String> ingredientsNotToIncludeList = new ArrayList<>();

        for (FilterIngredient ingredient : ingredientList) {
            if (ingredient.getState()> 0 ) {
                ingredientsToIncludeList.add(ingredient.getName());
            } else if (ingredient.getState() < 0) {
                ingredientsNotToIncludeList.add(ingredient.getName());
            }
        }
        if (ingredientsToIncludeList.isEmpty() && ingredientsNotToIncludeList.isEmpty()) {
            Toast.makeText(this, "You need to select at least one ingredient", Toast.LENGTH_LONG).show();
        }
        else {
            try {
                JSONObject filterObject = new JSONObject();
                JSONArray ingredientsToIncludeJsonArray = new JSONArray(ingredientsToIncludeList);
                JSONArray ingredientsNotToIncludeJsonArray = new JSONArray(ingredientsNotToIncludeList);

                filterObject.put("ingredientsToIncludeList", ingredientsToIncludeJsonArray);
                filterObject.put("ingredientsNotToIncludeList", ingredientsNotToIncludeJsonArray);

                Intent intent = new Intent(this, FilteredIngredientsResultsActivity.class);
                intent.putExtra("filterObject", filterObject.toString());
                startActivity(intent);

            } catch (Exception e) {
                Log.e("JsonError", "" + e.getMessage());
            }
        }
    }

    /**
     * Opens the AddRecipeActivity
     */
    public void addRecipe(View view) {
        Intent intent = new Intent (this, AddRecipeActivity.class);
        startActivity(intent);
    }

    /**
     * Inflates the menu into the toolbar
     * @param menu the menu
     * @return should return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    /**
     * Checks if the clicked menu item the home icon is
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

}
