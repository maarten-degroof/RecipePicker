package com.maarten.recipepicker.models;

/**
 * This object is used in FilterIngredientsActivity; contains the name and a boolean saying if it's checked
 */
public class FilterIngredient {

    private String name;
    private boolean isChecked;

    public FilterIngredient(String name) {
        this.name = name;
        this.isChecked = false;
    }

    public String getName() {
        return name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void toggleIsChecked() {
        isChecked = !isChecked;
    }

}
