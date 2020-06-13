package com.maarten.recipepicker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.maarten.recipepicker.adapters.RecipeAdapter;
import com.maarten.recipepicker.enums.CookTime;
import com.maarten.recipepicker.enums.Difficulty;
import com.maarten.recipepicker.enums.IngredientType;
import com.maarten.recipepicker.enums.QuantityType;
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
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;


/**
 ********* BUGS *********
 * Improve manifest
 *
 * Check portrait mode everywhere
 *
 * Portrait mode bugs:  - sidebar menu
 *
 * When filtering/sorting and pressing a recipe and then going back, list blinks
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
 * snackBar when adding a cooked time, so it can be undone
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
    private DrawerLayout drawerLayout;

    private MaterialButton orderByButton;
    private int currentOrderBySetting;
    private AlertDialog orderByDialog;

    private Random random;

    private MaterialButton addRecipeButton;
    private TextView noRecipesYetTextView;

    public static DecimalFormat decimalFormat = new DecimalFormat("0.###");

    /**
     * Creates the activity and initialises all UI-fields
     * Loads the list from memory or creates a new one
     * @param savedInstanceState A previously saved instance, gets passed onto the super class
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView listViewRecipes = findViewById(R.id.mainRecyclerView);

        // Initialise recipeList
        recipeList = new ArrayList<>();

        // Look for the recipeList file
        File file = new File(getFilesDir(), "recipeList.ser");

        // Check if the file exists
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
        // If the file doesn't exist, create an arrayList with dummy value
        else {
            insertDummyRecipes();
        }

        // Initialise the images for each recipe so the adapter won't have to do the calculations again
        for(Recipe recipe : recipeList) {
            recipe.getImage();
        }

        orderByButton = findViewById(R.id.orderByButton);
        // 0 -> Chronological
        currentOrderBySetting = 0;

        adapter = new RecipeAdapter(this, recipeList);

        listViewRecipes.setAdapter(adapter);
        listViewRecipes.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().getThemedContext();
        }

        // All the navigation drawer stuff
        drawerLayout = findViewById(R.id.dl);
        abdt = new ActionBarDrawerToggle(this, drawerLayout,R.string.Open, R.string.Close);
        abdt.setDrawerIndicatorEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Make the recyclerView also scrollable
        ViewCompat.setNestedScrollingEnabled(listViewRecipes, true);

        drawerLayout.addDrawerListener(abdt);
        abdt.syncState();

        NavigationView nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(menuItem -> {
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
        });

        random = new Random();
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);

        setFact();

        addRecipeButton = findViewById(R.id.addRecipeButton);
        noRecipesYetTextView = findViewById(R.id.noRecipesYetTextView);

        controlNoRecipeElements();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Check to show the tip. Change the preferences so it won't show again
        boolean shouldShowWelcomeScreen = sharedPrefs.getBoolean("welcome_screen", true);
        if(shouldShowWelcomeScreen) {
            showWelcomeScreen();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean("welcome_screen", false);
            editor.apply();
        }
    }

    /**
     * Creates the dialog to choose which type of ordering is applied
     * @param view the order button
     */
    public void openOrderDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.order_by)
                .setSingleChoiceItems(R.array.order_types_array_items, currentOrderBySetting, (dialogInterface, index) -> {
                    currentOrderBySetting = index;
                    setOrdering(index);
                    orderByDialog.dismiss();
                });
        orderByDialog = builder.create();
        orderByDialog.show();
    }

    /**
     * Takes care of the sort functions. Sorts the list based on the chosen item in the dialog.
     * order of the dialog:
     *      - Chronological (0)
     *      - Times cooked  (1)
     *      - Rating (2)
     * @param orderNumber the number saying which order the user chose
     */
    private void setOrdering(int orderNumber) {
        switch (orderNumber) {
            case 0:
                Collections.sort(recipeList, new DateSorter());
                orderByButton.setText(R.string.chronological);
                adapter.notifyDataSetChanged();
                return;
            case 1:
                Collections.sort(recipeList, new AmountCookedSorter());
                orderByButton.setText(R.string.times_cooked);
                adapter.notifyDataSetChanged();
                return;
            case 2:
                Collections.sort(recipeList, new RatingSorter());
                orderByButton.setText(R.string.rating);
                adapter.notifyDataSetChanged();
        }
    }

    /**
     * Shows the welcome screen
     */
    private void showWelcomeScreen() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Welcome!");
        builder.setMessage(getString(R.string.welcome_screen));

        builder.setPositiveButton("Let's get started", (dialog, id) -> {
        });
        // Create and show the dialog
        builder.create().show();
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
     * loads the fact- and tips-file into an arrayList and chooses a random item
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
     * Inserts the dummy recipe into the recipeList
     */
    public static void insertDummyRecipes() {
        // Spaghetti Bolognese
        List<Ingredient> SPIngredientList = new ArrayList<>();
        SPIngredientList.add(new Ingredient("Spaghetti",500.0, QuantityType.GRAMS, IngredientType.OTHER, ""));
        SPIngredientList.add(new Ingredient("Minced meat",350.0, QuantityType.GRAMS, IngredientType.OTHER, ""));
        SPIngredientList.add(new Ingredient("Tomatoes",3.0, QuantityType.OTHER, IngredientType.OTHER, ""));
        SPIngredientList.add(new Ingredient("Paprika's",3.0, QuantityType.OTHER, IngredientType.OTHER, ""));
        SPIngredientList.add(new Ingredient("Water",100.0, QuantityType.MILLILITRES, IngredientType.OTHER, ""));
        SPIngredientList.add(new Ingredient("A bit of salt and pepper",null, QuantityType.OTHER, IngredientType.OTHER, ""));

        String SPImage =  String.valueOf(R.drawable.spaghetti_bolognese);
        String SPURL = "https://www.jamieoliver.com/recipes/beef-recipes/spaghetti-bolognese/";
        String SPComments = "Really easy to make!\n\nOnly be sure not to cook the spaghetti too long.";

        ArrayList<Instruction> SPInstructionList = new ArrayList<>();
        SPInstructionList.add(new Instruction("Cook the spaghetti.",(long) 600000));
        SPInstructionList.add(new Instruction("Bake the minced meat.", null));
        SPInstructionList.add(new Instruction("Cut the tomatoes and the paprika into pieces.", null));
        SPInstructionList.add(new Instruction("Once the minced meat is done, throw the paprika and tomatoes in the same pan and bake them together. Spice with salt and pepper.", (long) 300000));
        SPInstructionList.add(new Instruction("Mix the sauce with the spaghetti.", (long) 10000));

        Set<String> SPCategories = new TreeSet<>();
        SPCategories.add("Pasta");
        SPCategories.add("Main course");
        SPCategories.add("Warm meal");

        recipeList.add(new Recipe("Spaghetti Bolognese for people who don't have a lot of time",
                SPIngredientList, false, CookTime.MEDIUM, SPImage, SPURL, Difficulty.BEGINNER,
                SPComments, SPInstructionList, 4, SPCategories));
    }

    /**
     * This connects the hamburger icon to the navigation drawer
     * @param item The hamburger icon
     * @return returns a boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Starts AddRecipeActivity
     */
    public void addRecipe(View view) {
        Intent intent = new Intent (this, AddRecipeActivity.class);
        startActivity(intent);
    }

    /**
     * Opens a random recipe in the recipeList
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
        drawerLayout.closeDrawer(GravityCompat.START);

        setOrdering(currentOrderBySetting);
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
