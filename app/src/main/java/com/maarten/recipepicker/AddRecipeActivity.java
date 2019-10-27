package com.maarten.recipepicker;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.maarten.recipepicker.Adapters.IngredientEditAdapter;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.maarten.recipepicker.MainActivity.recipeList;

public class AddRecipeActivity extends AppCompatActivity {

    private List<Ingredient> ingredientList = new ArrayList<>();
    private IngredientEditAdapter adapter;

    private EditText ingredientNameField, ingredientQuantityField;
    private Spinner ingredientTypeField;
    private ListView ingredientListView;

    private TextInputLayout recipeTitleLayout, recipeDescriptionLayout;

    private static final int READ_EXTERNAL_PERMISSIONS = 1;
    private static final int GALLERY_REQUEST_CODE = 2;

    private ImageView imageView;
    private String imagePath;

    private Button removeImageButton, differentImageButton, addImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_recipe);

        adapter = new IngredientEditAdapter(this, ingredientList);
        ingredientListView = findViewById(R.id.addRecipeIngredientList);
        ingredientListView.setAdapter(adapter);

        // hide the list since it is empty
        ingredientListView.setVisibility(TextView.INVISIBLE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Add Recipe");
        setSupportActionBar(toolbar);

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recipeTitleLayout = findViewById(R.id.nameFieldLayout);
        recipeDescriptionLayout = findViewById(R.id.descriptionFieldLayout);

        // make the listview (ingredientList) also scrollable when inserting text
        ViewCompat.setNestedScrollingEnabled(ingredientListView, true);

        imageView = findViewById(R.id.imageView);
        imagePath = null;

        addImageButton = findViewById(R.id.openGalleryButton);
        differentImageButton = findViewById(R.id.openGalleryAgainButton);
        removeImageButton = findViewById(R.id.cancelImageButton);

        // there's no image yet -> hide buttons
        differentImageButton.setVisibility(View.GONE);
        removeImageButton.setVisibility(View.GONE);

        // this makes sure that there's always one chip selected
        final ChipGroup mChipGroup = findViewById(R.id.chipGroup);
        mChipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                for (int i = 0; i < mChipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) mChipGroup.getChildAt(i);
                    if (chip != null) {
                        chip.setClickable(!(chip.getId() == mChipGroup.getCheckedChipId()));
                    }
                }
            }
        });


        // This makes it possible to scroll in the description field
        final TextInputEditText descriptionField = findViewById(R.id.recipeText);
        descriptionField.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (descriptionField.hasFocus()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_SCROLL){
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                    }
                }
                return false;
            }
        });

    }

    public void createIngredientDialog(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // get the layout
        View dialog_layout = getLayoutInflater().inflate(R.layout.add_ingredient, null);

        // Create the text field in the alert dialog.
        ingredientNameField = (EditText) dialog_layout.findViewById(R.id.ingredientNameField);
        ingredientQuantityField = (EditText) dialog_layout.findViewById(R.id.quantityField);
        ingredientTypeField = (Spinner) dialog_layout.findViewById(R.id.ingredientTypeSpinner);

        // create the spinner adapter with the choices + the standard views of how it should look like
        ArrayAdapter<CharSequence> ingredientTypeAdapter = ArrayAdapter.createFromResource(this, R.array.ingredient_types_array_items, android.R.layout.simple_spinner_item);
        ingredientTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ingredientTypeField.setAdapter(ingredientTypeAdapter);

        builder.setTitle("Add ingredient");
        builder.setMessage("choose an ingredient and a quantity");

        builder.setPositiveButton("Add ingredient", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Ingredient.type ingredientType = Ingredient.type.valueOf(ingredientTypeField.getSelectedItem().toString());
                if(ingredientQuantityField.getText().toString().isEmpty()) {
                    createIngredient(ingredientNameField.getText().toString(), null, ingredientType);
                }
                else {
                    createIngredient(ingredientNameField.getText().toString(), ingredientQuantityField.getText().toString(), ingredientType);
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        // create and show the dialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.setView(dialog_layout);
        alertDialog.show();
    }

    /**
     * creates an ingredient and adds it to the ingredientList
     *
     * @param name      name of the ingredient
     * @param quantity  quantity of the ingredient
     */
    private void createIngredient(String name, String quantity, Ingredient.type ingredientType) {
        try {
            validateIngredient(name, quantity);

            Ingredient ingredient;

            if(quantity == null) {
                 ingredient = new Ingredient(name, null, ingredientType);
            }
            else {
                 ingredient = new Ingredient(name, Double.parseDouble(quantity), ingredientType);
            }

            ingredientList.add(ingredient);
            // notify the adapter to update the list
            adapter.notifyDataSetChanged();

            // hide the 'no ingredients yet' text view
            TextView noIngredientTextView = (TextView) findViewById(R.id.noIngredientsTextView);
            noIngredientTextView.setVisibility(TextView.INVISIBLE);

            // unhide the list
            ingredientListView.setVisibility(TextView.VISIBLE);

        } catch (IllegalArgumentException e) {
            Toast.makeText(AddRecipeActivity.this, "Oops, something went wrong with that ingredient, try again", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Validates parameters for ingredient.
     *
     * @param name      value should not be empty
     * @param quantity  value should be a [Double] number (and not be empty)
     * @throws IllegalArgumentException if parameters not correct
     */
    private void validateIngredient(String name, String quantity) {
        if(quantity != null) {
            if( !isDouble(quantity)) {
                throw new IllegalArgumentException();
            }
        }
        if(name.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Checks if a string is a double
     *
     * @param str the string to test
     * @return true if parsable as double, false if not
     */
    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * gets called when the create recipe button is pressed
     * will check input and create recipe
     *
     * @param view  view given by the button
     */
    public void createRecipe(View view) {

        Boolean favouriteSwitch = ((SwitchMaterial) findViewById(R.id.favouriteSwitch)).isChecked();
        String recipeName = ((EditText) findViewById(R.id.nameField)).getText().toString();
        String recipeDescription = ((EditText) findViewById(R.id.recipeText)).getText().toString();

        // get the selected cookingtime
        ChipGroup chipGroup = findViewById(R.id.chipGroup);
        CookTime cookTime;

        switch (chipGroup.getCheckedChipId()) {
            case R.id.shortDurationChip:
                cookTime = CookTime.SHORT;
                break;
            case R.id.mediumDurationChip:
                cookTime = CookTime.MEDIUM;
                break;
            case R.id.longDurationChip:
                cookTime = CookTime.LONG;
                break;
            default:
                cookTime = CookTime.MEDIUM;

        }

        if(recipeName.isEmpty()) {
            recipeTitleLayout.setError("Please fill in a title");
        } else if (recipeDescription.isEmpty()) {
            recipeDescriptionLayout.setError("You have to fill in a description");
        } else if (ingredientList.isEmpty()) {
            Toast.makeText(AddRecipeActivity.this, "You have to add at least one ingredient", Toast.LENGTH_LONG).show();
        } else {
            Recipe recipe = new Recipe(recipeDescription,recipeName,ingredientList,favouriteSwitch, 0, cookTime, imagePath);
            recipeList.add(recipe);
            Toast.makeText(AddRecipeActivity.this, "Your recipe was added!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * the cancel button to return to the previous view
     *
     * @param view  needed for the button to connect
     */
    public void cancelCreation(View view) {
        finish();
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


    public void showPictureGallery(View view) {
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            pickFromGallery();
        } else {
            // permission hasn't been granted.

            if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(AddRecipeActivity.this, "External storage permission is needed to access your images.", Toast.LENGTH_LONG).show();
            }

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},READ_EXTERNAL_PERMISSIONS);
        }

    }

    //@Override
    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == READ_EXTERNAL_PERMISSIONS) {
            // check if the required permission is granted
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFromGallery();
            } else {
                Toast.makeText(AddRecipeActivity.this, "Permission was not granted.", Toast.LENGTH_SHORT).show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * creates intent with the parameters to open an image picker
     */
    private void pickFromGallery(){
        //Create an Intent with action as ACTION_PICK
        Intent intent = new Intent(Intent.ACTION_PICK);
        // Sets the type as imagePath/*. This ensures only components of type imagePath are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types are targeted.
        String[] mimeTypes = {"imagePath/jpeg", "imagePath/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        // Launching the Intent
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    /**
     * when image is chosen in an image picker, returns back to the activity and to this method
     * if image was chosen, extracts the path and puts the image in the imageView
     *
     * @param requestCode - the code which you passed on when starting the activity, identifier
     * @param resultCode - says if the user completed it and chose an image
     * @param data - contains the Uri
     */
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case GALLERY_REQUEST_CODE:
                    //data.getData returns the content URI for the selected Image
                    try {
                        Uri selectedImage = data.getData();
                        imagePath = getRealPathFromURI(this, selectedImage);

                        // generate a bitmap, to put in the imageview
                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                        imageView.setImageBitmap(bitmap);

                        // show & hide appropriate buttons
                        addImageButton.setVisibility(View.GONE);
                        differentImageButton.setVisibility(View.VISIBLE);
                        removeImageButton.setVisibility(View.VISIBLE);

                    } catch (Exception e) {
                        e.getMessage();
                    }
                    break;
            } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Gets the absolute path from a URI file
     * Code written by Kuray Ogun
     * https://freakycoder.com/android-notes-73-how-to-get-real-path-from-uri-2f78320987f5
     *
     * @param context - the current activity
     * @param contentUri - the Uri to get the path from
     * @return - returns the path as a string, or the empty string if something went wrong
     */
    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e("TAG", "getRealPathFromURI Exception : " + e.toString());
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Happens when a user presses the 'remove image' button
     *
     * @param view - the pressed button
     */
    public void removeImage(View view) {
        imageView.setImageBitmap(null);
        imagePath = null;

        // show & hide appropriate buttons
        addImageButton.setVisibility(View.VISIBLE);
        differentImageButton.setVisibility(View.GONE);
        removeImageButton.setVisibility(View.GONE);
    }

}
