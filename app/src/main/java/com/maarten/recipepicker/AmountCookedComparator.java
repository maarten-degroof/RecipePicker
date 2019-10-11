package com.maarten.recipepicker;

import java.util.Comparator;

public class AmountCookedComparator implements Comparator<Recipe> {

    @Override
    public int compare(Recipe r1, Recipe r2) {
        return r1.getAmountCooked().compareTo(r2.getAmountCooked());
    }
}
