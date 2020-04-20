package com.maarten.recipepicker.importRecipe;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.maarten.recipepicker.R;

public class ImportTextRecipeFragment extends Fragment {

    private TextInputEditText inputTextField;

    public static ImportTextRecipeFragment newInstance() {
        return new ImportTextRecipeFragment();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_import_text_recipe, container, false);

        MaterialButton cancelButton = view.findViewById(R.id.cancelImportButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().finish();
            }
        });

        final MaterialButton nextButton = view.findViewById(R.id.nextStepImportButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextStep();
            }
        });

        inputTextField = view.findViewById(R.id.importStringEditText);
        // This makes it possible to scroll in the input field
        inputTextField.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (inputTextField.hasFocus()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_SCROLL){
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                    }
                }
                return false;
            }
        });

        return view;
    }

    private void nextStep() {
        String json_text = inputTextField.getText().toString();
        if (json_text.equals("")) {
            Toast.makeText(requireActivity(), "No input", Toast.LENGTH_LONG).show();
        }
        else {
            ImportViewRecipeFragment importViewRecipeFragment = new ImportViewRecipeFragment();
            Bundle bundle = new Bundle();
            bundle.putString("json_recipe", json_text);
            importViewRecipeFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction().replace(R.id.importFragmentLayout, importViewRecipeFragment)
                    .addToBackStack(null).commit();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
