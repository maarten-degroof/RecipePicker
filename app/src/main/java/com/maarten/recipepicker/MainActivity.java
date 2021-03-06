package com.maarten.recipepicker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import com.maarten.recipepicker.viewModels.MainViewModel;

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

public class MainActivity extends AppCompatActivity {

    public static List<Recipe> recipeList;
    private RecipeAdapter adapter;

    private ActionBarDrawerToggle abdt;
    private DrawerLayout drawerLayout;

    private MaterialButton orderByButton;
    private AlertDialog orderByDialog;

    private Random random;

    private MaterialButton addRecipeButton;
    private TextView noRecipesYetTextView;

    private SharedPreferences sharedPrefs;
    private MainViewModel viewModel;

    private RecyclerView recipeRecyclerView;

    private BottomSheetDialog helpBottomSheetDialog;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().getThemedContext();
        }

        recipeRecyclerView = findViewById(R.id.mainRecyclerView);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Initialise recipeList
        recipeList = new ArrayList<>();

        // Look for the recipeList file
        File file = new File(getFilesDir(), "recipeList.ser");

        // Check if the file exists
        if(file.canRead()) {
            try (FileInputStream fileStream = new FileInputStream(file);
                 ObjectInputStream in = new ObjectInputStream(fileStream)) {
                recipeList = (ArrayList<Recipe>) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
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

        adapter = new RecipeAdapter(this, recipeList);

        recipeRecyclerView.setAdapter(adapter);
        recipeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // All the navigation drawer stuff
        drawerLayout = findViewById(R.id.dl);

        abdt = new ActionBarDrawerToggle(this, drawerLayout,R.string.Open, R.string.Close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);

                if (newState == DrawerLayout.STATE_SETTLING) {
                    if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        // starts opening
                        drawerLayout.openDrawer(GravityCompat.START);
                        viewModel.setShowingNavigationDrawer(true);
                    } else {
                        // closing drawer
                        drawerLayout.closeDrawer(GravityCompat.START);
                        viewModel.setShowingNavigationDrawer(false);
                    }
                    invalidateOptionsMenu();
                }
            }
        };
        abdt.setDrawerIndicatorEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Make the recyclerView also scrollable
        ViewCompat.setNestedScrollingEnabled(recipeRecyclerView, true);

        drawerLayout.addDrawerListener(abdt);
        abdt.syncState();

        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();

            if (id == R.id.addRecipe) {
                addRecipe(null);
                viewModel.setShowingNavigationDrawer(false);
            } else if (id == R.id.favorites) {
                viewFavorites();
                viewModel.setShowingNavigationDrawer(false);
            } else if (id == R.id.settings) {
                viewSettings();
                viewModel.setShowingNavigationDrawer(false);
            } else if (id == R.id.filter) {
                viewFilters();
                viewModel.setShowingNavigationDrawer(false);
            } else if (id == R.id.search) {
                viewSearch();
                viewModel.setShowingNavigationDrawer(false);
            } else if (id == R.id.random) {
                openRandomRecipe();
                viewModel.setShowingNavigationDrawer(false);
            } else if (id == R.id.help) {
                showHelpScreen(0);
            }
            return true;
        });

        random = new Random();
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);

        setFact();

        addRecipeButton = findViewById(R.id.addRecipeButton);
        noRecipesYetTextView = findViewById(R.id.noRecipesYetTextView);

        controlNoRecipeElements();

        helpBottomSheetDialog = new BottomSheetDialog(this);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Check to show the welcome screen
        boolean shouldShowWelcomeScreen = sharedPrefs.getBoolean("welcome_screen", true);
        if (shouldShowWelcomeScreen && viewModel.isShowingWelcomeScreen()) {
            showWelcomeScreen();
        } else {
            viewModel.setShowingWelcomeScreen(false);
        }
        if (viewModel.isShowingSortingDialog()) {
            openOrderDialog(null);
        } else if (viewModel.getCurrentHelpScreen() >= 0) {
            showHelpScreen(viewModel.getCurrentHelpScreen());
        }
    }

    /**
     * Creates the dialog to choose which type of ordering is applied
     * @param view the order button
     */
    public void openOrderDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        viewModel.setShowingSortingDialog(true);
        builder.setTitle(R.string.order_by)
                .setSingleChoiceItems(R.array.order_types_array_items, viewModel.getSortingType(), (dialogInterface, index) -> {
                    viewModel.setSortingType(index);
                    viewModel.setShowingSortingDialog(false);
                    setOrdering();
                    orderByDialog.dismiss();
                });
        orderByDialog = builder.create();
        orderByDialog.setOnCancelListener(dialog -> viewModel.setShowingSortingDialog(false));
        orderByDialog.show();
    }

    /**
     * Takes care of the sort functions. Sorts the list based on the chosen item in the dialog.
     * Retrieves the chosen item from the viewModel.
     * Order of the dialog:
     *      - Chronological (0)
     *      - Times cooked  (1)
     *      - Rating (2)
     */
    private void setOrdering() {
        switch (viewModel.getSortingType()) {
            case 1:
                recipeList.sort(new AmountCookedSorter());
                orderByButton.setText(R.string.times_cooked);
                adapter.notifyDataSetChanged();
                return;
            case 2:
                recipeList.sort(new RatingSorter());
                orderByButton.setText(R.string.rating);
                adapter.notifyDataSetChanged();
                return;
            default:
                recipeList.sort(new DateSorter());
                orderByButton.setText(R.string.chronological);
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
        builder.setCancelable(false);

        builder.setPositiveButton(R.string.got_it, (dialog, id) -> {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean("welcome_screen", false);
            editor.apply();
            viewModel.setShowingWelcomeScreen(false);
        });
        builder.setNegativeButton(R.string.show_again, (dialog, which) -> viewModel.setShowingWelcomeScreen(false));
        // Create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * Shows the help screen (a bottom Sheet) which is shown when pressing 'Help' in the navigation drawer.
     * There are 4 help windows, and the given parameter (0 to 3) decide which help window is shown.
     * @param whichWindow the index of the window to show. 0 is the first window, and 3 is the last one.
     */
    private void showHelpScreen(int whichWindow) {
        View dialogView = View.inflate(this, R.layout.help_bottom_sheet, null);
        TextView titleHelpText = dialogView.findViewById(R.id.help_text_title);
        TextView helpText = dialogView.findViewById(R.id.help_text);
        MaterialButton nextHelpButton = dialogView.findViewById(R.id.next_help_button);
        MaterialButton previousHelpButton = dialogView.findViewById(R.id.previous_help_button);

        viewModel.setCurrentHelpScreen(whichWindow);
        switch (whichWindow) {
            case 1:
                titleHelpText.setText(R.string.help_title_1);
                helpText.setText(R.string.help_main_window_1);
                nextHelpButton.setOnClickListener(view -> showHelpScreen(2));
                previousHelpButton.setOnClickListener(view -> showHelpScreen(0));
                break;
            case 2:
                titleHelpText.setText(R.string.help_title_2);
                helpText.setText(R.string.help_main_window_2);
                nextHelpButton.setOnClickListener(view -> showHelpScreen(3));
                previousHelpButton.setOnClickListener(view -> showHelpScreen(1));
                break;
            case 3:
                titleHelpText.setText(R.string.help_title_3);
                helpText.setText(R.string.help_main_window_3);
                nextHelpButton.setOnClickListener(view -> helpBottomSheetDialog.cancel());
                previousHelpButton.setOnClickListener(view -> showHelpScreen(2));
                break;
            default:
                titleHelpText.setText(R.string.help_title_0);
                helpText.setText(R.string.help_main_window_0);
                nextHelpButton.setOnClickListener(view -> showHelpScreen(1));
                previousHelpButton.setOnClickListener(null);
        }

        if (whichWindow == 3) {
            nextHelpButton.setText(R.string.finish);
        } else {
            nextHelpButton.setText(R.string.next);
        }

        if (whichWindow == 0) {
            previousHelpButton.setVisibility(View.GONE);
        } else {
            previousHelpButton.setVisibility(View.VISIBLE);
        }

        helpBottomSheetDialog.setContentView(dialogView);
        helpBottomSheetDialog.setOnCancelListener(dialogInterface -> viewModel.resetCurrentHelpScreen());

        if (!helpBottomSheetDialog.isShowing()) {
            helpBottomSheetDialog.show();
        }
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

        if (viewModel.getFact().equals("")) {
            ArrayList<String> factList = new ArrayList<>();
            try (Scanner scanner = new Scanner(getResources().getAssets().open("factList.txt"))) {
                while (scanner.hasNextLine()) {
                    factList.add(scanner.nextLine());
                }
                viewModel.setFact(factList.get(random.nextInt(factList.size())));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        factTextView.setText(viewModel.getFact());
    }

    /**
     * Inserts the dummy recipe into the recipeList
     */
    public static void insertDummyRecipes() {
        // Spaghetti Bolognese
        List<Ingredient> SPIngredientList = new ArrayList<>();
        SPIngredientList.add(new Ingredient("Spaghetti",500.0, QuantityType.GRAMS, IngredientType.PASTA, ""));
        SPIngredientList.add(new Ingredient("Minced meat",350.0, QuantityType.GRAMS, IngredientType.MEAT, ""));
        SPIngredientList.add(new Ingredient("Tomatoes",3.0, QuantityType.OTHER, IngredientType.VEGETABLES, ""));
        SPIngredientList.add(new Ingredient("Paprika's",3.0, QuantityType.OTHER, IngredientType.VEGETABLES, ""));
        SPIngredientList.add(new Ingredient("Water",100.0, QuantityType.MILLILITRES, IngredientType.OTHER, "Water"));
        SPIngredientList.add(new Ingredient("A bit of salt and pepper",null, QuantityType.OTHER, IngredientType.HERBS, ""));

        String SPImage =  String.valueOf(R.drawable.spaghetti_bolognese);
        String SPURL = "https://www.jamieoliver.com/recipes/beef-recipes/spaghetti-bolognese/";
        String SPComments = "Really easy to make!\n\nOnly be sure not to cook the spaghetti too long.";
        String SPTitle = "Spaghetti Bolognese for people who don't have a lot of time";

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

        recipeList.add(new Recipe(SPTitle, SPIngredientList, false, CookTime.MEDIUM, SPImage,
                SPURL, Difficulty.BEGINNER, SPComments, SPInstructionList, 4, SPCategories));
    }

    /**
     * This connects the hamburger icon to the navigation drawer
     * @param item The hamburger icon
     * @return returns a boolean
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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

        if (viewModel.isShowingNavigationDrawer()) {
            drawerLayout.openDrawer(GravityCompat.START);
        } else {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        setOrdering();
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

    /**
     * Resets the times cooked of each recipe to 0 and saves the recipeList
     */
    public static void clearAllAmountCooked() {
        for (Recipe recipe : recipeList) {
            recipe.resetAmountCooked();
        }
        saveRecipes();
    }

    @Override
    public void onStop() {
        super.onStop();
        saveRecipes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_with_search, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    resetAdapter();
                }
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        return true;
    }

    /**
     * Resets the recipe adapter of the recyclerView so it uses all the recipes again
     */
    private void resetAdapter() {
        adapter = new RecipeAdapter(this, recipeList);
        recipeRecyclerView.setAdapter(adapter);
    }

}
