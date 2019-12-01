package com.maarten.recipepicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.appyvet.materialrangebar.RangeBar;
import com.google.android.material.chip.Chip;

import java.util.Collections;

import android.view.View;

import org.json.JSONObject;

public class FilterActivity extends AppCompatActivity {

    int minCookedValue = 0;
    int maxCookedValue = 2;

    int minRatingValue = 0;
    int maxRatingValue = 10;

    // keeps track of which chips are ticked. Duration: [short, medium = default, long]
    //  Difficulty: [beginner, intermediate = default, expert]
    Boolean[] durationArray = {false, true, false};
    Boolean[] difficultyArray = {false, true, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Filter");
        setSupportActionBar(toolbar);

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        RangeBar amountCookedRangeBar = findViewById(R.id.amountCookedRangeBar);
        final TextView minAmountCooked = findViewById(R.id.minAmountCooked);
        final TextView maxAmountCooked = findViewById(R.id.maxAmountCooked);

        if(MainActivity.recipeList != null && MainActivity.recipeList.size() > 0) {
            // get the max and minimum values to default the slider to
            int tempAmount = Collections.max(MainActivity.recipeList, new AmountCookedComparator()).getAmountCooked();
            // only change the max if it's bigger than 2, if lower there won't be enough ticks and it will crash!
            if (tempAmount > 2) {
                maxCookedValue = tempAmount;
            }
            maxAmountCooked.setText(String.valueOf(maxCookedValue));
        }

        amountCookedRangeBar.setTickStart(minCookedValue);
        amountCookedRangeBar.setTickEnd(maxCookedValue);

        // change listener so the textViews on the sides update with the current values
        amountCookedRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                minAmountCooked.setText(leftPinValue);
                minCookedValue = Integer.valueOf(leftPinValue);
                maxAmountCooked.setText(rightPinValue);
                maxCookedValue = Integer.valueOf(rightPinValue);
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
                minRatingValue = Integer.valueOf(leftPinValue);
                if(Integer.valueOf(leftPinValue) == 0){
                   minRating.setText(getString(R.string.no_rating));
                } else {
                    minRating.setText(leftPinValue);
                }
                maxRatingValue = Integer.valueOf(rightPinValue);
                if(Integer.valueOf(rightPinValue) == 0) {
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

        // change listener for each chip, so it will update the durationArray which keeps track which buttons
        // are checked. Has to be this ugly since you can't ask the chipgroup which ones are ticked
        Chip shortDurationChip = findViewById(R.id.shortDurationChip);
        shortDurationChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                durationArray[0] = isChecked;
            }
        });
        Chip mediumDurationChip = findViewById(R.id.mediumDurationChip);
        mediumDurationChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                durationArray[1] = isChecked;
            }
        });
        Chip longDurationChip = findViewById(R.id.longDurationChip);
        longDurationChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                durationArray[2] = isChecked;
            }
        });

        Chip beginnerDifficultyChip = findViewById(R.id.beginnerDifficultyChip);
        beginnerDifficultyChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                difficultyArray[0] = isChecked;
            }
        });
        Chip intermediateDifficultyChip = findViewById(R.id.intermediateDifficultyChip);
        intermediateDifficultyChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                difficultyArray[1] = isChecked;
            }
        });
        Chip expertDifficultyChip = findViewById(R.id.expertDifficultyChip);
        expertDifficultyChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                difficultyArray[2] = isChecked;
            }
        });
    }

    /**
     * when the filter button is pressed, create a json object with all the filter requirements
     * then send the json object in an intent to the FilteredResultsActivity
     * @param view - the button which is pressed
     */
    public void viewFilteredResults(View view) {
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

            Intent intent = new Intent(this, FilteredResultsActivity.class);
            intent.putExtra("JSONObject", filter.toString());
            startActivity(intent);
        } catch(Exception e) {
            Log.d("ERROR", e.getLocalizedMessage());
        }
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
}
