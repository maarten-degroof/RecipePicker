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
import android.view.View;
import android.widget.TextView;

import com.maarten.recipepicker.adapters.SearchAdapter;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;
import static com.maarten.recipepicker.MainActivity.recipeList;

public class SearchResultsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSearched;
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

        recyclerViewSearched = findViewById(R.id.listViewSearched);

        adapter = new SearchAdapter(this, recipeList);
        recyclerViewSearched.setAdapter(adapter);
        recyclerViewSearched.setLayoutManager(new LinearLayoutManager(this));

        adapter.getFilter().filter(searchString);

        // write the text to say for which times cooked you have filtered. FromHtml is used to make the searched string bold
        TextView searchedDescriptionTextView = findViewById(R.id.searchedDescriptionTextField);
        String description = getString(R.string.searched_recipe_description, searchString);
        searchedDescriptionTextView.setText(Html.fromHtml(description, FROM_HTML_MODE_LEGACY));
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
