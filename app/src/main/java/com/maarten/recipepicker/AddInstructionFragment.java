package com.maarten.recipepicker;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.maarten.recipepicker.enums.FillInRecipeFragmentType;
import com.maarten.recipepicker.models.Instruction;
import com.maarten.recipepicker.viewModels.FillInRecipeViewModel;

public class AddInstructionFragment extends Fragment {

    private NumberPicker minuteNumberPicker, secondNumberPicker;
    private TextView minuteTextView, secondTextView;

    private SwitchMaterial timerEnabledSwitch;

    private EditText instructionTextEditText;
    private TextInputLayout instructionLayout;

    private FillInRecipeViewModel viewModel;

    public AddInstructionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_instruction, container, false);

        instructionTextEditText = view.findViewById(R.id.instructionInputEditText);
        instructionLayout = view.findViewById(R.id.instructionFieldLayout);

        minuteTextView = view.findViewById(R.id.minutesTextView);
        secondTextView = view.findViewById(R.id.secondsTextView);

        minuteNumberPicker = view.findViewById(R.id.minuteNumberPicker);
        secondNumberPicker = view.findViewById(R.id.secondNumberPicker);

        minuteNumberPicker.setMinValue(0);
        secondNumberPicker.setMinValue(0);

        minuteNumberPicker.setMaxValue(59);
        secondNumberPicker.setMaxValue(59);

        minuteTextView.setTextColor(Color.parseColor("#333333"));
        secondTextView.setTextColor(Color.parseColor("#333333"));

        final int enabledColor = ContextCompat.getColor(requireContext(), R.color.primaryColor);

        // Add eventListener to enable and disable the number pickers
        timerEnabledSwitch = view.findViewById(R.id.timerEnabledSwitch);
        timerEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                minuteNumberPicker.setEnabled(true);
                secondNumberPicker.setEnabled(true);
                minuteTextView.setEnabled(true);
                secondTextView.setEnabled(true);
                minuteTextView.setTextColor(enabledColor);
                secondTextView.setTextColor(enabledColor);
            }
            else {
                minuteNumberPicker.setEnabled(false);
                secondNumberPicker.setEnabled(false);
                minuteTextView.setTextColor(Color.parseColor("#333333"));
                secondTextView.setTextColor(Color.parseColor("#333333"));
            }
        });

        MaterialButton cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> {
            clearError();
            resetFields();
            ((AddRecipeInterface)requireActivity()).toggleCurrentFragment(FillInRecipeFragmentType.MAIN);});

        MaterialButton addInstructionButton = view.findViewById(R.id.addInstructionButton);
        addInstructionButton.setOnClickListener(v -> createInstruction());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FillInRecipeViewModel.class);

        minuteNumberPicker.setValue(viewModel.getInstructionTimerMin());
        secondNumberPicker.setValue(viewModel.getInstructionTimerSec());

        minuteNumberPicker.setEnabled(viewModel.isInstructionTimerEnabled());
        secondNumberPicker.setEnabled(viewModel.isInstructionTimerEnabled());

        timerEnabledSwitch.setChecked(viewModel.isInstructionTimerEnabled());

        instructionTextEditText.setText(viewModel.getInstructionText());
    }

    @Override
    public void onStop() {
        super.onStop();
        clearFocus();

        viewModel.setInstructionTimerMin(minuteNumberPicker.getValue());
        viewModel.setInstructionTimerSec(secondNumberPicker.getValue());

        viewModel.setInstructionText(instructionTextEditText.getText().toString());
        viewModel.setInstructionTimerEnabled(timerEnabledSwitch.isChecked());
    }

    /**
     * Removes the focus from the numberPickers otherwise a filled in number won't be registered
     */
    private void clearFocus() {
        minuteNumberPicker.clearFocus();
        secondNumberPicker.clearFocus();

        instructionTextEditText.clearFocus();
    }

    /**
     * Sets all the fields back into the default values
     */
    public void resetFields() {
        minuteNumberPicker.setValue(6);
        secondNumberPicker.setValue(30);

        timerEnabledSwitch.setChecked(false);
        instructionTextEditText.setText("");
        viewModel.resetInstructionFields();
    }

    /**
     * Removes a set error for the instruction name
     */
    private void clearError() {
        instructionLayout.setError(null);
    }

    /**
     * Checks if there is a description filled in and saves the instruction
     */
    private void createInstruction() {
        Instruction instruction;
        String description = instructionTextEditText.getText().toString();
        if (description.isEmpty()) {
            instructionLayout.setError("Please fill in an instruction.");
            return;
        }

        if(timerEnabledSwitch.isChecked()) {
            clearFocus();
            long totalMilliSeconds = calcMilliSeconds(minuteNumberPicker.getValue(), secondNumberPicker.getValue());
            instruction = new Instruction(instructionTextEditText.getText().toString(), totalMilliSeconds);
        }
        else {
            instruction = new Instruction(instructionTextEditText.getText().toString(), null);
        }
        clearError();
        viewModel.addInstruction(instruction);
        resetFields();
        ((AddRecipeInterface)requireActivity()).toggleCurrentFragment(FillInRecipeFragmentType.MAIN);
    }

    /**
     * Calculates the total amount of milliseconds
     * @param minutes the minutes given
     * @param seconds the seconds given
     * @return returns long, the total amount of seconds
     */
    private long calcMilliSeconds(int minutes, int seconds) {
        return ((minutes * 60) + seconds) * 1000;
    }

}
