package com.maarten.recipepicker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputLayout;
import com.maarten.recipepicker.enums.IngredientType;
import com.maarten.recipepicker.enums.QuantityType;
import com.maarten.recipepicker.models.Ingredient;

public class AddIngredientFragment extends Fragment {

    private Toolbar toolbar;

    private EditText ingredientNameEditText, ingredientQuantityEditText, ingredientTypeOtherEditText;
    private TextInputLayout ingredientNameLayout, ingredientQuantityLayout, ingredientTypeOtherLayout;

    private ChipGroup ingredientTypeChipGroup;

    private RadioGroup ingredientQuantityTypeRadioGroup;


    public AddIngredientFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddIngredientFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddIngredientFragment newInstance() {
        AddIngredientFragment fragment = new AddIngredientFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("fragment", "in on create");
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("fragment", "in on resume");
    }

    /**
     * Clears all the previously filled in fields
     */
    public void resetFields() {
        ingredientQuantityTypeRadioGroup.check(ingredientQuantityTypeRadioGroup.getChildAt(0).getId());
        ingredientTypeOtherEditText.setText("");
        ingredientQuantityEditText.setText("");
        ingredientNameEditText.setText("");
        ingredientTypeChipGroup.clearCheck();
    }

    /**
     * Converts a string into its respective IngredientType and returns that
     *
     * @param name a string representation of the type
     * @return the found IngredientType
     */
    private IngredientType stringToIngredientType(String name) {
        name = name.toUpperCase().replace(" ", "_");
        return IngredientType.valueOf(name);
    }

    /**
     * Converts a string into its respective QuantityType and returns that
     *
     * @param name a string representation of the type
     * @return the found IngredientType
     */
    private QuantityType stringToQuantityType(String name) {
        name = name.toUpperCase().replace(" ", "_");
        return QuantityType.valueOf(name);
    }

    private void createIngredient() {
        // TODO: check for valid ingredient

        String ingredientName = ingredientNameEditText.getText().toString();
        String ingredientQuantity = ingredientQuantityEditText.getText().toString();

        if (ingredientQuantity.isEmpty()) {
            ingredientQuantity = null;
        }

        if (ingredientName.isEmpty()) {
            Toast.makeText(requireActivity(), "You need to fill in a name for the ingredient.", Toast.LENGTH_LONG).show();
            ingredientNameLayout.setError("Please fill in a name.");
            return;
        }

        // The Quantity type
        QuantityType quantityType;
        String otherIngredientTypeName = "";
        try {
            MaterialRadioButton selectedRadioButton = requireView().findViewById(ingredientQuantityTypeRadioGroup.getCheckedRadioButtonId());
            String name = selectedRadioButton.getText().toString();
            quantityType = stringToQuantityType(name);
        } catch (NullPointerException exception) {
            Toast.makeText(requireActivity(), "Something went wrong when selecting the quantity type.", Toast.LENGTH_LONG).show();
            return;
        }

        if (quantityType == QuantityType.OTHER) {
            otherIngredientTypeName = ingredientTypeOtherEditText.getText().toString();
        }


        String ingredientTypeName = null;
        if(ingredientTypeChipGroup.getChildCount() > 0) {
            for (int i=0; i < ingredientTypeChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) ingredientTypeChipGroup.getChildAt(i);
                if (chip.isChecked()) {
                    ingredientTypeName = chip.getText().toString();
                }
            }
        }
        if (ingredientTypeName == null) {
            Toast.makeText(requireActivity(), "Oh no! Please select an ingredient type.", Toast.LENGTH_LONG).show();
            return;
        }

        // TODO: pasting text doesn't show up in the textscreen but does show up when creating the ingredient
        // TODO: add ingredient name recommendation
        // TODO: change add recipe activity into a fragment so it can be used on the other places as well
        // TODO: add some way to fix the switching mode
        // TODO: add pop up animation

        try {
            validateIngredient(ingredientName, ingredientQuantity);

            Ingredient ingredient;
            ingredientName = RecipeUtility.changeFirstLetterToCapital(ingredientName.trim());

            if(ingredientQuantity == null) {
                ingredient = new Ingredient(ingredientName, null, quantityType, stringToIngredientType(ingredientTypeName), otherIngredientTypeName);
            }
            else {
                ingredient = new Ingredient(ingredientName, Double.parseDouble(ingredientQuantity), quantityType, stringToIngredientType(ingredientTypeName), otherIngredientTypeName);
            }


            ((AddRecipeActivity)requireActivity()).addIngredientToList(ingredient);
            goBack();

        } catch (IllegalArgumentException e) {
            Toast.makeText(requireActivity(), "Oops, something went wrong with that ingredient, try again", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Validates parameters for ingredient
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

    private void goBack() {
        ((AddRecipeActivity)requireActivity()).toggleShowingAddIngredientFragment(false);
    }

    private void generateIngredientTypeChip(String name) {
        Chip chip = new Chip(requireContext());
        ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(requireContext(), null, 0, R.style.Widget_MaterialComponents_Chip_Choice);
        chip.setChipDrawable(chipDrawable);
        chip.setText(name);
        ingredientTypeChipGroup.addView(chip);
    }

    private void generateQuantityTypeRadioButton(String name) {
        MaterialRadioButton radioButton = new MaterialRadioButton(requireActivity());
        radioButton.setText(name);
        ingredientQuantityTypeRadioGroup.addView(radioButton);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_ingredient, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Add Ingredient");
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);

        toolbar.setNavigationOnClickListener(view1 -> goBack());

        ingredientNameEditText = view.findViewById(R.id.ingredientNameEditText);
        ingredientNameLayout = view.findViewById(R.id.ingredientNameLayout);

        ingredientQuantityEditText = view.findViewById(R.id.ingredientQuantityEditText);
        ingredientQuantityLayout = view.findViewById(R.id.ingredientQuantityLayout);

        ingredientTypeOtherEditText = view.findViewById(R.id.ingredientTypeOtherEditText);
        ingredientTypeOtherLayout = view.findViewById(R.id.ingredientTypeOtherLayout);

        MaterialButton cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(view2 ->
                goBack());

        MaterialButton createIngredientButton = view.findViewById(R.id.addIngredientButton);
        createIngredientButton.setOnClickListener(view12 -> createIngredient());

        ingredientTypeChipGroup = view.findViewById(R.id.ingredientTypeChipGroup);
        for (String type : RecipeUtility.convertEnumToStringList("IngredientType")) {
            generateIngredientTypeChip(type);
        }

        ingredientQuantityTypeRadioGroup = view.findViewById(R.id.ingredientQuantityTypeRadioGroup);
        for (String type : RecipeUtility.convertEnumToStringList("QuantityType")) {
            generateQuantityTypeRadioButton(type);
        }
        // Check the first item in the radio group
        ingredientQuantityTypeRadioGroup.check(ingredientQuantityTypeRadioGroup.getChildAt(0).getId());



        return view;
    }
}
