package com.example.capestone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class GeneralUserHomeActivity extends AppCompatActivity {

    private TextView tvUserName;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_user_home);

        tvUserName = findViewById(R.id.tvUserName);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get the current user
        String userId = auth.getCurrentUser().getUid();

        // Fetch the user's data from Firestore
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Get the name from Firestore and set it to the TextView
                            String name = document.getString("name");
                            tvUserName.setText("Welcome, " + name);
                        }
                    } else {
                        // Handle error (optional)
                        tvUserName.setText("Welcome, User");
                    }
                });

        Button btnViewBuyCrop = findViewById(R.id.btnViewBuyCrop);
        Button btnViewRentLand = findViewById(R.id.btnViewRentLand);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnViewBuyCrop.setOnClickListener(v -> startActivity(new Intent(this, ViewBuyCropActivity.class)));
        btnViewRentLand.setOnClickListener(v -> startActivity(new Intent(this, ViewRentLandActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, UserHistoryActivity.class)));
        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }
}
