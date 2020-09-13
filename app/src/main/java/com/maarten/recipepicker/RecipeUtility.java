package com.maarten.recipepicker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.exifinterface.media.ExifInterface;

import com.maarten.recipepicker.enums.IngredientType;
import com.maarten.recipepicker.enums.QuantityType;
import com.maarten.recipepicker.models.Ingredient;
import com.maarten.recipepicker.models.Recipe;

import java.io.IOException;
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
        if (input.length() > 0) {
            convertedValue += Character.toUpperCase(input.charAt(0));
            if (input.length() > 1) {
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
        for (Recipe recipe : MainActivity.recipeList) {
            List<Ingredient> tempList = recipe.getIngredientList();
            for (Ingredient ingredient : tempList) {
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

    /**
     * Rotates a given image with a given angle.
     * This is necessary because images that are taken aren't always in the right direction
     *
     * @param source The image as a Bitmap that you want to rotate
     * @param angle  The amount of degrees the image should be rotated
     * @return Returns a rotated Bitmap of the image
     */
    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    /**
     * Checks the meta data for a given image and rotates the image if necessary
     *
     * @param imagePath The path of the image to check
     * @return Returns a Bitmap of the rotated image
     */
    public static Bitmap rotateBitmap(String imagePath) {
        Bitmap rotatedBitmap;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;

                default:
                    rotatedBitmap = bitmap;
            }
        } catch (IOException exception) {
            return null;
        }

        return rotatedBitmap;
    }
}
