package com.maarten.recipepicker.Models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.maarten.recipepicker.R;
import com.maarten.recipepicker.RecipePickerApplication;
import com.maarten.recipepicker.enums.CookTime;
import com.maarten.recipepicker.enums.Difficulty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Recipe implements Serializable {
    private String title;
    private List<Ingredient> ingredientList;
    private Boolean favorite;
    private Integer amountCooked;
    private Date addedDate;
    private CookTime cookTime;
    private String imagePath;
    private String URL;
    private Difficulty difficulty;
    private String comments;
    private List<Instruction> instructionList;
    private int serves;
    private transient Bitmap image;
    private int rating;


    public Recipe(String title, List<Ingredient> ingredientList, Boolean favorite,
                  CookTime cookTime, String imagePath, String URL, Difficulty difficulty, String comments, List<Instruction> instructionList, int serves) {
        this.title = title;
        this.ingredientList = ingredientList;
        this.favorite = favorite;
        this.amountCooked = 0;
        this.addedDate = Calendar.getInstance().getTime();
        this.cookTime = cookTime;
        this.imagePath = imagePath;
        this.URL = URL;
        this.difficulty = difficulty;
        this.comments = comments;
        this.instructionList = instructionList;
        this.serves = serves;
        this.rating = 0;
    }

    public List<Instruction> getInstructionList() {
        return instructionList;
    }

    public void setInstructionList(List<Instruction> instructionList) {
        this.instructionList = instructionList;
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
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
        image = null;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int getServes() {
        return serves;
    }

    public void setServes(int serves) {
        this.serves = serves;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    /*
     * returns (and creates) a bitmap to use
     */
    public Bitmap getImage() {
        if(image != null) {
            return image;
        }
        else {
            Bitmap bitmap;
            // no image given
            if(imagePath == null) {
                bitmap = BitmapFactory.decodeResource(RecipePickerApplication.getAppContext().getResources(), R.drawable.no_image_available);
            }
            // image is a drawable
            else if(Character.isDigit(imagePath.charAt(0))) {
                bitmap = BitmapFactory.decodeResource(RecipePickerApplication.getAppContext().getResources(), Integer.decode(imagePath));
            } else {
                bitmap = BitmapFactory.decodeFile(imagePath);
            }
            image = bitmap;
            return bitmap;
        }
    }

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
        return o.getTitle().equals(this.getTitle());
        // check ingredientlist!!
    }

    /**
     * Generates the ingredient list ordered by quantity
     *
     * @return - string of the ingredient list
     */
    public String getOrderedIngredientString() {
        if(ingredientList == null) {
            return "";
        }
        List<Ingredient> tempList = new ArrayList<>(ingredientList);

        Collections.sort(tempList);

        StringBuilder builder = new StringBuilder();

        for (Ingredient ingredient : tempList) {

            builder.append(ingredient.getName());
            builder.append(", ");
        }
        // remove the last separator ', '
        if(builder.length() >= 2) {
            builder.delete(builder.length() - 2, builder.length() - 1) ;
        }

        return builder.toString();
    }
}
