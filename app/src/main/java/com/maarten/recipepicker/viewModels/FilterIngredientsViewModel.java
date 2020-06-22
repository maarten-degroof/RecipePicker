package com.maarten.recipepicker.viewModels;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class FilterIngredientsViewModel extends ViewModel {
    private List<String> ingredientListToInclude = new ArrayList<>();
    private List<String> ingredientListToExclude = new ArrayList<>();

    public List<String > getIngredientListToInclude() {
        return ingredientListToInclude;
    }

    public void setIngredientListToInclude(List<String> ingredientListToInclude) {
        this.ingredientListToInclude = ingredientListToInclude;
    }

    public List<String> getIngredientListToExclude() {
        return ingredientListToExclude;
    }

    public void setIngredientListToExclude(List<String> ingredientListToExclude) {
        this.ingredientListToExclude = ingredientListToExclude;
    }
}
