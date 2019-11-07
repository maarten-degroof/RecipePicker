package com.maarten.recipepicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CookNowActivity extends AppCompatActivity {

    private Recipe recipe;

    private int currentInstructionNumber;

    private MaterialButton previousButton, nextButton;
    private TextView currentInstructionTextView, currentInstructionNumberTextView;

    private Instruction currentInstruction;
    private List<CountDownTimer> timer;

    private NotificationManagerCompat notificationManager;


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
            currentInstructionTextView.setText("This was the last step. Enjoy your meal!");
            nextButton.setEnabled(false);
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



    public void setTimerAndNotification(View view) {
        int totalSeconds = (int) (currentInstruction.getMilliseconds() / 1000);
        int calcMinutes = totalSeconds / 60;
        int calcSeconds = totalSeconds % 60;
        final int instructionNumber = currentInstructionNumber;


        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.drawable.ic_favorite_black_24dp)
                .setContentTitle("Timer started")
                .setContentText("Instruction " + instructionNumber + " will take " + calcMinutes + " minutes and " + calcSeconds + " seconds.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);



        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(instructionNumber, builder.build());

        timer.add(new CountDownTimer(currentInstruction.getMilliseconds(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                builder.setContentText("Instruction " + instructionNumber +" will take " + (millisUntilFinished / 60000) + " minutes and " + ((millisUntilFinished / 1000) % 60) + " seconds.");
                notificationManager.notify(instructionNumber,builder.build());
            }

            @Override
            public void onFinish() {
                builder .setContentText("Your instruction (" + instructionNumber + ") is done!")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setOngoing(false);
                notificationManager.notify(instructionNumber, builder.build());
            }
        }.start());

    }

    /**
     * Be sure to remove the timers, otherwise memory leaks will occur.
     * Also remove any notifications still on screen.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(timer != null) {
            for (CountDownTimer timerItem : timer) {
                timerItem.cancel();
            }
        }
        notificationManager.cancelAll();
    }

    /**
     * the cancel button to return to the previous view
     *
     * @param view  needed for the button to connect
     */
    public void cancelCreation(View view) {
        if(timer.size() > 0) {
            for (CountDownTimer timerItem : timer) {
                timerItem.cancel();
            }
        }
        notificationManager.cancelAll();

        finish();
    }

}
