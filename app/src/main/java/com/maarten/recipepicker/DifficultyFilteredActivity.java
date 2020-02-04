package com.maarten.recipepicker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maarten.recipepicker.adapters.DifficultyFilteredAdapter;
import com.maarten.recipepicker.enums.Difficulty;

import static com.maarten.recipepicker.MainActivity.recipeList;

public class DifficultyFilteredActivity extends AppCompatActivity {

    private DifficultyFilteredAdapter difficultyFilteredAdapter;

    private RecyclerView listViewDifficultyFiltered;

    private Difficulty difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty_filtered);

        listViewDifficultyFiltered = findViewById(R.id.listViewDifficultyFiltered);

        try {
            Intent intent = getIntent();
            difficulty = (Difficulty) intent.getSerializableExtra("difficulty");

            difficultyFilteredAdapter = new DifficultyFilteredAdapter(this, recipeList);
            listViewDifficultyFiltered.setAdapter(difficultyFilteredAdapter);
            listViewDifficultyFiltered.setLayoutManager(new LinearLayoutManager(this));

            difficultyFilteredAdapter.getFilter().filter(difficulty.name());
        } catch (Exception e) {
            e.getStackTrace();
        }

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(difficultyType);
        setSupportActionBar(toolbar);
        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView filteredDifficultyDescriptionTextField = findViewById(R.id.filteredDifficultyDescriptionTextField);
        filteredDifficultyDescriptionTextField.setText(getString(R.string.difficulty_filtered_description, difficultyType));

    }
    /**
     * When activity resumes, update the adapter + filter again to get the correct results, in case you changed something in the recipes
     */
    @Override
    public void onResume() {
        super.onResume();
        difficultyFilteredAdapter.notifyDataSetChanged();
        difficultyFilteredAdapter.getFilter().filter(difficulty.name());
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
}
