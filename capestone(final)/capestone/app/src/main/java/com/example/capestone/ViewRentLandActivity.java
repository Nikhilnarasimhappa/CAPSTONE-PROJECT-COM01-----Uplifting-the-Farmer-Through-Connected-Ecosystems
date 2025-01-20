package com.example.capestone;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;

public class ViewRentLandActivity extends AppCompatActivity {

    private LinearLayout landContainer;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rent_land);

        landContainer = findViewById(R.id.landContainer);
        dbHelper = new DatabaseHelper(this);
        ImageView backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            Intent i = new Intent(ViewRentLandActivity.this, GeneralUserHomeActivity.class);
            startActivity(i);
            finish();
        });

        loadLand();
    }

    private void loadLand() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DatabaseHelper.COLUMN_SIZE,
                DatabaseHelper.COLUMN_PRICE,
                DatabaseHelper.COLUMN_LOCATION,
                DatabaseHelper.COLUMN_IMAGE_PATH
        };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_LAND, // Use the correct table name
                projection,
                null, // No selection criteria
                null, // No selection args
                null, // No group by
                null, // No having
                null  // No order by
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String size = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SIZE));
                String price = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRICE));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_PATH));

                addLandView(size, price, location, imagePath);
            }
            cursor.close();
        } else {
            Toast.makeText(this, "No land available for rent", Toast.LENGTH_SHORT).show();
        }
    }

    private void addLandView(String size, String price, String location, String imagePath) {
        View landEntry = getLayoutInflater().inflate(R.layout.land_entry, landContainer, false);

        ImageView landImage = landEntry.findViewById(R.id.landImageView);
        landImage.setImageURI(Uri.fromFile(new File(imagePath)));

        TextView landDetails = landEntry.findViewById(R.id.landDetails);
        landDetails.setText("Size: " + size + " acres\nPrice: " + price + " per acre\nLocation: " + location);

        Button rentButton = landEntry.findViewById(R.id.rentButton);
        rentButton.setOnClickListener(v -> showRentDialog(location, price));

        landContainer.addView(landEntry);
    }

    private void showRentDialog(String location, String price) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rent land in " + location);

        // Input field for quantity (size in acres)
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter size in acres");
        builder.setView(input);

        builder.setPositiveButton("Rent", (dialog, which) -> {
            String inputText = input.getText().toString();
            if (!inputText.isEmpty()) {
                int sizeToRent = Integer.parseInt(inputText);
                handleRentAction(location, price, sizeToRent);
            } else {
                Toast.makeText(this, "Please enter a valid size!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void handleRentAction(String location, String price, int sizeToRent) {
        if (sizeToRent <= 0) {
            Toast.makeText(this, "Please enter a valid size!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log the "rent" action for the current user
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID
        dbHelper.logHistory(userId, "bought", "land", location); // Log the "rent" action

        // Show a success message
        double totalPrice = Double.parseDouble(price) * sizeToRent;
        Toast.makeText(this, "Successfully rented " + sizeToRent + " acres of land in " + location + " for Rs." + totalPrice, Toast.LENGTH_SHORT).show();

        // Optional: Update database or UI after the rental if needed
        // Here you can update the land availability in the database if required (not shown here)

        landContainer.removeAllViews(); // Clear the existing UI
        loadLand(); // Reload the land list
    }
}
