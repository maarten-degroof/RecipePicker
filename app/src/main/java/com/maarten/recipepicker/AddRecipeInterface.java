package com.maarten.recipepicker;

import com.maarten.recipepicker.enums.FillInRecipeFragmentType;

public interface AddRecipeInterface {
    void toggleEndButtons(boolean shouldShowButtons);

    void toggleCurrentFragment(FillInRecipeFragmentType newFragmentType);

    void saveRecipe();
}
