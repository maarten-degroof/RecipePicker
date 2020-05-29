package com.maarten.recipepicker.viewModels;

import androidx.lifecycle.ViewModel;

public class viewRecipeViewModel extends ViewModel {
    private int amountCooked = 0;

    public void addOneAmountCooked() {
        amountCooked += 1;
    }

    public void removeOneAmountCooked() {
        amountCooked -= 1;
    }

    public int getAmountCooked() {
        return amountCooked;
    }

    public void setAmountCooked(int amountCooked) {
        this.amountCooked = amountCooked;
    }
    
}
