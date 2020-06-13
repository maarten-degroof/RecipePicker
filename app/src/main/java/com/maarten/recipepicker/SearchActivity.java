package com.maarten.recipepicker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.maarten.recipepicker.models.Recipe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Set;
import java.util.TreeSet;

import static com.maarten.recipepicker.RecipeUtility.changeFirstLetterToCapital;

public class SearchActivity extends AppCompatActivity {

    private ChipGroup categoryChipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Search");
        setSupportActionBar(toolbar);

        // This takes care of the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        Set<String> categorySet = new TreeSet<>();

        for (Recipe recipe : MainActivity.recipeList) {
            for (String category : recipe.getCategories()) {
                String current_category = changeFirstLetterToCapital(category);
                categorySet.add(current_category);
            }
        }

        for (String category : categorySet) {
            Chip chip = new Chip(this);
            ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(this, null, 0, R.style.Widget_MaterialComponents_Chip_Choice);
            chip.setChipDrawable(chipDrawable);
            chip.setCheckedIconVisible(true);
            chip.setText(category);
            categoryChipGroup.addView(chip);
        }

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
     * When the search button is pressed, create a json object with the search string
     * then send the json object in an intent to the SearchResultsActivity
     * @param view the button which is pressed
     */
    public void viewSearchResults(View view) {
        String searchString = ((EditText)findViewById(R.id.searchField)).getText().toString();
        boolean searchTitle = ((MaterialCheckBox) findViewById(R.id.titleCheckBox)).isChecked();
        boolean searchIngredients = ((MaterialCheckBox) findViewById(R.id.ingredientsCheckBox)).isChecked();
        boolean searchInstructions = ((MaterialCheckBox) findViewById(R.id.instructionsCheckBox)).isChecked();
        boolean searchComments = ((MaterialCheckBox) findViewById(R.id.commentsCheckBox)).isChecked();
        boolean searchOnlyFavorites = ((SwitchMaterial) findViewById(R.id.favoriteSearchSwitch)).isChecked();

        Set<String> checkedCategories = new TreeSet<>();
        if(categoryChipGroup.getChildCount() > 0) {
            for (int i=0; i < categoryChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) categoryChipGroup.getChildAt(i);
                if (chip.isChecked()) {
                    checkedCategories.add(chip.getText().toString());
                }
            }
        }

        boolean shouldFilterAllCategories = false;
        RadioGroup categoriesRadioGroup = findViewById(R.id.categoryRadioGroup);
        if (categoriesRadioGroup.getCheckedRadioButtonId() == R.id.allCategoriesRadioButton) {
            shouldFilterAllCategories = true;
        }

        try {
            JSONObject filter = new JSONObject();

            filter.put("searchString", searchString);
            filter.put("searchTitle", searchTitle);
            filter.put("searchIngredients", searchIngredients);
            filter.put("searchInstructions", searchInstructions);
            filter.put("searchComments", searchComments);
            filter.put("searchOnlyFavorites", searchOnlyFavorites);

            filter.put("shouldFilterAllCategories", shouldFilterAllCategories);
            JSONArray categoriesArray = new JSONArray(checkedCategories);
            filter.put("categories", categoriesArray);

            Intent intent = new Intent(this, SearchResultsActivity.class);
            intent.putExtra("JSONObject", filter.toString());
            startActivity(intent);
        } catch (Exception e) {
            Log.e("ERROR", "" + e.getLocalizedMessage());
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
