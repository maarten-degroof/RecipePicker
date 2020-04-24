package com.maarten.recipepicker;

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
}
