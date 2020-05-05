package com.maarten.recipepicker.cookNow;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.maarten.recipepicker.R;
import com.maarten.recipepicker.RecipePickerApplication;
import com.maarten.recipepicker.adapters.CookNowTabAdapter;
import com.maarten.recipepicker.models.Recipe;

import java.util.ArrayList;
import java.util.List;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class CookNowActivity extends AppCompatActivity {

    private Recipe recipe;

    private CookNowTimerFragment timerFragment;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_now);


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Cooking now");
        setSupportActionBar(toolbar);

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // back button pressed
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        recipe = (Recipe) intent.getSerializableExtra("Recipe");

        tabLayout = findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Instructions"));
        tabLayout.addTab(tabLayout.newTab().setText("Timers"));

        CookNowInstructionFragment instructionFragment = CookNowInstructionFragment.newInstance(recipe);
        List<Fragment> fragmentList = new ArrayList<>();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.disallowAddToBackStack();
        fragmentList.add(instructionFragment);

        timerFragment = new CookNowTimerFragment();
        fragmentList.add(timerFragment);

        fragmentTransaction.commit();

        viewPager = findViewById(R.id.viewPager);
        CookNowTabAdapter cookNowTabAdapter = new CookNowTabAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, fragmentList);
        viewPager.setAdapter(cookNowTabAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        notificationManager = NotificationManagerCompat.from(this);
        createNotificationChannel();
    }

    /**
     * Call the function in the timerFragment to add a new timer
     *
     * @param step - the step to which the timer belongs
     * @param duration - the duration of the timer, noted in milliseconds
     */
    public void addTimer(int step, long duration) {
        timerFragment.setTimer(step, duration);
    }

    /**
     * Change the view to the Timer tab
     */
    public void changeToTimerFragment() {
        viewPager.setCurrentItem(1, true);
    }

    /**
     * Get the CookNowTimerFragment
     *
     * @return - returns the instance of the CookNowTimerFragment
     */
    public CookNowTimerFragment getTimerFragment() {
        return timerFragment;
    }

    /**
     * Create the notificationChannel. This is needed for Oreo and above.
     * Users can see these channels when going to the notification settings and can turn off notifications for
     * certain channels.
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Instruction is finished";
            String description = "Notification given when a timer of an instruction is finished";
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
     * Create a notification saying that the timer of an instruction is finished
     *
     * @param instructionNumber - the instruction number of the finished timer
     */
    public void createInstructionFinishedNotification(int instructionNumber) {
        Intent openThisIntent = new Intent(RecipePickerApplication.getAppContext(), CookNowActivity.class);
        final PendingIntent openThisPendingIntent =
                PendingIntent.getActivity(RecipePickerApplication.getAppContext(), 0, openThisIntent, 0);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(RecipePickerApplication.getAppContext(), "1")
                .setSmallIcon(R.drawable.ic_favorite_black_24dp)
                .setContentTitle("Timer finished")
                .setContentText("Instruction " + instructionNumber + " is finished.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openThisPendingIntent);
        notificationManager.notify(instructionNumber, builder.build());
    }

    /**
     * Remove a notification
     *
     * @param step - the instruction number of the notification
     */
    public void removeNotification(int step) {
        notificationManager.cancel(step);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancelAll();
        notificationManager = null;
    }

}
