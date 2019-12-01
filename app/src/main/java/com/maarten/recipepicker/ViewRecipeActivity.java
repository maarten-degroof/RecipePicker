package com.maarten.recipepicker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.core.view.ViewCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.snackbar.Snackbar;
import com.maarten.recipepicker.Models.Ingredient;
import com.maarten.recipepicker.Models.Instruction;
import com.maarten.recipepicker.Models.Recipe;
import com.maarten.recipepicker.adapters.IngredientAdapter;
import com.maarten.recipepicker.adapters.InstructionAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;

public class ViewRecipeActivity extends AppCompatActivity {

    private Recipe recipe;
    private int recipeIndex;

    private MenuItem favoriteItemEmpty;
    private MenuItem favoriteItemFull;

    private TextView amountCookedField;
    private int amountCookedValue;

    private float currentRating;
    private MaterialButton ratingButton;
    private TextView currentRatingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Recipe");
        setSupportActionBar(toolbar);

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                supportFinishAfterTransition();
            }
        });

        Intent intent = getIntent();
        recipe = (Recipe) intent.getSerializableExtra("Recipe");

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

        amountCookedValue = recipe.getAmountCooked();
        amountCookedField.setText(String.valueOf(amountCookedValue));

        ImageView recipeImageView = findViewById(R.id.recipeImageView);

        recipeImageView.setImageBitmap(recipe.getImage());

        // hide the appropriate elements
        TextView noWebsiteTextView = findViewById(R.id.noWebsiteTextView);
        MaterialButton copyWebsiteButton = findViewById(R.id.copyURLButton);
        MaterialButton browseWebsiteButton = findViewById(R.id.BrowseURLButton);

        if(recipe.getURL() != null && !recipe.getURL().equals("")) {
            noWebsiteTextView.setVisibility(View.INVISIBLE);
        } else {
            copyWebsiteButton.setVisibility(View.INVISIBLE);
            browseWebsiteButton.setVisibility(View.INVISIBLE);
        }

        // get the ingredientlist and add it to the listview
        RecyclerView ingredientListRecyclerView = findViewById(R.id.viewRecipeIngredientList);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        int servesCount = Integer.parseInt(sharedPrefs.getString("serves_value", "4"));

        // create a new ingredientList, and create all the ingredients again, otherwise it'd be the same object and you'd change the wrong values
        List<Ingredient> calculatedIngredientList = new ArrayList<>();

        for (Ingredient ingredient : recipe.getIngredientList()) {
            calculatedIngredientList.add(new Ingredient(ingredient));
        }
        // now update the values
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

        // make the listview also scrollable
        ViewCompat.setNestedScrollingEnabled(ingredientListRecyclerView, true);

        // the index is used to update the favorite status
        recipeIndex = MainActivity.recipeList.indexOf(recipe);


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

        // fill the string in so it says 'ingredients needed for [4] persons:
        String ingredientString = getString(R.string.needed_ingredients, servesCount);
        TextView neededIngredients = findViewById(R.id.neededIngredients);
        neededIngredients.setText(Html.fromHtml(ingredientString, FROM_HTML_MODE_LEGACY));

        // check to show the tip. Change the preferences so it won't show again
        boolean shouldShowServesTip = sharedPrefs.getBoolean("tip_serves", true);
        if(shouldShowServesTip) {
            showServeTip();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean("tip_serves", false);
            editor.apply();
        }

        currentRating = recipe.getRating();

        //ratingButton = findViewById(R.id.ratingButton);
        currentRatingTextView = findViewById(R.id.currentRatingTextView);
        ratingButton = findViewById(R.id.ratingButton);

        if(currentRating != 0) {
            currentRatingTextView.setText(String.valueOf((int)currentRating));
        } else {
            currentRatingTextView.setText("No Rating");
            currentRatingTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_star_border_green_24dp, 0);
        }
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

        final RatingBar recipeRatingBar = dialog_layout.findViewById(R.id.recipeRatingBar);
        recipeRatingBar.setRating(currentRating);

        recipeRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                currentRatingTextView.setText(String.valueOf((int)rating));
            }
        });

        builder.setTitle("Choose rating");
        builder.setMessage("Tap or drag the stars to set a rating. Set it to 0 to remove the rating");

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                currentRating = recipeRatingBar.getRating();
                MainActivity.recipeList.get(recipeIndex).setRating((int)currentRating);
                if(currentRating != 0) {
                    ViewRecipeActivity.this.currentRatingTextView.setText(String.valueOf((int)currentRating));
                    ViewRecipeActivity.this.currentRatingTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_star_green_24dp, 0);
                } else {
                    ViewRecipeActivity.this.currentRatingTextView.setText("No Rating");
                    ViewRecipeActivity.this.currentRatingTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_star_border_green_24dp, 0);
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        // create and show the dialog
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
     * this has the favorites, edit and delete button
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

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Creates the delete dialog
     */
    public void createDeleteDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Remove Recipe");
        builder.setMessage("Are you sure you want to remove this recipe?");

        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                removeRecipe();
            }
        });
        builder.setNegativeButton("Keep", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        // create and show the dialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

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
        amountCookedValue++;
        amountCookedField.setText(String.valueOf(amountCookedValue));

        Snackbar.make(view, "You cooked this", Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amountCookedValue--;
                amountCookedField.setText(String.valueOf(amountCookedValue));
                MainActivity.recipeList.get(recipeIndex).removeOneAmountCooked();
            }
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
     * opens the webpage with the URL in the recipe
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
     * When app closed from this activity, save the main recipelist to a file
     */
    @Override
    protected void onStop() {
        super.onStop();

        File file = new File(getFilesDir(), "recipeList.ser");


        try (OutputStream fileStream = new FileOutputStream(file);
             ObjectOutputStream out = new ObjectOutputStream(fileStream)) {

            out.writeObject(MainActivity.recipeList);
            Log.d("WRITE", "object recipelist is written: " + MainActivity.recipeList);

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("WRITE", "object recipelist is NOT written: " + MainActivity.recipeList);
        }

    }

    /**
     * When the difficulty button is pressed, open DifficultyFilteredActivity
     *
     * @param view - the pressed chip
     */
    public void startDifficultyFilteredActivity(View view) {
        try{
            Intent intent = new Intent(this, DifficultyFilteredActivity.class);
            intent.putExtra("difficulty", recipe.getDifficulty());
            startActivity(intent);
        } catch (Exception e) {
            Log.d("ERROR", e.getLocalizedMessage());
        }
    }

    /**
     * open the Cook now activity
     *
     * @param view - the cook now button
     */
    public void startCookNow(View view) {
        Intent intent = new Intent(this, CookNowActivity.class);
        intent.putExtra("Recipe", recipe);
        startActivity(intent);
    }

}
