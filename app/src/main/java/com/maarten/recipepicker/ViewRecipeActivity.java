package com.maarten.recipepicker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.maarten.recipepicker.Adapters.IngredientAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class ViewRecipeActivity extends AppCompatActivity {

    private Recipe recipe;
    private int recipeIndex;

    private MenuItem favoriteItemEmpty;
    private MenuItem favoriteItemFull;

    private TextView amountCookedField;
    private int amountCookedValue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Recipe");
        setSupportActionBar(toolbar);

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        recipe = (Recipe) intent.getSerializableExtra("Recipe");

        TextView recipeTitle = findViewById(R.id.textViewTitle);
        TextView recipeDescription = findViewById(R.id.viewRecipeDescription);
        amountCookedField = findViewById(R.id.amountCookedField);
        Chip durationChip = findViewById(R.id.durationChip);

        recipeDescription.setText(recipe.getDescription());
        recipeTitle.setText(recipe.getTitle());
        amountCookedValue = recipe.getAmountCooked();
        amountCookedField.setText(String.valueOf(amountCookedValue));

        switch (recipe.getCookTime()) {
            case SHORT:
                durationChip.setText("Short: -30 min");
                break;
            case MEDIUM:
                durationChip.setText("Medium: 30-60 min");
                break;
            case LONG:
                durationChip.setText("Long: 60+ min");
                break;
        }

        // get the ingredientlist and add it to the listview
        ListView listView = findViewById(R.id.viewRecipeIngredientList);

        IngredientAdapter adapter = new IngredientAdapter(this,recipe.getIngredientList());
        listView.setAdapter(adapter);

        // make the listview also scrollable
        ViewCompat.setNestedScrollingEnabled(listView, true);

        // the index is used to update the favorite status
        recipeIndex = MainActivity.recipeList.indexOf(recipe);

    }

    /**
     * Inflates the menu into the toolbar
     * Also checks the recipe and changes the favorite value to update it
     *
     * @param menu the menu
     * @return for some reason should return true
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

}
