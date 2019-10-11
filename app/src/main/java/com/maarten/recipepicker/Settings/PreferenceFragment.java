package com.maarten.recipepicker.Settings;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.maarten.recipepicker.MainActivity;
import com.maarten.recipepicker.R;

/**
 * This fragment is used in the settings, and this takes care of the 'delete all' button (can also be used for other settings)
 */
public class PreferenceFragment extends PreferenceFragmentCompat {

    private Preference buttonPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Indicate here the XML resource you created above that holds the preferences
        setPreferencesFromResource(R.xml.settings, rootKey);

        buttonPreference =  getPreferenceManager().findPreference("remove_all_button");
        if (buttonPreference != null) {
            buttonPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    createDeleteDialog();
                    return true;
                }
            });
        }
    }

    /**
     * creates the confirm dialog to clear the recipelist
     */
    private void createDeleteDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Remove ALL recipes");
        builder.setMessage("Are you sure you want to remove ALL recipes? Be careful, this can not be undone!");

        builder.setPositiveButton("Remove all", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                removeRecipe();
            }
        });
        builder.setNegativeButton("Keep", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        // create and show the dialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Clears the recipelist
     */
    private void removeRecipe() {
        MainActivity.recipeList.clear();
        Toast.makeText(getActivity(), "Removed all the recipes", Toast.LENGTH_LONG).show();
    }
}
