package com.maarten.recipepicker;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.maarten.recipepicker.Models.Ingredient;
import com.maarten.recipepicker.Models.Instruction;
import com.maarten.recipepicker.Models.Recipe;
import com.maarten.recipepicker.adapters.RecipeAdapter;
import com.maarten.recipepicker.enums.CookTime;
import com.maarten.recipepicker.enums.Difficulty;
import com.maarten.recipepicker.listSorters.AmountCookedSorter;
import com.maarten.recipepicker.listSorters.DateSorter;
import com.maarten.recipepicker.settings.SettingsActivity;

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
 * Performance issue when having a longer list with pictures
 * -> can possibly be solved by having transparent field in recipe which holds the bitmap -> load this when starting the app
 *
 * when viewing favorites, the sort by feature doesn't work
 *
 * Timers aren't started on the same millisecond
 *
 * Something is wrong when asking file permissions
 *
 * When adding an instruction with a timer, if you type the number, the last typed number will not be saved
 *
 ********* THINGS TO MAKE *********
 * give optional checkbox when removing all items, to put the dummy values back
 *
 * archiving an item (by swiping the listitem)
 * reordering a list (by dragging)
 * material design everywhere
 * Tags
 * Splitting the ingredients into categories
 * settings
 *    - Dark theme
 * filter on ingredients
 *
 * Add each step + a timer to give you a notification when you should do the next step
 *
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
 * servings
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
                Log.d("WRITE", "Couldn't read file");
                e.printStackTrace();
            }
        }
        // if the file doesn't exist, create an arrayList with dummy value
        else {
            insertDummyRecipes();
        }

        // initialise the images for each recipe so the adapter won't have to do the calculations again
        for(Recipe recipe : recipeList) {
            recipe.getImage();
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

    public static void insertDummyRecipes() {
        List<Ingredient> dummyIngredientList = new ArrayList<>();
        dummyIngredientList.add(new Ingredient("Spaghetti",500.0,Ingredient.type.grams));
        dummyIngredientList.add(new Ingredient("Minced meat",350.0,Ingredient.type.grams));
        dummyIngredientList.add(new Ingredient("Tomatoes",3.0,Ingredient.type.empty));
        dummyIngredientList.add(new Ingredient("Paprika's",3.0,Ingredient.type.empty));
        dummyIngredientList.add(new Ingredient("Water",100.0,Ingredient.type.millimetres));
        dummyIngredientList.add(new Ingredient("A bit of salt and pepper",null,Ingredient.type.empty));

        String dummyImage =  String.valueOf(R.drawable.spaghetti_bolognese);
        String dummyURL = "https://www.jamieoliver.com/recipes/beef-recipes/spaghetti-bolognese/";
        String dummyComments = "Really easy to make!\n\nOnly be sure not to cook the spaghetti too long.";

        ArrayList<Instruction> tempInstructionList = new ArrayList<>();
        tempInstructionList.add(new Instruction("Cook the spaghetti.",(long) 600000));
        tempInstructionList.add(new Instruction("Bake the minced meat.", null));
        tempInstructionList.add(new Instruction("Cut the tomatoes and the paprika into pieces.", null));
        tempInstructionList.add(new Instruction("Once the minced meat is done, throw the paprika and tomatoes in the same pan and bake them together. Spice with salt and pepper.", (long) 300000));
        tempInstructionList.add(new Instruction("Mix the sauce with the spaghetti.", (long) 10000));

        recipeList.add(new Recipe("Spaghetti Bolognese for people who don't have a lot of time",dummyIngredientList,
                false, CookTime.MEDIUM, dummyImage, dummyURL, Difficulty.BEGINNER, dummyComments, tempInstructionList, 4));
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
