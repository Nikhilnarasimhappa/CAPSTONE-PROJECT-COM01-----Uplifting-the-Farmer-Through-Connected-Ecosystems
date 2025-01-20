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

public class BuyFertilizersActivity extends AppCompatActivity {

    private LinearLayout fertilizerContainer;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_fertilizers);

        fertilizerContainer = findViewById(R.id.fertilizerContainer);
        dbHelper = new DatabaseHelper(this);
        ImageView backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            Intent i = new Intent(BuyFertilizersActivity.this, FarmerHomeActivity.class);
            startActivity(i);
            finish();
        });

        loadFertilizers();
    }

    private void loadFertilizers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DatabaseHelper.COLUMN_NAME,
                DatabaseHelper.COLUMN_QUANTITY,
                DatabaseHelper.COLUMN_PRICE,
                DatabaseHelper.COLUMN_IMAGE_PATH
        };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_FERTILIZERS,
                projection,
                null, // No selection criteria
                null, // No selection args
                null, // No group by
                null, // No having
                null  // No order by
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUANTITY));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRICE));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_PATH));

                addFertilizerView(name, quantity, price, imagePath);
            }
            cursor.close();
        } else {
            Toast.makeText(this, "No fertilizers available", Toast.LENGTH_SHORT).show();
        }
    }

    private void addFertilizerView(String name, int quantity, double price, String imagePath) {
        // Inflate the fertilizer entry layout
        View fertilizerEntry = getLayoutInflater().inflate(R.layout.fertilizer_entry, fertilizerContainer, false);

        // Set fertilizer image
        ImageView fertilizerImage = fertilizerEntry.findViewById(R.id.fertilizerImageView);
        fertilizerImage.setImageURI(Uri.fromFile(new File(imagePath)));

        // Set fertilizer details
        TextView fertilizerDetails = fertilizerEntry.findViewById(R.id.fertilizerDetails);
        fertilizerDetails.setText("Name: " + name + "\nQuantity: " + quantity + "\nPrice: Rs." + price);

        // Handle Buy Button
        Button buyButton = fertilizerEntry.findViewById(R.id.buyButton);
        buyButton.setOnClickListener(v -> showQuantityDialog(name, quantity, price));

        // Add the fertilizer entry to the container
        fertilizerContainer.addView(fertilizerEntry);
    }

    private void showQuantityDialog(String fertilizerName, int availableQuantity, double fertilizerPrice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Purchase " + fertilizerName);

        // Input field for quantity
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter quantity");
        builder.setView(input);

        builder.setPositiveButton("Buy", (dialog, which) -> {
            String inputText = input.getText().toString();
            if (!inputText.isEmpty()) {
                int quantityToBuy = Integer.parseInt(inputText);
                handleBuyAction(fertilizerName, availableQuantity, fertilizerPrice, quantityToBuy);
            } else {
                Toast.makeText(this, "Please enter a valid quantity!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void handleBuyAction(String fertilizerName, int availableQuantity, double fertilizerPrice, int quantityToBuy) {
        if (quantityToBuy <= 0) {
            Toast.makeText(this, "Please enter a valid quantity!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quantityToBuy > availableQuantity) {
            Toast.makeText(this, "Only " + availableQuantity + " items are available!", Toast.LENGTH_SHORT).show();
            return;
        }

        int updatedQuantity = availableQuantity - quantityToBuy;
        double totalPrice = fertilizerPrice * quantityToBuy;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_QUANTITY, updatedQuantity);
        db.update(DatabaseHelper.TABLE_FERTILIZERS, values, DatabaseHelper.COLUMN_NAME + "=?", new String[]{fertilizerName});

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbHelper.logHistory(userId, "bought", "fertilizer", fertilizerName);

        Toast.makeText(this, "Successfully bought " + quantityToBuy + " " + fertilizerName + "(s) for Rs." + totalPrice, Toast.LENGTH_SHORT).show();

        if (updatedQuantity == 0) {
            db.delete(DatabaseHelper.TABLE_FERTILIZERS, DatabaseHelper.COLUMN_NAME + "=?", new String[]{fertilizerName});

            Toast.makeText(this, fertilizerName + " is now out of stock!", Toast.LENGTH_SHORT).show();
        }

        fertilizerContainer.removeAllViews(); // Clear the existing UI
        loadFertilizers(); // Reload fertilizer list after purchase
    }
}
