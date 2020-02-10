package com.maarten.recipepicker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.maarten.recipepicker.models.Recipe;


public class CookNowTimerFragment extends Fragment {


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

        return view;
    }

    public static CookNowTimerFragment newInstance(Recipe recipe) {
//        Bundle args = new Bundle();
        CookNowTimerFragment fragment = new CookNowTimerFragment();
//        args.putSerializable("Recipe", recipe);
//        fragment.setArguments(args);
        return fragment;
    }

    public void setTimerAndNotification(View view) {

    }

    public void nextInstruction(View view) {

    }

    public void previousInstruction(View view) {

    }

    public void createFinishCookingDialog(View view) {

    }

    public void cancelCookNow(View view) {

    }

}
