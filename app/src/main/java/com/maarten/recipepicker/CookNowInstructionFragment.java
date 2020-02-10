package com.maarten.recipepicker;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.maarten.recipepicker.models.Instruction;
import com.maarten.recipepicker.models.Recipe;


public class CookNowInstructionFragment extends Fragment {

    private Recipe recipe;

    private TextView currentInstructionNumberTextView;
    private TextView currentInstructionTextView;
    private TextView timerDescriptionTextView;

    private MaterialButton startTimerButton;
    private MaterialButton previousInstructionButton, nextInstructionButton;
    private MaterialButton cancelButton, finishCookingButton;

    private Instruction currentInstruction;
    private int currentInstructionNumber;

    public CookNowInstructionFragment() {
        // Required empty public constructor
    }

    /**
     * Static function, returns a CookNowInstructionFragment with a given recipe bundled.
     * This method should be used when initialising a CookNowInstructionFragment.
     *
     * @param recipe - the recipe to load
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        recipe = (Recipe) getArguments().getSerializable("Recipe");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cook_now_instruction, container, false);

        currentInstructionNumberTextView = view.findViewById(R.id.currentInstructionNumberTextView);
        currentInstructionTextView = view.findViewById(R.id.currentInstructionTextView);
        timerDescriptionTextView = view.findViewById(R.id.timerDescriptionTextView);

        currentInstructionNumber = 1;
        currentInstruction = recipe.getInstructionList().get(0);

        currentInstructionNumberTextView.setText(getString(R.string.step, currentInstructionNumber));
        currentInstructionTextView.setText(currentInstruction.getDescription());

        timerDescriptionTextView = view.findViewById(R.id.timerDescriptionTextView);

        cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCookNow();
            }
        });

        finishCookingButton = view.findViewById(R.id.finishCookingButton);
        finishCookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFinishCookingDialog();
            }
        });

        previousInstructionButton = view.findViewById(R.id.previousInstructionButton);
        previousInstructionButton.setEnabled(false);
        previousInstructionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousInstruction();
            }
        });


        nextInstructionButton = view.findViewById(R.id.nextInstructionButton);
        nextInstructionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextInstruction();
            }
        });

        startTimerButton = view.findViewById(R.id.startTimerButton);
        startTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTimerAndNotification();
            }
        });

        if(currentInstruction.getMilliseconds() == null) {
            startTimerButton.setEnabled(false);
            timerDescriptionTextView.setVisibility(View.INVISIBLE);
        } else {
            int totalSeconds = (int) (currentInstruction.getMilliseconds() / 1000);
            int calcMinutes = totalSeconds / 60;
            int calcSeconds = totalSeconds % 60;

            timerDescriptionTextView.setText(getString(R.string.timer_duration_text, calcMinutes, calcSeconds));
        }

        // there's only one instruction
        if(currentInstructionNumber >= recipe.getInstructionList().size()) {
            nextInstructionButton.setEnabled(false);
        }

        return view;
    }

    private void setTimerAndNotification() {
        Toast.makeText(getActivity(), "Timer button is pressed", Toast.LENGTH_SHORT).show();
    }

    /**
     * Shows the next instruction
     */
    private void nextInstruction() {
        previousInstructionButton.setEnabled(true);
        if(currentInstructionNumber < recipe.getInstructionList().size()) {
            currentInstructionNumber++;
            currentInstruction =  recipe.getInstructionList().get(currentInstructionNumber - 1);

            currentInstructionTextView.setText(currentInstruction.getDescription());
            currentInstructionNumberTextView.setText(getString(R.string.step, currentInstructionNumber));

            if(currentInstruction.getMilliseconds() == null) {
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

            // we're at the last instruction
            if(currentInstructionNumber+1 > recipe.getInstructionList().size()) {
                nextInstructionButton.setEnabled(false);
            }
        }
    }

    /**
     * Shows the previous instruction
     */
    private void previousInstruction() {
        nextInstructionButton.setEnabled(true);
        currentInstructionNumber--;

        currentInstruction =  recipe.getInstructionList().get(currentInstructionNumber - 1);
        currentInstructionTextView.setText(currentInstruction.getDescription());
        currentInstructionNumberTextView.setText(getString(R.string.step, currentInstructionNumber));

        if(currentInstruction.getMilliseconds() == null) {
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

        // we're at the first step
        if(currentInstructionNumber <= 1) {
            previousInstructionButton.setEnabled(false);
        }
    }

    /**
     * Creates and shows the finish dialog when the finish button is pressed
     */
    private void createFinishCookingDialog() {
        if(getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("You're finished!");
            builder.setMessage("This was the last step. You finished cooking this! Press finish to close this, or you can go back to view a previous step.\n" +
                    "Pressing finish will stop all running timers.");

            builder.setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    cancelCookNow();
                }
            });
            builder.setNegativeButton("Go back", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            // create and show the dialog
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
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
