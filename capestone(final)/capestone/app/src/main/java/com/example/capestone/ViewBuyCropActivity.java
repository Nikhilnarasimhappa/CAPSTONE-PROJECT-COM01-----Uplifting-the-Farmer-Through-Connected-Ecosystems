package com.example.capestone;

import android.content.ContentValues;
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

public class ViewBuyCropActivity extends AppCompatActivity {

    private LinearLayout cropContainer;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_buy_crop);

        cropContainer = findViewById(R.id.cropContainer);
        dbHelper = new DatabaseHelper(this);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent i = new Intent(ViewBuyCropActivity.this, GeneralUserHomeActivity.class);
            startActivity(i);
            finish();
        });

        loadCrops();
    }

    private void loadCrops() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_CROPS,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUANTITY));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRICE));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_PATH));

                addCropView(name, quantity, price, imagePath);
            }
            cursor.close();
        } else {
            Toast.makeText(this, "No crops available", Toast.LENGTH_SHORT).show();
        }
    }

    private void addCropView(String name, int quantity, double price, String imagePath) {
        View cropEntry = getLayoutInflater().inflate(R.layout.crop_entry, cropContainer, false);

        ImageView cropImage = cropEntry.findViewById(R.id.cropImage);
        cropImage.setImageURI(Uri.fromFile(new File(imagePath)));

        TextView cropDetails = cropEntry.findViewById(R.id.cropDetails);
        cropDetails.setText("Name: " + name + "\nQuantity: " + quantity + "\nPrice: Rs." + price);

        Button buyButton = cropEntry.findViewById(R.id.buyButton);
        buyButton.setOnClickListener(v -> showQuantityDialog(name, quantity, price));

        cropContainer.addView(cropEntry);
    }

    private void showQuantityDialog(String cropName, int cropQuantity, double cropPrice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Purchase " + cropName);

        // Input field for quantity
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter quantity");
        builder.setView(input);

        builder.setPositiveButton("Buy", (dialog, which) -> {
            String inputText = input.getText().toString();
            if (!inputText.isEmpty()) {
                int quantityToBuy = Integer.parseInt(inputText);
                handleBuyAction(cropName, cropQuantity, cropPrice, quantityToBuy);
            } else {
                Toast.makeText(this, "Please enter a valid quantity!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void handleBuyAction(String cropName, int availableQuantity, double cropPrice, int quantityToBuy) {
        if (quantityToBuy <= 0) {
            Toast.makeText(this, "Please enter a valid quantity!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quantityToBuy > availableQuantity) {
            Toast.makeText(this, "Only " + availableQuantity + " items are available!", Toast.LENGTH_SHORT).show();
            return;
        }

        int updatedQuantity = availableQuantity - quantityToBuy;
        double totalPrice = cropPrice * quantityToBuy;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_QUANTITY, updatedQuantity);
        db.update(DatabaseHelper.TABLE_CROPS, values, DatabaseHelper.COLUMN_NAME + "=?", new String[]{cropName});

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbHelper.logHistory(userId, "bought", "crop", cropName);

        Toast.makeText(this, "Successfully bought " + quantityToBuy + " " + cropName + "(s) for Rs." + totalPrice, Toast.LENGTH_SHORT).show();

        if (updatedQuantity == 0) {
            db.delete(DatabaseHelper.TABLE_CROPS, DatabaseHelper.COLUMN_NAME + "=?", new String[]{cropName});

            Toast.makeText(this, cropName + " is now out of stock!", Toast.LENGTH_SHORT).show();
        }
        cropContainer.removeAllViews(); // Clear the existing UI
        loadCrops();
    }
}
