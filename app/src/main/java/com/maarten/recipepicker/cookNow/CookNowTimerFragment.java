package com.maarten.recipepicker.cookNow;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maarten.recipepicker.R;
import com.maarten.recipepicker.RecipePickerApplication;
import com.maarten.recipepicker.adapters.TimerAdapter;
import com.maarten.recipepicker.models.TimerListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * The fragment takes care of all the timers and shows them
 */
public class CookNowTimerFragment extends Fragment {

    private RecyclerView timerRecyclerView;
    private static TimerAdapter timerAdapter;
    private static List<TimerListItem> timerList;

    public CookNowTimerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cook_now_timer, container, false);

        timerList = new ArrayList<>();
        timerRecyclerView = view.findViewById(R.id.timerRecyclerView);
        timerAdapter = new TimerAdapter((CookNowActivity) requireActivity(), timerList);
        timerRecyclerView.setAdapter(timerAdapter);
        timerRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        return view;
    }

    /**
     * Add a timer to the list. Checks if a timer is already running.
     * If so, will show a dialog with an option to restart it.
     *
     * @param step - the step to which the timer belongs
     * @param durationTime - the duration of the timer, in milliseconds
     */
    public void setTimer(int step, long durationTime) {
        if(checkIfTimerAlreadyRunning(step)) {
            showAlreadyRunningDialog(step, durationTime);
        } else {
            TimerListItem newTimer = new TimerListItem(step, System.currentTimeMillis() + durationTime);
            timerList.add(newTimer);
            timerAdapter.notifyDataSetChanged();
            ((CookNowActivity)requireActivity()).changeToTimerFragment();
        }
    }

    /**
     * Restart a timer
     *
     * @param step - the step to which the timer belongs
     * @param durationTime - the duration of the timer, in milliseconds
     */
    private void restartTimer(int step, long durationTime) {
        removeTimer(step, false);
        TimerListItem newTimer = new TimerListItem(step, System.currentTimeMillis() + durationTime);
        timerList.add(newTimer);
        timerAdapter.notifyDataSetChanged();
        ((CookNowActivity)requireActivity()).changeToTimerFragment();
    }

    /**
     * Remove a given timer
     *
     * @param step - the step to which the timer belongs
     * @param showCanceledToast - if true shows a toast that it's canceled.
     */
    public void removeTimer(int step, boolean showCanceledToast) {
        TimerListItem timerToRemove = null;
        for (TimerListItem timer : timerList) {
            if(timer.getInstructionNumber() == step) {
                timerToRemove = timer;
            }
        }
        if(timerToRemove != null) {
            timerList.remove(timerToRemove);
            timerAdapter.removeHolderFromList(step);
            timerAdapter.notifyDataSetChanged();
            if(showCanceledToast) {
                Toast.makeText(RecipePickerApplication.getAppContext(), "Canceled the timer for instruction " + step, Toast.LENGTH_LONG).show();
            }
            removeNotification(step);
        }
    }

    /**
     * Check if the step is already running
     *
     * @param step - the instruction number
     * @return - returns true if the timer for the given step is already running
     */
    private boolean checkIfTimerAlreadyRunning(int step) {
        for (TimerListItem timer : timerList) {
            if(timer.getInstructionNumber() == step) {
                return true;
            }
        }
        return false;
    }

    /**
     * Show a dialog when a timer is already running, with the option to restart the timer
     *
     * @param step - the instruction number
     * @param durationTime - the duration of the timer, noted in milliseconds
     */
    private void showAlreadyRunningDialog(final int step, final long durationTime) {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        builder.setTitle("Timer already running");
        builder.setMessage("Are you sure you want to start this timer? It's already running. Do you want to restart it?");

        builder.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                restartTimer(step, durationTime);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        // create and show the dialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timerAdapter.onDestroy();
    }

    /**
     * Remove a notification
     *
     * @param step - the instruction number of the notification
     */
    private void removeNotification(int step) {
        ((CookNowActivity)requireActivity()).removeNotification(step);
    }
}
