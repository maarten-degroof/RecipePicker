package com.maarten.recipepicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
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

import org.json.JSONObject;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;
import static com.maarten.recipepicker.MainActivity.recipeList;

public class FilteredResultsActivity extends AppCompatActivity {

    private FilterAdapter filterAdapter;

    private RecyclerView listViewFiltered;

    private int filterMin, filterMax;
    private int ratingMin, ratingMax;
    private Boolean durationShort, durationMedium, durationLong;
    private Boolean difficultyBeginner, difficultyIntermediate, difficultyExpert;

    private JSONObject filterObject;

    private MaterialButton addRecipeButton;
    private TextView noRecipesTextView;

    private int amountOfItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_results);
        amountOfItems = 0;

        try {
            Intent intent = getIntent();
            filterObject = new JSONObject(intent.getStringExtra("JSONObject"));

            // get the values for which you filtered
            filterMin = (int) filterObject.get("filterMin");
            filterMax = (int) filterObject.get("filterMax");
            ratingMin = (int) filterObject.get("ratingMin");
            ratingMax = (int) filterObject.get("ratingMax");
            durationShort = (Boolean) filterObject.get("durationShort");
            durationMedium = (Boolean) filterObject.get("durationMedium");
            durationLong = (Boolean) filterObject.get("durationLong");
            difficultyBeginner = (Boolean) filterObject.get("difficultyBeginner");
            difficultyIntermediate = (Boolean) filterObject.get("difficultyIntermediate");
            difficultyExpert = (Boolean) filterObject.get("difficultyExpert");

            listViewFiltered = findViewById(R.id.listViewFiltered);

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

        ChipGroup chipGroup = findViewById(R.id.chipGroup);
        if (durationShort) {
            Chip chip = new Chip(this);
            chip.setText(getString(R.string.duration_short));
            chip.layout(5, 5, 5, 5);
            chipGroup.addView(chip);
        } if (durationMedium) {
            Chip chip = new Chip(this);
            chip.setText(getString(R.string.duration_medium));
            chip.layout(5, 5, 5, 5);
            chipGroup.addView(chip);
        } if (durationLong) {
            Chip chip = new Chip(this);
            chip.setText(getString(R.string.duration_long));
            chip.layout(5, 5, 5, 5);
            chipGroup.addView(chip);
        } if (difficultyBeginner) {
            Chip chip = new Chip(this);
            chip.setText(getString(R.string.beginner));
            chip.layout(5, 5, 5, 5);
            chipGroup.addView(chip);
        } if (difficultyIntermediate) {
            Chip chip = new Chip(this);
            chip.setText(getString(R.string.intermediate));
            chip.layout(5, 5, 5, 5);
            chipGroup.addView(chip);
        } if(difficultyExpert) {
            Chip chip = new Chip(this);
            chip.setText(getString(R.string.expert));
            chip.layout(5, 5, 5, 5);
            chipGroup.addView(chip);
        }

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
        filterAdapter = new FilterAdapter(this, recipeList);
        listViewFiltered.setAdapter(filterAdapter);
        amountOfItems = filterAdapter.filterAndReturnAmount(filterObject.toString());
        Log.d("COUNT", "in onResume: "+amountOfItems);
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
