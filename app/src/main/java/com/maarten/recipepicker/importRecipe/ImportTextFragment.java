package com.maarten.recipepicker.importRecipe;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.maarten.recipepicker.AddRecipeInterface;
import com.maarten.recipepicker.viewModels.FillInRecipeViewModel;
import com.maarten.recipepicker.R;
import com.maarten.recipepicker.enums.FillInRecipeFragmentType;
import com.maarten.recipepicker.models.Recipe;

public class ImportTextFragment extends Fragment {

    private EditText inputTextField;

    private FillInRecipeViewModel viewModel;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_import_text, container, false);

        MaterialButton cancelButton = view.findViewById(R.id.cancelImportButton);
        cancelButton.setOnClickListener(view1 -> requireActivity().finish());

        final MaterialButton nextButton = view.findViewById(R.id.nextStepImportButton);
        nextButton.setOnClickListener(view12 -> nextStep());

        inputTextField = view.findViewById(R.id.importStringEditText);
        // This makes it possible to scroll in the input field
        inputTextField.setOnTouchListener((v, event) -> {
            if (inputTextField.hasFocus()) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_SCROLL){
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                }
            }
            return false;
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(FillInRecipeViewModel.class);

        inputTextField.setText(viewModel.getInputJson());
    }

    @Override
    public void onStop() {
        super.onStop();

        viewModel.setInputJson(inputTextField.getText().toString());
    }

    /**
     * Is called when the 'next' button is pressed. Will try and convert the inputted text into a recipe
     */
    private void nextStep() {
        String json_text = inputTextField.getText().toString();
        if (json_text.equals("")) {
            Toast.makeText(requireActivity(), "No input", Toast.LENGTH_LONG).show();
            return;
        }
        Recipe recipe = null;
        Gson gson = new Gson();
        try {
            recipe = gson.fromJson(json_text, Recipe.class);
            if (recipe == null) {
                throw new JsonParseException("Parse error, no object created.");
            }
        } catch (JsonParseException e) {
            Toast.makeText(requireActivity(), "Something went wrong trying to read the recipe.", Toast.LENGTH_LONG).show();
            return;
        }

        viewModel.setRecipe(recipe);
        ((AddRecipeInterface)requireActivity()).toggleCurrentFragment(FillInRecipeFragmentType.MAIN);

    }

}
