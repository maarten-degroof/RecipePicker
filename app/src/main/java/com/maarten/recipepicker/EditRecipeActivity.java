package com.maarten.recipepicker;

import android.content.DialogInterface;
import android.content.Intent;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.maarten.recipepicker.Adapters.IngredientEditAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static com.maarten.recipepicker.MainActivity.recipeList;

public class EditRecipeActivity extends AppCompatActivity {

    private Recipe recipe;
    private int recipeIndex;
    private IngredientEditAdapter adapter;

    private TextView recipeTitle, recipeDescription, noIngredientTextview;
    private ListView ingredientListView;
    private List<Ingredient> ingredientList;

    private EditText ingredientNameField, ingredientQuantityField;
    private Spinner ingredientTypeField;

    private TextInputLayout recipeTitleLayout, recipeDescriptionLayout;

    private ChipGroup chipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Edit recipe");
        setSupportActionBar(toolbar);

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        recipe = (Recipe) intent.getSerializableExtra("Recipe");

        recipeTitle = findViewById(R.id.nameField);
        recipeTitle.setText(recipe.getTitle());

        recipeDescription = findViewById(R.id.recipeText);
        recipeDescription.setText(recipe.getDescription());

        ingredientList = recipe.getIngredientList();

        adapter = new IngredientEditAdapter(this,ingredientList);
        ingredientListView = findViewById(R.id.editRecipeIngredientList);
        ingredientListView.setAdapter(adapter);

        recipeTitleLayout = findViewById(R.id.nameFieldLayout);
        recipeDescriptionLayout = findViewById(R.id.descriptionFieldLayout);

        // hide
        noIngredientTextview = findViewById(R.id.noIngredientsTextView);
        noIngredientTextview.setVisibility(View.INVISIBLE);

        chipGroup = findViewById(R.id.chipGroup);

        // check the current selected chip
        switch (recipe.getCookTime()) {
            case SHORT:
                chipGroup.check(R.id.shortDurationChip);
                break;
            case LONG:
                chipGroup.check(R.id.longDurationChip);
                break;
            default:
                chipGroup.check(R.id.mediumDurationChip);
        }

        // this makes sure that there's always one chip selected
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                for (int i = 0; i < chipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroup.getChildAt(i);
                    if (chip != null) {
                        chip.setClickable(!(chip.getId() == chipGroup.getCheckedChipId()));
                    }
                }
            }
        });
    }


    /**
     * validates input and updates the recipe
     *
     * @param view  needed for the button
     */
    public void updateRecipe(View view) {
        String tempRecipeName = recipeTitle.getText().toString();
        String tempRecipeDescription = recipeDescription.getText().toString();

        // get the current selected chip
        CookTime cookTime;
        switch (chipGroup.getCheckedChipId()) {
            case R.id.shortDurationChip:
                cookTime = CookTime.SHORT;
                break;
            case  R.id.longDurationChip:
                cookTime = CookTime.LONG;
                break;
            default:
                cookTime = CookTime.MEDIUM;
        }

        if(tempRecipeName.isEmpty()) {
            recipeTitleLayout.setError("Please fill in a title");
        } else if (tempRecipeDescription.isEmpty()) {
            recipeDescriptionLayout.setError("You have to fill in a description");
        } else if (ingredientList.isEmpty()) {
            Toast.makeText(EditRecipeActivity.this, "You have to add at least one ingredient", Toast.LENGTH_LONG).show();
        } else {
            boolean resetCookedCounter = ((MaterialCheckBox) findViewById(R.id.resetAmountCookedCheckBox)).isChecked();

            // Do the actual updating
            recipeIndex = recipeList.indexOf(recipe);
            recipeList.get(recipeIndex).setDescription(tempRecipeDescription);
            recipeList.get(recipeIndex).setTitle(tempRecipeName);
            recipeList.get(recipeIndex).setIngredientList(ingredientList);
            if(resetCookedCounter) {
                recipeList.get(recipeIndex).resetAmountCooked();
            }
            recipeList.get(recipeIndex).setCookTime(cookTime);

            Toast.makeText(EditRecipeActivity.this, "Your recipe was updated!", Toast.LENGTH_LONG).show();

            returnToMainActivity();
        }
    }

    /**
     * the cancel button to return to the previous view
     *
     * @param view  needed for the button to connect
     */
    public void cancelEdit(View view) {
        finish();
    }

    /**
     * returns to the main activity and removes the backstack
     * This is necessary because otherwise we would go back to an unedited recipe
     */
    private void returnToMainActivity() {
        Intent intent = new Intent(EditRecipeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Creates the recipe dialog to insert an ingredient
     * @param view  needed for the button-linking
     */
    public void createRecipeDialog(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // get the layout
        View dialog_layout = getLayoutInflater().inflate(R.layout.add_ingredient, null);

        // Create the text field in the alert dialog.
        ingredientNameField = (EditText) dialog_layout.findViewById(R.id.ingredientNameField);
        ingredientQuantityField = (EditText) dialog_layout.findViewById(R.id.quantityField);
        ingredientTypeField = (Spinner) dialog_layout.findViewById(R.id.ingredientTypeSpinner);

        // create the spinner adapter with the choices + the standard views of how it should look like
        ArrayAdapter<CharSequence> ingredientTypeAdapter = ArrayAdapter.createFromResource(this, R.array.ingredient_types_array_items, android.R.layout.simple_spinner_item);
        ingredientTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ingredientTypeField.setAdapter(ingredientTypeAdapter);

        builder.setTitle("Add ingredient");
        builder.setMessage("choose an ingredient and a quantity");

        builder.setPositiveButton("Add ingredient", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Ingredient.type ingredientType = Ingredient.type.valueOf(ingredientTypeField.getSelectedItem().toString());
                if(ingredientQuantityField.getText().toString().isEmpty()) {
                    createIngredient(ingredientNameField.getText().toString(), null, ingredientType);
                }
                else {
                    createIngredient(ingredientNameField.getText().toString(), ingredientQuantityField.getText().toString(), ingredientType);
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
     * creates an ingredient and adds it to the ingredientList
     *
     * @param name      name of the ingredient
     * @param quantity  quantity of the ingredient
     */
    private void createIngredient(String name, String quantity, Ingredient.type ingredientType) {
        try {
            validateIngredient(name, quantity);

            Ingredient ingredient;

            if(quantity == null) {
                ingredient = new Ingredient(name, null, ingredientType);
            }
            else {
                ingredient = new Ingredient(name, Double.parseDouble(quantity), ingredientType);
            }

            ingredientList.add(ingredient);
            // notify the adapter to update the list
            adapter.notifyDataSetChanged();

            // hide the 'no ingredients yet' text view
            TextView noIngredientTextView = (TextView) findViewById(R.id.noIngredientsTextView);
            noIngredientTextView.setVisibility(TextView.INVISIBLE);

            // unhide the list
            ingredientListView.setVisibility(TextView.VISIBLE);

        } catch (IllegalArgumentException e) {
            Toast.makeText(EditRecipeActivity.this, "Oops, something went wrong with that ingredient, try again", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Validates parameters for ingredient.
     *
     * @param name      value should not be empty
     * @param quantity  value should be a number and not be empty
     * @throws IllegalArgumentException if parameters not correct
     */
    private void validateIngredient(String name, String quantity) {
        if(quantity != null) {
            if( !isDouble(quantity)) {
                throw new IllegalArgumentException();
            }
        }
        if(name.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Checks if a string is a double
     *
     * @param str the string to test
     * @return true if parseable as double, false if not
     */
    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
