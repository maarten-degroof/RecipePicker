package com.maarten.recipepicker.models;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class Instruction implements Serializable {
    @Expose private String description;
    @Expose private Long milliseconds;

    public Instruction(String description, Long milliseconds) {
        this.description = description;
        this.milliseconds = milliseconds;
    }

    public String getDescription() {
        return description;
    }

    public Long getMilliseconds() {
        return milliseconds;
    }
}
