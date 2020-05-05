package com.maarten.recipepicker.importRecipe;

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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.maarten.recipepicker.MainActivity;
import com.maarten.recipepicker.R;
import com.maarten.recipepicker.RecipeUtility;
import com.maarten.recipepicker.adapters.IngredientEditAdapter;
import com.maarten.recipepicker.adapters.InstructionEditAdapter;
import com.maarten.recipepicker.enums.CookTime;
import com.maarten.recipepicker.enums.Difficulty;
import com.maarten.recipepicker.models.Ingredient;
import com.maarten.recipepicker.models.Instruction;
import com.maarten.recipepicker.models.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.maarten.recipepicker.MainActivity.recipeList;


public class ImportViewRecipeFragment extends Fragment {

    private Recipe recipe;
    private TextView recipeTitle, recipeComments, recipeURL;
    private TextInputLayout recipeTitleLayout;

    private RecyclerView ingredientListRecyclerView;
    private List<Ingredient> ingredientList;
    private IngredientEditAdapter ingredientAdapter;
    private EditText ingredientNameField, ingredientQuantityField;
    private Spinner ingredientTypeField;

    private ChipGroup chipGroupDuration, chipGroupDifficulty;

    private static final int READ_EXTERNAL_PERMISSIONS = 1;
    private static final int GALLERY_REQUEST_CODE = 2;

    private ImageView imageView;
    private String imagePath;
    private MaterialButton removeImageButton, differentImageButton, addImageButton;

    private InstructionEditAdapter instructionAdapter;
    private NumberPicker minuteNumberPicker, secondNumberPicker;
    private TextView minuteTextView, secondTextView;
    private List<Instruction> instructionList;
    private EditText instructionDescription;

    private NumberPicker servesNumberPicker;

