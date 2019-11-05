package com.maarten.recipepicker;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.maarten.recipepicker.Adapters.RecipeAdapter;
import com.maarten.recipepicker.Enums.CookTime;
import com.maarten.recipepicker.ListSorters.AmountCookedSorter;
import com.maarten.recipepicker.ListSorters.DateSorter;
import com.maarten.recipepicker.Settings.SettingsActivity;

import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 ********* BUGS *********
 * when removing all ingredients; doesn't show the text to at least add one ingredient again
 *
 * sort by is not automatically updated, have to 'set' the times cooked value again in order to see the difference
 * -> can possibly be fixed by putting an extra check in onResume()
 *
 * Can't show image from drawable yet
 *
 * when viewing favorites, the sort by feature doesn't work
 *
 ********* THINGS TO MAKE *********
 * archiving an item (by swiping the listitem)
 * reordering a list (by dragging)
 * material design everywhere
 * Tags
 * Splitting the ingredients into categories
 * settings
 *    - Dark theme
 * filter on ingredients
 ********* WORKING *********
 * adding recipe
 * saving/loading
 * showing a recipe + ingredients
 * favorites
 * remove
 * partial material design in textInput + buttons
 * remove an ingredient
 * updating recipe
 * Settings:
 *      - remove all recipes
 * times cooked + able to reset counter
 * reordering by times cooked & by date
 * snackbar when adding a cooked time, so it can be undone
 * filtering:
 *      - a slider to choose how many times you have to have cooked it minimum&maximum
 *      - add cooking time with 'chips' so you can filter them
 * search on title and ingredients
 * add home button in all screens
 * images
 **/


public class MainActivity extends AppCompatActivity {

    public static List<Recipe> recipeList;
    private RecipeAdapter adapter;

    private RecyclerView listViewRecipes;

    private DrawerLayout dl;
    private ActionBarDrawerToggle abdt;

    private FloatingActionButton fab;

    private Spinner sortSpinner;

    // create activity and the UI links with it
    // only runs ONCE when the application starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_list);

        // get listView object
        listViewRecipes = findViewById(R.id.listViewFiltered);

        // initialise recipeList
        recipeList = new ArrayList<>();

        // look for the recipelist file
        File file = new File(getFilesDir(), "recipeList.ser");

        // check if the file exists
        if(file.canRead()) {
            try (FileInputStream fileStream = new FileInputStream(file);
                 ObjectInputStream in = new ObjectInputStream(fileStream)) {

                recipeList = (ArrayList<Recipe>) in.readObject();

                Log.d("WRITE", "object recipeList is written: " + recipeList);

            } catch (IOException | ClassNotFoundException e) {
                Log.d("WRITE", "Coundn't read file");
                e.printStackTrace();
            }
        }
        // if the file doesn't exist, create an arrayList with dummy value
        else {
            List<Ingredient> dummyIngredientList = new ArrayList<>();
            dummyIngredientList.add(new Ingredient("Spaghetti",500.0,Ingredient.type.grams));
            dummyIngredientList.add(new Ingredient("Minced meat",350.0,Ingredient.type.grams));
            dummyIngredientList.add(new Ingredient("Tomatoes",3.0,Ingredient.type.empty));
            dummyIngredientList.add(new Ingredient("Paprika's",3.0,Ingredient.type.empty));
            dummyIngredientList.add(new Ingredient("Water",100.0,Ingredient.type.millimetres));
            dummyIngredientList.add(new Ingredient("A bit of salt and pepper",null,Ingredient.type.empty));

            String dummyImage =  "drawable://" + R.drawable.spaghetti_bolognese;
            Log.d("dummy", dummyImage);
            recipeList.add(new Recipe("First cook the spaghetti.\n\nSecondly you bake the minced meat.\nCut the tomatoes and the paprika into pieces.\nOnce the minced meat is done, thow the paprika and tomatoes in the same pan and bake them together. Spice it with salt and pepper.\n\nOnce everything is ready, mix it together with the spaghetti and you're done.","Spaghetti Bolognese for people who don't have a lot of time",dummyIngredientList,false, 0, CookTime.MEDIUM, null, null, null, null));
        }

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
                        return;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        adapter = new RecipeAdapter(this, recipeList);

        listViewRecipes.setAdapter(adapter);
        listViewRecipes.setLayoutManager(new LinearLayoutManager(this));


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);
        getSupportActionBar().getThemedContext();

        // all the navigation drawer stuff
        dl = findViewById(R.id.dl);
        abdt = new ActionBarDrawerToggle(this, dl,R.string.Open, R.string.Close);
        abdt.setDrawerIndicatorEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // make the listview also scrollable
        ViewCompat.setNestedScrollingEnabled(listViewRecipes, true);

        dl.addDrawerListener(abdt);
        abdt.syncState();

        NavigationView nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                if (id == R.id.addRecipe) {
                    addRecipe(null);
                } else if (id == R.id.favorites) {
                    viewFavorites();
                } else if (id == R.id.settings) {
                    viewSettings();
                } else if (id == R.id.filters) {
                    viewFilters();
                } else if (id == R.id.search) {
                    viewSearch();
                }
                return true;
            }
        });

        fab = findViewById(R.id.floating_action_button);

        // Listener hides the floatingActionButton when scrolling & shows it again afterwards
        listViewRecipes.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && fab.isShown())
                    fab.hide();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    fab.show();
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

    }

    // this connects the hamburger icon to the navigation drawer
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     *  opens the AddRecipeActivity
     */
    public void addRecipe(View view) {
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
     * opens ViewFavoritesActivity
     */
    private void viewSearch() {
        Intent intent = new Intent(this, SearchActivity.class);
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
     * opens FilterActivity
     */
    private void viewFilters() {
        Intent intent = new Intent(this, FilterActivity.class);
        startActivity(intent);
    }

    // when focus resumes to THIS activity
    // update the list
    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onStop() {
        super.onStop();

        File file = new File(getFilesDir(), "recipeList.ser");


        try (OutputStream fileStream = new FileOutputStream(file);
             ObjectOutputStream out = new ObjectOutputStream(fileStream)) {

            out.writeObject(recipeList);
            Log.d("WRITE", "object recipelist is written: " + recipeList);

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("WRITE", "object recipelist is NOT written: " + recipeList);
        }

    }
}
