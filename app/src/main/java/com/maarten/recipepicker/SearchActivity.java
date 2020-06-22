package com.maarten.recipepicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.maarten.recipepicker.models.Recipe;
import com.maarten.recipepicker.viewModels.SearchViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Set;
import java.util.TreeSet;

import static com.maarten.recipepicker.RecipeUtility.changeFirstLetterToCapital;

public class SearchActivity extends AppCompatActivity {

    private ChipGroup categoryChipGroup;
    private EditText searchEditText;
    private MaterialCheckBox titleCheckBox, ingredientsCheckBox, instructionsCheckBox, commentsCheckBox;
    private SwitchMaterial favoriteSearchSwitch;
    private RadioGroup categoryRadioGroup;

    private SearchViewModel viewModel;


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

        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);

        searchEditText = findViewById(R.id.searchNameEditText);
        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                }
            }
        });

        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        Set<String> categorySet = new TreeSet<>();

        for (Recipe recipe : MainActivity.recipeList) {
            for (String category : recipe.getCategories()) {
                String current_category = changeFirstLetterToCapital(category);
                categorySet.add(current_category);
            }
        }

        Set<String> selectedCategories = viewModel.getSelectedCategories();

        for (String category : categorySet) {
            Chip chip = new Chip(this);
            ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(this, null, 0, R.style.Widget_MaterialComponents_Chip_Choice);
            chip.setChipDrawable(chipDrawable);
            chip.setCheckedIconVisible(true);
            chip.setText(category);
            // Add listener so the keyboard will hide when you press a category
            chip.setOnCheckedChangeListener((compoundButton, b) -> searchEditText.clearFocus());

            if (selectedCategories.contains(category)) {
                chip.setChecked(true);
            }

            categoryChipGroup.addView(chip);
        }

        titleCheckBox = findViewById(R.id.titleCheckBox);
        titleCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> searchEditText.clearFocus());

        ingredientsCheckBox = findViewById(R.id.ingredientsCheckBox);
        ingredientsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> searchEditText.clearFocus());

        instructionsCheckBox = findViewById(R.id.instructionsCheckBox);
        instructionsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> searchEditText.clearFocus());

        commentsCheckBox = findViewById(R.id.commentsCheckBox);
        commentsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> searchEditText.clearFocus());

        favoriteSearchSwitch = findViewById(R.id.favoriteSearchSwitch);
        favoriteSearchSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> searchEditText.clearFocus());

        categoryRadioGroup = findViewById(R.id.categoryRadioGroup);
        categoryRadioGroup.setOnCheckedChangeListener((group, checkedId) -> searchEditText.clearFocus());
    }

    @Override
    protected void onStop() {
        super.onStop();

        viewModel.setSelectedCategories(generateCategorySet());
    }

    /**
     * Generates a list of all the checked categories (as strings) from the chipGroup
     * and puts it into a treeSet so they're ordered alphabetically
     * @return returns a treeSet of all the checked categories
     */
    private Set<String> generateCategorySet() {
        Set<String> checkedCategories = new TreeSet<>();
        if(categoryChipGroup.getChildCount() > 0) {
            for (int i=0; i < categoryChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) categoryChipGroup.getChildAt(i);
                if (chip.isChecked()) {
                    checkedCategories.add(chip.getText().toString());
                }
            }
        }
        return checkedCategories;
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
        String searchString = searchEditText.getText().toString();
        boolean searchTitle = titleCheckBox.isChecked();
        boolean searchIngredients = ingredientsCheckBox.isChecked();
        boolean searchInstructions = instructionsCheckBox.isChecked();
        boolean searchComments = commentsCheckBox.isChecked();
        boolean searchOnlyFavorites = favoriteSearchSwitch.isChecked();

        boolean shouldFilterAllCategories = false;
        if (categoryRadioGroup.getCheckedRadioButtonId() == R.id.allCategoriesRadioButton) {
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
            JSONArray categoriesArray = new JSONArray(generateCategorySet());
            filter.put("categories", categoriesArray);

            Intent intent = new Intent(this, SearchResultsActivity.class);
            intent.putExtra("JSONObject", filter.toString());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Oops, something went wrong trying to get all the search information.", Toast.LENGTH_LONG).show();
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
