package com.maarten.recipepicker.models;

import java.io.Serializable;

public class Ingredient implements Serializable, Comparable<Ingredient> {

    private String name;
    private Double quantity;
    private type ingredientType;

    public enum type {
        grams,
        kilograms,
        millimetres,
        centilitres,
        litres,
        empty
    }

    public Ingredient(Ingredient ingredient){
        this.name = ingredient.getName();
        this.quantity = ingredient.getQuantity();
        this.ingredientType = ingredient.getIngredientType();
    }

    public Ingredient(String name, Double quantity, type ingredientType) {
        this.name = name;
        this.quantity = quantity;
        this.ingredientType = ingredientType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public type getIngredientType() {
        return ingredientType;
    }

    @Override
    public String toString() {
        return (printQuantity() + " " + printType() + " " + name).trim();
    }

    /**
     * Makes it possible for quantity to be 'null'
     *
     * @return returns "" if null or the quantity if not null
     */
    private String printQuantity() {
        if (quantity == null) {
            return "";
        }
        else {
            return quantity.toString();
        }
    }

    /**
     * Makes it possible for quantity to be 'null'
     *
     * @return returns "" if null or the quantity if not null
     */
    private String printType() {
        if (ingredientType.equals(type.empty) ) {
            return "";
        }
        else {
            return ingredientType.name();
        }
    }

    /**
     * this orders the ingredient list on the quantity
     *
     * @param o - the other ingredient to compare to
     * @return returns -1,0,1 depending on the other ingredient
     */
    @Override
    public int compareTo(Ingredient o) {
        // different null checks
        if(getQuantity()  == null && o.getQuantity() == null) {
            return 0;
        }
        if(getQuantity() == null) {
            return 1;
        }
        if (o.getQuantity() == null) {
            return -1;
        }
        return o.getQuantity().compareTo(this.getQuantity());
    }
}
