package com.maarten.recipepicker;

import android.content.Intent;
import android.net.Uri;
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
import com.maarten.recipepicker.enums.CookTime;
import com.maarten.recipepicker.enums.Difficulty;
import com.maarten.recipepicker.enums.FillInRecipeFragmentType;
import com.maarten.recipepicker.models.Ingredient;
import com.maarten.recipepicker.models.Instruction;
import com.maarten.recipepicker.models.Recipe;
import com.maarten.recipepicker.viewModels.FillInRecipeViewModel;

import java.io.File;
import java.util.List;
import java.util.Set;

import static com.maarten.recipepicker.MainActivity.recipeList;
import static com.maarten.recipepicker.enums.FillInRecipeFragmentType.ADD_INGREDIENT;
import static com.maarten.recipepicker.enums.FillInRecipeFragmentType.ADD_INSTRUCTION;
import static com.maarten.recipepicker.enums.FillInRecipeFragmentType.IMPORT;
import static com.maarten.recipepicker.enums.FillInRecipeFragmentType.MAIN;

public class EditRecipeActivity extends AppCompatActivity implements AddRecipeInterface {

    private Recipe recipe;

    private FillInRecipeFragment fillInRecipeFragment;
    private AddIngredientFragment addIngredientFragment;
    private AddInstructionFragment addInstructionFragment;

    private MaterialButton cancelButton, updateButton;

    private Toolbar toolbar;
    private AppBarLayout appBarLayout;

    private FillInRecipeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        appBarLayout = findViewById(R.id.appBarLayout);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Edit recipe");
        setSupportActionBar(toolbar);

        // This takes care of the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        recipe = (Recipe) intent.getSerializableExtra("Recipe");

        fillInRecipeFragment = new FillInRecipeFragment();
        addIngredientFragment = new AddIngredientFragment();
        addInstructionFragment = new AddInstructionFragment();

        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> finish());
        updateButton = findViewById(R.id.updateRecipeButton);
        updateButton.setOnClickListener(v -> saveRecipe());

        viewModel = new ViewModelProvider(this).get(FillInRecipeViewModel.class);

        if (viewModel.getCurrentFragmentType() == IMPORT) {
            viewModel.setCurrentFragmentType(MAIN);
        }

        // Initialise the viewModel with the recipe
        viewModel.setRecipe(recipe);

        toggleCurrentFragment(viewModel.getCurrentFragmentType());
    }

    /**
     * This decides what fragment to load and show; also decides which toolbar to show
     * @param newFragmentType defines the type of the fragment which should be shown
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

            default:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fillInFragmentContainerView, fillInRecipeFragment)
                        .commit();
                toggleEndButtons(true);
                toolbar.setTitle("Update Recipe");
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
                toolbar.setNavigationOnClickListener(view -> finish());
        }
    }

    /**
     * This gets called when the user pressed the 'physical' back button.
     * @param keyCode the physical that was pressed
     * @param event the event that caused it
     * @return returns true if the event was handled
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
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * This toggles the cancel and add recipe button on the bottom of the window
     * @param shouldShowButtons if true, shows the buttons
     */
    @Override
    public void toggleEndButtons(boolean shouldShowButtons) {
        if (shouldShowButtons) {
            cancelButton.setVisibility(View.VISIBLE);
            updateButton.setVisibility(View.VISIBLE);
        } else {
            cancelButton.setVisibility(View.GONE);
            updateButton.setVisibility(View.GONE);
        }
    }

    /**
     * This allows the gallery apps to find the taken picture
     */
    private void addPictureToGallery(String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    /**
     * Retrieves the data from the viewModel; checks if the necessary items are filled in,
     * and updates the recipe
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
        Set<String> categorySet = viewModel.getCategorySet();

        Boolean favorite = viewModel.isRecipeFavorite();
        int serves = viewModel.getServeCount();

        Difficulty difficulty = viewModel.getRecipeDifficulty();
        CookTime cookTime = viewModel.getRecipeCookTime();

        String imagePath = viewModel.getRecipeImagePath();
        if (!imagePath.isEmpty()) {
            addPictureToGallery(imagePath);
        }

        // Do the actual updating
        int recipeIndex = recipeList.indexOf(recipe);
        recipeList.get(recipeIndex).setTitle(title);
        recipeList.get(recipeIndex).setIngredientList(ingredientList);
        recipeList.get(recipeIndex).setInstructionList(instructionList);
        recipeList.get(recipeIndex).setCategories(categorySet);

        recipeList.get(recipeIndex).setCookTime(cookTime);
        recipeList.get(recipeIndex).setDifficulty(difficulty);

        recipeList.get(recipeIndex).setImagePath(imagePath);

        recipeList.get(recipeIndex).setURL(url);
        recipeList.get(recipeIndex).setComments(comments);

        recipeList.get(recipeIndex).setServes(serves);
        recipeList.get(recipeIndex).setFavorite(favorite);

        MainActivity.saveRecipes();

        Toast.makeText(this, "Your recipe was updated!", Toast.LENGTH_LONG).show();

        goToMainActivity();
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
        if (item.getItemId() == R.id.action_home) {
            goToMainActivity();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Returns to the main activity and removes the backstack
     * This is necessary because otherwise we would go back to an unedited recipe
     */
    private void goToMainActivity() {
        Intent intent = new Intent(EditRecipeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
