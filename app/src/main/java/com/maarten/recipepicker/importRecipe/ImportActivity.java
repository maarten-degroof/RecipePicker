package com.maarten.recipepicker.importRecipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.maarten.recipepicker.AddIngredientFragment;
import com.maarten.recipepicker.AddInstructionFragment;
import com.maarten.recipepicker.AddRecipeInterface;
import com.maarten.recipepicker.FillInRecipeFragment;
import com.maarten.recipepicker.viewModels.FillInRecipeViewModel;
import com.maarten.recipepicker.MainActivity;
import com.maarten.recipepicker.R;
import com.maarten.recipepicker.enums.CookTime;
import com.maarten.recipepicker.enums.Difficulty;
import com.maarten.recipepicker.enums.FillInRecipeFragmentType;
import com.maarten.recipepicker.models.Ingredient;
import com.maarten.recipepicker.models.Instruction;
import com.maarten.recipepicker.models.Recipe;

import java.util.List;
import java.util.Set;

import static com.maarten.recipepicker.MainActivity.recipeList;
import static com.maarten.recipepicker.enums.FillInRecipeFragmentType.ADD_INGREDIENT;
import static com.maarten.recipepicker.enums.FillInRecipeFragmentType.ADD_INSTRUCTION;
import static com.maarten.recipepicker.enums.FillInRecipeFragmentType.IMPORT;
import static com.maarten.recipepicker.enums.FillInRecipeFragmentType.MAIN;

public class ImportActivity extends AppCompatActivity implements AddRecipeInterface {

    private FillInRecipeFragment fillInRecipeFragment;
    private AddIngredientFragment addIngredientFragment;
    private AddInstructionFragment addInstructionFragment;
    private ImportTextFragment importTextFragment;

    private MaterialButton backButton, addRecipeButton;

    private Toolbar toolbar;
    private AppBarLayout appBarLayout;

    private FillInRecipeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        appBarLayout = findViewById(R.id.appBarLayout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Import recipe");
        setSupportActionBar(toolbar);

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fillInRecipeFragment = new FillInRecipeFragment();
        addIngredientFragment = new AddIngredientFragment();
        addInstructionFragment = new AddInstructionFragment();
        importTextFragment = new ImportTextFragment();

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> toggleCurrentFragment(IMPORT));
        addRecipeButton = findViewById(R.id.addRecipeButton);
        addRecipeButton.setOnClickListener(v -> saveRecipe());

        viewModel = new ViewModelProvider(this).get(FillInRecipeViewModel.class);

        FillInRecipeFragmentType currentFragmentType = viewModel.getCurrentFragmentType();
        toggleCurrentFragment(currentFragmentType);

    }

    /**
     * This decides what fragment to load and show; also decides which toolbar to show
     *
     * @param newFragmentType - defines the type of the fragment which should be shown
     */
    @Override
    public void toggleCurrentFragment(FillInRecipeFragmentType newFragmentType) {
        viewModel.setCurrentFragmentType(newFragmentType);
        switch (newFragmentType) {
            case ADD_INGREDIENT:
                appBarLayout.setExpanded(true);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fillInFragmentContainerView, addIngredientFragment)
                        .commit();
                toggleEndButtons(false);
                toolbar.setTitle("Add Ingredient");
                toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
                toolbar.setNavigationOnClickListener(view -> {
                    toggleCurrentFragment(MAIN);
                    addIngredientFragment.resetFields();
                });
                break;

            case ADD_INSTRUCTION:
                appBarLayout.setExpanded(true);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fillInFragmentContainerView, addInstructionFragment)
                        .commit();
                toggleEndButtons(false);
                toolbar.setTitle("Add Instruction");
                toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
                toolbar.setNavigationOnClickListener(view -> {
                    toggleCurrentFragment(MAIN);
                    addInstructionFragment.resetFields();
                });
                break;

            case IMPORT:
                appBarLayout.setExpanded(true);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fillInFragmentContainerView, importTextFragment)
                        .commit();
                toggleEndButtons(false);
                toolbar.setTitle("Import Recipe (Step 1/2)");
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
                toolbar.setNavigationOnClickListener(view -> finish());
                viewModel.removeRecipe();
                break;

            default:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fillInFragmentContainerView, fillInRecipeFragment)
                        .commit();
                toggleEndButtons(true);
                toolbar.setTitle("Import Recipe (Step 2/2)");
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
                toolbar.setNavigationOnClickListener(view -> {
                    toggleCurrentFragment(IMPORT);
                    fillInRecipeFragment.resetFragment();
                });
        }
    }

    /**
     * This gets called when the user pressed the 'physical' back button.
     *
     * @param keyCode - the physical that was pressed
     * @param event - the event that caused it
     * @return returns true if the event was catched
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && viewModel.getCurrentFragmentType() == ADD_INGREDIENT) {
            toggleCurrentFragment(MAIN);
            addIngredientFragment.resetFields();
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_BACK && viewModel.getCurrentFragmentType() == ADD_INSTRUCTION) {
            toggleCurrentFragment(MAIN);
            addInstructionFragment.resetFields();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK && viewModel.getCurrentFragmentType() == MAIN) {
            toggleCurrentFragment(IMPORT);
            fillInRecipeFragment.resetFragment();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * this toggles the cancel and add recipe button on the bottom of the window
     *
     * @param shouldShowButtons - if true, shows the buttons
     */
    @Override
    public void toggleEndButtons(boolean shouldShowButtons) {
        if (shouldShowButtons) {
            backButton.setVisibility(View.VISIBLE);
            addRecipeButton.setVisibility(View.VISIBLE);
        } else {
            backButton.setVisibility(View.GONE);
            addRecipeButton.setVisibility(View.GONE);
        }
    }

    /**
     * Retrieves the data from the viewModel; checks if the necessary items are filled in,
     * and inserts the recipe into the recipeList
     */
    @Override
    public void saveRecipe() {
        fillInRecipeFragment.saveToViewModel();

        String title = viewModel.getRecipeTitle();
        List<Ingredient> ingredientList = viewModel.getIngredientList();
        List<Instruction> instructionList = viewModel.getInstructionList();

        if (title.isEmpty()) {
            fillInRecipeFragment.showTitleError();
            return;
        } else if (ingredientList.isEmpty()) {
            Toast.makeText(this, "You have to add at least one ingredient.", Toast.LENGTH_LONG).show();
            return;
        } else if (instructionList.isEmpty()) {
            Toast.makeText(this, "You have to add at least one instruction.", Toast.LENGTH_LONG).show();
            return;
        }

        String url = viewModel.getRecipeURL();
        String comments = viewModel.getRecipeComments();
        String imagePath = viewModel.getRecipeImagePath();
        Set<String> categorySet = viewModel.getCategorySet();

        Boolean favorite = viewModel.isRecipeFavorite();
        int serves = viewModel.getServeCount();

        Difficulty difficulty = viewModel.getRecipeDifficulty();
        CookTime cookTime = viewModel.getRecipeCookTime();

        Recipe recipe = new Recipe(title, ingredientList, favorite, cookTime, imagePath, url, difficulty, comments, instructionList, serves, categorySet);
        recipeList.add(recipe);
        MainActivity.saveRecipes();
        Toast.makeText(this, "Your recipe was added!", Toast.LENGTH_LONG).show();
        finish();
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
