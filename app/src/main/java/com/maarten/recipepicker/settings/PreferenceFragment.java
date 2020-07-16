package com.maarten.recipepicker.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
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
import com.maarten.recipepicker.viewModels.SettingsViewModel;

/**
 * This fragment is used in the settings, and this takes care of the 'delete all' button (can also be used for other settings)
 */
public class PreferenceFragment extends PreferenceFragmentCompat {

    private EditTextPreference servesPreference;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private SettingsViewModel viewModel;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Indicate here the XML resource you created above that holds the preferences
        setPreferencesFromResource(R.xml.settings, rootKey);

        setRetainInstance(true);

        viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        Preference removeAllButtonPreference =  getPreferenceManager().findPreference("remove_all_button");
        if (removeAllButtonPreference != null) {
            removeAllButtonPreference.setOnPreferenceClickListener(preference -> {
                createDeleteDialog();
                return true;
            });
        }

        Preference removeAllTimesCookedPreference =  getPreferenceManager().findPreference("remove_all_times_cooked_button");
        if (removeAllTimesCookedPreference != null) {
            removeAllTimesCookedPreference.setOnPreferenceClickListener(preference -> {
                createDeleteAmountCookedDialog();
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

        // Create a filter so the user can fill in a maximum of 2 characters
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(2);

        if(servesPreference != null) {
            // Takes care of inputType="Number"
            servesPreference.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setFilters(filterArray);
            });
        }
        createListener();

        PreferenceManager.getDefaultSharedPreferences(requireActivity())
                .registerOnSharedPreferenceChangeListener(listener);

        if (viewModel.isShowingResetEverythingDialog()) {
            createDeleteDialog();
        } else if (viewModel.isShowingResetTimesCookedDialog()) {
            createDeleteAmountCookedDialog();
        }
    }

    /**
     * Listener to make sure that when the user leaves the serves blank, it fills in '4'.
     * Also checks for a starting '0', and sets the upper boundary to '50'.
     * If the filled in value is less than '1', fills in '1'.
     */
    private void createListener() {
        listener = (sharedPreferences, key) -> {
            if (key.equals("serves_value")) {
                String value = sharedPreferences.getString("serves_value", "4");

                if (value.equals("")) {
                    servesPreference.setText("4");
                }
                else if (Integer.parseInt(value) < 1) {
                    servesPreference.setText("1");
                }
                // Remove starting '0'
                else if (value.startsWith("0")) {
                    value = value.substring(1);
                    servesPreference.setText(value);
                }
                else if (Integer.parseInt(value) > 50) {
                    servesPreference.setText("50");
                }
            }
        };
    }

    /**
     * Creates the confirm dialog to clear the recipeList
     */
    private void createDeleteDialog() {
        viewModel.setShowingResetEverythingDialog(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        // Get the layout
        View dialog_layout = View.inflate(requireContext(), R.layout.remove_all_dialog, null);
        final MaterialCheckBox resetListCheckbox = dialog_layout.findViewById(R.id.resetListCheckBox);

        resetListCheckbox.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setResetOriginalValuesResetEverythingIsTicked(isChecked));

        resetListCheckbox.setChecked(viewModel.isResetOriginalValuesResetEverythingIsTicked());

        builder.setTitle("Remove ALL recipes");
        builder.setMessage("Are you sure you want to remove ALL recipes? Be careful, this can not be undone!");

        builder.setPositiveButton("Remove all", (dialog, id) -> {
            removeRecipe();
            // If it's checked, restock the list
            if(resetListCheckbox.isChecked()) {
                MainActivity.insertDummyRecipes();
                MainActivity.saveRecipes();
            }
            viewModel.setShowingResetEverythingDialog(false);
            viewModel.setResetOriginalValuesResetEverythingIsTicked(false);
        });
        builder.setNegativeButton("Keep", (dialog, id) -> {
            viewModel.setShowingResetEverythingDialog(false);
            viewModel.setResetOriginalValuesResetEverythingIsTicked(false);
        });
        // Create and show the dialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.setView(dialog_layout);
        alertDialog.setOnCancelListener(dialog -> {
            viewModel.setShowingResetEverythingDialog(false);
            viewModel.setResetOriginalValuesResetEverythingIsTicked(false);
        });
        alertDialog.show();
    }

    /**
     * Creates the confirm dialog to clear the amount cooked for each recipe
     */
    private void createDeleteAmountCookedDialog() {
        viewModel.setShowingResetTimesCookedDialog(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        builder.setTitle("Remove ALL times cooked");
        builder.setMessage("Are you sure you want to reset ALL the times you've cooked your recipes back to 0? This cannot be undone.");

        builder.setPositiveButton("Reset all", (dialog, id) -> {
            removeAllTimesCooked();
            viewModel.setShowingResetTimesCookedDialog(false);
        });
        builder.setNegativeButton("Cancel", (dialog, id) -> viewModel.setShowingResetTimesCookedDialog(true));
        // Create and show the dialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnCancelListener(dialog -> viewModel.setShowingResetTimesCookedDialog(false));
        alertDialog.show();
    }

    /**
     * Clears the recipeList
     */
    private void removeRecipe() {
        MainActivity.recipeList.clear();
        MainActivity.saveRecipes();
        Toast.makeText(requireActivity(), "Removed all the recipes", Toast.LENGTH_LONG).show();
    }

    /**
     * Sets the times cooked of each recipe to 0
     */
    private void removeAllTimesCooked() {
        MainActivity.clearAllAmountCooked();
        Toast.makeText(requireActivity(), "Reset all recipes to 0 times cooked.", Toast.LENGTH_LONG).show();
    }

    /**
     * Opens the GitHub page
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
