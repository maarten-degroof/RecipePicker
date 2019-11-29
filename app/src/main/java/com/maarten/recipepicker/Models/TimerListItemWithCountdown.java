package com.maarten.recipepicker.Models;

public class TimerListItemWithCountdown {
    private int instructionNumber;
    private Long timeRemaining;

    public TimerListItemWithCountdown(int instructionNumber, Long timeRemaining) {
        this.instructionNumber = instructionNumber;
        this.timeRemaining = timeRemaining;
    }

    public int getInstructionNumber() {
        return instructionNumber;
    }

    public void lowerBy1000Millis() {
        timeRemaining -= 1000;
    }

    public void setInstructionNumber(int instructionNumber) {
        this.instructionNumber = instructionNumber;
    }

    public Long getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(Long timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
}
