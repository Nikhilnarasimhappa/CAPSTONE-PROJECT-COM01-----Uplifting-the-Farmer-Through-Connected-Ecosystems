package com.example.capestone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class FarmerHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_home);

        Button btnAddSellCrop = findViewById(R.id.btnAddSellCrop);
        Button btnRentLand = findViewById(R.id.btnRentLand);
        Button btnBuyMachinery = findViewById(R.id.btnBuyMachinery);
        Button btnBuyFertilizer = findViewById(R.id.btnBuyFertilizer);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnLogout = findViewById(R.id.btnLogout);


        btnAddSellCrop.setOnClickListener(v -> startActivity(new Intent(this, AddCropActivity.class)));
        btnRentLand.setOnClickListener(v -> startActivity(new Intent(this, RentLandActivity.class)));
        btnBuyMachinery.setOnClickListener(v -> startActivity(new Intent(this, BuyMachineryActivity.class)));
        btnBuyFertilizer.setOnClickListener(v -> startActivity(new Intent(this, BuyFertilizersActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, FarmerHistoryActivity.class)));
        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }
}
