package com.maarten.recipepicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.maarten.recipepicker.Adapters.FilterAdapter;

import org.json.JSONObject;

import static com.maarten.recipepicker.MainActivity.recipeList;

public class FilteredResultsActivity extends AppCompatActivity {

    private FilterAdapter adapter;

    private ListView listViewFiltered;

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
            FilteredResultsActivity.this.adapter.getFilter().filter(filterObject.toString());

            listViewFiltered.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Recipe clickedRecipe = adapter.getItem(position);
                    viewRecipe(clickedRecipe);
                }
            });

        } catch (Exception e) {
            e.getStackTrace();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Filter Results");
        setSupportActionBar(toolbar);

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView filteredDescriptionTextView = findViewById(R.id.filteredDescriptionTextField);
        filteredDescriptionTextView.setText("Showing all recipes which you have cooked between " + filterMin + " and " + filterMax + " times.");

        ChipGroup chipGroup = findViewById(R.id.chipGroup);

        if(durationShort) {
            Chip chip = new Chip(this);
            chip.setText("Short: -30 min");
            chip.layout(5,5,5,5);
            chipGroup.addView(chip);
        } if(durationMedium) {
            Chip chip = new Chip(this);
            chip.setText("Medium: 30-60 min");
            chip.layout(5,5,5,5);
            chipGroup.addView(chip);
        } if(durationLong) {
            Chip chip = new Chip(this);
            chip.setText("Long: 60+ min");
            chip.layout(5,5,5,5);
            chipGroup.addView(chip);
        }
    }

    /**
     * gets called when you tap a recipe
     * @param recipe - the recipe to open
     */
    public void viewRecipe(Recipe recipe) {
        Intent intent = new Intent(this, ViewRecipeActivity.class);
        //intent.putExtra("objectName", object);
        intent.putExtra("Recipe", recipe);
        startActivity(intent);
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
}
