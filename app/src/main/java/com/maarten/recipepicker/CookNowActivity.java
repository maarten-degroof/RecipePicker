package com.maarten.recipepicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.maarten.recipepicker.adapters.TimerAdapter;

import java.util.ArrayList;
import java.util.List;

public class CookNowActivity extends AppCompatActivity {

    private Recipe recipe;

    private int currentInstructionNumber;

    private MaterialButton previousButton, nextButton, startTimerButton, finishCookingButton;
    private TextView currentInstructionTextView, currentInstructionNumberTextView, timerDescriptionTextView;

    private Instruction currentInstruction;

    private static List<TimerListItem> timerList;
    private static List<TimerListItemWithCountdown> timerListCountdown;

    public static NotificationManagerCompat notificationManager;

    private RecyclerView timerRecyclerView;
    private static TimerAdapter timerAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_now);


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Cook");
        setSupportActionBar(toolbar);

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                finish();
            }
        });

        Intent intent = getIntent();
        recipe = (Recipe) intent.getSerializableExtra("Recipe");

        currentInstructionNumberTextView = findViewById(R.id.currentInstructionNumberTextView);
        currentInstructionTextView = findViewById(R.id.currentInstructionTextView);

        previousButton = findViewById(R.id.previousInstructionButton);
        previousButton.setEnabled(false);

        nextButton = findViewById(R.id.nextInstructionButton);

        currentInstructionNumber = 1;

        currentInstruction = recipe.getInstructionList().get(0);

        currentInstructionNumberTextView.setText(getString(R.string.step, currentInstructionNumber));
        currentInstructionTextView.setText(currentInstruction.getDescription());

        createNotificationChannel();

        timerList = new ArrayList<>();

        notificationManager = NotificationManagerCompat.from(this);

        startTimerButton = findViewById(R.id.startTimerButton);
        timerDescriptionTextView = findViewById(R.id.timerDescriptionTextView);
        if(currentInstruction.getMilliseconds() == null) {
            startTimerButton.setEnabled(false);
            timerDescriptionTextView.setVisibility(View.INVISIBLE);
        } else {
            int totalSeconds = (int) (currentInstruction.getMilliseconds() / 1000);
            int calcMinutes = totalSeconds / 60;
            int calcSeconds = totalSeconds % 60;

            timerDescriptionTextView.setText(getString(R.string.timer_duration_text, calcMinutes, calcSeconds));
        }

        timerListCountdown = new ArrayList<>();
        timerRecyclerView = findViewById(R.id.timerRecyclerView);
        timerAdapter = new TimerAdapter(this, timerListCountdown);

        timerRecyclerView.setAdapter(timerAdapter);
        timerRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        // there's only one instruction
        if(currentInstructionNumber >= recipe.getInstructionList().size()) {
            nextButton.setEnabled(false);
        }

    }

    /**
     * Shows the next instruction
     *
     * @param view - the 'next' button
     */
    public void nextInstruction(View view) {
        previousButton.setEnabled(true);
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
                nextButton.setEnabled(false);
            }
        }
    }

    /**
     * Shows the previous instruction
     *
     * @param view - the 'previous' button
     */
    public void previousInstruction(View view) {
        nextButton.setEnabled(true);
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
            previousButton.setEnabled(false);
        }
    }

    /**
     * Shows the AlertDialog when you pressed the finish button
     * User can choose to finish this activity or go back and view a previous step
     */
    public void createFinishCookingDialog(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("You're finished!");
        builder.setMessage("This was the last step. You finished cooking this! Press finish to close this, or you can go back to view a previous step.\n" +
                "Pressing finish will stop all running timers.");

        builder.setPositiveButton("Finish", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                cancelCookNow(null);
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

    /**
     * Creates the notificationChannel. This is needed for Oreo and above.
     * Users can see these channels when going to the notification settings and can turn off notifications for
     * certain channels.
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CookingInstructionNotification";
            String description = "Notification about the current instruction when cooking";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Cancels a given notification if it exists
     *
     * @param index - the instructionNumber of the notification
     * @param showRemoveToast - if true shows the canceled toast, if false shows restarted toast.
     */
    public static void cancelNotification(int index, boolean showRemoveToast) {
        try {
            TimerListItem listItemToRemove = null;
            for(TimerListItem listItem : timerList) {
                if(listItem.getInstruction() == index) {
                    listItem.getTimer().cancel();
                    listItemToRemove = listItem;
                    notificationManager.cancel(index);
                }
            }
            if(listItemToRemove != null) {
                timerList.remove(listItemToRemove);
                TimerListItemWithCountdown tempItem = getTimerListCountdownObject(index);

                Long timeRemaining = null;

                if(tempItem != null) {
                    timeRemaining = tempItem.getTimeRemaining();
                    timerListCountdown.remove(tempItem);
                    timerAdapter.notifyDataSetChanged();
                }

                // if timeRemaining == 0 -> timer was completed
                if(timeRemaining != null && timeRemaining == 0) {
                    Toast.makeText(RecipePickerApplication.getAppContext(), "Removed the completed instruction " + index, Toast.LENGTH_LONG).show();
                } else if(showRemoveToast) {
                    Toast.makeText(RecipePickerApplication.getAppContext(), "Canceled the timer for instruction " + index, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RecipePickerApplication.getAppContext(), "Restarted the timer for instruction " + index, Toast.LENGTH_LONG).show();
                }
            }

        } catch (Exception e) {
            Log.e("NotificationError", e.getMessage());
        }
    }

    /**
     * Returns the TimerListItemWithCountdown from the timerListCountdown list with a given instructionNumber
     *
     * @param instructionNumber - the instructionNumber you want to find the object of
     * @return - the found TimerListItemWithCountdown, or null if it doesn't exist
     */
    private static TimerListItemWithCountdown getTimerListCountdownObject(int instructionNumber) {
        for (TimerListItemWithCountdown listItem : timerListCountdown) {
            if(listItem.getInstructionNumber() == instructionNumber) {
                return listItem;
            }
        }
        return null;
    }

    /**
     * Creates a timer, notification, and puts the timer in the 'timer' list.
     * Also removes the notification if it already exists.
     *
     * @param view - the 'start timer' button
     */
    public void setTimerAndNotification(View view) {
        int totalSeconds = (int) (currentInstruction.getMilliseconds() / 1000);
        int calcMinutes = totalSeconds / 60;
        int calcSeconds = totalSeconds % 60;
        final int instructionNumber = currentInstructionNumber;

        // cancel any running timers for this instruction
        cancelNotification(instructionNumber, false);

        Intent cancelIntent = new Intent(this, CancelNotification.class);
        cancelIntent.putExtra("instructionNumber", instructionNumber);
        final PendingIntent cancelPendingIntent =
                PendingIntent.getActivity(this, instructionNumber, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.drawable.ic_favorite_black_24dp)
                .setContentTitle("Timer started")
                .setContentText("Instruction " + instructionNumber + " will take " + calcMinutes + " minutes and " + calcSeconds + " seconds.")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_home_black_24dp, "Cancel", cancelPendingIntent);

        final TimerListItemWithCountdown tempTimerListItemWithCountdown = new TimerListItemWithCountdown(instructionNumber, currentInstruction.getMilliseconds());
        timerListCountdown.add(tempTimerListItemWithCountdown);

        TimerListItem tempTimerlistItem = new TimerListItem(instructionNumber, new CountDownTimer(currentInstruction.getMilliseconds(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                builder.setContentText("Instruction " + instructionNumber +" will take " + (millisUntilFinished / 60000) + " minutes and " + ((millisUntilFinished / 1000) % 60) + " seconds.");
                notificationManager.notify(instructionNumber,builder.build());
                timerListCountdown.get(timerListCountdown.indexOf(tempTimerListItemWithCountdown)).lowerBy1000Millis();
                timerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFinish() {
                builder .setContentTitle("Timer " + instructionNumber + " finished")
                        .setContentText("Your instruction (" + instructionNumber + ") is done!")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setOnlyAlertOnce(false)
                        .setOngoing(false);
                // notificationId is a unique int for each notification that you must define
                notificationManager.notify(instructionNumber, builder.build());
            }
        }.start());
        timerList.add(tempTimerlistItem);

    }

    /**
     * Be sure to remove the timers, otherwise memory leaks will occur.
     * Also remove any notifications still on screen.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(timerList.size() > 0) {
            for (TimerListItem timerItem : timerList) {
                timerItem.getTimer().cancel();
            }
        }
        notificationManager.cancelAll();
    }

    /**
     * the cancel button to return to the previous view
     *
     * @param view  needed for the button to connect
     */
    public void cancelCookNow(View view) {
        if(timerList.size() > 0) {
            for (TimerListItem timerItem : timerList) {
                timerItem.getTimer().cancel();
            }
        }
        notificationManager.cancelAll();

        finish();
    }

}
