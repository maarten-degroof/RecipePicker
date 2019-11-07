package com.maarten.recipepicker;

import java.io.Serializable;

public class Instruction implements Serializable {
    private String description;
    private long milliseconds;

    public Instruction(String description, long milliseconds) {
        this.description = description;
        this.milliseconds = milliseconds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(long milliseconds) {
        this.milliseconds = milliseconds;
    }
}
