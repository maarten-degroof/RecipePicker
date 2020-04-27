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
import com.maarten.recipepicker.adapters.SearchAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;
import static com.maarten.recipepicker.MainActivity.recipeList;

public class SearchResultsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSearched;
    private SearchAdapter adapter;

    private MaterialButton addRecipeButton;
    private TextView noRecipesTextView;

    private int amountOfItems;

    private JSONObject filterObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Search Results");
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
        String searchString = "";
        boolean searchTitle = true;
        boolean searchIngredients = true;
        boolean searchInstructions = true;
        boolean searchComments = false;
        boolean searchOnlyFavorites = false;
        boolean shouldFilterAllCategories = false;

        Set<String> categoryList = new TreeSet<>();
        ChipGroup filteredCategoryChipGroup = findViewById(R.id.filteredCategoryChipGroup);

        try {
            Intent intent = getIntent();
            filterObject = new JSONObject(intent.getStringExtra("JSONObject"));

            searchString = filterObject.getString("searchString");
            searchTitle = filterObject.getBoolean("searchTitle");
            searchIngredients = filterObject.getBoolean("searchIngredients");
            searchInstructions = filterObject.getBoolean("searchInstructions");
            searchComments = filterObject.getBoolean("searchComments");
            searchOnlyFavorites = filterObject.getBoolean("searchOnlyFavorites");

            shouldFilterAllCategories = filterObject.getBoolean("shouldFilterAllCategories");
            JSONArray categoryJsonArray = filterObject.getJSONArray("categories");
            for (int i=0; i < categoryJsonArray.length(); i++) {
                categoryList.add(categoryJsonArray.getString(i));
            }


            amountOfItems = adapter.filterAndReturnAmount(filterObject.toString());

        } catch (Exception e) {
            Log.e("intentError", e.getMessage());
        }

        // Todo: show all you searched for




        recyclerViewSearched = findViewById(R.id.listViewSearched);

        adapter = new SearchAdapter(this, recipeList);
        recyclerViewSearched.setAdapter(adapter);
        recyclerViewSearched.setLayoutManager(new LinearLayoutManager(this));

        if (searchOnlyFavorites) {
            searchString += ", only searching favorites";
        }

        // write the text to say for which times cooked you have filtered. FromHtml is used to make the searched string bold
        TextView searchedDescriptionTextView = findViewById(R.id.searchedDescriptionTextField);
        String description = getString(R.string.searched_recipe_description, searchString);
        searchedDescriptionTextView.setText(Html.fromHtml(description, FROM_HTML_MODE_LEGACY));

        StringBuilder builder = new StringBuilder();
        if(searchTitle) {
            builder.append("the title, ");
        }
        if (searchIngredients) {
            builder.append("the ingredients, ");
        }
        if (searchInstructions) {
            builder.append("the instructions, ");
        }
        if (searchComments) {
            builder.append("the comments, ");
        }
        // if at least one of the boxes was checked, remove the last ', '
        if (builder.length() > 2) {
            builder.setLength(builder.length() - 2);
        } else {
            builder.append("no fields");
        }

        TextView searchedFieldsDescriptionTextView = findViewById(R.id.searchedFieldsDescriptionTextView);
        String checkboxDescription = getString(R.string.search_fields_description, builder);
        searchedFieldsDescriptionTextView.setText(Html.fromHtml(checkboxDescription, FROM_HTML_MODE_LEGACY));

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
            filteredCategoryChipGroup.setVisibility(View.GONE);
        }

        addRecipeButton = findViewById(R.id.addRecipeButton);
        noRecipesTextView = findViewById(R.id.noFoundRecipesTextView);
        controlNoRecipeElements();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter = new SearchAdapter(this, recipeList);
        recyclerViewSearched.setAdapter(adapter);
        amountOfItems = adapter.filterAndReturnAmount(filterObject.toString());
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
     * Opens the main activity and closes the previous activities
     */
    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
     *  opens the AddRecipeActivity
     */
    public void addRecipe(View view) {
        Intent intent = new Intent (this, AddRecipeActivity.class);
        startActivity(intent);
    }

}
