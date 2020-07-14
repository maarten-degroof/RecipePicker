package com.maarten.recipepicker.viewModels;

import androidx.lifecycle.ViewModel;

import com.maarten.recipepicker.cookNow.CookNowTimerFragment;
import com.maarten.recipepicker.models.TimerListItem;

import java.util.ArrayList;
import java.util.List;

public class CookNowViewModel extends ViewModel {
    private int currentStep = 1;
    private boolean isShowingFinishDialog = false;
    private boolean isShowingTimerAlreadyRunningDialog = false;
    private int isAlreadyRunningStep = 0;
    private long isAlreadyRunningDuration = 0;
    private CookNowTimerFragment timerFragment = null;

    private List<TimerListItem> timerList = new ArrayList<>();

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public boolean isShowingFinishDialog() {
        return isShowingFinishDialog;
    }

    public void setShowingFinishDialog(boolean showingFinishDialog) {
        isShowingFinishDialog = showingFinishDialog;
    }

    public boolean isShowingTimerAlreadyRunningDialog() {
        return isShowingTimerAlreadyRunningDialog;
    }

    public void setShowingTimerAlreadyRunningDialog(boolean showingTimerAlreadyRunningDialog) {
        isShowingTimerAlreadyRunningDialog = showingTimerAlreadyRunningDialog;
    }

    public int getIsAlreadyRunningStep() {
        return isAlreadyRunningStep;
    }

    public void setIsAlreadyRunningStep(int isAlreadyRunningStep) {
        this.isAlreadyRunningStep = isAlreadyRunningStep;
    }

    public long getIsAlreadyRunningDuration() {
        return isAlreadyRunningDuration;
    }

    public void setIsAlreadyRunningDuration(long isAlreadyRunningDuration) {
        this.isAlreadyRunningDuration = isAlreadyRunningDuration;
    }

    public void addTimer(TimerListItem timer) {
        timerList.add(timer);
    }

    public void clearAllTimers() {
        timerList.clear();
    }

    public List<TimerListItem> getTimerList() {
        return timerList;
    }

    public CookNowTimerFragment getInstructionFragment() {
        return timerFragment;
    }

    public void setInstructionFragment(CookNowTimerFragment timerFragment) {
        this.timerFragment = timerFragment;
    }
}
