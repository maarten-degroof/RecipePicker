package com.maarten.recipepicker;


import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import com.maarten.recipepicker.Adapters.RecipeAdapter;
import com.maarten.recipepicker.ListSorters.AmountCookedSorter;
import com.maarten.recipepicker.ListSorters.DateSorter;
import com.maarten.recipepicker.Settings.SettingsActivity;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
 * After opening a favorite recipe and going back, goes back to main activity instead of favorites
 *
 * when removing all ingredients; doesn't show the text to at least add one ingredient again
 *
 * Nav bar (the icons) behave unexpectedly when a second text color is given!
 *
 * Toolbar text and back arrow is in black instead of in white
 *
 * !!!home page is not scrollable!!!
 *
 * sort by is not automatically updated, have to 'set' the times cooked value again in order to see the difference
 * -> can possibly be fixed by putting an extra check in onResume()
 ********* THINGS TO MAKE *********
 * images
 * archiving an item (by swiping the listitem)
 * reordering a list (by dragging)
 * material design everywhere
 * Tags
 * Splitting the ingredients into categories
 * settings
 *    - Dark theme
 * CHANGE LISTVIEW TO THE NEWER BETTER RECYCLERVIEW!
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
 **/


public class MainActivity extends AppCompatActivity {

    public static List<Recipe> recipeList;
    private RecipeAdapter adapter;

    private ListView listViewRecipes;

    private DrawerLayout dl;
    private ActionBarDrawerToggle abdt;

    private Spinner sortSpinner;

    // create activity and the UI links with it
    // only runs ONCE when the application starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.AppTheme);

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
            dummyIngredientList.add(new Ingredient("Tomatoes",3.0,Ingredient.type.items));
            dummyIngredientList.add(new Ingredient("Paprika",3.0,Ingredient.type.items));
            dummyIngredientList.add(new Ingredient("Salt and pepper",null,Ingredient.type.items));

            recipeList.add(new Recipe("First cook the spaghetti.\n\nSecondly you bake the minced meat.\nCut the tomatoes and the paprika into pieces.\nOnce the minced meat is done, thow the paprika and tomatoes in the same pan and bake them together. Spice it with salt and pepper.\n\nOnce everything is ready, mix it together with the spaghetti and you're done.","Spaghetti Bolognese for people who don't have a lot of time",dummyIngredientList,false, 0, CookTime.MEDIUM, null));
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
        listViewRecipes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewRecipe(recipeList.get(position));
            }
        });


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);
        getSupportActionBar().getThemedContext();

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
    }

    // this connects the hamburger icon to the navigation drawer
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     *  opens the AddRecipeActivity
     */
    private void addRecipe() {
        Intent intent = new Intent (this, AddRecipeActivity.class);
        startActivity(intent);
    }

    /**
     * opens the RecipeViewActivity with the given recipe-data
     * @param recipe recipe which will be used to show
     */
    private void viewRecipe(Recipe recipe) {
        Intent intent = new Intent(this, ViewRecipeActivity.class);
         //intent.putExtra("objectName", object);
        intent.putExtra("Recipe", recipe);
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
