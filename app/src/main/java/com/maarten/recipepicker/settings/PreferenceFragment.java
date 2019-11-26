package com.maarten.recipepicker.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.maarten.recipepicker.BuildConfig;
import com.maarten.recipepicker.MainActivity;
import com.maarten.recipepicker.R;

/**
 * This fragment is used in the settings, and this takes care of the 'delete all' button (can also be used for other settings)
 */
public class PreferenceFragment extends PreferenceFragmentCompat {

    private Preference removeAllButtonPreference, goToGithubButtonPreference, versionPreference;
    private EditTextPreference servesPreference;

    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Indicate here the XML resource you created above that holds the preferences
        setPreferencesFromResource(R.xml.settings, rootKey);

        removeAllButtonPreference =  getPreferenceManager().findPreference("remove_all_button");
        if (removeAllButtonPreference != null) {
            removeAllButtonPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    createDeleteDialog();
                    return true;
                }
            });
        }

        goToGithubButtonPreference = getPreferenceManager().findPreference("github_button");
        if(goToGithubButtonPreference != null) {
            goToGithubButtonPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    goToGithub();
                    return true;
                }
            });
        }

        versionPreference = getPreferenceManager().findPreference("version");
        versionPreference.setSummary(BuildConfig.VERSION_NAME);

        servesPreference = getPreferenceManager().findPreference("serves_value");

        if(servesPreference != null) {
            // takes care of inputType="Number"
            servesPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            });
        }
        createListener();

    }

    /**
     * Listener to make sure that when the user leaves the serves blank, it fills in '4'
     */
    private void createListener() {
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                if(key.equals("serves_value")) {
                    String value = sharedPreferences.getString("serves_value", "4");
                    if(value.equals("")) {
                        servesPreference.setText("4");
                    }
                    // more than two characters is always longer dan 50
                    else if(value.length() > 2) {
                        servesPreference.setText("50");
                    }
                    else if(Integer.parseInt(value) > 50) {
                        servesPreference.setText("50");
                    }
                }
            }
        };
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .registerOnSharedPreferenceChangeListener(listener);

    }

    /**
     * creates the confirm dialog to clear the recipelist
     */
    private void createDeleteDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // get the layout
        View dialog_layout = getLayoutInflater().inflate(R.layout.remove_all_dialog, null);
        final MaterialCheckBox resetListCheckbox = dialog_layout.findViewById(R.id.resetListCheckBox);

        builder.setTitle("Remove ALL recipes");
        builder.setMessage("Are you sure you want to remove ALL recipes? Be careful, this can not be undone!");

        builder.setPositiveButton("Remove all", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                removeRecipe();
                // if it's checked, restock the list
                if(resetListCheckbox.isChecked()) {
                    MainActivity.insertDummyRecipes();
                }
            }
        });
        builder.setNegativeButton("Keep", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
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
}
