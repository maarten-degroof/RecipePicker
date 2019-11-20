package com.maarten.recipepicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.maarten.recipepicker.adapters.IngredientEditAdapter;
import com.maarten.recipepicker.adapters.InstructionEditAdapter;
import com.maarten.recipepicker.enums.CookTime;
import com.maarten.recipepicker.enums.Difficulty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static com.maarten.recipepicker.MainActivity.recipeList;

public class EditRecipeActivity extends AppCompatActivity {

    private Recipe recipe;
    private int recipeIndex;
    private IngredientEditAdapter ingredientAdapter;

    private TextView recipeTitle, noIngredientTextView, recipeComments, recipeURL;
    private ListView ingredientListView;
    private List<Ingredient> ingredientList;

    private EditText ingredientNameField, ingredientQuantityField;
    private Spinner ingredientTypeField;

    private TextInputLayout recipeTitleLayout;

    private ChipGroup chipGroupDuration, chipGroupDifficulty;

    private static final int READ_EXTERNAL_PERMISSIONS = 1;
    private static final int GALLERY_REQUEST_CODE = 2;

    private ImageView imageView;
    private String imagePath;

    private Button removeImageButton, differentImageButton, addImageButton;

    private InstructionEditAdapter instructionAdapter;
    private RecyclerView instructionRecyclerView;
    private NumberPicker minuteNumberPicker, secondNumberPicker;
    private TextView minuteTextView, secondTextView;
    private List<Instruction> instructionList;
    private EditText instructionDescription;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Edit recipe");
        setSupportActionBar(toolbar);

        // this takes care of the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        recipe = (Recipe) intent.getSerializableExtra("Recipe");

        recipeTitle = findViewById(R.id.nameField);
        recipeTitle.setText(recipe.getTitle());

        ingredientList = recipe.getIngredientList();

        ingredientAdapter = new IngredientEditAdapter(this,ingredientList);
        ingredientListView = findViewById(R.id.editRecipeIngredientList);
        ingredientListView.setAdapter(ingredientAdapter);

        // make the listview (ingredientList) also scrollable when inserting text
        ViewCompat.setNestedScrollingEnabled(ingredientListView, true);

        recipeTitleLayout = findViewById(R.id.nameFieldLayout);

        // hide
        noIngredientTextView = findViewById(R.id.noIngredientsTextView);
        noIngredientTextView.setVisibility(View.INVISIBLE);

        imageView = findViewById(R.id.imageView);
        imagePath = recipe.getImagePath();

        addImageButton = findViewById(R.id.openGalleryButton);
        differentImageButton = findViewById(R.id.openGalleryAgainButton);
        removeImageButton = findViewById(R.id.cancelImageButton);

        // check if there's an image & hide the correct buttons
        if(imagePath != null) {
            // generate a bitmap, to put in the imageview
            Bitmap bitmap;

            if(Character.isDigit(imagePath.charAt(0))) {
                bitmap = BitmapFactory.decodeResource(this.getResources(), Integer.decode(imagePath));
            } else {
                bitmap = BitmapFactory.decodeFile(imagePath);
            }

            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
            // show & hide appropriate buttons
            addImageButton.setVisibility(View.GONE);
            differentImageButton.setVisibility(View.VISIBLE);
            removeImageButton.setVisibility(View.VISIBLE);
        } else {
            // there's no image yet -> hide buttons
            differentImageButton.setVisibility(View.GONE);
            removeImageButton.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
        }

        recipeURL = findViewById(R.id.URLField);
        recipeURL.setText(recipe.getURL());

        recipeComments = findViewById(R.id.commentsText);
        recipeComments.setText(recipe.getComments());

        // check the current selected chips
        chipGroupDuration = findViewById(R.id.chipGroupDuration);
        switch (recipe.getCookTime()) {
            case SHORT:
                chipGroupDuration.check(R.id.shortDurationChip);
                break;
            case LONG:
                chipGroupDuration.check(R.id.longDurationChip);
                break;
            default:
                chipGroupDuration.check(R.id.mediumDurationChip);
        }

        chipGroupDifficulty = findViewById(R.id.chipGroupDifficulty);
        switch (recipe.getDifficulty()) {
            case BEGINNER:
                chipGroupDifficulty.check(R.id.beginnerDifficultyChip);
                break;
            case EXPERT:
                chipGroupDifficulty.check(R.id.expertDifficultyChip);
                break;
            default:
                chipGroupDifficulty.check(R.id.intermediateDifficultyChip);
        }

