package com.maarten.recipepicker.models;


/**
 * The timer object. It has a step and an expiration time.
 * The expiration time is a millisecond value representing a time in the future.
 */
public class TimerListItem {
    private int instructionNumber;

    private long expirationTime;

    public TimerListItem(int instructionNumber, long expirationTime) {
        this.instructionNumber = instructionNumber;
        this.expirationTime = expirationTime;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public int getInstructionNumber() {
        return instructionNumber;
    }

}
