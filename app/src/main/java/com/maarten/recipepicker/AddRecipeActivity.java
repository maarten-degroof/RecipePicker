package com.maarten.recipepicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.maarten.recipepicker.Models.Ingredient;
import com.maarten.recipepicker.Models.Instruction;
import com.maarten.recipepicker.Models.Recipe;
import com.maarten.recipepicker.adapters.IngredientEditAdapter;
import com.maarten.recipepicker.adapters.InstructionEditAdapter;
import com.maarten.recipepicker.enums.CookTime;
import com.maarten.recipepicker.enums.Difficulty;

import java.util.ArrayList;
import java.util.List;

import static com.maarten.recipepicker.MainActivity.recipeList;

public class AddRecipeActivity extends AppCompatActivity {

    private List<Ingredient> ingredientList = new ArrayList<>();
    private IngredientEditAdapter ingredientAdapter;

    private EditText ingredientNameField, ingredientQuantityField;
    private Spinner ingredientTypeField;
    private RecyclerView ingredientListView;

    private TextInputLayout recipeTitleLayout;

    private static final int READ_EXTERNAL_PERMISSIONS = 1;
    private static final int GALLERY_REQUEST_CODE = 2;

    private ImageView imageView;
    private String imagePath;
    private Button removeImageButton, differentImageButton, addImageButton;

    private InstructionEditAdapter instructionAdapter;
    private RecyclerView instructionRecyclerView;
    private NumberPicker minuteNumberPicker, secondNumberPicker;
    private TextView minuteTextView, secondTextView;
    private List<Instruction> instructionList = new ArrayList<>();
    private EditText instructionDescription;

