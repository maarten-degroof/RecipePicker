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
import com.google.android.material.chip.ChipGroup;

import java.util.Collections;

import android.view.View;

import org.json.JSONObject;

public class FilterActivity extends AppCompatActivity {


    /**
     * Create new activity for the results; create new adapter and send the items on which to filter
     * to the new activity. You can do so by using an intent and putting a json object through it
     *
     *
     * @param savedInstanceState
     */

    int minCookedValue = 0;
    int maxCookedValue = 2;

    // keeps track of which chips are ticked: [short, medium = default ticked, long]
    Boolean[] durationArray = {false, true, false};


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
            filter.put("durationShort", durationArray[0]);
            filter.put("durationMedium", durationArray[1]);
            filter.put("durationLong", durationArray[2]);

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
