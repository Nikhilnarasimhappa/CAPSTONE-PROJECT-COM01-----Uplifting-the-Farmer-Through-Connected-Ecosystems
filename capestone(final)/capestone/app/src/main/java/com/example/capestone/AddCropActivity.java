package com.example.capestone;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AddCropActivity extends AppCompatActivity {
    private EditText etCropName, etCropQuantity, etCropPrice;
    private ImageView cropImageView;
    private Uri imageUri;
    private File savedImageFile;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_crop);

        dbHelper = new DatabaseHelper(this);

        etCropName = findViewById(R.id.etCropName);
        etCropQuantity = findViewById(R.id.etCropQuantity);
        etCropPrice = findViewById(R.id.etCropPrice);
        cropImageView = findViewById(R.id.cropImageView);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        ImageView backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            Intent i = new Intent(AddCropActivity.this, FarmerHomeActivity.class);
            startActivity(i);
            finish();
        });

        btnSelectImage.setOnClickListener(v -> selectImage());
        btnSubmit.setOnClickListener(v -> addCrop());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        imageUri = data.getData();
                        cropImageView.setImageURI(imageUri);
                    }
                }
            }
    );

    private void addCrop() {
        String cropName = etCropName.getText().toString();
        String cropQuantity = etCropQuantity.getText().toString();
        String cropPrice = etCropPrice.getText().toString();

        if (cropName.isEmpty() || cropQuantity.isEmpty() || cropPrice.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save image locally
        savedImageFile = saveImageToInternalStorage(imageUri);

        if (savedImageFile != null) {
            saveCropToLocalDatabase(cropName, cropQuantity, cropPrice, savedImageFile.getAbsolutePath());
        }
    }

    private File saveImageToInternalStorage(Uri uri) {
        try {
            // Get the Bitmap from the URI
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Define the file path and name
            File directory = getFilesDir();
            File file = new File(directory, "crop_" + System.currentTimeMillis() + ".jpg");

            // Save the Bitmap to the file
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            Toast.makeText(this, "Image saved locally: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void saveCropToLocalDatabase(String name, String quantity, String price, String imagePath) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_QUANTITY, quantity);
        values.put(DatabaseHelper.COLUMN_PRICE, price);
        values.put(DatabaseHelper.COLUMN_IMAGE_PATH, imagePath);

        long newRowId = dbHelper.getWritableDatabase().insert(DatabaseHelper.TABLE_CROPS, null, values);

        if (newRowId != -1) {
            // Log the event in the history table
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            dbHelper.logHistory(userId, "added", "crop", name); // Log the action

            Toast.makeText(this, "Crop added successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity after adding the crop
        } else {
            Toast.makeText(this, "Failed to add crop to database", Toast.LENGTH_SHORT).show();
        }
    }
}
