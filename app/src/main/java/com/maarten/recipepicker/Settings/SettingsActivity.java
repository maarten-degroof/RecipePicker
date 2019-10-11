package com.maarten.recipepicker.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.os.Bundle;

import com.maarten.recipepicker.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);

        // takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);


        // loads the PreferenceFragment which will load the settings
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new PreferenceFragment())
                .commit();

        setTheme(R.style.AppTheme);

    }
}
