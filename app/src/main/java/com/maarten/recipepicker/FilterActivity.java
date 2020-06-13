package com.maarten.recipepicker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.appyvet.materialrangebar.RangeBar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.maarten.recipepicker.RecipeUtility.generateCategoryList;

public class FilterActivity extends AppCompatActivity {

    private int minCookedValue = 0;
    private int maxCookedValue = 2;

    private int minRatingValue = 0;
    private int maxRatingValue = 10;

    private ChipGroup categoryChipGroup;

    // Keeps track of which chips are ticked. Duration: [short, medium = default, long]
    //  Difficulty: [beginner, intermediate = default, expert]
    Boolean[] durationArray = {false, true, false};
    Boolean[] difficultyArray = {false, true, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Filter");
        setSupportActionBar(toolbar);

        // This takes care of the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RangeBar amountCookedRangeBar = findViewById(R.id.amountCookedRangeBar);
        final TextView minAmountCooked = findViewById(R.id.minAmountCooked);
        final TextView maxAmountCooked = findViewById(R.id.maxAmountCooked);

        if(MainActivity.recipeList != null && MainActivity.recipeList.size() > 0) {
            // Get the max and minimum values to default the slider to
            int tempAmount = Collections.max(MainActivity.recipeList, new AmountCookedComparator()).getAmountCooked();
            // Only change the max if it's bigger than 2, if lower there won't be enough ticks and it will crash!
            if (tempAmount > 2) {
                maxCookedValue = tempAmount;
            }
            maxAmountCooked.setText(String.valueOf(maxCookedValue));
        }

        amountCookedRangeBar.setTickStart(minCookedValue);
        amountCookedRangeBar.setTickEnd(maxCookedValue);

        // Change listener so the textViews on the sides update with the current values
        amountCookedRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                minAmountCooked.setText(leftPinValue);
                minCookedValue = Integer.parseInt(leftPinValue);
                maxAmountCooked.setText(rightPinValue);
                maxCookedValue = Integer.parseInt(rightPinValue);
            }

            @Override
            public void onTouchStarted(RangeBar rangeBar) {
            }

            @Override
            public void onTouchEnded(RangeBar rangeBar) {
            }
        });

        RangeBar ratingRangeBar = findViewById(R.id.ratingRangeBar);
        final TextView minRating = findViewById(R.id.minRating);
        final TextView maxRating = findViewById(R.id.maxRating);

        ratingRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                minRatingValue = Integer.parseInt(leftPinValue);
                if(Integer.parseInt(leftPinValue) == 0){
                   minRating.setText(getString(R.string.no_rating));
                } else {
                    minRating.setText(leftPinValue);
                }
                maxRatingValue = Integer.parseInt(rightPinValue);
                if(Integer.parseInt(rightPinValue) == 0) {
                    maxRating.setText(getString(R.string.no_rating));
                } else {
                    maxRating.setText(rightPinValue);
                }
            }

            @Override
            public void onTouchStarted(RangeBar rangeBar) {
            }

            @Override
            public void onTouchEnded(RangeBar rangeBar) {
            }
        });

        // Change listener for each chip, so it will update the durationArray which keeps track which buttons
        // are checked. Has to be this ugly since you can't ask the chipGroup which ones are ticked
        Chip shortDurationChip = findViewById(R.id.shortDurationChip);
        shortDurationChip.setOnCheckedChangeListener((buttonView, isChecked) -> durationArray[0] = isChecked);

        Chip mediumDurationChip = findViewById(R.id.mediumDurationChip);
        mediumDurationChip.setOnCheckedChangeListener((buttonView, isChecked) -> durationArray[1] = isChecked);

        Chip longDurationChip = findViewById(R.id.longDurationChip);
        longDurationChip.setOnCheckedChangeListener((buttonView, isChecked) -> durationArray[2] = isChecked);

        Chip beginnerDifficultyChip = findViewById(R.id.beginnerDifficultyChip);
        beginnerDifficultyChip.setOnCheckedChangeListener((buttonView, isChecked) -> difficultyArray[0] = isChecked);

        Chip intermediateDifficultyChip = findViewById(R.id.intermediateDifficultyChip);
        intermediateDifficultyChip.setOnCheckedChangeListener((buttonView, isChecked) -> difficultyArray[1] = isChecked);

        Chip expertDifficultyChip = findViewById(R.id.expertDifficultyChip);
        expertDifficultyChip.setOnCheckedChangeListener((buttonView, isChecked) -> difficultyArray[2] = isChecked);


        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        List<String> categoryList = generateCategoryList();

        for (String category : categoryList) {
            Chip chip = new Chip(this);
            ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(this, null, 0, R.style.Widget_MaterialComponents_Chip_Choice);
            chip.setChipDrawable(chipDrawable);
            chip.setCheckedIconVisible(true);
            chip.setText(category);
            categoryChipGroup.addView(chip);
        }
    }

    /**
     * When the filter button is pressed, create a json object with all the filter requirements
     * then send the json object in an intent to the FilteredResultsActivity
     * @param view the button which is pressed
     */
    public void viewFilteredResults(View view) {
        boolean shouldFilterAllCategories = false;
        RadioGroup categoriesRadioGroup = findViewById(R.id.categoryRadioGroup);
        if (categoriesRadioGroup.getCheckedRadioButtonId() == R.id.allCategoriesRadioButton) {
            shouldFilterAllCategories = true;
        }

        List<String> checkedCategories = new ArrayList<>();
        if(categoryChipGroup.getChildCount() > 0) {
            for (int i=0; i < categoryChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) categoryChipGroup.getChildAt(i);
                if (chip.isChecked()) {
                    checkedCategories.add(chip.getText().toString());
                }
            }
        }

        try {
            JSONObject filter = new JSONObject();
            filter.put("filterMin", minCookedValue);
            filter.put("filterMax", maxCookedValue);
            filter.put("ratingMin", minRatingValue);
            filter.put("ratingMax", maxRatingValue);

            filter.put("durationShort", durationArray[0]);
            filter.put("durationMedium", durationArray[1]);
            filter.put("durationLong", durationArray[2]);

            filter.put("difficultyBeginner", difficultyArray[0]);
            filter.put("difficultyIntermediate", difficultyArray[1]);
            filter.put("difficultyExpert", difficultyArray[2]);

            filter.put("shouldFilterAllCategories", shouldFilterAllCategories);
            JSONArray categoriesArray = new JSONArray(checkedCategories);
            filter.put("categories", categoriesArray);

            Intent intent = new Intent(this, FilteredResultsActivity.class);
            intent.putExtra("JSONObject", filter.toString());
            startActivity(intent);
        } catch(Exception e) {
            Log.e("ERROR", "" + e.getLocalizedMessage());
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
     * Opens the main activity and closes the previous activities
     */
    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Opens the FilterIngredientsActivity
     * @param view the 'filter on ingredients' button
     */
    public void openFilterIngredientsActivity(View view) {
        Intent intent = new Intent(this, FilterIngredientsActivity.class);
        startActivity(intent);
    }
}
