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

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Long getMilliseconds() {
        return milliseconds;
    }
}
