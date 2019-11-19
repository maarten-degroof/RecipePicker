package com.maarten.recipepicker;

import android.content.Intent;
import com.maarten.recipepicker.adapters.FavoriteAdapter;
import com.maarten.recipepicker.listSorters.AmountCookedSorter;
import com.maarten.recipepicker.listSorters.DateSorter;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Collections;

import static com.maarten.recipepicker.MainActivity.recipeList;

public class ViewFavoritesActivity extends AppCompatActivity {

    private FavoriteAdapter adapter;

    private RecyclerView listViewFavorites;
    private Spinner sortSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_favorites);

        listViewFavorites = findViewById(R.id.listViewFavorites);

        adapter = new FavoriteAdapter(this, recipeList);

        listViewFavorites.setAdapter(adapter);
        listViewFavorites.setLayoutManager(new LinearLayoutManager(this));

        adapter.getFilter().filter("");


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Favorites");
        setSupportActionBar(toolbar);


        // get the spinner
        sortSpinner = findViewById(R.id.sortSpinner);

        // create the spinner adapter with the choices + the standard views of how it should look like
        ArrayAdapter<CharSequence> sortTypeAdapter = ArrayAdapter.createFromResource(this, R.array.sort_types_array_items, android.R.layout.simple_spinner_item);
        sortTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortTypeAdapter);


        /**
         * Takes care of the sort functions. Sorts the list based on the chosen item in the spinner.
         * order of the spinner:
         *      - chronological (0)
         *      - times cooked  (1)
         */
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch ((int) id) {
                    case 0:
                        Collections.sort(recipeList, new DateSorter());
                        adapter.notifyDataSetChanged();
                        return;
                    case 1:
                        Collections.sort(recipeList, new AmountCookedSorter());
                        adapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /**
     * When activity resumes, update the adapter + filter again to get the correct results
     */
    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        adapter.getFilter().filter("");
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
