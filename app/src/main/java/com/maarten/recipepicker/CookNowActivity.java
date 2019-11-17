package com.maarten.recipepicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

public class CookNowActivity extends AppCompatActivity {

    private Recipe recipe;

    private int currentInstructionNumber;

    private MaterialButton previousButton, nextButton;
    private TextView currentInstructionTextView, currentInstructionNumberTextView;

    private Instruction currentInstruction;

    public static List<TimerListItem> timer;

    private static NotificationManagerCompat notificationManager;

    // class is used in the timer array ; otherwise we have no way to give the instruction-index to the notification ender.
    private class TimerListItem {
        private CountDownTimer timer;
        private int instruction;
        public TimerListItem(int instruction, CountDownTimer timer)  {
            this.instruction = instruction;
            this.timer = timer;
        }

        public CountDownTimer getTimer() {
            return timer;
        }

        public int getInstruction() {
            return instruction;
        }
    }


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

        currentInstructionNumberTextView.setText(String.valueOf(currentInstructionNumber));
        currentInstructionTextView.setText(currentInstruction.getDescription());

        createNotificationChannel();

        timer = new ArrayList<>();

        notificationManager = NotificationManagerCompat.from(this);

    }

    public void nextInstruction(View view) {
        previousButton.setEnabled(true);
        if(currentInstructionNumber < recipe.getInstructionList().size()) {
            currentInstructionNumber++;
            currentInstruction =  recipe.getInstructionList().get(currentInstructionNumber - 1);

            currentInstructionTextView.setText(currentInstruction.getDescription());
            currentInstructionNumberTextView.setText(String.valueOf(currentInstructionNumber));

        }
        // else we're at the last instruction
        else {
            //nextButton.setEnabled(false);
            createFinishCookingDialog();
        }


    }

    public void previousInstruction(View view) {
        nextButton.setEnabled(true);
        currentInstructionNumber--;

        currentInstruction =  recipe.getInstructionList().get(currentInstructionNumber - 1);
        currentInstructionTextView.setText(currentInstruction.getDescription());
        currentInstructionNumberTextView.setText(String.valueOf(currentInstructionNumber));

        // we're at the first step
        if(currentInstructionNumber <= 1) {
            previousButton.setEnabled(false);
        }
    }

    /**
     * Shows the AlertDialog when you have reached the last step.
     * User can choose to finish this activity or go back and view a previous step
     */
    public void createFinishCookingDialog() {

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
            int importance = NotificationManager.IMPORTANCE_HIGH;
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
            for(TimerListItem listItem : timer) {
                if(listItem.getInstruction() == index) {
                    listItem.getTimer().cancel();
                    listItemToRemove = listItem;
                    notificationManager.cancel(index);
                }
            }
            if(listItemToRemove != null) {
                timer.remove(listItemToRemove);

                if(showRemoveToast) {
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
        PendingIntent cancelPendingIntent =
                PendingIntent.getActivity(this, instructionNumber, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.drawable.ic_favorite_black_24dp)
                .setContentTitle("Timer started")
                .setContentText("Instruction " + instructionNumber + " will take " + calcMinutes + " minutes and " + calcSeconds + " seconds.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .addAction(R.drawable.ic_home_black_24dp, "Cancel", cancelPendingIntent);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(instructionNumber, builder.build());

        timer.add( new TimerListItem(instructionNumber, new CountDownTimer(currentInstruction.getMilliseconds(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                builder.setContentText("Instruction " + instructionNumber +" will take " + (millisUntilFinished / 60000) + " minutes and " + ((millisUntilFinished / 1000) % 60) + " seconds.");
                notificationManager.notify(instructionNumber,builder.build());
            }

            @Override
            public void onFinish() {
                builder .setContentTitle("Timer " + instructionNumber + " finished")
                        .setContentText("Your instruction (" + instructionNumber + ") is done!")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setOngoing(false);
                notificationManager.notify(instructionNumber, builder.build());
            }
        }.start()));

    }

    /**
     * Be sure to remove the timers, otherwise memory leaks will occur.
     * Also remove any notifications still on screen.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(timer.size() > 0) {
            for (TimerListItem timerItem : timer) {
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
        if(timer.size() > 0) {
            for (TimerListItem timerItem : timer) {
                timerItem.getTimer().cancel();
            }
        }
        notificationManager.cancelAll();

        finish();
    }

}