        // this makes sure that there's always one chip selected
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
        recipeComments.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (recipeComments.hasFocus()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_SCROLL){
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                    }
                }
                return false;
            }
        });

        instructionList = recipe.getInstructionList();

        // the instruction recyclerview stuff
        instructionAdapter = new InstructionEditAdapter(this, instructionList);
        instructionRecyclerView = findViewById(R.id.instructionRecyclerView);

        instructionRecyclerView.setAdapter(instructionAdapter);
        instructionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    /**
     * validates input and updates the recipe
     *
     * @param view  needed for the button
     */
    public void updateRecipe(View view) {
        String tempRecipeName = recipeTitle.getText().toString();

        // get the current selected chip
        CookTime cookTime;
        switch (chipGroupDuration.getCheckedChipId()) {
            case R.id.shortDurationChip:
                cookTime = CookTime.SHORT;
                break;
            case  R.id.longDurationChip:
                cookTime = CookTime.LONG;
                break;
            default:
                cookTime = CookTime.MEDIUM;
        }
        Difficulty difficulty;
        switch (chipGroupDifficulty.getCheckedChipId()) {
            case R.id.beginnerDifficultyChip:
                difficulty = Difficulty.BEGINNER;
                break;
            case  R.id.expertDifficultyChip:
                difficulty = Difficulty.EXPERT;
                break;
            default:
                difficulty = Difficulty.INTERMEDIATE;
        }


        if(tempRecipeName.isEmpty()) {
            recipeTitleLayout.setError("Please fill in a title");
        } else if (ingredientList.isEmpty()) {
            Toast.makeText(this, "You have to add at least one ingredient", Toast.LENGTH_LONG).show();
        } else if (instructionList.isEmpty()) {
            Toast.makeText(EditRecipeActivity.this, "You have to add at least one instruction", Toast.LENGTH_LONG).show();
        } else {
            boolean resetCookedCounter = ((MaterialCheckBox) findViewById(R.id.resetAmountCookedCheckBox)).isChecked();

            // Do the actual updating
            recipeIndex = recipeList.indexOf(recipe);
            recipeList.get(recipeIndex).setTitle(tempRecipeName);
            recipeList.get(recipeIndex).setIngredientList(ingredientList);
            recipeList.get(recipeIndex).setInstructionList(instructionList);

            if(resetCookedCounter) {
                recipeList.get(recipeIndex).resetAmountCooked();
            }
            recipeList.get(recipeIndex).setCookTime(cookTime);
            recipeList.get(recipeIndex).setDifficulty(difficulty);

            recipeList.get(recipeIndex).setImagePath(imagePath);

            recipeList.get(recipeIndex).setURL(recipeURL.getText().toString());
            recipeList.get(recipeIndex).setComments(recipeComments.getText().toString());

            Toast.makeText(this, "Your recipe was updated!", Toast.LENGTH_LONG).show();

            returnToMainActivity();
        }
    }

    /**
     * the cancel button to return to the previous view
     *
     * @param view  needed for the button to connect
     */
    public void cancelEdit(View view) {
        finish();
    }

    /**
     * returns to the main activity and removes the backstack
     * This is necessary because otherwise we would go back to an unedited recipe
     */
    private void returnToMainActivity() {
        Intent intent = new Intent(EditRecipeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Creates the recipe dialog to insert an ingredient
     * @param view  needed for the button-linking
     */
    public void createIngredientDialog(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // get the layout
        View dialog_layout = getLayoutInflater().inflate(R.layout.add_ingredient, null);

        // Create the text field in the alert dialog.
        ingredientNameField = dialog_layout.findViewById(R.id.ingredientNameField);
        ingredientQuantityField = dialog_layout.findViewById(R.id.quantityField);
        ingredientTypeField = dialog_layout.findViewById(R.id.ingredientTypeSpinner);

        // create the spinner ingredientAdapter with the choices + the standard views of how it should look like
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
            TextView noIngredientTextView = findViewById(R.id.noIngredientsTextView);
            noIngredientTextView.setVisibility(TextView.INVISIBLE);

            // unhide the list
            ingredientListView.setVisibility(TextView.VISIBLE);

        } catch (IllegalArgumentException e) {
            Toast.makeText(EditRecipeActivity.this, "Oops, something went wrong with that ingredient, try again", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Validates parameters for ingredient.
     *
     * @param name      value should not be empty
     * @param quantity  value should be a number and not be empty
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
     * @return true if parseable as double, false if not
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
                Toast.makeText(this, "External storage permission is needed to access your images.", Toast.LENGTH_LONG).show();
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
                Toast.makeText(this, "Permission was not granted.", Toast.LENGTH_SHORT).show();
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

        minuteNumberPicker.setMaxValue(60);
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

        } catch (IllegalArgumentException e) {
            Toast.makeText(EditRecipeActivity.this, "Oops, something went wrong with that instruction. Try again", Toast.LENGTH_LONG).show();
        }
    }

}
