package com.maarten.recipepicker.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.maarten.recipepicker.RecipeUtility;
import com.maarten.recipepicker.enums.IngredientType;
import com.maarten.recipepicker.enums.QuantityType;

import java.io.Serializable;

import static com.maarten.recipepicker.MainActivity.decimalFormat;

public class Ingredient implements Serializable, Comparable<Ingredient> {

    @Expose private String name;
    @Expose private Double quantity;
    @Expose private QuantityType ingredientQuantityType;
    @Expose private IngredientType ingredientType;
    @Expose private String otherIngredientTypeName;

    public Ingredient(Ingredient ingredient){
        this.name = ingredient.getName();
        this.quantity = ingredient.getQuantity();
        this.ingredientQuantityType = ingredient.getIngredientQuantityType();
        this.ingredientType = ingredient.getIngredientType();
        this.otherIngredientTypeName = ingredient.getOtherIngredientTypeName();
    }

    public Ingredient(String name, Double quantity, QuantityType ingredientQuantityType, IngredientType ingredientType, String otherIngredientTypeName) {
        this.name = name;
        this.quantity = quantity;
        this.ingredientQuantityType = ingredientQuantityType;
        this.ingredientType = ingredientType;
        this.otherIngredientTypeName = otherIngredientTypeName;
    }

    public IngredientType getIngredientType() {
        return ingredientType;
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

    public QuantityType getIngredientQuantityType() {
        return ingredientQuantityType;
    }

    public String getOtherIngredientTypeName() {
        return otherIngredientTypeName;
    }

    public void setIngredientQuantityType(QuantityType ingredientQuantityType) {
        this.ingredientQuantityType = ingredientQuantityType;
    }

    public void setIngredientType(IngredientType ingredientType) {
        this.ingredientType = ingredientType;
    }

    public void setOtherIngredientTypeName(String otherIngredientTypeName) {
        this.otherIngredientTypeName = otherIngredientTypeName;
    }

    @NonNull
    @Override
    public String toString() {
        return (printQuantity() + " " + printType() + " " + name + " ("+ printIngredientType() + ")").trim();
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
            return decimalFormat.format(quantity);
        }
    }

    private String printIngredientType() {
        return RecipeUtility.changeFirstLetterToCapital(ingredientType.name());
    }

    /**
     * Makes it possible for quantity to be 'null'
     * @return returns "" if null or the quantity if not null
     */
    private String printType() {
        if (ingredientQuantityType.equals(QuantityType.OTHER) ) {
            return otherIngredientTypeName;
        }
        else {
            return ingredientQuantityType.name().toLowerCase();
        }
    }

    /**
     * This orders the ingredient list on the quantity
     * @param o the other ingredient to compare to
     * @return returns -1, 0 or 1 depending on the other ingredient
     */
    @Override
    public int compareTo(Ingredient o) {
        // Different null checks
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
