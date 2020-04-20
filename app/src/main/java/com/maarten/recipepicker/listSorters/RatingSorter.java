package com.maarten.recipepicker.listSorters;

import com.maarten.recipepicker.models.Recipe;

import java.util.Comparator;

public class RatingSorter implements Comparator<Recipe> {
    public int compare(Recipe r1, Recipe r2) {
        return r2.getRating() - r1.getRating();
    }
}
