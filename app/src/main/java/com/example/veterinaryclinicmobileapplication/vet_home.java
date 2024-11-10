package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class vet_home extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;
    ImageButton logoutBtn;
    BarChart barChart;
    Map<String, Integer> appointmentData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vet_home);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> logout());

        barChart = findViewById(R.id.barChart);

        fetchAppointmentData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAppointmentData();
    }

    public void fetchAppointmentData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            db.collection("veterinarian")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String vetId = document.getString("id");
                                fetchAppointmentsForVet(vetId);
                            }
                        } else {
                            Toast.makeText(vet_home.this, "No veterinarian found for this email", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                    });
        }
    }

    public void fetchAppointmentsForVet(String vetId) {
        db.collection("booking")
                .whereEqualTo("veterinarian_id", vetId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String date = document.getString("date");

                        appointmentData.put(date, appointmentData.getOrDefault(date, 0) + 1);
                    }
                    updateAppointmentBarChart();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }

    public void updateAppointmentBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        Map<String, Integer> monthlyAppointments = new LinkedHashMap<>();

        String[] months = new String[]{
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };

        // Calculate total appointments
        for (Map.Entry<String, Integer> entry : appointmentData.entrySet()) {
            String[] yearMonth = entry.getKey().split("-");
            String monthYearKey = months[Integer.parseInt(yearMonth[1]) - 1] + " " + yearMonth[0];
            monthlyAppointments.put(monthYearKey, monthlyAppointments.getOrDefault(monthYearKey, 0) + entry.getValue());
        }

        // Create bar for each month-year
        int index = 0;
        for (Map.Entry<String, Integer> entry : monthlyAppointments.entrySet()) {
            entries.add(new BarEntry(index++, entry.getValue())); // y-value is the appointment count (already an integer)
        }

        // Create BarDataSet and BarData
        BarDataSet dataSet = new BarDataSet(entries, "Monthly Appointments");
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        // Show integers without decimal points
        barData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        // Set data to the chart
        barChart.setData(barData);
        barChart.setFitBars(true);

        // Customize X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new MonthYearValueFormatter(monthlyAppointments.keySet().toArray(new String[0])));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(monthlyAppointments.size());
        xAxis.setYOffset(-2f);
        xAxis.setDrawLabels(true);
        xAxis.setLabelRotationAngle(0);
        xAxis.setYOffset(10f);

        // Customize Y-axis
        barChart.setExtraBottomOffset(10f);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawLabels(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        barChart.invalidate();
    }

    public void goMyPatientPage(View view) {
        Intent intent = new Intent(this, vet_my_patient.class);
        ImageView goMyPatientBtn = findViewById(R.id.goPatientBtn);
        startActivity(intent);
    }

    public void goMyAppointmentPage(View view) {
        Intent intent = new Intent(this, vet_my_appointment.class);
        ImageView goMyAppointmentBtn = findViewById(R.id.goAppointmentBtn);
        startActivity(intent);
    }

    public void goHomePage(View view) {
        Intent intent = new Intent(this, vet_home.class);
        ImageView goHomeBtn = findViewById(R.id.logo);
        startActivity(intent);
    }

    public void goProfilePage(View view) {
        Intent intent = new Intent(this, vet_profile.class);
        ImageButton goProfileBtn = findViewById(R.id.goProfileBtn);
        startActivity(intent);
    }

    private void logout() {
        auth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(vet_home.this, intro.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}