package com.example.capestone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class FertilizerSellerHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fertilizer_seller_home);

        Button btnAddSellFertilizer = findViewById(R.id.btnAddSellFertilizer);
        Button btnAddSellMachinery = findViewById(R.id.btnAddSellMachinery);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnLogout = findViewById(R.id.btnLogout);


        btnAddSellFertilizer.setOnClickListener(v -> startActivity(new Intent(this, AddSellFertilizerActivity.class)));
        btnAddSellMachinery.setOnClickListener(v -> startActivity(new Intent(this, AddSellMachineryActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, SellerHistoryActivity.class)));
        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

    }
}