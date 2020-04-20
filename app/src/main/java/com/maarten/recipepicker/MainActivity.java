package com.maarten.recipepicker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.maarten.recipepicker.adapters.RecipeAdapter;
import com.maarten.recipepicker.enums.CookTime;
import com.maarten.recipepicker.enums.Difficulty;
import com.maarten.recipepicker.listSorters.AmountCookedSorter;
import com.maarten.recipepicker.listSorters.DateSorter;
import com.maarten.recipepicker.listSorters.RatingSorter;
import com.maarten.recipepicker.models.Ingredient;
import com.maarten.recipepicker.models.Instruction;
import com.maarten.recipepicker.models.Recipe;
import com.maarten.recipepicker.settings.SettingsActivity;

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
import java.util.Random;
import java.util.Scanner;


/**
 ********* BUGS *********
 * Filter on ingredients: tapping an ingredient in the corners changes the checkbox but doesn't put the value on true
 *
 * Something is wrong when asking file permissions
 *
 * When adding an instruction with a timer, if you type the number, the last typed number will not be saved
 *
 * Improve manifest
 *
 * Check portrait mode everywhere
 *
 * Portrait mode bugs:  - sidebar menu
 *
 ********* THINGS TO MAKE *********
 * create licence + credit used technologies:
 *      Some Icons made by "https://www.flaticon.com/authors/freepik" Freepik from href="https://www.flaticon.com/"
 *
 * archiving an item (by swiping the listitem)
 * reordering a list (by dragging)
 * Tags
 * Splitting the ingredients into categories
 ********* WORKING *********
 * adding recipe
 * saving/loading
 * showing a recipe + ingredients
 * favorites
 * remove
 * material design
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
 *      - on ingredients
 * search on title and ingredients
 * add home button in all screens
 * images
 * servings
 * share + import
 **/


public class MainActivity extends AppCompatActivity {

    public static List<Recipe> recipeList;
    private RecipeAdapter adapter;

    private ActionBarDrawerToggle abdt;

    private Spinner sortSpinner;

    private Random random;

    private MaterialButton addRecipeButton;
    private TextView noRecipesYetTextView;

