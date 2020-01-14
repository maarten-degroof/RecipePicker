package com.maarten.recipepicker.listSorters;

import com.maarten.recipepicker.models.Recipe;

import java.util.Comparator;

/**
 * A compare function and class which is used to compare two recipes on the 'amount cooked' field. Ordered from high to low
 */
public class AmountCookedSorter implements Comparator<Recipe> {
    public int compare(Recipe r1, Recipe r2) {
        return r2.getAmountCooked() - r1.getAmountCooked();
    }
}
