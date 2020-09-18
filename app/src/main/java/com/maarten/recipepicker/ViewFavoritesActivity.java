package com.maarten.recipepicker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maarten.recipepicker.adapters.FavoriteAdapter;

import static com.maarten.recipepicker.MainActivity.recipeList;

public class ViewFavoritesActivity extends AppCompatActivity {

    private FavoriteAdapter adapter;

    private TextView noFavoritesYetTextView;
    private int amountOfFavorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_favorites);

        RecyclerView favoritesRecyclerView = findViewById(R.id.listViewFavorites);

        adapter = new FavoriteAdapter(this, recipeList);

        favoritesRecyclerView.setAdapter(adapter);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Favorites");
        setSupportActionBar(toolbar);

        noFavoritesYetTextView = findViewById(R.id.noFavoritesYetTextView);
        amountOfFavorites = 0;
        amountOfFavorites = adapter.filterAndReturnAmount();
        controlNoRecipeElements();
    }

    /**
     * This takes care of the 'no found recipes' and 'add recipe' elements when the list is empty
     */
    private void controlNoRecipeElements() {
        if(amountOfFavorites > 0) {
            noFavoritesYetTextView.setVisibility(View.GONE);
        } else {
            noFavoritesYetTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * When activity resumes, update the adapter + filter again to get the correct results
     */
    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        amountOfFavorites = adapter.filterAndReturnAmount();
        controlNoRecipeElements();
    }

    /**
     * Inflates the menu into the toolbar
     * @param menu the menu
     * @return should return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_with_home, menu);
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
}