    /**
     * Creates the activity and initialises all UI-fields
     * Loads the list from memory or creates a new one
     * @param savedInstanceState - A previously saved instance, gets passed onto the super class
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get listView object
        RecyclerView listViewRecipes = findViewById(R.id.mainRecyclerView);

        // initialise recipeList
        recipeList = new ArrayList<>();

        // look for the recipeList file
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
        sortSpinner = findViewById(R.id.orderSpinner);

        // create the spinner adapter with the choices + the standard views of how it should look like
        ArrayAdapter<CharSequence> sortTypeAdapter = ArrayAdapter.createFromResource(this, R.array.order_types_array_items, android.R.layout.simple_spinner_item);
        sortTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortTypeAdapter);


         // Takes care of the sort functions. Sorts the list based on the chosen item in the spinner.
         // order of the spinner:
         //     - chronological (0)
         //     - times cooked  (1)
        //      - rating (2)
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
                    case 2:
                        Collections.sort(recipeList, new RatingSorter());
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
        DrawerLayout dl = findViewById(R.id.dl);
        abdt = new ActionBarDrawerToggle(this, dl,R.string.Open, R.string.Close);
        abdt.setDrawerIndicatorEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // make the recyclerView also scrollable
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
                } else if (id == R.id.filter) {
                    viewFilters();
                } else if (id == R.id.search) {
                    viewSearch();
                } else if (id == R.id.random) {
                    openRandomRecipe();
                }
                return true;
            }
        });

        random = new Random();

        setFact();

        addRecipeButton = findViewById(R.id.addRecipeButton);
        noRecipesYetTextView = findViewById(R.id.noRecipesYetTextView);

        controlNoRecipeElements();
    }

    /**
     * This takes care of the 'no found recipes' and 'add recipe' elements when the list is empty
     */
    private void controlNoRecipeElements() {
        if(recipeList.size() > 0) {
            addRecipeButton.setVisibility(View.GONE);
            noRecipesYetTextView.setVisibility(View.GONE);
        } else {
            addRecipeButton.setVisibility(View.VISIBLE);
            noRecipesYetTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * loads the fact- and tips-file into an arraylist and chooses a random item
     * from that list which it loads into the TextView in the drawer
     */
    private void setFact() {
        TextView factTextView = findViewById(R.id.factTextView);
        ArrayList<String> factList = new ArrayList<>();
        try (Scanner scanner = new Scanner(getResources().getAssets().open("factList.txt"))) {
            while (scanner.hasNextLine()) {
                factList.add(scanner.nextLine());
            }

            factTextView.setText(factList.get(random.nextInt(factList.size())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * inserts the dummy recipe into the recipeList
     */
    public static void insertDummyRecipes() {
        List<Ingredient> spaghettiBologneseIngredientList = new ArrayList<>();
        spaghettiBologneseIngredientList.add(new Ingredient("Spaghetti",500.0,Ingredient.type.grams));
        spaghettiBologneseIngredientList.add(new Ingredient("Minced meat",350.0,Ingredient.type.grams));
        spaghettiBologneseIngredientList.add(new Ingredient("Tomatoes",3.0,Ingredient.type.empty));
        spaghettiBologneseIngredientList.add(new Ingredient("Paprika's",3.0,Ingredient.type.empty));
        spaghettiBologneseIngredientList.add(new Ingredient("Water",100.0,Ingredient.type.millimetres));
        spaghettiBologneseIngredientList.add(new Ingredient("A bit of salt and pepper",null,Ingredient.type.empty));

        String spaghettiBologneseImage =  String.valueOf(R.drawable.spaghetti_bolognese);
        String spaghettiBologneseURL = "https://www.jamieoliver.com/recipes/beef-recipes/spaghetti-bolognese/";
        String SpaghettiBologneseComments = "Really easy to make!\n\nOnly be sure not to cook the spaghetti too long.";

        ArrayList<Instruction> spaghettiBologneseInstrutionList = new ArrayList<>();
        spaghettiBologneseInstrutionList.add(new Instruction("Cook the spaghetti.",(long) 600000));
        spaghettiBologneseInstrutionList.add(new Instruction("Bake the minced meat.", null));
        spaghettiBologneseInstrutionList.add(new Instruction("Cut the tomatoes and the paprika into pieces.", null));
        spaghettiBologneseInstrutionList.add(new Instruction("Once the minced meat is done, throw the paprika and tomatoes in the same pan and bake them together. Spice with salt and pepper.", (long) 300000));
        spaghettiBologneseInstrutionList.add(new Instruction("Mix the sauce with the spaghetti.", (long) 10000));

        recipeList.add(new Recipe("Spaghetti Bolognese for people who don't have a lot of time",spaghettiBologneseIngredientList,
                false, CookTime.MEDIUM, spaghettiBologneseImage, spaghettiBologneseURL, Difficulty.BEGINNER, SpaghettiBologneseComments, spaghettiBologneseInstrutionList, 4));
    }

    /**
     * this connects the hamburger icon to the navigation drawer
     * @param item - The hamburger icon
     * @return Returns a boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     *  Starts AddRecipeActivity
     */
    public void addRecipe(View view) {
        Intent intent = new Intent (this, AddRecipeActivity.class);
        startActivity(intent);
    }

    /**
     *  Opens a random recipe in the recipeList
     */
    public void openRandomRecipe() {
        Intent intent = new Intent (this, ViewRecipeActivity.class);

        if(recipeList.size() == 0) {
            Toast.makeText(this, "You need at least one recipe for this", Toast.LENGTH_LONG).show();
        } else {
            Recipe randomRecipe = recipeList.get(random.nextInt(recipeList.size()));
            intent.putExtra("Recipe", randomRecipe);
            startActivity(intent);
        }
    }

    /**
     * Starts ViewFavoritesActivity
     */
    private void viewFavorites() {
        Intent intent = new Intent(this, ViewFavoritesActivity.class);
        startActivity(intent);
    }

    /**
     * Starts ViewFavoritesActivity
     */
    private void viewSearch() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    /**
     * Starts ViewSettingsActivity
     */
    private void viewSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Starts FilterActivity
     */
    private void viewFilters() {
        Intent intent = new Intent(this, FilterActivity.class);
        startActivity(intent);
    }

    /**
     * When focus resumes to this activity,
     * update the list and order it again.
     */
    @Override
    public void onResume() {
        super.onResume();

        controlNoRecipeElements();
        int selectedSortById = (int) sortSpinner.getSelectedItemId();
        switch (selectedSortById) {
            case 0:
                Collections.sort(recipeList, new DateSorter());
                adapter.notifyDataSetChanged();
                return;
            case 1:
                Collections.sort(recipeList, new AmountCookedSorter());
                adapter.notifyDataSetChanged();
                return;
            case 2:
                Collections.sort(recipeList, new RatingSorter());
                adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Saves the recipeList to the internal memory
     */
    public static void saveRecipes() {
        File file = new File(RecipePickerApplication.getAppContext().getFilesDir(), "recipeList.ser");

        try (OutputStream fileStream = new FileOutputStream(file);
             ObjectOutputStream out = new ObjectOutputStream(fileStream)) {

            out.writeObject(recipeList);
            Log.d("WRITE", "object recipeList is written");
        } catch (IOException e) {
            Log.e("WriteError",""+ e.getMessage());
            Log.d("WRITE", "object recipeList is NOT written");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        saveRecipes();
    }
}
