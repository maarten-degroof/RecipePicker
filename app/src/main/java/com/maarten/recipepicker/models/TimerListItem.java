package com.maarten.recipepicker.models;

import android.os.CountDownTimer;

/**
 * class is used in the timer array ; otherwise we have no way to give the instruction-index to the notification ender.
 */

public class TimerListItem {
    private CountDownTimer timer;
    private int instruction;

    public TimerListItem(int instruction, CountDownTimer timer)  {
        this.instruction = instruction;
        this.timer = timer;
    }

    public CountDownTimer getTimer() {
        return timer;
    }

    public int getInstruction() {
        return instruction;
    }
}
