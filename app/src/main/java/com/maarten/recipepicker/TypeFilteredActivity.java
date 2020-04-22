package com.maarten.recipepicker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.maarten.recipepicker.adapters.CategoryFilteredAdapter;
import com.maarten.recipepicker.adapters.DifficultyFilteredAdapter;
import com.maarten.recipepicker.enums.Difficulty;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;
import static com.maarten.recipepicker.MainActivity.recipeList;

public class TypeFilteredActivity extends AppCompatActivity {

    RecyclerView typeFilteredRecyclerView;

    private DifficultyFilteredAdapter difficultyFilteredAdapter;
    private CategoryFilteredAdapter categoryFilteredAdapter;

    private MaterialButton addRecipeButton;
    private TextView noRecipesYetTextView;

    private int amountOfResults;

    private Difficulty difficulty;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_filtered);

        typeFilteredRecyclerView = findViewById(R.id.typeFilteredRecyclerView);

        addRecipeButton = findViewById(R.id.addRecipeButton);
        noRecipesYetTextView = findViewById(R.id.noRecipesYetTextView);

        difficulty = null;
        category = null;
        amountOfResults = 0;

        try {
            Intent intent = getIntent();

            typeFilteredRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            String type = intent.getStringExtra("filterType");
            if (type.equals("Difficulty")) {
                difficulty = (Difficulty) intent.getSerializableExtra("difficulty");
                difficultyFilteredAdapter = new DifficultyFilteredAdapter(this, recipeList);
                typeFilteredRecyclerView.setAdapter(difficultyFilteredAdapter);
                amountOfResults = difficultyFilteredAdapter.filterAndReturnAmount(difficulty.name());
            }
            else if (type.equals("Category")) {
                category = intent.getStringExtra("category");
                categoryFilteredAdapter = new CategoryFilteredAdapter(this, recipeList);
                typeFilteredRecyclerView.setAdapter(categoryFilteredAdapter);
                amountOfResults = categoryFilteredAdapter.filterAndReturnAmount(category);
            }

        } catch (Exception e) {
            e.getStackTrace();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView filteredDifficultyDescriptionTextField = findViewById(R.id.filteredDifficultyDescriptionTextField);
        String description = "";
        if (difficulty != null) {
            String difficultyType;
            switch (difficulty) {
                case BEGINNER:
                    difficultyType = getString(R.string.beginner);
                    break;
                case EXPERT:
                    difficultyType = getString(R.string.expert);
                    break;
                default:
                    difficultyType = getString(R.string.intermediate);
            }
            toolbar.setTitle(difficultyType);
            description = getString(R.string.difficulty_filtered_description, difficultyType);
        }
        else if (category != null) {
            toolbar.setTitle(category);
            description = getString(R.string.category_filtered_description, category);
        }

        filteredDifficultyDescriptionTextField.setText(Html.fromHtml(description, FROM_HTML_MODE_LEGACY));

        setSupportActionBar(toolbar);
        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        controlNoRecipeElements();
    }

    /**
     * When activity resumes, update the adapter + filter again to get the correct results, in case you changed something in the recipes
     */
    @Override
    public void onResume() {
        super.onResume();
        if(difficulty != null) {
            difficultyFilteredAdapter = new DifficultyFilteredAdapter(this, recipeList);
            typeFilteredRecyclerView.setAdapter(difficultyFilteredAdapter);
            amountOfResults = difficultyFilteredAdapter.filterAndReturnAmount(difficulty.name());
        }
        else if (category != null) {
            categoryFilteredAdapter = new CategoryFilteredAdapter(this, recipeList);
            amountOfResults = categoryFilteredAdapter.filterAndReturnAmount(category);
            typeFilteredRecyclerView.setAdapter(categoryFilteredAdapter);

        }

        controlNoRecipeElements();
    }

    /**
     * This takes care of the 'no found recipes' and 'add recipe' elements when the list is empty
     */
    private void controlNoRecipeElements() {
        if(amountOfResults > 0) {
            noRecipesYetTextView.setVisibility(View.GONE);
            addRecipeButton.setVisibility(View.GONE);
        } else {
            noRecipesYetTextView.setVisibility(View.VISIBLE);
            addRecipeButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Inflates the menu into the toolbar
     *
     * @param menu the menu
     * @return should return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    /**
     * checks if the clicked menu item the home icon is
     * @param item  the clicked menu item
     * @return  should return true when item found
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_home) {
            goToMainActivity();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Opens the main activity and closes the previous activities
     */
    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     *  Starts AddRecipeActivity
     */
    public void addRecipe(View view) {
        Intent intent = new Intent (this, AddRecipeActivity.class);
        startActivity(intent);
    }
}
