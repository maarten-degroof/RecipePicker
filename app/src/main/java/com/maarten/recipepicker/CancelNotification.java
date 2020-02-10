package com.maarten.recipepicker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

/**
 * Class exists only to be able to remove the notification if the cancel button is pressed.
 */
public class CancelNotification extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int instructionNumber = intent.getIntExtra("instructionNumber",0);
        if(instructionNumber != 0) {
            //CookNowActivity.cancelNotification(instructionNumber, true);
        }
        finish();
    }
}
