package com.maarten.recipepicker.listSorters;

import com.maarten.recipepicker.models.Recipe;

import java.util.Comparator;

/**
 * A compare function and class which is used to compare two recipes on the 'date added' field. Ordered from new to old
 */
public class DateSorter implements Comparator<Recipe> {
    public int compare(Recipe r1, Recipe r2) {
        if (r1.getAddedDate().before(r2.getAddedDate())) {
            return 1;
        } else if (r1.getAddedDate().after(r2.getAddedDate())) {
            return -1;
        } else {
            return 0;
        }
    }
}
