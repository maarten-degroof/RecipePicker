package com.maarten.recipepicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.maarten.recipepicker.Adapters.FilterAdapter;
import com.maarten.recipepicker.Adapters.SearchAdapter;

import static com.maarten.recipepicker.MainActivity.recipeList;

public class SearchResultsActivity extends AppCompatActivity {

    private ListView listViewSearched;
    private SearchAdapter adapter;
    private String searchString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Search Results");
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                finish();
            }
        });


        Intent intent = getIntent();
        searchString = intent.getExtras().getString("searchString");

        listViewSearched = findViewById(R.id.listViewSearched);

        adapter = new SearchAdapter(this, recipeList);
        listViewSearched.setAdapter(adapter);
        SearchResultsActivity.this.adapter.getFilter().filter(searchString);

        listViewSearched.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Recipe clickedRecipe = adapter.getItem(position);
                viewRecipe(clickedRecipe);
            }
        });


        TextView searchedDescriptionTextView = findViewById(R.id.searchedDescriptionTextField);
        searchedDescriptionTextView.setText("Showing all recipes that match your search: " + searchString);
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

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        this.adapter.getFilter().filter(searchString);
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
}
