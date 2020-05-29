package com.maarten.recipepicker;

import androidx.lifecycle.ViewModel;

import com.maarten.recipepicker.enums.CookTime;
import com.maarten.recipepicker.enums.Difficulty;
import com.maarten.recipepicker.enums.FillInRecipeFragmentType;
import com.maarten.recipepicker.enums.IngredientType;
import com.maarten.recipepicker.enums.QuantityType;
import com.maarten.recipepicker.models.Ingredient;
import com.maarten.recipepicker.models.Instruction;
import com.maarten.recipepicker.models.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class FillInRecipeViewModel extends ViewModel {
    private FillInRecipeFragmentType currentFragmentType = FillInRecipeFragmentType.MAIN;

    private List<Ingredient> ingredientList = new ArrayList<>();
    private List<Instruction> instructionList = new ArrayList<>();

    private Set<String> categorySet = new TreeSet<>();

    private String recipeTitle = "";
    private String recipeURL = "";
    private String recipeComments = "";
    private String recipeImagePath = "";

    private boolean recipeFavorite = false;
    private int serveCount = 4;

    private Difficulty recipeDifficulty = Difficulty.INTERMEDIATE;
    private CookTime recipeCookTime = CookTime.MEDIUM;

    private Recipe recipe = null;

    private String ingredientName = "";
    private String ingredientOtherQuantity = "";
    private Double ingredientQuantity = null;

    private IngredientType ingredientType = null;
    private QuantityType quantityType = null;

    private String instructionText = "";
    private boolean instructionTimerEnabled = false;
    private int instructionTimerMin = 6;
    private int instructionTimerSec = 30;

    public void setRecipe(Recipe recipe) {
        if (this.recipe == null) {
            this.recipe = recipe;
        }
        if (this.recipe == null) {
            return;
        }
        // Initialise all the fields with the given recipe
        setRecipeTitle(recipe.getTitle());
        setRecipeCookTime(recipe.getCookTime());
        setRecipeDifficulty(recipe.getDifficulty());

        setIngredientList(recipe.getIngredientList());
        setInstructionList(recipe.getInstructionList());
        setCategorySet(recipe.getCategories());

        setRecipeImagePath(recipe.getImagePath());
        setServeCount(recipe.getServes());
        setRecipeComments(recipe.getComments());
        setRecipeURL(recipe.getURL());
        setRecipeFavorite(recipe.getFavorite());

        // TODO: add the checks from import recipe here and initialise the fields
    }

    public FillInRecipeFragmentType getCurrentFragmentType() {
        return currentFragmentType;
    }

    public void setCurrentFragmentType(FillInRecipeFragmentType currentFragmentType) {
        this.currentFragmentType = currentFragmentType;
    }

    public Difficulty getRecipeDifficulty() {
        return recipeDifficulty;
    }

    public void setRecipeDifficulty(Difficulty recipeDifficulty) {
        this.recipeDifficulty = recipeDifficulty;
    }

    public CookTime getRecipeCookTime() {
        return recipeCookTime;
    }

    public void setRecipeCookTime(CookTime recipeCookTime) {
        this.recipeCookTime = recipeCookTime;
    }

    public void addIngredient(Ingredient ingredient) {
        ingredientList.add(ingredient);
    }

    public void setIngredientList(List<Ingredient> ingredientList) {
        this.ingredientList = ingredientList;
    }

    public List<Ingredient> getIngredientList() {
        return ingredientList;
    }

    public void addInstruction(Instruction instruction) {
        instructionList.add(instruction);
    }

    public void setInstructionList(List<Instruction> instructionList) {
        this.instructionList = instructionList;
    }

    public List<Instruction> getInstructionList() {
        return instructionList;
    }

    public String getRecipeTitle() {
        return recipeTitle;
    }

    public void setRecipeTitle(String recipeTitle) {
        this.recipeTitle = recipeTitle;
    }

    public int getServeCount() {
        return serveCount;
    }

    public void setServeCount(int serveCount) {
        this.serveCount = serveCount;
    }

    public String getRecipeURL() {
        return recipeURL;
    }

    public void setRecipeURL(String recipeURL) {
        this.recipeURL = recipeURL;
    }

    public String getRecipeComments() {
        return recipeComments;
    }

    public void setRecipeComments(String recipeComments) {
        this.recipeComments = recipeComments;
    }

    public String getRecipeImagePath() {
        return recipeImagePath;
    }

    public void setRecipeImagePath(String recipeImagePath) {
        this.recipeImagePath = recipeImagePath;
    }

    public boolean isRecipeFavorite() {
        return recipeFavorite;
    }

    public void setRecipeFavorite(boolean recipeFavorite) {
        this.recipeFavorite = recipeFavorite;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getIngredientOtherQuantity() {
        return ingredientOtherQuantity;
    }

    public void setIngredientOtherQuantity(String ingredientOtherQuantity) {
        this.ingredientOtherQuantity = ingredientOtherQuantity;
    }

    public Double getIngredientQuantity() {
        if (ingredientQuantity == null) {
            return 0.0;
        }
        return ingredientQuantity;
    }

    public void setIngredientQuantity(Double ingredientQuantity) {
        this.ingredientQuantity = ingredientQuantity;
    }

    public IngredientType getIngredientType() {
        return ingredientType;
    }

    public void setIngredientType(IngredientType ingredientType) {
        this.ingredientType = ingredientType;
    }

    public QuantityType getQuantityType() {
        if (quantityType == null) {
            return QuantityType.GRAMS;
        }
        return quantityType;
    }

    public void setQuantityType(QuantityType quantityType) {
        this.quantityType = quantityType;
    }

    public String getInstructionText() {
        return instructionText;
    }

    public void setInstructionText(String instructionText) {
        this.instructionText = instructionText;
    }

    public boolean isInstructionTimerEnabled() {
        return instructionTimerEnabled;
    }

    public void setInstructionTimerEnabled(boolean instructionTimerEnabled) {
        this.instructionTimerEnabled = instructionTimerEnabled;
    }

    public int getInstructionTimerMin() {
        return instructionTimerMin;
    }

    public void setInstructionTimerMin(int instructionTimerMin) {
        this.instructionTimerMin = instructionTimerMin;
    }

    public int getInstructionTimerSec() {
        return instructionTimerSec;
    }

    public void setInstructionTimerSec(int instructionTimerSec) {
        this.instructionTimerSec = instructionTimerSec;
    }

    public void setCategorySet(Set<String> categorySet) {
            this.categorySet = categorySet;
    }

    public Set<String> getCategorySet() {
        return categorySet;
    }

    /**
     * Is called before showing the ingredient screen
     */
    public void resetIngredientFields() {
        ingredientType = null;
        ingredientName = "";
        ingredientQuantity = null;
        ingredientOtherQuantity = "";
    }

    /**
     * Is called when the instruction is reset, happens after inserting an ingredient
     * or after pressing cancel ingredient
     */
    public void resetInstructionFields() {
        instructionText = "";
        instructionTimerEnabled = false;
        instructionTimerMin = 6;
        instructionTimerSec = 30;
    }
}
