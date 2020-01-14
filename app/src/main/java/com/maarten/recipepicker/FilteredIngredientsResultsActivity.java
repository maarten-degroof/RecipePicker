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

import java.util.List;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;
import static com.maarten.recipepicker.MainActivity.recipeList;

public class FilteredIngredientsResultsActivity extends AppCompatActivity {

    private FilterIngredientsResultsAdapter filterIngredientsResultsAdapter;
    private RecyclerView filteredRecyclerView;

    private List<String> ingredientsList;

    private MaterialButton addRecipeButton;
    private TextView noRecipesTextView;

    private int amountOfItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_ingredients_results);

        amountOfItems = 0;
        try {
            Intent intent = getIntent();
            ingredientsList = intent.getStringArrayListExtra("ingredientList");

            filteredRecyclerView = findViewById(R.id.filteredRecyclerView);
            filterIngredientsResultsAdapter = new FilterIngredientsResultsAdapter(this, recipeList);
            filteredRecyclerView.setAdapter(filterIngredientsResultsAdapter);
            filteredRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            amountOfItems = filterIngredientsResultsAdapter.filterAndReturnAmount(ingredientsList.toString());
        }
        catch (Exception e) {
            Log.e("intentError", e.getMessage());
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Filter Results");
        setSupportActionBar(toolbar);

        // back button pressed
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // write the text to say for which ingredients you have filtered. FromHtml is used to make the numbers bold
        TextView filteredDescriptionTextView = findViewById(R.id.filteredDescriptionTextField);
        StringBuilder builder = new StringBuilder();
        for (String ingredient : ingredientsList) {
            builder.append(ingredient);
            builder.append(", ");
        }
        // remove the last ", "
        builder.setLength(builder.length() - 2);
        String description = getString(R.string.filtered_recipe_with_ingredients_description, builder.toString());
        filteredDescriptionTextView.setText(Html.fromHtml(description, FROM_HTML_MODE_LEGACY));

        addRecipeButton = findViewById(R.id.addRecipeButton);
        noRecipesTextView = findViewById(R.id.noFoundRecipesTextView);

        controlNoRecipeElements();
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
        amountOfItems = filterIngredientsResultsAdapter.filterAndReturnAmount(ingredientsList.toString());
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
