package com.maarten.recipepicker.viewModels;

import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private boolean isShowingResetTimesCookedDialog = false;
    private boolean isShowingResetEverythingDialog = false;
    private boolean resetOriginalValuesResetEverythingIsTicked = false;

    public boolean isShowingResetTimesCookedDialog() {
        return isShowingResetTimesCookedDialog;
    }

    public void setShowingResetTimesCookedDialog(boolean showingResetTimesCookedDialog) {
        isShowingResetTimesCookedDialog = showingResetTimesCookedDialog;
    }

    public boolean isShowingResetEverythingDialog() {
        return isShowingResetEverythingDialog;
    }

    public void setShowingResetEverythingDialog(boolean showingResetEverythingDialog) {
        isShowingResetEverythingDialog = showingResetEverythingDialog;
    }

    public boolean isResetOriginalValuesResetEverythingIsTicked() {
        return resetOriginalValuesResetEverythingIsTicked;
    }

    public void setResetOriginalValuesResetEverythingIsTicked(boolean resetOriginalValuesResetEverythingIsTicked) {
        this.resetOriginalValuesResetEverythingIsTicked = resetOriginalValuesResetEverythingIsTicked;
    }
}
