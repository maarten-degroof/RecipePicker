package com.maarten.recipepicker.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.maarten.recipepicker.CookNowInstructionFragment;

import java.util.List;

public class CookNowTabAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragmentList;

    public CookNowTabAdapter(@NonNull FragmentManager fm, int behavior, List<Fragment> fragmentList) {
        super(fm, behavior);
        this.fragmentList = fragmentList;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return 2;
    }
}