    private NumberPicker servesNumberPicker;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_recipe);

        ingredientAdapter = new IngredientEditAdapter(this, ingredientList);
        ingredientListView = findViewById(R.id.addRecipeIngredientList);
        ingredientListView.setAdapter(ingredientAdapter);
        ingredientListView.setLayoutManager(new LinearLayoutManager(this));
        ingredientListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // hide the list since it is empty
        //ingredientListView.setVisibility(TextView.INVISIBLE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Add Recipe");
        setSupportActionBar(toolbar);

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                finish();
            }
        });

        recipeTitleLayout = findViewById(R.id.nameFieldLayout);

        // make the listView (ingredientList) also scrollable when inserting text
        //ViewCompat.setNestedScrollingEnabled(ingredientListView, true);

        imageView = findViewById(R.id.imageView);
        imagePath = null;
        imageView.setVisibility(View.GONE);

        addImageButton = findViewById(R.id.openGalleryButton);
        differentImageButton = findViewById(R.id.openGalleryAgainButton);
        removeImageButton = findViewById(R.id.cancelImageButton);

        // there's no image yet -> hide buttons
        differentImageButton.setVisibility(View.GONE);
        removeImageButton.setVisibility(View.GONE);

        // the instruction recyclerView stuff
        instructionAdapter = new InstructionEditAdapter(this, instructionList);
        instructionRecyclerView = findViewById(R.id.instructionRecyclerView);

        instructionRecyclerView.setAdapter(instructionAdapter);
        instructionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // this makes sure that there's always one chip selected
        final ChipGroup chipGroupDuration = findViewById(R.id.chipGroupDuration);
        chipGroupDuration.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                for (int i = 0; i < chipGroupDuration.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupDuration.getChildAt(i);
                    if (chip != null) {
                        chip.setClickable(!(chip.getId() == chipGroupDuration.getCheckedChipId()));
                    }
                }
            }
        });
        final ChipGroup chipGroupDifficulty = findViewById(R.id.chipGroupDifficulty);
        chipGroupDifficulty.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                for (int i = 0; i < chipGroupDifficulty.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupDifficulty.getChildAt(i);
                    if (chip != null) {
                        chip.setClickable(!(chip.getId() == chipGroupDifficulty.getCheckedChipId()));
                    }
                }
            }
        });

        // This makes it possible to scroll in the comment field
        final TextInputEditText commentField = findViewById(R.id.commentsText);
        commentField.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (commentField.hasFocus()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_SCROLL){
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                    }
                }
                return false;
            }
        });

        servesNumberPicker = findViewById(R.id.servesNumberPicker);
        servesNumberPicker.setMinValue(1);
        servesNumberPicker.setMaxValue(50);
        servesNumberPicker.setValue(4);

    }

    /**
     * Creates the AlertDialog where the user adds ingredients
     *
     * @param view - the 'add ingredient' button which was pressed
     */
    public void createIngredientDialog(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // get the layout
        View dialog_layout = getLayoutInflater().inflate(R.layout.add_ingredient, null);

        // Create the text field in the alert dialog.
        ingredientNameField = dialog_layout.findViewById(R.id.ingredientNameField);
        ingredientQuantityField = dialog_layout.findViewById(R.id.quantityField);
        ingredientTypeField = dialog_layout.findViewById(R.id.ingredientTypeSpinner);

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
            // notify the ingredientAdapter to update the list
            ingredientAdapter.notifyDataSetChanged();

            // hide the 'no ingredients yet' text view
//            TextView noIngredientTextView = findViewById(R.id.noIngredientsTextView);
//            noIngredientTextView.setVisibility(TextView.GONE);

        } catch (IllegalArgumentException e) {
            Toast.makeText(AddRecipeActivity.this, "Oops, something went wrong with that ingredient, try again", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Validates parameters for ingredient
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
     * Creates the alertdialog where the user can add an instruction
     *
     * @param view - the 'add instruction' button
     */
    public void createInstructionDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // get the layout
        View dialog_layout = getLayoutInflater().inflate(R.layout.add_instruction, null);

        // get all the fields and initialise + disable them
        minuteTextView = dialog_layout.findViewById(R.id.minutesTextView);
        secondTextView = dialog_layout.findViewById(R.id.secondsTextView);

        minuteNumberPicker = dialog_layout.findViewById(R.id.minuteNumberPicker);
        secondNumberPicker = dialog_layout.findViewById(R.id.secondNumberPicker);

        minuteNumberPicker.setMinValue(0);
        secondNumberPicker.setMinValue(0);

        minuteNumberPicker.setMaxValue(59);
        secondNumberPicker.setMaxValue(59);

        minuteNumberPicker.setValue(6);
        secondNumberPicker.setValue(30);

        minuteNumberPicker.setEnabled(false);
        secondNumberPicker.setEnabled(false);
        minuteTextView.setTextColor(Color.parseColor("#333333"));
        secondTextView.setTextColor(Color.parseColor("#333333"));

        final int enabledColor = ContextCompat.getColor(this, R.color.primaryColor);

        // add eventListener to enable and disable the number pickers
        final SwitchMaterial timerEnabledSwitch = dialog_layout.findViewById(R.id.timerEnabledSwitch);
        timerEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    minuteNumberPicker.setEnabled(true);
                    secondNumberPicker.setEnabled(true);
                    minuteTextView.setEnabled(true);
                    secondTextView.setEnabled(true);
                    minuteTextView.setTextColor(enabledColor);
                    secondTextView.setTextColor(enabledColor);
                }
                else {
                    minuteNumberPicker.setEnabled(false);
                    secondNumberPicker.setEnabled(false);
                    minuteTextView.setTextColor(Color.parseColor("#333333"));
                    secondTextView.setTextColor(Color.parseColor("#333333"));
                }
            }
        });

        instructionDescription = dialog_layout.findViewById(R.id.instructionInputField);

        builder.setTitle("Add instruction");
        builder.setMessage("Add an instruction, and optionally a timer by selecting the minutes and seconds.");

        builder.setPositiveButton("Add Instruction", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(timerEnabledSwitch.isChecked()) {
                    long totalMilliSeconds = calcMilliSeconds(minuteNumberPicker.getValue(), secondNumberPicker.getValue());
                    createInstruction(instructionDescription.getText().toString(), totalMilliSeconds);
                }
                else {
                    createInstruction(instructionDescription.getText().toString(), null);
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
     * calculates the total amount of milliseconds
     *
     * @param minutes - the minutes given
     * @param seconds - the seconds given
     * @return - returns long, the total amount of seconds
     */
    private long calcMilliSeconds(int minutes, int seconds) {
        return ((minutes * 60) + seconds) * 1000;
    }

    /**
     * Creates an instruction, or shows an error toast if something goes wrong
     *
     * @param instructionDescription - the description of the instruction
     * @param timerDuration - the time in milliseconds, or null if no timer
     */
    private void createInstruction(String instructionDescription, Long timerDuration) {
        try {
            Instruction instruction;

            if(instructionDescription.isEmpty()) {
                throw new IllegalArgumentException();
            }

            instruction = new Instruction(instructionDescription, timerDuration);

            instructionList.add(instruction);
            // notify the adapter to update the list
            instructionAdapter.notifyDataSetChanged();

            TextView noInstructionTextView = findViewById(R.id.noInstructionsTextView);
            noInstructionTextView.setVisibility(View.GONE);

        } catch (IllegalArgumentException e) {
            Toast.makeText(AddRecipeActivity.this, "Oops, something went wrong with that instruction. Try again", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * gets called when the create recipe button is pressed
     * will check input and create recipe
     *
     * @param view  view given by the button
     */
    public void createRecipe(View view) {

        Boolean favouriteSwitch = ((SwitchMaterial) findViewById(R.id.favoriteSwitch)).isChecked();
        String recipeName = ((EditText) findViewById(R.id.nameField)).getText().toString();
        String recipeURL = ((EditText) findViewById(R.id.URLField)).getText().toString();
        String comments = ((EditText) findViewById(R.id.commentsText)).getText().toString();
        int serves = servesNumberPicker.getValue();

        // get the selected cookingTime
        ChipGroup chipGroupDuration = findViewById(R.id.chipGroupDuration);
        CookTime cookTime;

        switch (chipGroupDuration.getCheckedChipId()) {
            case R.id.shortDurationChip:
                cookTime = CookTime.SHORT;
                break;
            case R.id.longDurationChip:
                cookTime = CookTime.LONG;
                break;
            default:
                cookTime = CookTime.MEDIUM;
        }

        // get the selected difficulty
        ChipGroup chipGroupDifficulty = findViewById(R.id.chipGroupDifficulty);
        Difficulty difficulty;

        switch (chipGroupDifficulty.getCheckedChipId()) {
            case R.id.beginnerDifficultyChip:
                difficulty = Difficulty.BEGINNER;
                break;
            case R.id.expertDifficultyChip:
                difficulty = Difficulty.EXPERT;
                break;
            default:
                difficulty = Difficulty.INTERMEDIATE;
        }


        if(recipeName.isEmpty()) {
            recipeTitleLayout.setError("Please fill in a title");
        } else if (ingredientList.isEmpty()) {
            Toast.makeText(AddRecipeActivity.this, "You have to add at least one ingredient", Toast.LENGTH_LONG).show();
        } else if (instructionList.isEmpty()) {
            Toast.makeText(AddRecipeActivity.this, "You have to add at least one instruction", Toast.LENGTH_LONG).show();
        } else {
            Recipe recipe = new Recipe(recipeName, ingredientList, favouriteSwitch,
                    cookTime, imagePath, recipeURL, difficulty, comments, instructionList, serves);
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
     *
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
     * Checks if the permission has been accepted if so opens the pickfromgallery function
     * if not tries to request the permission again
     * @param view - the pressed button to add an image
     */
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

                        // generate a bitmap, to put in the imageView
                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                        imageView.setImageBitmap(bitmap);

                        imageView.setVisibility(View.VISIBLE);

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

        imageView.setVisibility(View.GONE);

        // show & hide appropriate buttons
        addImageButton.setVisibility(View.VISIBLE);
        differentImageButton.setVisibility(View.GONE);
        removeImageButton.setVisibility(View.GONE);
    }

}
