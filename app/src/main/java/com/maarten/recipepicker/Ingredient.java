package com.maarten.recipepicker;

import java.io.Serializable;

public class Ingredient implements Serializable {

    private String name;
    private Double quantity;
    private type ingredientType;

    public enum type {
        grams,
        kilograms,
        millimetres,
        centilitres,
        litres,
        items
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

    public void setIngredientType(type ingredientType) {
        this.ingredientType = ingredientType;
    }

    @Override
    public String toString() {
        return printQuantity() + " " + ingredientType.name() + " " + name;
    }

    /**
     * Makes it possible for quantity to be 'null'
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
}
