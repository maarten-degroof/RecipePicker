package com.maarten.recipepicker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputLayout;
import com.maarten.recipepicker.enums.FillInRecipeFragmentType;
import com.maarten.recipepicker.enums.IngredientType;
import com.maarten.recipepicker.enums.QuantityType;
import com.maarten.recipepicker.models.Ingredient;
import com.maarten.recipepicker.viewModels.FillInRecipeViewModel;

import static com.maarten.recipepicker.RecipeUtility.changeFirstLetterToCapital;

public class AddIngredientFragment extends Fragment {

    private EditText ingredientNameEditText, ingredientQuantityEditText, ingredientTypeOtherEditText;
    private TextInputLayout ingredientNameLayout;

    private ChipGroup ingredientTypeChipGroup;
    private RadioGroup ingredientQuantityTypeRadioGroup;

    private FillInRecipeViewModel viewModel;

    public AddIngredientFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_ingredient, container, false);

        ingredientNameEditText = view.findViewById(R.id.ingredientNameEditText);
        ingredientNameLayout = view.findViewById(R.id.ingredientNameLayout);

        ingredientQuantityEditText = view.findViewById(R.id.ingredientQuantityEditText);

        ingredientTypeOtherEditText = view.findViewById(R.id.ingredientTypeOtherEditText);

        MaterialButton cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(view2 -> goBack());

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

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FillInRecipeViewModel.class);

        ingredientNameEditText.setText(viewModel.getIngredientName());
        Double ingredientQuantity = viewModel.getIngredientQuantity();
        if (ingredientQuantity == 0.0) {
            ingredientQuantityEditText.setText("");
        }
        else {
            ingredientQuantityEditText.setText(String.valueOf(ingredientQuantity));
        }
        ingredientTypeOtherEditText.setText(viewModel.getIngredientOtherQuantity());

        setSelectedQuantityType(viewModel.getQuantityType());
        setSelectedIngredientType(viewModel.getIngredientType());
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.setIngredientName(ingredientNameEditText.getText().toString());
        viewModel.setIngredientOtherQuantity(ingredientTypeOtherEditText.getText().toString());

        viewModel.setQuantityType(getSelectedQuantityType());

        String ingredientQuantity = ingredientQuantityEditText.getText().toString();
        if (ingredientQuantity.isEmpty() || checkQuantityNotValid(ingredientQuantity)) {
            viewModel.setIngredientQuantity(null);
        }
        else {
            viewModel.setIngredientQuantity(Double.parseDouble(ingredientQuantity));
        }
        viewModel.setIngredientType(getSelectedIngredientType());
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

    /**
     * Checks all the quantityType radioButtons and returns the selected type
     *
     * @return - the selected quantityType
     */
    private QuantityType getSelectedQuantityType() {
        QuantityType quantityType;
        try {
            MaterialRadioButton selectedRadioButton = requireView().findViewById(ingredientQuantityTypeRadioGroup.getCheckedRadioButtonId());
            String name = selectedRadioButton.getText().toString();
            quantityType = stringToQuantityType(name);
        } catch (NullPointerException exception) {
            return QuantityType.CENTILITRES;
        }
        return quantityType;
    }

    /**
     * Sets the correct radioButton with the given quantityType
     *
     * @param type - the type to set the radio button with
     */
    private void setSelectedQuantityType(QuantityType type) {
        String name = changeFirstLetterToCapital(type.toString());
        for (int index=0; index<ingredientQuantityTypeRadioGroup.getChildCount(); index++) {
            MaterialRadioButton button = (MaterialRadioButton) ingredientQuantityTypeRadioGroup.getChildAt(index);
            if (button.getText().equals(name)) {
                ingredientQuantityTypeRadioGroup.check(button.getId());
            }
        }
    }

    /**
     * Checks all the ingredientType chips and returns the selected type
     *
     * @return - the selected ingredientType; null if no ingredientType was selected
     */
    private IngredientType getSelectedIngredientType() {
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
            return null;
        }

        return stringToIngredientType(ingredientTypeName);
    }

    /**
     * Sets a given ingredientType to the chipGroup.
     * If null is given, no chip is set.
     *
     * @param type - the type item to check
     */
    private void setSelectedIngredientType(IngredientType type) {
        if (type == null) {
            return;
        }
        String convertedType = changeFirstLetterToCapital(type.name()).replace("_", " ");
        if(ingredientTypeChipGroup.getChildCount() > 0) {
            for (int i=0; i < ingredientTypeChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) ingredientTypeChipGroup.getChildAt(i);
                if (chip.getText().equals(convertedType)) {
                    ingredientTypeChipGroup.check(chip.getId());
                }
            }
        }
    }

    /**
     * Checks if each field is filled in and creates the ingredient
     */
    private void createIngredient() {
        String ingredientName = ingredientNameEditText.getText().toString();
        String ingredientQuantity = ingredientQuantityEditText.getText().toString();

        if (ingredientQuantity.isEmpty()) {
            ingredientQuantity = null;
        }

        if (checkQuantityNotValid(ingredientQuantity)) {
            Toast.makeText(requireActivity(), "Oopsie, something went wrong with that quantity, please try again.", Toast.LENGTH_LONG).show();
            return;
        }

        if (ingredientName.isEmpty()) {
            Toast.makeText(requireActivity(), "You need to fill in a name for the ingredient.", Toast.LENGTH_LONG).show();
            ingredientNameLayout.setError("Please fill in a name.");
            return;
        }

        QuantityType quantityType = getSelectedQuantityType();
        if (quantityType == null) {
            Toast.makeText(requireActivity(), "Something went wrong when selecting the quantity type.", Toast.LENGTH_LONG).show();
            return;
        }

        String otherIngredientTypeName = "";
        if (quantityType == QuantityType.OTHER) {
            otherIngredientTypeName = ingredientTypeOtherEditText.getText().toString();
        }

        IngredientType ingredientTypeName = getSelectedIngredientType();
        if (ingredientTypeName == null) {
            Toast.makeText(requireActivity(), "Oh no! Please select an ingredient type.", Toast.LENGTH_LONG).show();
            return;
        }

        Ingredient ingredient;
        ingredientName = changeFirstLetterToCapital(ingredientName.trim());

        if(ingredientQuantity == null) {
            ingredient = new Ingredient(ingredientName, null, quantityType, ingredientTypeName, otherIngredientTypeName);
        }
        else {
            ingredient = new Ingredient(ingredientName, Double.parseDouble(ingredientQuantity), quantityType, ingredientTypeName, otherIngredientTypeName);
        }

        viewModel.addIngredient(ingredient);
        goBack();
    }

    /**
     * Validates if the quantity is actually a double
     *
     * @param quantity - value should be a Double or be null
     * @return - returns true if the quantity is not a Double, false if it is
     */
    private boolean checkQuantityNotValid(String quantity) {
        if(quantity != null) {
            return !isDouble(quantity);
        }
        return false;
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
     * Closes this fragment window and shows the FillInRecipeFragment again
     */
    private void goBack() {
        resetFields();
        ((AddRecipeInterface)requireActivity()).toggleCurrentFragment(FillInRecipeFragmentType.MAIN);
    }

    /**
     * Creates an ingredientType chip and adds it to the chipGroup
     *
     * @param name - the name to give to the chip
     */
    private void generateIngredientTypeChip(String name) {
        Chip chip = new Chip(requireContext());
        ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(requireContext(), null, 0, R.style.Widget_MaterialComponents_Chip_Choice);
        chip.setChipDrawable(chipDrawable);
        chip.setText(name);
        ingredientTypeChipGroup.addView(chip);
    }

    /**
     * Creates a quantity radioButton and adds it to the radioGroup
     *
     * @param name - the name of the radioButton
     */
    private void generateQuantityTypeRadioButton(String name) {
        MaterialRadioButton radioButton = new MaterialRadioButton(requireActivity());
        radioButton.setText(name);
        ingredientQuantityTypeRadioGroup.addView(radioButton);
    }

    /**
     * Clears all the previously filled in fields
     */
    public void resetFields() {
        ingredientQuantityTypeRadioGroup.check(ingredientQuantityTypeRadioGroup.getChildAt(0).getId());

        ingredientQuantityEditText.setText("");
        ingredientTypeOtherEditText.setText("");
        ingredientNameEditText.setText("");
        ingredientNameLayout.setError(null);

        ingredientTypeChipGroup.clearCheck();
        viewModel.resetIngredientFields();
    }

}
