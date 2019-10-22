package com.maarten.recipepicker;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;



public class Recipe implements Serializable {
    private String description;
    private String title;
    private List<Ingredient> ingredientList;
    private Boolean favorite;
    private Integer amountCooked;
    private Date addedDate;
    private CookTime cookTime;


    public Recipe(String description, String title, List<Ingredient> ingredientList, Boolean favorite, Integer amountCooked, CookTime cookTime) {
        this.description = description;
        this.title = title;
        this.ingredientList = ingredientList;
        this.favorite = favorite;
        this.amountCooked = amountCooked;
        this.addedDate = Calendar.getInstance().getTime();
        this.cookTime = cookTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Ingredient> getIngredientList() {
        return ingredientList;
    }

    public void setIngredientList(List<Ingredient> ingredientList) {
        this.ingredientList = ingredientList;
    }

    public Boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    public Integer getAmountCooked() {
        return amountCooked;
    }

    public CookTime getCookTime() {
        return cookTime;
    }

    public void setCookTime(CookTime cookTime) {
        this.cookTime = cookTime;
    }

    public void resetAmountCooked() {
        this.amountCooked = 0;
    }

    public void addOneAmountCooked() {
        this.amountCooked++;
    }

    public void removeOneAmountCooked() {
        this.amountCooked--;
    }

    public Date getAddedDate() {
        return addedDate;
    };

    /**
     * compares an object to itself. returns true if they're the same
     *
     * @param obj object to compare, should be of type recipe
     * @return returns true if object is the same as this object
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Recipe)) return false;
        Recipe o = (Recipe) obj;
        return o.getTitle().equals(this.getTitle()) && o.getDescription().equals(this.getDescription());
        // check ingredientlist!!
    }
}
