package com.maarten.recipepicker.models;

/**
 * This class is used in FilterIngredientsActivity;
 * contains the name and a state. State can be three things:
 *      - -1: A recipe should not have this ingredient
 *      -  0: Doesn't care
 *      -  1: A recipe should have this ingredient
 */
public class FilterIngredient {

    private String name;
    private int state;

    public FilterIngredient(String name) {
        this.name = name;
        this.state = 0;
    }

    public String getName() {
        return name;
    }

    public int getState() {
        return state;
    }

    /**
     * Toggles the state. 0 -> 1 -> -1 -> 0
     */
    public void setNextState() {
        switch (this.state) {
            case 1:
                this.state = -1;
                return;
            case -1:
                this.state = 0;
                return;
            default:
                this.state = 1;
        }
    }

    public void setStateToInclude() {
        this.state = 1;
    }

    public void setStateToExclude() {
        this.state = -1;
    }

}
