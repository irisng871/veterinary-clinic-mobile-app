package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class staff_home extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseFirestore db;
    ImageButton logoutBtn;
    PieChart pieChart;
    TextView catAmount, dogAmount;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_home);

        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> logout());

        pieChart = findViewById(R.id.pieChart);
        db = FirebaseFirestore.getInstance();

        fetchPetData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchPetData();
    }

    public void fetchPetData() {
        db.collection("adoptable_pet")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int occupied = 0;
                        int totalCapacity = 100;
                        int catCount = 0;
                        int dogCount = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            occupied++;
                            String petType = document.getString("type");
                            if ("cat".equalsIgnoreCase(petType)) {
                                catCount++;
                            } else if ("dog".equalsIgnoreCase(petType)) {
                                dogCount++;
                            }
                        }

                        int available = totalCapacity - occupied;
                        setupPieChart(occupied, available);
                        displayPetCounts(catCount, dogCount);
                    } else {
                        Log.w("FirestoreError", "Error getting documents.", task.getException());
                    }
                });
    }

    public void setupPieChart(int occupied, int available) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(occupied, "Occupied"));
        entries.add(new PieEntry(available, "Available"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{Color.parseColor("#dda7a7"), Color.parseColor("#a7ddaa")});
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(16f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setHoleColor(Color.TRANSPARENT);

        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelColor(Color.BLACK);

        pieChart.invalidate();
    }

    public void displayPetCounts(int catCount, int dogCount) {
        catAmount = findViewById(R.id.cat);
        dogAmount = findViewById(R.id.dog);

        catAmount.setText(String.valueOf(catCount));
        dogAmount.setText(String.valueOf(dogCount));
    }

    public void goHomePage(View view){
        Intent intent = new Intent(this, staff_home.class);
        startActivity(intent);
    }

    public void goPetfolioPage(View view){
        Intent intent = new Intent(this, staff_petfolio.class);
        startActivity(intent);
    }

    public void goProfilePage(View view){
        Intent intent = new Intent(this, staff_profile.class);
        startActivity(intent);
    }

    public void logout() {
        auth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(staff_home.this, intro.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
