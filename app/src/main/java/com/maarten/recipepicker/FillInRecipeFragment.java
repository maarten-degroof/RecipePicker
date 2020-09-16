package com.maarten.recipepicker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.maarten.recipepicker.adapters.IngredientEditAdapter;
import com.maarten.recipepicker.adapters.InstructionEditAdapter;
import com.maarten.recipepicker.enums.CookTime;
import com.maarten.recipepicker.enums.Difficulty;
import com.maarten.recipepicker.enums.FillInRecipeFragmentType;
import com.maarten.recipepicker.models.Ingredient;
import com.maarten.recipepicker.models.Instruction;
import com.maarten.recipepicker.viewModels.FillInRecipeViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import static android.app.Activity.RESULT_OK;

public class FillInRecipeFragment extends Fragment {

    private RecyclerView ingredientRecyclerView, instructionRecyclerView;

    private List<Ingredient> ingredientList;
    private List<Instruction> instructionList;

    private EditText titleEditText, urlEditText, commentsEditText;

    private TextInputLayout recipeTitleLayout;

    private SwitchMaterial favoriteSwitch;

    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION = 1;
    private static final int GALLERY_REQUEST_CODE = 2;
    private static final int CAMERA_REQUEST_CODE = 3;

    private boolean shouldLoadCameraScreen;

    private ImageView imageView;
    private String imagePath;
    private Button removeImageButton, changeImageButton, addImageButton;

    private NumberPicker servesNumberPicker;

    private ChipGroup categoriesChipGroup;
    private ChipGroup chipGroupDuration;
    private ChipGroup difficultyChipGroup;

    private FillInRecipeViewModel viewModel;

    private File storageDir;
    private AlertDialog addPhotoDialog;
    private AlertDialog categoryDialog;

