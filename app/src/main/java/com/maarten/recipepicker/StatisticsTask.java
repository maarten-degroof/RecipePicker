package com.maarten.recipepicker;

import android.os.AsyncTask;
import android.text.Html;
import android.widget.TextView;

import com.maarten.recipepicker.models.Recipe;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;
import static com.maarten.recipepicker.MainActivity.recipeList;

/**
 * - total amount of recipes
 * - total amount of ingredients
 * - total amount of categories
 * - total amount of times cooked
 *
 * . most cooked recipe - least cooked recipe
 * . most used ingredient - least used ingredient
 * . most used category - least used ingredient
 * . oldest recipe - newest recipe
 * . percentage of recipes that use the most used ingredient/category
 */


public class StatisticsTask extends AsyncTask<Void, Void, List<Integer>> {

    private WeakReference<List<TextView>> textViewList;

    StatisticsTask(List<TextView> textViewList){
        this.textViewList = new WeakReference<>(textViewList);
    }

    @Override
    protected List<Integer> doInBackground(Void... voids) {
        List<Integer> resultList = new ArrayList<>();

        resultList.add(recipeList.size());

        resultList.add(RecipeUtility.generateIngredientList().size());

        resultList.add(RecipeUtility.generateCategoryList().size());

        int totalAmountCooked = 0;
        for (Recipe recipe : recipeList) {
            totalAmountCooked += recipe.getAmountCooked();
        }
        resultList.add(totalAmountCooked);

        return resultList;
    }

    protected void onPostExecute(List<Integer> result) {

        String totalRecipesOutput = RecipePickerApplication.getAppContext().getString(R.string.statistics_total_recipes_found, result.get(0));
        textViewList.get().get(0).setText(Html.fromHtml(totalRecipesOutput, FROM_HTML_MODE_LEGACY));

        String totalIngredientsOutput = RecipePickerApplication.getAppContext().getString(R.string.statistics_total_ingredients_found, result.get(0));
        textViewList.get().get(1).setText(Html.fromHtml(totalIngredientsOutput, FROM_HTML_MODE_LEGACY));

    }
}

