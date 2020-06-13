package com.maarten.recipepicker.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.maarten.recipepicker.BuildConfig;
import com.maarten.recipepicker.MainActivity;
import com.maarten.recipepicker.R;
import com.maarten.recipepicker.StatisticsActivity;
import com.maarten.recipepicker.importRecipe.ImportActivity;

/**
 * This fragment is used in the settings, and this takes care of the 'delete all' button (can also be used for other settings)
 */
public class PreferenceFragment extends PreferenceFragmentCompat {

    private EditTextPreference servesPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Indicate here the XML resource you created above that holds the preferences
        setPreferencesFromResource(R.xml.settings, rootKey);

        Preference removeAllButtonPreference =  getPreferenceManager().findPreference("remove_all_button");
        if (removeAllButtonPreference != null) {
            removeAllButtonPreference.setOnPreferenceClickListener(preference -> {
                createDeleteDialog();
                return true;
            });
        }

        Preference importRecipeButton =  getPreferenceManager().findPreference("import_button");
        if(importRecipeButton != null) {
            importRecipeButton.setOnPreferenceClickListener(preference -> {
                goToRecipeImport();
                return true;
            });
        }

        Preference goToGithubButtonPreference = getPreferenceManager().findPreference("github_button");
        if(goToGithubButtonPreference != null) {
            goToGithubButtonPreference.setOnPreferenceClickListener(preference -> {
                goToGithub();
                return true;
            });
        }

        Preference goToStatisticsButtonPreference = getPreferenceManager().findPreference("open_statistics_button");
        if(goToStatisticsButtonPreference != null) {
            goToStatisticsButtonPreference.setOnPreferenceClickListener(preference -> {
                goToStatistics();
                return true;
            });
        }

        Preference versionPreference = getPreferenceManager().findPreference("version");
        if (versionPreference != null) {
            versionPreference.setSummary(BuildConfig.VERSION_NAME);
        }

        servesPreference = getPreferenceManager().findPreference("serves_value");

        if(servesPreference != null) {
            // takes care of inputType="Number"
            servesPreference.setOnBindEditTextListener(
                    editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
        }
        createListener();

    }

    /**
     * Listener to make sure that when the user leaves the serves blank, it fills in '4'
     */
    private void createListener() {
        SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {

            if(key.equals("serves_value")) {
                String value = sharedPreferences.getString("serves_value", "4");
                if(value != null && value.equals("")) {
                    servesPreference.setText("4");
                }
                // more than two characters is always longer dan 50
                else if(value != null && value.length() > 2) {
                    servesPreference.setText("50");
                }
                else if (value != null && Integer.parseInt(value) > 50) {
                    servesPreference.setText("50");
                }
            }
        };
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Creates the confirm dialog to clear the recipeList
     */
    private void createDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        // get the layout
        View dialog_layout = View.inflate(requireContext(), R.layout.remove_all_dialog, null);
        final MaterialCheckBox resetListCheckbox = dialog_layout.findViewById(R.id.resetListCheckBox);

        builder.setTitle("Remove ALL recipes");
        builder.setMessage("Are you sure you want to remove ALL recipes? Be careful, this can not be undone!");

        builder.setPositiveButton("Remove all", (dialog, id) -> {
            removeRecipe();
            // if it's checked, restock the list
            if(resetListCheckbox.isChecked()) {
                MainActivity.insertDummyRecipes();
                MainActivity.saveRecipes();
            }
        });
        builder.setNegativeButton("Keep", (dialog, id) -> {
        });
        // create and show the dialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.setView(dialog_layout);
        alertDialog.show();
    }

    /**
     * Clears the recipelist
     */
    private void removeRecipe() {
        MainActivity.recipeList.clear();
        MainActivity.saveRecipes();
        Toast.makeText(getActivity(), "Removed all the recipes", Toast.LENGTH_LONG).show();
    }

    /**
     * opens the GitHub page
     */
    private void goToGithub() {
        String url = "https://github.com/maarten-degroof/RecipePicker";
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(url));
        startActivity(intent);
    }

    /**
     * Goes to the Statistics activity
     */
    private void goToStatistics() {
        Intent intent = new Intent(requireActivity(), StatisticsActivity.class);
        startActivity(intent);
    }

    /**
     * Goes to the import activity where you can import a recipe
     */
    private void goToRecipeImport() {
        Intent intent = new Intent(requireActivity(), ImportActivity.class);
        startActivity(intent);
    }
}