    private ChipGroup categoriesChipGroup;
    private Set<String> categorySet;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_import_view_recipe, container, false);

        if (this.getArguments() == null) {
            Toast.makeText(requireActivity(), "Something went wrong trying to read the recipe. [No arguments found]", Toast.LENGTH_LONG).show();
            goBack();
            return null;
        }

        String json_input = this.getArguments().getString("json_recipe");

        Gson gson = new Gson();
        try {
            recipe = gson.fromJson(json_input, Recipe.class);
            if (recipe == null) {
                throw new JsonParseException("Parse error, no object created.");
            }
        } catch (JsonParseException e) {
            Toast.makeText(requireActivity(), "Something went wrong trying to read the recipe.", Toast.LENGTH_LONG).show();
            Log.e("Gson parse error", e.getMessage());
            goBack();
            return null;
        }

        recipe.resetAddedDate();
        recipe.setFavorite(false);
        recipe.setRating(0);
        recipe.setImagePath(null);
        recipe.resetAmountCooked();

        recipeTitle = view.findViewById(R.id.nameField);
        if (recipe.getTitle() == null) {
            recipe.setTitle("");
        }
        recipeTitle.setText(recipe.getTitle());

        MaterialButton addIngredientButton = view.findViewById(R.id.addIngredientButton);
        addIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createIngredientDialog();
            }
        });

        if (recipe.getIngredientList() == null) {
            recipe.setIngredientList(new ArrayList<Ingredient>());
        }
        ingredientList = recipe.getIngredientList();

        ingredientAdapter = new IngredientEditAdapter(requireActivity(), ingredientList);
        ingredientListRecyclerView = view.findViewById(R.id.editRecipeIngredientList);
        ingredientListRecyclerView.setAdapter(ingredientAdapter);
        ingredientListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        ingredientListRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));

        // make the listview (ingredientList) also scrollable when inserting text
        ViewCompat.setNestedScrollingEnabled(ingredientListRecyclerView, true);

        recipeTitleLayout = view.findViewById(R.id.nameFieldLayout);

        imageView = view.findViewById(R.id.imageView);
        imagePath = null;

        addImageButton = view.findViewById(R.id.openGalleryButton);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPictureGallery();
            }
        });

        differentImageButton = view.findViewById(R.id.openGalleryAgainButton);
        differentImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPictureGallery();
            }
        });

        removeImageButton = view.findViewById(R.id.cancelImageButton);
        removeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeImage();
            }
        });

        // there's no image yet -> hide buttons
        differentImageButton.setVisibility(View.GONE);
        removeImageButton.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);

        recipeURL = view.findViewById(R.id.URLField);
        if (recipe.getURL() == null) {
            recipe.setURL("");
        }
        recipeURL.setText(recipe.getURL());

        recipeComments = view.findViewById(R.id.commentsText);
        if (recipe.getComments() == null) {
            recipe.setComments("");
        }
        recipeComments.setText(recipe.getComments());

        if (recipe.getCookTime() == null) {
            recipe.setCookTime(CookTime.MEDIUM);
        }
        // check the current selected chips
        chipGroupDuration = view.findViewById(R.id.durationChipGroup);
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

        if (recipe.getDifficulty() == null) {
            recipe.setDifficulty(Difficulty.INTERMEDIATE);
        }
        chipGroupDifficulty = view.findViewById(R.id.difficultyChipGoup);
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

        MaterialButton addInstructionButton = view.findViewById(R.id.instructionButton);
        addInstructionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createInstructionDialog();
            }
        });

        if (recipe.getInstructionList() == null) {
            recipe.setInstructionList(new ArrayList<Instruction>());
        }
        instructionList = recipe.getInstructionList();

        // the instruction recyclerview stuff
        instructionAdapter = new InstructionEditAdapter(requireActivity(), instructionList);
        RecyclerView instructionRecyclerView = view.findViewById(R.id.instructionRecyclerView);

        instructionRecyclerView.setAdapter(instructionAdapter);
        instructionRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        servesNumberPicker = view.findViewById(R.id.servesNumberPicker);
        servesNumberPicker.setMinValue(1);
        servesNumberPicker.setMaxValue(50);
        if (recipe.getServes() <= 0) {
            recipe.setServes(4);
        } else if (recipe.getServes() >= 50) {
            recipe.setServes(50);
        }
        servesNumberPicker.setValue(recipe.getServes());

        categoriesChipGroup = view.findViewById(R.id.categoriesChipGroup);
        if (recipe.getCategories() == null) {
            recipe.setCategories(new TreeSet<>());
        }
        categorySet = recipe.getCategories();
        for (String category : categorySet) {
            addCategoryChip(category);
        }

        MaterialButton addCategoryButton = view.findViewById(R.id.addCategoryButton);
        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCategoryDialog();
            }
        });

        MaterialButton addRecipeButton = view.findViewById(R.id.addRecipeButton);
        addRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createRecipe();
            }
        });

        MaterialButton backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        return view;
    }

    /**
     * Goes back the previous fragment.
     * Important notice, you also need to call 'return' in your other function
     */
    private void goBack() {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.popBackStack();
    }

    /**
     * validates input and creates the recipe
     */
    private void createRecipe() {
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
            Toast.makeText(requireActivity(), "You have to add at least one ingredient", Toast.LENGTH_LONG).show();
        } else if (instructionList.isEmpty()) {
            Toast.makeText(requireActivity(), "You have to add at least one instruction", Toast.LENGTH_LONG).show();
        } else {

            // Do the actual updating of the recipe
            recipe.setTitle(tempRecipeName);
            recipe.setIngredientList(ingredientList);
            recipe.setInstructionList(instructionList);
            recipe.setCategories(categorySet);

            recipe.setCookTime(cookTime);
            recipe.setDifficulty(difficulty);

            recipe.setImagePath(imagePath);

            recipe.setURL(recipeURL.getText().toString());
            recipe.setComments(recipeComments.getText().toString());

            recipe.setServes(servesNumberPicker.getValue());

            recipeList.add(recipe);
            MainActivity.saveRecipes();

            Toast.makeText(requireActivity(), "Your recipe was added!", Toast.LENGTH_LONG).show();

            returnToMainActivity();
        }
    }

    /**
     * Creates a dialog to add a category
     */
    private void createCategoryDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        // get the layout
        View dialog_layout = getLayoutInflater().inflate(R.layout.add_category_dialog, null);

        // Create the text field in the alert dialog.
        final EditText categoryEditText = dialog_layout.findViewById(R.id.categoryEditText);

        builder.setTitle("Add category");
        builder.setMessage("Add a category here. A category can be 'Pasta' or 'Main course' for example.");

        builder.setPositiveButton("Add category", (dialog, id) -> {
            String inputText = categoryEditText.getText().toString();
            // only add if it's not empty and it doesn't exist yet
            if(inputText.isEmpty()) {
                return;
            }
            inputText = RecipeUtility.changeFirstLetterToCapital(inputText.trim());
            if(!categorySet.contains(inputText)) {
                categorySet.add(inputText);
                addCategoryChip(inputText);
            } else {
                Toast.makeText(requireActivity(), "This category already exists", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, id) -> {
        });
        // create and show the dialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.setView(dialog_layout);
        alertDialog.show();
    }

    /**
     * Creates a chip and adds it to the categoriesChipGroup
     *
     * @param category - the name of the category
     */
    private void addCategoryChip(String category) {
        final Chip chip = new Chip(requireActivity());
        chip.setText(category);
        chip.setCloseIconResource(R.drawable.ic_close_black_24dp);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoriesChipGroup.removeView(chip);
            }
        });

        categoriesChipGroup.addView(chip);
    }

    /**
     * returns to the main activity and removes the backstack
     * This is necessary because otherwise we would go back to an unedited recipe
     */
    private void returnToMainActivity() {
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Creates the recipe dialog to insert an ingredient
     */
    private void createIngredientDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        // get the layout
        View dialog_layout = getLayoutInflater().inflate(R.layout.add_ingredient_dialog, null);

        // Create the text field in the alert dialog.
        ingredientNameField = dialog_layout.findViewById(R.id.ingredientNameField);
        ingredientQuantityField = dialog_layout.findViewById(R.id.quantityField);
        ingredientTypeField = dialog_layout.findViewById(R.id.ingredientTypeSpinner);

        // create the spinner ingredientAdapter with the choices + the standard views of how it should look like
        ArrayAdapter<CharSequence> ingredientTypeAdapter = ArrayAdapter.createFromResource(requireActivity(), R.array.ingredient_types_array_items, android.R.layout.simple_spinner_item);
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
            String ingredientName = RecipeUtility.changeFirstLetterToCapital(name.trim());

            if(quantity == null) {
                ingredient = new Ingredient(ingredientName, null, ingredientType);
            }
            else {
                ingredient = new Ingredient(ingredientName, Double.parseDouble(quantity), ingredientType);
            }

            ingredientList.add(ingredient);
            // notify the ingredientAdapter to update the list
            ingredientAdapter.notifyDataSetChanged();

            // unhide the list
            ingredientListRecyclerView.setVisibility(TextView.VISIBLE);

        } catch (IllegalArgumentException e) {
            Toast.makeText(requireActivity(), "Oops, something went wrong with that ingredient, try again", Toast.LENGTH_LONG).show();
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
     */
    private void showPictureGallery() {
        if(requireActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            pickFromGallery();
        } else {
            // permission hasn't been granted.

            if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(requireActivity(), "External storage permission is needed to access your images.", Toast.LENGTH_LONG).show();
            }

            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},READ_EXTERNAL_PERMISSIONS);
        }

    }

    //@Override
    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == READ_EXTERNAL_PERMISSIONS) {
            // check if the required permission is granted
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFromGallery();
            } else {
                Toast.makeText(requireActivity(), "Permission was not granted.", Toast.LENGTH_SHORT).show();
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
                        imagePath = getRealPathFromURI(requireActivity(), selectedImage);

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
     */
    private void removeImage() {
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
     */
    private void createInstructionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        // get the layout
        View dialog_layout = getLayoutInflater().inflate(R.layout.add_instruction_dialog, null);

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

        final int enabledColor = ContextCompat.getColor(requireActivity(), R.color.primaryColor);

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
            Toast.makeText(requireActivity(), "Oops, something went wrong with that instruction. Try again", Toast.LENGTH_LONG).show();
        }
    }
}
