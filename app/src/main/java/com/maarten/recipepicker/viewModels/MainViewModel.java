package com.maarten.recipepicker.viewModels;

import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private String fact = "";
    private int sortingType = 0;

    private boolean isShowingSortingDialog = false;
    private boolean isShowingWelcomeScreen = true;
    private int currentHelpScreen = -1;

    public String getFact() {
        return fact;
    }

    public void setFact(String fact) {
        this.fact = fact;
    }

    public int getSortingType() {
        return sortingType;
    }

    public void setSortingType(int sortingType) {
        this.sortingType = sortingType;
    }

    public boolean isShowingWelcomeScreen() {
        return isShowingWelcomeScreen;
    }

    public void setShowingWelcomeScreen(boolean showingWelcomeScreen) {
        isShowingWelcomeScreen = showingWelcomeScreen;
    }

    public boolean isShowingSortingDialog() {
        return isShowingSortingDialog;
    }

    public void setShowingSortingDialog(boolean showingSortingDialog) {
        isShowingSortingDialog = showingSortingDialog;
    }

    public int getCurrentHelpScreen() {
        return currentHelpScreen;
    }

    public void setCurrentHelpScreen(int currentHelpScreen) {
        this.currentHelpScreen = currentHelpScreen;
    }

    public void resetCurrentHelpScreen() {
        this.currentHelpScreen = -1;
    }

}
