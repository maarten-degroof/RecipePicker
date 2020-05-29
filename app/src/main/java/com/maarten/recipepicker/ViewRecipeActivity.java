package com.maarten.recipepicker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.maarten.recipepicker.cookNow.CookNowActivity;
import com.maarten.recipepicker.models.Ingredient;
import com.maarten.recipepicker.models.Instruction;
import com.maarten.recipepicker.models.Recipe;
import com.maarten.recipepicker.adapters.IngredientAdapter;
import com.maarten.recipepicker.adapters.InstructionAdapter;
import com.maarten.recipepicker.viewModels.viewRecipeViewModel;

import java.util.ArrayList;
import java.util.List;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;

public class ViewRecipeActivity extends AppCompatActivity {

    private Recipe recipe;
    private int recipeIndex;

    private MenuItem favoriteItemEmpty;
    private MenuItem favoriteItemFull;

    private TextView amountCookedField;

    private float currentRating;
    private Chip ratingChip;

    private Gson gson;

    private viewRecipeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);

        Intent intent = getIntent();
        recipe = (Recipe) intent.getSerializableExtra("Recipe");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(recipe.getTitle());
        setSupportActionBar(toolbar);

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            // back button pressed
            supportFinishAfterTransition();
        });

        TextView recipeTitle = findViewById(R.id.textViewTitle);
        amountCookedField = findViewById(R.id.amountCookedField);

        ChipDrawable durationChip = ChipDrawable.createFromResource(this, R.xml.chip);

        switch (recipe.getCookTime()) {
            case SHORT:
                durationChip.setText(getString(R.string.duration_short));
                break;
            case MEDIUM:
                durationChip.setText(getString(R.string.duration_medium));
                break;
            case LONG:
                durationChip.setText(getString(R.string.duration_long));
                break;
        }
        durationChip.setBounds(0, 0, durationChip.getIntrinsicWidth(), durationChip.getIntrinsicHeight());

        Spannable span = new SpannableString("  " + recipe.getTitle());
        ImageSpan image = new ImageSpan(durationChip);
        span.setSpan(image, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        recipeTitle.setText(span);

        // The index is used to update the favorite status
        recipeIndex = MainActivity.recipeList.indexOf(recipe);

        viewModel = new ViewModelProvider(this).get(viewRecipeViewModel.class);

        ImageView recipeImageView = findViewById(R.id.recipeImageView);

        recipeImageView.setImageBitmap(recipe.getImage());

        // Hide the appropriate elements
        TextView noWebsiteTextView = findViewById(R.id.noWebsiteTextView);
        MaterialButton copyWebsiteButton = findViewById(R.id.copyURLButton);
        MaterialButton browseWebsiteButton = findViewById(R.id.BrowseURLButton);

        if(recipe.getURL() != null && !recipe.getURL().equals("")) {
            noWebsiteTextView.setVisibility(View.INVISIBLE);
        } else {
            copyWebsiteButton.setVisibility(View.INVISIBLE);
            browseWebsiteButton.setVisibility(View.INVISIBLE);
        }

        // Get the ingredientlist and add it to the listview
        RecyclerView ingredientListRecyclerView = findViewById(R.id.viewRecipeIngredientList);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        int servesCount = Integer.parseInt(sharedPrefs.getString("serves_value", "4"));

        // Create a new ingredientList, and create all the ingredients again, otherwise it'd be the same object and you'd change the wrong values
        List<Ingredient> calculatedIngredientList = new ArrayList<>();

        for (Ingredient ingredient : recipe.getIngredientList()) {
            calculatedIngredientList.add(new Ingredient(ingredient));
        }
        // Now update the values
        for (Ingredient ingredient : calculatedIngredientList) {
            if(ingredient.getQuantity()!= null) {
                ingredient.setQuantity(ingredient.getQuantity() / recipe.getServes() * servesCount);
            }
        }

        IngredientAdapter adapter = new IngredientAdapter(this,calculatedIngredientList);
        ingredientListRecyclerView.setAdapter(adapter);
        ingredientListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ingredientListRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ingredientListRecyclerView.setHasFixedSize(true);

        // Make the recyclerView also scrollable
        ViewCompat.setNestedScrollingEnabled(ingredientListRecyclerView, true);

        Chip difficultyChip = findViewById(R.id.difficultyChip);

        switch (recipe.getDifficulty()) {
            case BEGINNER:
                difficultyChip.setText(getString(R.string.beginner));
                break;
            case INTERMEDIATE:
                difficultyChip.setText(getString(R.string.intermediate));
                break;
            case EXPERT:
                difficultyChip.setText(getString(R.string.expert));
                break;
        }

        TextView commentTextView = findViewById(R.id.commentTextView);
        if(recipe.getComments() != null && !recipe.getComments().equals("")) {
            commentTextView.setText(recipe.getComments());
        } else {
            commentTextView.setText(getString(R.string.no_comments));
        }

        // Instructions
        List<Instruction> instructionList = recipe.getInstructionList();

        InstructionAdapter instructionAdapter = new InstructionAdapter(this, instructionList);
        RecyclerView instructionRecyclerView = findViewById(R.id.viewInstructionRecyclerView);

        instructionRecyclerView.setAdapter(instructionAdapter);
        instructionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fill the string in so it says 'ingredients needed for [4] persons:
        String ingredientString = getString(R.string.needed_ingredients, servesCount);
        TextView neededIngredients = findViewById(R.id.neededIngredients);
        neededIngredients.setText(Html.fromHtml(ingredientString, FROM_HTML_MODE_LEGACY));

        // Check to show the tip. Change the preferences so it won't show again
        boolean shouldShowServesTip = sharedPrefs.getBoolean("tip_serves", true);
        if(shouldShowServesTip) {
            showServeTip();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean("tip_serves", false);
            editor.apply();
        }

        currentRating = recipe.getRating();

        ratingChip = findViewById(R.id.ratingChip);
        if (currentRating == 0) {
            ratingChip.setText(getString(R.string.no_rating));
            ratingChip.setChipIconResource(R.drawable.ic_star_border_green_24dp);
        }
        else {
            ratingChip.setText(String.valueOf((int) currentRating));
            ratingChip.setChipIconResource(R.drawable.ic_star_green_24dp);
        }

        ChipGroup categoriesChipGroup = findViewById(R.id.categoriesChipGroup);
        for (final String category : recipe.getCategories()) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primaryColor)));
            chip.setTextColor(Color.WHITE);
            chip.setOnClickListener(view -> startCategoryFilteredActivity(category));
            categoriesChipGroup.addView(chip);
        }

        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If this recipe was deleted and you get here through going back,
        // make sure the user can't interact with the recipe
        if (!MainActivity.recipeList.contains(recipe)) {
            createRecipeDeletedDialog();
        }

        viewModel.setAmountCooked(MainActivity.recipeList.get(recipeIndex).getAmountCooked());
        amountCookedField.setText(String.valueOf(viewModel.getAmountCooked()));
    }

    /**
     * Creates the AlertDialog where the user can choose the rating
     *
     * @param view - the 'add' and 'edit' rating
     */
    public void createRatingDialog(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // get the layout
        View dialog_layout = getLayoutInflater().inflate(R.layout.rating_dialog, null);

        final TextView currentRatingTextView = dialog_layout.findViewById(R.id.currentRatingTextView);
        currentRatingTextView.setText(String.valueOf((int)currentRating));
        if(currentRating == 0) {
            currentRatingTextView.setText(getString(R.string.no_rating));
        }

        final RatingBar recipeRatingBar = dialog_layout.findViewById(R.id.recipeRatingBar);
        recipeRatingBar.setRating(currentRating);

        recipeRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                currentRatingTextView.setText(String.valueOf((int)rating));
                if (rating == 0) {
                    currentRatingTextView.setText(getString(R.string.no_rating));
                }
            }
        });

        builder.setTitle("Choose rating");
        builder.setMessage("Tap or drag the stars to set a rating.");

        builder.setPositiveButton("Rate", (dialog, id) -> {
            currentRating = recipeRatingBar.getRating();
            MainActivity.recipeList.get(recipeIndex).setRating((int)currentRating);
            MainActivity.saveRecipes();

            ratingChip.setText(String.valueOf((int) currentRating));
            ratingChip.setChipIconResource(R.drawable.ic_star_green_24dp);

            if (currentRating == 0) {
                ratingChip.setText(getString(R.string.no_rating));
                ratingChip.setChipIconResource(R.drawable.ic_star_border_green_24dp);
            }

        });
        builder.setNegativeButton("Remove rating", (dialog, id) -> {
            currentRating = 0;
            MainActivity.recipeList.get(recipeIndex).setRating(0);
            MainActivity.saveRecipes();

            ratingChip.setText(getString(R.string.no_rating));
            ratingChip.setChipIconResource(R.drawable.ic_star_border_green_24dp);
        });
        // Create and show the dialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.setView(dialog_layout);
        alertDialog.show();
    }

    /**
     * Inflates the menu into the toolbar
     * Also checks the recipe and changes the favorite value to update it
     *
     * @param menu the menu
     * @return should return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_recipe, menu);

        favoriteItemEmpty = menu.findItem(R.id.action_favorite_empty);
        favoriteItemEmpty.setVisible(false);
        favoriteItemFull = menu.findItem(R.id.action_favorite_full);
        favoriteItemFull.setVisible(false);

        if(recipe.getFavorite()) {
            favoriteItemFull.setVisible(true);
        }
        else {
            favoriteItemEmpty.setVisible(true);
        }
        return true;
    }

    /**
     * This function will be called when a menu item is selected
     * This has the favorites, edit, delete, share and reset amount cooked buttons
     *
     * @param item the clicked menu item object
     * @return returns true if the clicked item is found
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Recipe tempRecipe = MainActivity.recipeList.get(recipeIndex);

        switch (item.getItemId()) {
            case R.id.action_favorite_empty:
                tempRecipe.setFavorite(true);
                favoriteItemEmpty.setVisible(false);
                favoriteItemFull.setVisible(true);
                MainActivity.recipeList.set(recipeIndex,tempRecipe);
                Toast.makeText(ViewRecipeActivity.this, "recipe is added to favorites", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.action_favorite_full:
                tempRecipe.setFavorite(false);
                favoriteItemFull.setVisible(false);
                favoriteItemEmpty.setVisible(true);
                MainActivity.recipeList.set(recipeIndex,tempRecipe);
                Toast.makeText(ViewRecipeActivity.this, "recipe is removed from favorites", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.action_delete:
                createDeleteDialog();
                return true;

            case R.id.action_edit:
                editRecipe();
                return true;

            case R.id.action_home:
                goToMainActivity();
                return true;

            case R.id.action_share:
                shareRecipe();
                return true;

            case R.id.action_reset_cook_counter:
                createResetAmountCookedDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Creates the dialog to reset the amount cooked counter
     */
    private void createResetAmountCookedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Reset amount cooked");
        builder.setMessage("Are you sure you want to reset the amount of times you have cooked this recipe?");

        builder.setPositiveButton("Reset", (dialog, id) -> {
            MainActivity.recipeList.get(recipeIndex).resetAmountCooked();
            MainActivity.saveRecipes();
            viewModel.setAmountCooked(0);
            amountCookedField.setText(String.valueOf(viewModel.getAmountCooked()));

            Toast.makeText(this, "Amount of times cooked is reset for this recipe", Toast.LENGTH_LONG).show();
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> {
        });
        // create and show the dialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Creates the delete dialog
     */
    public void createDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Remove Recipe");
        builder.setMessage("Are you sure you want to remove this recipe?");

        builder.setPositiveButton("Remove", (dialog, id) -> removeRecipe());

        builder.setNegativeButton("Keep", (dialog, id) -> {
        });
        // create and show the dialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Creates the dialog to say that this recipe doesn't exist anymore.
     * Will finish this activity.
     */
    public void createRecipeDeletedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Stop!");
        builder.setMessage("This recipe doesn't exist anymore, so you can't interact with it anymore.");
        builder.setCancelable(false);

        builder.setPositiveButton("OK I understand", (dialog, id) -> finish());
        // create and show the dialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * Converts the recipe to a Json-representation
     * and opens a picker where you can choose to send it
     */
    public void shareRecipe() {
        String json_recipe = gson.toJson(recipe);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, json_recipe);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share the recipe");
        startActivity(shareIntent);
    }

    /**
     * Shows the tip on how the serve works
     */
    private void showServeTip() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Tip!");
        builder.setMessage("In the settings you can change the serve amount. All the recipes will then be recalculated for that amount of serves!");

        builder.setPositiveButton("OK I understand", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        // create and show the dialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Removes the current recipe
     */
    private void removeRecipe() {
        MainActivity.recipeList.remove(recipe);
        MainActivity.saveRecipes();
        Toast.makeText(ViewRecipeActivity.this, "Recipe is removed", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Add one to the cooked counter
     * Displays a snackbar with an undo option
     *
     * @param view - the add button
     */
    public void addToCookedCounter(View view) {
        MainActivity.recipeList.get(recipeIndex).addOneAmountCooked();
        MainActivity.saveRecipes();
        viewModel.addOneAmountCooked();
        amountCookedField.setText(String.valueOf(viewModel.getAmountCooked()));

        Snackbar.make(view, "You cooked this", Snackbar.LENGTH_LONG).setAction("Undo", v -> {
            viewModel.removeOneAmountCooked();
            amountCookedField.setText(String.valueOf(viewModel.getAmountCooked()));
            MainActivity.recipeList.get(recipeIndex).removeOneAmountCooked();
            MainActivity.saveRecipes();
        }).show();
    }

    /**
     * Opens the EditRecipeActivity
     */
    private void editRecipe() {
        Intent intent = new Intent(this, EditRecipeActivity.class);
        intent.putExtra("Recipe", recipe);
        startActivity(intent);
    }

    /**
     * Copies the recipe URL to the clipboard
     *
     * @param view - the copy button
     */
    public void copyURLToClipboard(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Recipe URL", recipe.getURL());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "website copied:\n" + recipe.getURL(), Toast.LENGTH_LONG).show();
    }

    /**
     * Copies all the ingredients to the clipboard
     *
     * @param view - the copy ingredients button
     */
    public void copyIngredientsToClipboard(View view) {
        StringBuilder builder = new StringBuilder();
        for (Ingredient ingredient : recipe.getIngredientList()) {
            builder.append("- ").append(ingredient.toString()).append("\n");
        }
        // remove the last '/n'
        if(builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Ingredients", builder.toString());
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Ingredients are copied", Toast.LENGTH_SHORT).show();
    }

    /**
     * Opens the webpage with the URL in the recipe
     * Works with and without http(s) in the given URL
     *
     * @param view - the open webpage button
     */
    public void openURL(View view) {
        String url;
        if(recipe.getURL().startsWith("http://") || recipe.getURL().startsWith("https://")) {
            url = recipe.getURL();
        } else {
            url = "https://" + recipe.getURL();
        }
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(url));
        startActivity(intent);
    }

    /**
     * Opens the main activity and closes the previous activities
     */
    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * When the difficulty button is pressed, open TypeFilteredActivity
     *
     * @param view - the pressed difficulty chip
     */
    public void startDifficultyFilteredActivity(View view) {
        Intent intent = new Intent(this, TypeFilteredActivity.class);
        intent.putExtra("filterType", "Difficulty");
        intent.putExtra("difficulty", recipe.getDifficulty());
        startActivity(intent);
    }

    /**
     * When a category chip is pressed, open TypeFilteredActivity
     *
     * @param category - the category that was pressed - a String
     */
    public void startCategoryFilteredActivity(String category) {
        Intent intent = new Intent(this, TypeFilteredActivity.class);
        intent.putExtra("filterType", "Category");
        intent.putExtra("category", category);
        startActivity(intent);
    }

    /**
     * Open the Cook now activity
     *
     * @param view - the cook now button
     */
    public void startCookNow(View view) {
        Intent intent = new Intent(this, CookNowActivity.class);
        intent.putExtra("Recipe", recipe);
        startActivity(intent);
    }

}
