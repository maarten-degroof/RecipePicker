package com.maarten.recipepicker;

import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import com.maarten.recipepicker.Adapters.FavoriteAdapter;
import com.maarten.recipepicker.Settings.SettingsActivity;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import static com.maarten.recipepicker.MainActivity.recipeList;

public class ViewFavoritesActivity extends AppCompatActivity {
    private FavoriteAdapter adapter;

    private ListView listViewFavorites;


    private DrawerLayout dl;
    private ActionBarDrawerToggle abdt;


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


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Favorites");
        setSupportActionBar(toolbar);

        // all the navigation drawer stuff
        dl = findViewById(R.id.dl);
        abdt = new ActionBarDrawerToggle(this, dl,R.string.Open, R.string.Close);
        abdt.setDrawerIndicatorEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dl.addDrawerListener(abdt);
        abdt.syncState();

        NavigationView nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                if (id == R.id.addRecipe) {
                    addRecipe();
                }
                else if (id == R.id.favorites) {
                    viewFavorites();
                }
                else if (id == R.id.settings) {
                    viewSettings();
                }
                return true;
            }
        });
    }

    /**
     *  this connects the hamburger icon to the navigation drawer
     * @param item prob the hamburger icon i dunno
     * @return apparently a boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
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
     *  opens the AddRecipeActivity
     */
    private void addRecipe() {
        Intent intent = new Intent (this, AddRecipeActivity.class);
        startActivity(intent);
    }

    /**
     * opens ViewFavoritesActivity
     */
    private void viewFavorites() {
        Intent intent = new Intent(this, ViewFavoritesActivity.class);
        startActivity(intent);
    }

    /**
     * opens ViewSettingsActivity
     */
    private void viewSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
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
}
