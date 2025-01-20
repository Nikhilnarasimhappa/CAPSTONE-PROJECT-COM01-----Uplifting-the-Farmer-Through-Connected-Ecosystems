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

public class BuyMachineryActivity extends AppCompatActivity {

    private LinearLayout machineryContainer;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_machinery);

        machineryContainer = findViewById(R.id.machineryContainer);
        dbHelper = new DatabaseHelper(this);
        ImageView backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            Intent i = new Intent(BuyMachineryActivity.this, FarmerHomeActivity.class);
            startActivity(i);
            finish();
        });

        loadMachinery();
    }

    private void loadMachinery() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DatabaseHelper.COLUMN_NAME,
                DatabaseHelper.COLUMN_QUANTITY,
                DatabaseHelper.COLUMN_PRICE,
                DatabaseHelper.COLUMN_IMAGE_PATH
        };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_MACHINERY,
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

                addMachineryView(name, quantity, price, imagePath);
            }
            cursor.close();
        } else {
            Toast.makeText(this, "No machinery available", Toast.LENGTH_SHORT).show();
        }
    }

    private void addMachineryView(String name, int quantity, double price, String imagePath) {
        // Inflate the machinery entry layout
        View machineryEntry = getLayoutInflater().inflate(R.layout.machinery_entry, machineryContainer, false);

        // Set machinery image
        ImageView machineryImage = machineryEntry.findViewById(R.id.machineryImageView);
        machineryImage.setImageURI(Uri.fromFile(new File(imagePath)));

        // Set machinery details
        TextView machineryDetails = machineryEntry.findViewById(R.id.machineryDetails);
        machineryDetails.setText("Name: " + name + "\nQuantity: " + quantity + "\nPrice: Rs." + price);

        // Handle Buy Button
        Button buyButton = machineryEntry.findViewById(R.id.buyButton);
        buyButton.setOnClickListener(v -> showQuantityDialog(name, quantity, price));

        // Add the machinery entry to the container
        machineryContainer.addView(machineryEntry);
    }

    private void showQuantityDialog(String machineryName, int availableQuantity, double machineryPrice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Purchase " + machineryName);

        // Input field for quantity
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter quantity");
        builder.setView(input);

        builder.setPositiveButton("Buy", (dialog, which) -> {
            String inputText = input.getText().toString();
            if (!inputText.isEmpty()) {
                int quantityToBuy = Integer.parseInt(inputText);
                handleBuyAction(machineryName, availableQuantity, machineryPrice, quantityToBuy);
            } else {
                Toast.makeText(this, "Please enter a valid quantity!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void handleBuyAction(String machineryName, int availableQuantity, double machineryPrice, int quantityToBuy) {
        if (quantityToBuy <= 0) {
            Toast.makeText(this, "Please enter a valid quantity!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quantityToBuy > availableQuantity) {
            Toast.makeText(this, "Only " + availableQuantity + " items are available!", Toast.LENGTH_SHORT).show();
            return;
        }

        int updatedQuantity = availableQuantity - quantityToBuy;
        double totalPrice = machineryPrice * quantityToBuy;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_QUANTITY, updatedQuantity);
        db.update(DatabaseHelper.TABLE_MACHINERY, values, DatabaseHelper.COLUMN_NAME + "=?", new String[]{machineryName});

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbHelper.logHistory(userId, "bought", "machinery", machineryName);

        Toast.makeText(this, "Successfully bought " + quantityToBuy + " " + machineryName + "(s) for Rs." + totalPrice, Toast.LENGTH_SHORT).show();

        if (updatedQuantity == 0) {
            db.delete(DatabaseHelper.TABLE_MACHINERY, DatabaseHelper.COLUMN_NAME + "=?", new String[]{machineryName});

            Toast.makeText(this, machineryName + " is now out of stock!", Toast.LENGTH_SHORT).show();
        }

        machineryContainer.removeAllViews(); // Clear the existing UI
        loadMachinery(); // Reload machinery list after purchase
    }
}
