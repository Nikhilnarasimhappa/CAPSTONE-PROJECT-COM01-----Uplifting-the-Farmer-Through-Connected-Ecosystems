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

public class RentLandActivity extends AppCompatActivity {

    private EditText etLandSize, etLandPrice, etLandLocation;
    private ImageView landImageView;
    private Uri imageUri;
    private File savedImageFile;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_land);

        dbHelper = new DatabaseHelper(this);

        etLandSize = findViewById(R.id.etLandSize);
        etLandPrice = findViewById(R.id.etLandPrice);
        etLandLocation = findViewById(R.id.etLandLocation);
        landImageView = findViewById(R.id.landImageView);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        ImageView btnBack = findViewById(R.id.backButton);

        btnBack.setOnClickListener(v -> finish());
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnSubmit.setOnClickListener(v -> addLand());
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
                        landImageView.setImageURI(imageUri);
                    }
                }
            }
    );

    private void addLand() {
        String landSize = etLandSize.getText().toString();
        String landPrice = etLandPrice.getText().toString();
        String landLocation = etLandLocation.getText().toString();

        if (landSize.isEmpty() || landPrice.isEmpty() || landLocation.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save image locally
        savedImageFile = saveImageToInternalStorage(imageUri);

        if (savedImageFile != null) {
            saveLandToLocalDatabase(landSize, landPrice, landLocation, savedImageFile.getAbsolutePath());
        }
    }

    private File saveImageToInternalStorage(Uri uri) {
        try {
            // Get the Bitmap from the URI
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Define the file path and name
            File directory = getFilesDir();
            File file = new File(directory, "land_" + System.currentTimeMillis() + ".jpg");

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

    private void saveLandToLocalDatabase(String size, String price, String location, String imagePath) {
        ContentValues values = new ContentValues();
        values.put("size", size);
        values.put("price", price);
        values.put("location", location);
        values.put("imagepath", imagePath);

        long newRowId = dbHelper.getWritableDatabase().insert(DatabaseHelper.TABLE_LAND, null, values);

        if (newRowId != -1) {
            // Log the event in the history table
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            dbHelper.logHistory(userId, "added", "land", location); // Log the action

            Toast.makeText(this, "Land added successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity after adding the land
        } else {
            Toast.makeText(this, "Failed to add land to database", Toast.LENGTH_SHORT).show();
        }
    }
}
