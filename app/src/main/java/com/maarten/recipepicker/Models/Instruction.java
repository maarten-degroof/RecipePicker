package com.maarten.recipepicker.Models;

import java.io.Serializable;

public class Instruction implements Serializable {
    private String description;
    private Long milliseconds;

    public Instruction(String description, Long milliseconds) {
        this.description = description;
        this.milliseconds = milliseconds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(Long milliseconds) {
        this.milliseconds = milliseconds;
    }
}
