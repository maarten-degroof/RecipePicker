package com.maarten.recipepicker;


import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.maarten.recipepicker.Adapters.IngredientEditAdapter;

import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.maarten.recipepicker.MainActivity.recipeList;

public class AddRecipeActivity extends AppCompatActivity {

    private List<Ingredient> ingredientList = new ArrayList<>();
    private IngredientEditAdapter adapter;

    private EditText ingredientNameField, ingredientQuantityField;
    private Spinner ingredientTypeField;
    private ListView ingredientListView;

    private TextInputLayout recipeTitleLayout, recipeDescriptionLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_recipe);

        adapter = new IngredientEditAdapter(this, ingredientList);
        ingredientListView = findViewById(R.id.addRecipeIngredientList);
        ingredientListView.setAdapter(adapter);

        // hide the list since it is empty
        ingredientListView.setVisibility(TextView.INVISIBLE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Add Recipe");
        setSupportActionBar(toolbar);

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recipeTitleLayout = findViewById(R.id.nameFieldLayout);
        recipeDescriptionLayout = findViewById(R.id.descriptionFieldLayout);

        // make the listview (ingredientList) also scrollable when inserting text
        ViewCompat.setNestedScrollingEnabled(ingredientListView, true);



        // this makes sure that there's always one chip selected
        final ChipGroup mChipGroup = findViewById(R.id.chipGroup);
        mChipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                for (int i = 0; i < mChipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) mChipGroup.getChildAt(i);
                    if (chip != null) {
                        chip.setClickable(!(chip.getId() == mChipGroup.getCheckedChipId()));
                    }
                }
            }
        });




    }

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
            Toast.makeText(AddRecipeActivity.this, "Oops, something went wrong with that ingredient, try again", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Validates parameters for ingredient.
     *
     * @param name      value should not be empty
     * @param quantity  value should be a [Double] number (and not be empty)
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
     * @return true if parsable as double, false if not
     */
    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * gets called when the create recipe button is pressed
     * will check input and create recipe
     *
     * @param view  view given by the button
     */
    public void createRecipe(View view) {

        Boolean favouriteSwitch = ((SwitchMaterial) findViewById(R.id.favouriteSwitch)).isChecked();
        String recipeName = ((EditText) findViewById(R.id.nameField)).getText().toString();
        String recipeDescription = ((EditText) findViewById(R.id.recipeText)).getText().toString();

        // get the selected cookingtime
        ChipGroup chipGroup = findViewById(R.id.chipGroup);
        CookTime cookTime;

        switch (chipGroup.getCheckedChipId()) {
            case R.id.shortDurationChip:
                cookTime = CookTime.SHORT;
                break;
            case R.id.mediumDurationChip:
                cookTime = CookTime.MEDIUM;
                break;
            case R.id.longDurationChip:
                cookTime = CookTime.LONG;
                break;
            default:
                cookTime = CookTime.MEDIUM;

        }

        if(recipeName.isEmpty()) {
            recipeTitleLayout.setError("Please fill in a title");
        } else if (recipeDescription.isEmpty()) {
            recipeDescriptionLayout.setError("You have to fill in a description");
        } else if (ingredientList.isEmpty()) {
            Toast.makeText(AddRecipeActivity.this, "You have to add at least one ingredient", Toast.LENGTH_LONG).show();
        } else {
            Recipe recipe = new Recipe(recipeDescription,recipeName,ingredientList,favouriteSwitch, 0, cookTime);
            recipeList.add(recipe);
            Toast.makeText(AddRecipeActivity.this, "Your recipe was added!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * the cancel button to return to the previous view
     *
     * @param view  needed for the button to connect
     */
    public void cancelCreation(View view) {
        finish();
    }
}