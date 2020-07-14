package com.maarten.recipepicker.cookNow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.maarten.recipepicker.R;
import com.maarten.recipepicker.models.Instruction;
import com.maarten.recipepicker.models.Recipe;
import com.maarten.recipepicker.viewModels.CookNowViewModel;

/**
 * This fragment takes care of the instructions
 */
public class CookNowInstructionFragment extends Fragment {

    private Recipe recipe;

    private TextView currentInstructionNumberTextView;
    private TextView currentInstructionTextView;
    private TextView timerDescriptionTextView;

    private MaterialButton startTimerButton;
    private MaterialButton previousInstructionButton, nextInstructionButton;

    private Instruction currentInstruction;
    private int currentInstructionNumber;

    private CookNowViewModel viewModel;

    public CookNowInstructionFragment() {
        // Required empty public constructor
    }

    /**
     * Static function, returns a CookNowInstructionFragment with a given recipe bundled.
     * This method should be used when initialising a CookNowInstructionFragment.
     * @param recipe the recipe to load
     * @return returns a CookNowInstructionFragment with a recipe bundled
     */
    static CookNowInstructionFragment newInstance(Recipe recipe) {
        Bundle args = new Bundle();
        CookNowInstructionFragment fragment = new CookNowInstructionFragment();
        args.putSerializable("Recipe", recipe);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        recipe = (Recipe) getArguments().getSerializable("Recipe");

        View view = inflater.inflate(R.layout.fragment_cook_now_instruction, container, false);

        currentInstructionNumberTextView = view.findViewById(R.id.currentInstructionNumberTextView);
        currentInstructionTextView = view.findViewById(R.id.currentInstructionTextView);
        timerDescriptionTextView = view.findViewById(R.id.timerDescriptionTextView);

        MaterialButton cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> cancelCookNow());

        MaterialButton finishCookingButton = view.findViewById(R.id.finishCookingButton);
        finishCookingButton.setOnClickListener(v -> createFinishCookingDialog());

        previousInstructionButton = view.findViewById(R.id.previousInstructionButton);
        previousInstructionButton.setOnClickListener(v -> previousInstruction());

        nextInstructionButton = view.findViewById(R.id.nextInstructionButton);
        nextInstructionButton.setOnClickListener(v -> nextInstruction());

        startTimerButton = view.findViewById(R.id.startTimerButton);
        startTimerButton.setOnClickListener(v -> setTimer());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CookNowViewModel.class);

        currentInstructionNumber = viewModel.getCurrentStep();
        loadInstruction();

        if (viewModel.isShowingFinishDialog()) {
            createFinishCookingDialog();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        viewModel.setCurrentStep(currentInstructionNumber);
    }

    /**
     * Start a new timer on the currently selected instruction
     */
    private void setTimer() {
        // what gets passed here??
        ((CookNowActivity)requireActivity()).addTimer(currentInstructionNumber, currentInstruction.getMilliseconds());
    }

    /**
     * Show the next instruction
     */
    private void nextInstruction() {
        currentInstructionNumber++;
        loadInstruction();
    }

    /**
     * Loads the instruction by looking at the currentInstructionNumber variable. Toggles the relevant buttons.
     */
    private void loadInstruction() {
        currentInstruction = recipe.getInstructionList().get(currentInstructionNumber - 1);

        currentInstructionTextView.setText(currentInstruction.getDescription());
        currentInstructionNumberTextView.setText(getString(R.string.step, currentInstructionNumber));

        if (currentInstruction.getMilliseconds() == null) {
            startTimerButton.setEnabled(false);
            timerDescriptionTextView.setVisibility(View.GONE);
        } else {
            startTimerButton.setEnabled(true);
            timerDescriptionTextView.setVisibility(View.VISIBLE);
            int totalSeconds = (int) (currentInstruction.getMilliseconds() / 1000);
            int calcMinutes = totalSeconds / 60;
            int calcSeconds = totalSeconds % 60;

            timerDescriptionTextView.setText(getString(R.string.timer_duration_text, calcMinutes, calcSeconds));
        }

        // We're at the first instruction
        if (currentInstructionNumber <= 1) {
            previousInstructionButton.setEnabled(false);
        } else {
            previousInstructionButton.setEnabled(true);
        }

        // We're at the last instruction
        if (currentInstructionNumber+1 > recipe.getInstructionList().size()) {
            nextInstructionButton.setEnabled(false);
        } else {
            nextInstructionButton.setEnabled(true);
        }
    }

    /**
     * Show the previous instruction
     */
    private void previousInstruction() {
        currentInstructionNumber--;
        loadInstruction();
    }

    /**
     * Create and show the finish dialog when the finish button is pressed
     */
    private void createFinishCookingDialog() {
        viewModel.setShowingFinishDialog(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("You're finished!");
        builder.setMessage("This was the last step. You finished cooking this! Press finish to close this, or you can go back to view a previous step.\n" +
                "Pressing finish will stop all running timers.");

        builder.setPositiveButton("Finish", (dialog, id) -> {
            cancelCookNow();
            viewModel.setShowingFinishDialog(false);
        });
        builder.setNegativeButton("Go back", (dialog, id) -> {
            viewModel.setShowingFinishDialog(false);
        });
        // Create and show the dialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnCancelListener(dialog -> viewModel.setShowingFinishDialog(false));
        alertDialog.show();

    }

    /**
     * Removes all timers, and goes back to the previous activity
     */
    private void cancelCookNow() {
        if(getActivity() != null) {
            getActivity().finish();
        }
    }

}
