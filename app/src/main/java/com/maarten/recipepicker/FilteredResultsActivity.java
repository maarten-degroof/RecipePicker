package com.maarten.recipepicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.maarten.recipepicker.adapters.FilterAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;
import static com.maarten.recipepicker.MainActivity.recipeList;

public class FilteredResultsActivity extends AppCompatActivity {

    private FilterAdapter filterAdapter;

    private RecyclerView listViewFiltered;

    private int filterMin, filterMax;
    private int ratingMin, ratingMax;
    private Boolean durationShort, durationMedium, durationLong;
    private Boolean difficultyBeginner, difficultyIntermediate, difficultyExpert;

    private List<String> categoryList;

    private JSONObject filterObject;

    private MaterialButton addRecipeButton;
    private TextView noRecipesTextView;

    private int amountOfItems;

    private  ChipGroup filteredDurationDifficultyChipGroup;
    private ChipGroup filteredCategoryChipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_results);
        amountOfItems = 0;

        boolean shouldFilterAllCategories = false;

        categoryList = new ArrayList<>();
        filteredCategoryChipGroup = findViewById(R.id.filteredCategoryChipGroup);

        try {
            Intent intent = getIntent();
            filterObject = new JSONObject(intent.getStringExtra("JSONObject"));

            // get the values for which you filtered
            filterMin = filterObject.getInt("filterMin");
            filterMax = filterObject.getInt("filterMax");
            ratingMin = filterObject.getInt("ratingMin");
            ratingMax = filterObject.getInt("ratingMax");

            durationShort = filterObject.getBoolean("durationShort");
            durationMedium = filterObject.getBoolean("durationMedium");
            durationLong = filterObject.getBoolean("durationLong");

            difficultyBeginner = filterObject.getBoolean("difficultyBeginner");
            difficultyIntermediate = filterObject.getBoolean("difficultyIntermediate");
            difficultyExpert = filterObject.getBoolean("difficultyExpert");

            shouldFilterAllCategories = filterObject.getBoolean("shouldFilterAllCategories");
            JSONArray categoryJsonArray = filterObject.getJSONArray("categories");
            for (int i=0; i < categoryJsonArray.length(); i++) {
                categoryList.add(categoryJsonArray.getString(i));
            }

            listViewFiltered = findViewById(R.id.mainRecyclerView);

            filterAdapter = new FilterAdapter(this, recipeList);
            listViewFiltered.setAdapter(filterAdapter);
            listViewFiltered.setLayoutManager(new LinearLayoutManager(this));

            amountOfItems = filterAdapter.filterAndReturnAmount(filterObject.toString());

        } catch (Exception e) {
            Log.e("intentError", e.getMessage());
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Filter Results");
        setSupportActionBar(toolbar);

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // write the text to say for which times cooked you have filtered. FromHtml is used to make the numbers bold
        TextView filteredDescriptionTextView = findViewById(R.id.filteredDescriptionTextField);
        String description = getString(R.string.filtered_recipe_description, filterMin, filterMax, ratingMin, ratingMax);
        filteredDescriptionTextView.setText(Html.fromHtml(description, FROM_HTML_MODE_LEGACY));

        filteredDurationDifficultyChipGroup = findViewById(R.id.filteredDurationDifficultyChipGroup);
        if (durationShort) {
            createChip(getString(R.string.duration_short), filteredDurationDifficultyChipGroup);
        } if (durationMedium) {
            createChip(getString(R.string.duration_medium), filteredDurationDifficultyChipGroup);
        } if (durationLong) {
            createChip(getString(R.string.duration_long), filteredDurationDifficultyChipGroup);
        } if (difficultyBeginner) {
            createChip(getString(R.string.beginner), filteredDurationDifficultyChipGroup);
        } if (difficultyIntermediate) {
            createChip(getString(R.string.intermediate), filteredDurationDifficultyChipGroup);
        } if(difficultyExpert) {
            createChip(getString(R.string.expert), filteredDurationDifficultyChipGroup);
        }

        TextView categoryTextView = findViewById(R.id.categoriesSelectedTextView);
        if (!categoryList.isEmpty()) {
            String categoryText;
            if (shouldFilterAllCategories) {
                categoryText = getString(R.string.filtered_category_description, getString(R.string.all));
            } else {
                categoryText = getString(R.string.filtered_category_description, getString(R.string.some));
            }
            categoryTextView.setText(Html.fromHtml(categoryText, FROM_HTML_MODE_LEGACY));

            for (String category : categoryList) {
                createChip(category, filteredCategoryChipGroup);
            }
        } else {
            categoryTextView.setVisibility(View.GONE);
            filteredDurationDifficultyChipGroup.setVisibility(View.GONE);
        }


        addRecipeButton = findViewById(R.id.addRecipeButton);
        noRecipesTextView = findViewById(R.id.noFoundRecipesTextView);

        controlNoRecipeElements();
    }

    /**
     * Creates a chip and adds it to the chipGroup
     *
     * @param name - the name of the chip
     */
    private void createChip(String name, ChipGroup chipGroup) {
        Chip chip = new Chip(this);
        chip.setText(name);
        chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primaryColor)));
        chip.setTextColor(Color.WHITE);
        chipGroup.addView(chip);
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
        filterAdapter = new FilterAdapter(this, recipeList);
        listViewFiltered.setAdapter(filterAdapter);
        amountOfItems = filterAdapter.filterAndReturnAmount(filterObject.toString());
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
     * @param item  the clicked menu item
     * @return  should return true when item found
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
