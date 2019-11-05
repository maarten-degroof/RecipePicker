package com.maarten.recipepicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.maarten.recipepicker.Adapters.FilterAdapter;

import org.json.JSONObject;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;
import static com.maarten.recipepicker.MainActivity.recipeList;

public class FilteredResultsActivity extends AppCompatActivity {

    private FilterAdapter adapter;

    private RecyclerView listViewFiltered;

    private int filterMin, filterMax;
    private Boolean durationShort, durationMedium, durationLong;

    private JSONObject filterObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_results);

        try {
            Intent intent = getIntent();
            filterObject = new JSONObject(intent.getStringExtra("JSONObject"));

            //Log.d("JSON", filterObject.toString());

            // get the values for which you filtered
            filterMin = (int) filterObject.get("filterMin");
            filterMax = (int) filterObject.get("filterMax");
            durationShort = (Boolean) filterObject.get("durationShort");
            durationMedium = (Boolean) filterObject.get("durationMedium");
            durationLong = (Boolean) filterObject.get("durationLong");

            listViewFiltered = findViewById(R.id.listViewFiltered);

            adapter = new FilterAdapter(this, recipeList);
            listViewFiltered.setAdapter(adapter);
            listViewFiltered.setLayoutManager(new LinearLayoutManager(this));

            FilteredResultsActivity.this.adapter.getFilter().filter(filterObject.toString());

        } catch (Exception e) {
            e.getStackTrace();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Filter Results");
        setSupportActionBar(toolbar);

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // write the text to say for which times cooked you have filtered. FromHtml is used to make the numbers bold
        TextView filteredDescriptionTextView = findViewById(R.id.filteredDescriptionTextField);
        String description = getString(R.string.filtered_recipe_description, filterMin, filterMax);
        filteredDescriptionTextView.setText(Html.fromHtml(description, FROM_HTML_MODE_LEGACY));

        ChipGroup chipGroup = findViewById(R.id.chipGroup);
        if(durationShort) {
            Chip chip = new Chip(this);
            chip.setText(getString(R.string.duration_short));
            chip.layout(5,5,5,5);
            chipGroup.addView(chip);
        } if(durationMedium) {
            Chip chip = new Chip(this);
            chip.setText(getString(R.string.duration_medium));
            chip.layout(5,5,5,5);
            chipGroup.addView(chip);
        } if(durationLong) {
            Chip chip = new Chip(this);
            chip.setText(getString(R.string.duration_long));
            chip.layout(5,5,5,5);
            chipGroup.addView(chip);
        }
    }

    /**
     * When activity resumes, update the adapter + filter again to get the correct results, in case you changed something in the recipes
     */
    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        FilteredResultsActivity.this.adapter.getFilter().filter(filterObject.toString());
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
