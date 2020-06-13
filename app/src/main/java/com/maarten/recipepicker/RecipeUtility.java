package com.maarten.recipepicker;

import com.maarten.recipepicker.enums.IngredientType;
import com.maarten.recipepicker.enums.QuantityType;
import com.maarten.recipepicker.models.Ingredient;
import com.maarten.recipepicker.models.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

public final class RecipeUtility {

    /**
     * Converts string to a string with a capital and all lowercase characters followed
     *
     * @param input the ingredient name to convert
     * @return returns the converted name
     */
    public static String changeFirstLetterToCapital(String input) {
        String convertedValue = "";
        if(input.length() > 0) {
            convertedValue += Character.toUpperCase(input.charAt(0));
            if(input.length() > 1) {
                convertedValue += input.substring(1).toLowerCase();
            }
        }

        return convertedValue;
    }

    /**
     * Generates a list of categories, compiled from all the recipes
     *
     * @return returns the generated list
     */
    public static List<String> generateCategoryList() {
        List<String> categoryList = new ArrayList<>();
        for (Recipe recipe : MainActivity.recipeList) {
            for (String category : recipe.getCategories()) {
                String current_category = changeFirstLetterToCapital(category);
                if (!categoryList.contains(current_category)) {
                    categoryList.add(current_category);
                }
            }
        }
        return categoryList;
    }

    /**
     * Generates a list of all the unique ingredients from all of the recipes.
     * This will sort the recipes alphabetically, and change the first letter to a capital.
     *
     * @return returns an ArrayList containing the names of the ingredients, sorted alphabetically.
     */
    public static List<String> generateIngredientList() {
        NavigableSet<String> ingredientNameList = new TreeSet<>();
        for(Recipe recipe : MainActivity.recipeList) {
            List<Ingredient> tempList = recipe.getIngredientList();
            for(Ingredient ingredient : tempList) {
                ingredientNameList.add(changeFirstLetterToCapital(ingredient.getName()));
            }
        }
        return new ArrayList<>(ingredientNameList);
    }

    /**
     * converts all the items of an enum to a better readable format (with spaces and not in full caps)
     *
     * @param which which enum to use. Options are:
     *              - "QuantityType"
     *              - "IngredientType"
     * @return returns a list containing all the items of the enum
     */
    public static List<String> convertEnumToStringList(String which) {
        List<String> convertedList = new ArrayList<>();
        switch (which) {
            case "QuantityType":
                for (QuantityType type : QuantityType.values()) {
                    convertedList.add(type.name());
                }
                break;
            case "IngredientType":
                for (IngredientType type : IngredientType.values()) {
                    convertedList.add(type.name());
                }
                break;
        }
        List<String> finalList = new ArrayList<>();

        for (String item : convertedList) {
            finalList.add(changeFirstLetterToCapital(item).replace("_", " "));
        }
        return finalList;
    }
}
