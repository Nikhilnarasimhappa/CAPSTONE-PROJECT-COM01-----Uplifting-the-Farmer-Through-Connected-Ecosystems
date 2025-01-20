package com.example.capestone;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SellerHistoryActivity extends AppCompatActivity {

    private TableLayout historyTable; // Reference to the TableLayout
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_history);

        // Initialize UI components
        ImageView backButton = findViewById(R.id.backButton);
        historyTable = findViewById(R.id.historyTable);
        dbHelper = new DatabaseHelper(this);

        // Back button functionality
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SellerHistoryActivity.this, FertilizerSellerHomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Get the current user's ID using Firebase Authentication
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadSellerHistory(userId); // Load the seller's history
    }

    private void loadSellerHistory(String userId) {
        Cursor cursor = dbHelper.getHistory(userId); // Fetch history for the seller

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String actionType = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ACTION_TYPE));
                String itemType = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_TYPE));
                String itemName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_NAME));
                String timestamp = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMESTAMP));

                // Only display relevant actions and item types
                if ((actionType.equals("added") || actionType.equals("bought")) &&
                        (itemType.equals("fertilizer") || itemType.equals("machinery") || itemType.equals("crop") || itemType.equals("land"))) {
                    addHistoryEntry(actionType, itemName, timestamp); // Add a row to the table
                }
            }
            cursor.close();
        } else {
            Toast.makeText(this, "No history found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addHistoryEntry(String actionType, String itemName, String timestamp) {
        // Inflate the custom history entry layout
        TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.history_entry, null);

        // Find the TextViews in the row layout
        TextView actionView = row.findViewById(R.id.tvActionType);
        TextView itemView = row.findViewById(R.id.tvItemType);
        TextView dateView = row.findViewById(R.id.tvTimestamp);

        // Set the data for the row
        actionView.setText(actionType);
        itemView.setText(itemName);
        dateView.setText(timestamp);

        // Add the populated row to the table
        historyTable.addView(row);
    }
}
