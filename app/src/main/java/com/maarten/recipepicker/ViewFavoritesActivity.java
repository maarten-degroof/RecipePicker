package com.maarten.recipepicker;

import android.content.Intent;
import com.maarten.recipepicker.Adapters.FavoriteAdapter;
import com.maarten.recipepicker.Settings.SettingsActivity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import static com.maarten.recipepicker.MainActivity.recipeList;

public class ViewFavoritesActivity extends AppCompatActivity {
    private FavoriteAdapter adapter;

    private ListView listViewFavorites;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_favorites);

        listViewFavorites = findViewById(R.id.favoriteList);

        adapter = new FavoriteAdapter(this, recipeList);
        listViewFavorites.setAdapter(adapter);
        ViewFavoritesActivity.this.adapter.getFilter().filter("");

        listViewFavorites.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Recipe clickedRecipe = adapter.getItem(position);
                viewRecipe(clickedRecipe);
            }


        });


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Favorites");
        setSupportActionBar(toolbar);


    }

    /**
     * opens the RecipeViewActivity with the given recipe-data
     **/
    public void viewRecipe(Recipe recipe) {
        Intent intent = new Intent(this, ViewRecipeActivity.class);
        //intent.putExtra("objectName", object);
        intent.putExtra("Recipe", recipe);
        startActivity(intent);
    }

    /**
     * When activity resumes, update the adapter + filter again to get the correct results
     */
    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        ViewFavoritesActivity.this.adapter.getFilter().filter("");
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