    public FillInRecipeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fill_in_recipe, container, false);

        recipeTitleLayout = view.findViewById(R.id.titleLayout);

        imagePath = null;

        titleEditText = view.findViewById(R.id.titleEditText);
        urlEditText = view.findViewById(R.id.URLEditText);
        commentsEditText = view.findViewById(R.id.commentsEditText);
        recipeTitleLayout = view.findViewById(R.id.titleLayout);

        imageView = view.findViewById(R.id.imageView);
        imageView.setVisibility(View.GONE);

        addImageButton = view.findViewById(R.id.addImageButton);
        addImageButton.setOnClickListener(view1 -> createAddPhotoDialog());

        changeImageButton = view.findViewById(R.id.changeImageButton);
        changeImageButton.setOnClickListener(view1 -> createAddPhotoDialog());

        removeImageButton = view.findViewById(R.id.cancelImageButton);
        removeImageButton.setOnClickListener(view1 -> removeImage());

        // There's no image yet -> hide buttons
        changeImageButton.setVisibility(View.GONE);
        removeImageButton.setVisibility(View.GONE);

        ingredientRecyclerView = view.findViewById(R.id.ingredientRecyclerView);
        ingredientRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        ingredientRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));

        instructionRecyclerView = view.findViewById(R.id.instructionRecyclerView);
        instructionRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        chipGroupDuration = view.findViewById(R.id.durationChipGroup);
        difficultyChipGroup = view.findViewById(R.id.difficultyChipGroup);

        // This makes sure that there's always one chip selected
        chipGroupDuration.setOnCheckedChangeListener((group, checkedId) -> {
            for (int i = 0; i < chipGroupDuration.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupDuration.getChildAt(i);
                if (chip != null) {
                    chip.setClickable(!(chip.getId() == chipGroupDuration.getCheckedChipId()));
                }
            }
        });
        difficultyChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            for (int i = 0; i < difficultyChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) difficultyChipGroup.getChildAt(i);
                if (chip != null) {
                    chip.setClickable(!(chip.getId() == difficultyChipGroup.getCheckedChipId()));
                }
            }
        });

        // This makes it possible to scroll in the comment field
        commentsEditText.setOnTouchListener((v, event) -> {
            if (commentsEditText.hasFocus()) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_SCROLL){
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                }
            }
            return false;
        });

        servesNumberPicker = view.findViewById(R.id.servesNumberPicker);
        servesNumberPicker.setMinValue(1);
        servesNumberPicker.setMaxValue(50);

        favoriteSwitch = view.findViewById(R.id.favoriteSwitch);

        categoriesChipGroup = view.findViewById(R.id.categoriesChipGroup);
        MaterialButton addCategoryButton = view.findViewById(R.id.addCategoryButton);
        addCategoryButton.setOnClickListener(view1 -> createCategoryDialog());

        MaterialButton addIngredientButton = view.findViewById(R.id.addIngredientButton);
        addIngredientButton.setOnClickListener(view1 -> {
            removeCursorFromWidget();
            ((AddRecipeInterface)requireActivity()).toggleCurrentFragment(FillInRecipeFragmentType.ADD_INGREDIENT);
        });

        MaterialButton addInstructionButton = view.findViewById(R.id.addInstructionButton);
        addInstructionButton.setOnClickListener(v -> {
            removeCursorFromWidget();
            ((AddRecipeInterface)requireActivity()).toggleCurrentFragment(FillInRecipeFragmentType.ADD_INSTRUCTION);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(FillInRecipeViewModel.class);

        ingredientList = viewModel.getIngredientList();
        IngredientEditAdapter ingredientAdapter = new IngredientEditAdapter(requireActivity(), ingredientList);
        ingredientAdapter.notifyDataSetChanged();
        ingredientRecyclerView.setAdapter(ingredientAdapter);

        instructionList = viewModel.getInstructionList();
        InstructionEditAdapter instructionAdapter = new InstructionEditAdapter(requireActivity(), instructionList);
        instructionAdapter.notifyDataSetChanged();
        instructionRecyclerView.setAdapter(instructionAdapter);

        titleEditText.setText(viewModel.getRecipeTitle());
        urlEditText.setText(viewModel.getRecipeURL());
        commentsEditText.setText(viewModel.getRecipeComments());

        favoriteSwitch.setChecked(viewModel.isRecipeFavorite());

        servesNumberPicker.setValue(viewModel.getServeCount());

        imagePath = viewModel.getRecipeImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            showImage(imagePath);
        }

        setCookTime(viewModel.getRecipeCookTime());
        setDifficulty(viewModel.getRecipeDifficulty());

        for (String category : viewModel.getCategorySet()) {
            addCategoryChip(category);
        }

        shouldLoadCameraScreen = false;

        createImageFolder();

        if (viewModel.isShowingAddPhotoDialog()) {
            createAddPhotoDialog();
        } else if (viewModel.isShowingCategoryDialog()) {
            createCategoryDialog();
        }
    }

    /**
     * Creates an empty image file, and updates the imagePath variable to have the path to that file
     * The name of the file is the current date and time, with an added unique identifier.
     * @return returns the File that was created
     * @throws IOException If no file could be created, an IOException is thrown
     */
    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HH:mm:ss_", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        imagePath = image.getAbsolutePath();
        return image;
    }

    /**
     * Shows a dialog with the option to add a picture with the camera or to choose an image through
     * a gallery.
     */
    private void createAddPhotoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        viewModel.setShowingAddPhotoDialog(true);
        // Get the layout
        View dialog_layout = View.inflate(requireActivity(), R.layout.add_image_dialog, null);

        final MaterialButton openCameraButton = dialog_layout.findViewById(R.id.openCameraButton);
        final MaterialButton openGalleryButton = dialog_layout.findViewById(R.id.openGalleryButton);

        openCameraButton.setOnClickListener(v -> {
            shouldLoadCameraScreen = true;
            checkStoragePermission();
            addPhotoDialog.dismiss();
            viewModel.setShowingAddPhotoDialog(false);
        });

        openGalleryButton.setOnClickListener(v -> {
            shouldLoadCameraScreen = false;
            checkStoragePermission();
            addPhotoDialog.dismiss();
            viewModel.setShowingAddPhotoDialog(false);

        });

        // If it's a device without any cameras, remove the option
        if (!requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            openCameraButton.setVisibility(View.GONE);
        }

        builder.setTitle("Add a photo");

        builder.setNegativeButton("Cancel", (dialog, id) -> viewModel.setShowingAddPhotoDialog(false));
        // Create and show the dialog
        addPhotoDialog = builder.create();
        addPhotoDialog.setView(dialog_layout);
        addPhotoDialog.setOnCancelListener(dialog -> viewModel.setShowingAddPhotoDialog(false));
        addPhotoDialog.show();
    }

    /**
     * Starts an intent with the created imagePath to open the camera to take a picture. The
     * camera will then use the imagePath to put the created picture on that location.
     */
    private void startCameraWindow() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                File image = createImageFile();

                Uri photoUri = FileProvider.getUriForFile(RecipePickerApplication.getAppContext(),
                        RecipePickerApplication.getAppContext().getPackageName() + ".provider", image);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            } catch (IOException e) {
                Toast.makeText(requireActivity(), "Something went wrong trying to start the camera, please try again.", Toast.LENGTH_LONG).show();
            }
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(requireActivity(), "Sorry there's no camera to use.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Creates the folder 'RecipePicker' where images will be placed, if necessary.
     */
    private void createImageFolder() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    + File.separator + "RecipePicker");

            if(!storageDir.isDirectory()) {
                storageDir.mkdirs();
            }
        }
        if(!storageDir.isDirectory()) {
            storageDir = requireActivity().getFilesDir();
        }
    }

    /**
     * Resets all the fields and triggers the reset of the viewModel
     */
    public void resetFragment() {
        ingredientList = new ArrayList<>();
        instructionList = new ArrayList<>();

        categoriesChipGroup.removeAllViews();

        titleEditText.setText("");
        recipeTitleLayout.setError(null);

        urlEditText.setText("");
        commentsEditText.setText("");

        favoriteSwitch.setChecked(false);
        servesNumberPicker.setValue(4);

        imagePath = "";
        setCookTime(CookTime.MEDIUM);
        setDifficulty(Difficulty.INTERMEDIATE);

        viewModel.reset();
    }

    /**
     * When the fragment is stopped, save all the data to the ViewModel
     */
    @Override
    public void onStop() {
        if (addPhotoDialog != null && addPhotoDialog.isShowing()) {
            addPhotoDialog.dismiss();
        } else if (categoryDialog != null && categoryDialog.isShowing()) {
            categoryDialog.dismiss();
        }

        super.onStop();

        saveToViewModel();
    }

    /**
     * Saves all the variables to the ViewModel
     */
    public void saveToViewModel() {
        viewModel.setRecipeTitle(titleEditText.getText().toString());
        viewModel.setRecipeURL(urlEditText.getText().toString());
        viewModel.setRecipeComments(commentsEditText.getText().toString());

        viewModel.setRecipeFavorite(favoriteSwitch.isChecked());
        viewModel.setServeCount(servesNumberPicker.getValue());

        viewModel.setRecipeImagePath(imagePath);

        viewModel.setRecipeDifficulty(getDifficulty());
        viewModel.setRecipeCookTime(getCookTime());

        viewModel.setCategorySet(generateCategorySet());
    }

    /**
     * Generates a Set of all the categories that are in the categoriesChipGroup
     * @return Returns the created Set.
     */
    private Set<String> generateCategorySet() {
        Set<String> categorySet = new TreeSet<>();
        for (int index=0; index < categoriesChipGroup.getChildCount(); index++) {
            categorySet.add(((Chip)categoriesChipGroup.getChildAt(index)).getText().toString());
        }
        return categorySet;
    }

    /**
     * Shows the error in the title input field
     */
    public void showTitleError() {
        recipeTitleLayout.setError("Please fill in a title.");
    }

    /**
     * Checks which chip is checked and returns the type of that chip
     * @return returns the selected Difficulty type
     */
    private Difficulty getDifficulty() {
        Difficulty difficulty;
        switch (difficultyChipGroup.getCheckedChipId()) {
            case R.id.beginnerDifficultyChip:
                difficulty = Difficulty.BEGINNER;
                break;
            case R.id.expertDifficultyChip:
                difficulty = Difficulty.EXPERT;
                break;
            default:
                difficulty = Difficulty.INTERMEDIATE;
        }
        return difficulty;
    }

    /**
     * Checks which chip is checked and returns the type of that chip
     * @return returns the selected CookTime type
     */
    private CookTime getCookTime() {
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
        return cookTime;
    }

    /**
     * Sets the Difficulty to the given one
     * @param difficulty the difficulty to set
     */
    private void setDifficulty(Difficulty difficulty) {
        switch (difficulty) {
            case BEGINNER:
                difficultyChipGroup.check(R.id.beginnerDifficultyChip);
                break;
            case EXPERT:
                difficultyChipGroup.check(R.id.expertDifficultyChip);
                break;
            default:
                difficultyChipGroup.check(R.id.intermediateDifficultyChip);
        }
    }

    /**
     * Sets the cookTime chip to the given one
     * @param cookTime the cookTime to set
     */
    private void setCookTime(CookTime cookTime) {
        switch (cookTime) {
            case SHORT:
                chipGroupDuration.check(R.id.shortDurationChip);
                break;
            case LONG:
                chipGroupDuration.check(R.id.longDurationChip);
                break;
            default:
                chipGroupDuration.check(R.id.mediumDurationChip);
        }
    }

    /**
     * If one of the EditTextFields or the serves numberPicker is selected, removes the focus from that field.
     * If this doesn't happen, when pressing a button the focus will jump back to that textField.
     */
    private void removeCursorFromWidget() {
        titleEditText.clearFocus();
        urlEditText.clearFocus();
        commentsEditText.clearFocus();
        servesNumberPicker.clearFocus();
    }

    /**
     * Creates a chip and adds it to the categoriesChipGroup
     * @param category the name of the category
     */
    private void addCategoryChip(String category) {
        final Chip chip = new Chip(requireActivity());
        chip.setText(category);
        chip.setCloseIconResource(R.drawable.ic_close_black_24dp);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> categoriesChipGroup.removeView(chip));

        categoriesChipGroup.addView(chip);
    }

    /**
     * Checks if the permission has been accepted if so starts the gallery opening function or
     * the camera function (depending on shouldLoadCameraScreen).
     * If not tries to request the permission again
     */
    private void checkStoragePermission() {
        removeCursorFromWidget();
        if (requireActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (shouldLoadCameraScreen) {
                startCameraWindow();
            } else {
                pickFromGallery();
            }
        } else {
            // Permission hasn't been granted.
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.d("Permissions", "Should show extra info for the permission");
                String message;
                if (shouldLoadCameraScreen) {
                    message = "This permission is needed to be able to access the camera";
                } else {
                    message = "External storage permission is needed to access your images.";
                }
                Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    /**
     * Function is called when a permission was granted (or denied)
     * This checks if the permission was granted for the external files, and opens the gallery/camera if so.
     * @param requestCode the code for external file permission (1)
     * @param permissions a list of requested permissions
     * @param grantResults this says if the permission was granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (shouldLoadCameraScreen){
                    startCameraWindow();
                } else {
                    pickFromGallery();
                }
            } else {
            Toast.makeText(requireActivity(), "Permission was not granted.", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Creates intent with the parameters to open an image picker
     */
    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // We pass an extra array with the accepted mime types. This will ensure only components with these MIME types are targeted.
        String[] mimeTypes = {"imagePath/jpeg", "imagePath/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    /**
     * When image is chosen in an image picker, returns back to the activity and to this method
     * if image was chosen, copies it into the RecipePicker image folder and shows it.
     * If a picture was made through the camera, the image path has already been given to it and all
     * that needs to be done is to show it.
     * @param requestCode the code which you passed on when starting the activity, identifier
     * @param resultCode says if the user completed it and chose an image
     * @param data contains the Uri
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                // data.getData returns the content URI for the selected Image
                try {
                    Uri selectedImage = data.getData();

                    if(selectedImage == null || selectedImage.getPath() == null) {
                        throw new IOException("Something went wrong!");
                    }
                    // Remove the old image first
                    removeImage();

                    File newFile = createImageFile();

                    // Copy the old file to the new location in the RecipePicker folder
                    try(InputStream inputStream = requireActivity().getContentResolver()
                            .openInputStream(selectedImage);
                        FileOutputStream fileOutputStream = new FileOutputStream(
                                newFile)) {
                        if(inputStream == null) {
                            throw new IOException("There was no image found.");
                        }
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                        }
                    }

                    // change the creation date of the file so it's shown as created now
                    newFile.setLastModified(System.currentTimeMillis());

                    showImage(imagePath);

                } catch (Exception e) {
                    Log.e("file", "An exception happened when loading the image path: " + e.getMessage());
                }
            }
            else if (requestCode == CAMERA_REQUEST_CODE) {
                showImage(imagePath);
            }
            else {
                super.onActivityResult(requestCode, resultCode, data);
            }

        }
    }

    /**
     * Shows a given image and toggles the necessary buttons
     * @param imagePath the path to the image
     */
    private void showImage(String imagePath) {
        Bitmap rotatedBitmap = RecipeUtility.rotateBitmap(imagePath);

        if (rotatedBitmap == null)  {
            Toast.makeText(requireActivity(), "Ohno something went wrong trying to load the image",
                    Toast.LENGTH_LONG).show();
            return;
        }

        imageView.setImageBitmap(rotatedBitmap);
        imageView.setVisibility(View.VISIBLE);

        // Show & hide appropriate buttons
        addImageButton.setVisibility(View.GONE);
        changeImageButton.setVisibility(View.VISIBLE);
        removeImageButton.setVisibility(View.VISIBLE);
    }

    /**
     * Removes the current image and hides the appropriate buttons
     */
    private void removeImage() {
        removeCursorFromWidget();
        imageView.setImageBitmap(null);

        // Remove the actual image
        File image = new File(imagePath);
        if (image.exists()) {
            image.delete();
        }

        imagePath = null;

        imageView.setVisibility(View.GONE);

        // Show & hide appropriate buttons
        addImageButton.setVisibility(View.VISIBLE);
        changeImageButton.setVisibility(View.GONE);
        removeImageButton.setVisibility(View.GONE);
    }

    /**
     * Creates a dialog to add a category
     */
    private void createCategoryDialog() {
        removeCursorFromWidget();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        viewModel.setShowingCategoryDialog(true);
        // Get the layout
        View dialog_layout = View.inflate(requireActivity(), R.layout.add_category_dialog, null);

        // Create the text field in the alert dialog.
        final EditText categoryEditText = dialog_layout.findViewById(R.id.categoryEditText);

        categoryEditText.setText(viewModel.getTempCategory());

        categoryEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setTempCategory(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        builder.setTitle("Add category");
        builder.setMessage("Add a category here. A category can be 'Pasta' or 'Main course' for example.");

        builder.setPositiveButton("Add category", (dialog, id) -> {
            viewModel.setShowingCategoryDialog(false);
            String inputText = categoryEditText.getText().toString();
            // Only add if it's not empty and it doesn't exist yet
            if (inputText.isEmpty()) {
                return;
            }
            inputText = RecipeUtility.changeFirstLetterToCapital(inputText.trim());
            if (!generateCategorySet().contains(inputText)) {
                addCategoryChip(inputText);
            } else {
                Toast.makeText(requireActivity(), "This category already exists", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, id) -> viewModel.setShowingCategoryDialog(false));
        // Create and show the dialog
        categoryDialog = builder.create();
        categoryDialog.setView(dialog_layout);
        categoryDialog.setOnCancelListener(dialog -> viewModel.setShowingCategoryDialog(false));
        categoryDialog.show();
    }

}
