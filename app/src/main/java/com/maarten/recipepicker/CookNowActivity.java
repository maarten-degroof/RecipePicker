package com.maarten.recipepicker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.maarten.recipepicker.adapters.CookNowTabAdapter;
import com.maarten.recipepicker.models.Recipe;

import java.util.ArrayList;
import java.util.List;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class CookNowActivity extends AppCompatActivity {

    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_now);


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Cooking now");
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

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Instructions"));
        tabLayout.addTab(tabLayout.newTab().setText("Timers"));

        CookNowInstructionFragment instructionFragment = CookNowInstructionFragment.newInstance(recipe);
        List<Fragment> fragmentList = new ArrayList<>();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.disallowAddToBackStack();
        fragmentList.add(instructionFragment);

        CookNowTimerFragment timerFragment = new CookNowTimerFragment();
        fragmentList.add(timerFragment);

        fragmentTransaction.commit();

        final ViewPager viewPager = findViewById(R.id.viewPager);
        CookNowTabAdapter cookNowTabAdapter = new CookNowTabAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, fragmentList);
        viewPager.setAdapter(cookNowTabAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
    }
}
