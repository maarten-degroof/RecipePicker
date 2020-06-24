package com.maarten.recipepicker.viewModels;

import androidx.lifecycle.ViewModel;

public class ViewRecipeViewModel extends ViewModel {
    private int amountCooked = 0;
    private int rating = 0;

    private int tempRating = -1;
    private boolean isShowingDeleteDialog = false;
    private boolean isShowingResetCookedDialog = false;
    private boolean isShowingServesTipDialog = true;

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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getTempRating() {
        return tempRating;
    }

    public void setTempRating(int tempRating) {
        this.tempRating = tempRating;
    }

    public void resetTempRating() {
        this.tempRating = -1;
    }

    public boolean isShowingDeleteDialog() {
        return isShowingDeleteDialog;
    }

    public void setShowingDeleteDialog(boolean showingDeleteDialog) {
        isShowingDeleteDialog = showingDeleteDialog;
    }

    public boolean isShowingResetCookedDialog() {
        return isShowingResetCookedDialog;
    }

    public void setShowingResetCookedDialog(boolean showingResetCookedDialog) {
        isShowingResetCookedDialog = showingResetCookedDialog;
    }

    public boolean isShowingServesTipDialog() {
        return isShowingServesTipDialog;
    }

    public void setShowingServesTipDialog(boolean showingServesTipDialog) {
        isShowingServesTipDialog = showingServesTipDialog;
    }
}
