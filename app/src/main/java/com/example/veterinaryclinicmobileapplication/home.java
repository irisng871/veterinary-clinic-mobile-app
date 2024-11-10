package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class home extends AppCompatActivity {

    RecyclerView appointmentHistoryRecyclerView;
    AppointmentAdapter appointmentAdapter;
    List<Appointment> appointmentList;
    FirebaseAuth auth;
    FirebaseFirestore db;
    String vetId;
    ImageButton aiChatbot, logoutBtn;
    BarChart barChart;
    Map<String, Double> monthlyExpenses = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> logout());

        aiChatbot = findViewById(R.id.aiChatbot);
        aiChatbot.setOnClickListener(v -> {
            Intent intent = new Intent(home.this, ai_chatbot.class);
            startActivity(intent);
        });

        barChart = findViewById(R.id.barChart);

        appointmentHistoryRecyclerView = findViewById(R.id.appointmentHistoryRecyclerView);
        appointmentHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        appointmentList = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(this, appointmentList, "history");
        appointmentHistoryRecyclerView.setAdapter(appointmentAdapter);

        loadAppointments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointments();
    }

    public void loadAppointments() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("pet_owner").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String petOwnerId = documentSnapshot.getString("id");

                        if (petOwnerId != null) {
                            db.collection("booking")
                                    .whereEqualTo("pet_owner_id", petOwnerId)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            appointmentList.clear();

                                            // Create a list of booking documents
                                            List<QueryDocumentSnapshot> bookingDocs = new ArrayList<>();

                                            // Populate the list with QueryDocumentSnapshots
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                bookingDocs.add(document);
                                            }

                                            for (QueryDocumentSnapshot document : bookingDocs) {
                                                String petId = document.getString("pet_id");
                                                String date = document.getString("date");
                                                String time = document.getString("time");
                                                String bookingId = document.getId();

                                                fetchPetName(petId, date, time, vetId, bookingId);
                                            }
                                            Log.d("Appointment", "Bookings loaded successfully.");
                                        } else {
                                            Log.e("Appointment", "Error getting documents: ", task.getException());
                                        }
                                    });
                        } else {
                            Log.d("Appointment", "Pet Owner ID not found for this user.");
                        }
                    } else {
                        Log.d("Appointment", "Pet owner document not found for this user.");
                    }
                })
                .addOnFailureListener(e -> Log.e("Appointment", "Error fetching pet owner document: ", e));
    }

    public void fetchPetName(String petId, String date, String time, String vetId, String bookingId) {
        db.collection("pet")
                .document(petId)
                .get()
                .addOnSuccessListener(petDocument -> {
                    if (petDocument.exists()) {
                        String petName = petDocument.getString("name");

                        Appointment appointment = new Appointment(petName, date, time, vetId, bookingId);
                        appointmentList.add(appointment);

                        fetchPaymentAmount(bookingId, date);

                        sortAppointmentsDescending();

                        appointmentAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("Appointment", "Pet document not found for petId: " + petId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Appointment", "Error fetching pet document: ", e));
    }

    public void sortAppointmentsDescending() {
        appointmentList.sort((a1, a2) -> a2.getAppointmentId().compareTo(a1.getAppointmentId()));
    }

    public void fetchPaymentAmount(String bookingId, String date) {
        db.collection("medical_record")
                .whereEqualTo("appointment_id", bookingId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Object paymentAmountObj = document.get("payment_amount");
                            Double paymentAmount = null;

                            if (paymentAmountObj instanceof Number) {
                                paymentAmount = ((Number) paymentAmountObj).doubleValue();
                            } else if (paymentAmountObj instanceof String) {
                                try {
                                    paymentAmount = Double.parseDouble((String) paymentAmountObj);
                                } catch (NumberFormatException e) {
                                    Log.e("Expense", "Invalid payment amount format for bookingId: " + bookingId, e);
                                }
                            }

                            if (paymentAmount != null) {
                                processExpense(date, paymentAmount);
                            } else {
                                Log.d("Expense", "Payment amount is null for bookingId: " + bookingId);
                            }
                        }
                    } else {
                        Log.e("Expense", "Error getting payment amount: ", task.getException());
                    }
                });
    }

    public void processExpense(String date, double paymentAmount) {
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            calendar.setTime(sdf.parse(date));
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);

            String monthKey = year + "-" + month;
            monthlyExpenses.put(monthKey, monthlyExpenses.getOrDefault(monthKey, 0.00) + paymentAmount);

            updateBarChart();

        } catch (ParseException e) {
            Log.e("Expense", "Error parsing date: " + date, e);
        }
    }

    public void updateBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        Map<String, Double> monthlyTotals = new LinkedHashMap<>(); // Maintain insertion order

        String[] months = new String[]{
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };

        // Calculate total expenses
        for (Map.Entry<String, Double> entry : monthlyExpenses.entrySet()) {
            String[] yearMonth = entry.getKey().split("-");
            String monthYearKey = months[Integer.parseInt(yearMonth[1]) - 1] + " " + yearMonth[0];
            monthlyTotals.put(monthYearKey, monthlyTotals.getOrDefault(monthYearKey, 0.00) + entry.getValue());
        }

        // Create BarEntry for each month-year
        int index = 0;
        for (Map.Entry<String, Double> entry : monthlyTotals.entrySet()) {
            entries.add(new BarEntry(index++, entry.getValue().floatValue()));
        }

        // Create BarDataSet and BarData
        BarDataSet dataSet = new BarDataSet(entries, "Monthly Expenses");
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        // Set the custom value formatter to the dataset
        dataSet.setValueFormatter(new DecimalValueFormatter());

        // Set data to the chart
        barChart.setData(barData);
        barChart.setFitBars(true);

        // Customize X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new MonthYearValueFormatter(monthlyTotals.keySet().toArray(new String[0])));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(monthlyTotals.size());
        xAxis.setYOffset(-2f);
        barChart.setExtraBottomOffset(10f);

        xAxis.setDrawLabels(true);
        xAxis.setLabelRotationAngle(0);
        xAxis.setYOffset(10f);

        // Customize Y-axis
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawLabels(false);
        barChart.getAxisRight().setEnabled(false);

        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        barChart.invalidate();
    }

    public class DecimalValueFormatter extends ValueFormatter {
        private final DecimalFormat decimalFormat;

        public DecimalValueFormatter() {
            decimalFormat = new DecimalFormat("#.00");
        }

        @Override
        public String getPointLabel(Entry entry) {
            return decimalFormat.format(entry.getY());
        }
    }

    // Custom ValueFormatter for X-axis labels
    public class MonthYearValueFormatter extends ValueFormatter {
        private final String[] months;

        public MonthYearValueFormatter(String[] months) {
            this.months = months;
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            int index = Math.round(value);
            return (index >= 0 && index < months.length) ? months[index] : "";
        }
    }

    public void goAppointmentPage(View view) {
        Intent intent = new Intent(this, booking_select_vet.class);
        Button appointmentBtn = findViewById(R.id.appointmentBtn);
        startActivity(intent);
    }

    public void goHomePage(View view) {
        Intent intent = new Intent(this, home.class);
        ImageButton goHomeBtn = findViewById(R.id.goHomeBtn);
        startActivity(intent);
    }

    public void goMyPetPage(View view) {
        Intent intent = new Intent(this, my_pet.class);
        ImageButton goMyPetBtn = findViewById(R.id.goMyPetBtn);
        startActivity(intent);
    }

    public void goRecommendAdoptablePetPage(View view) {
        Intent intent = new Intent(this, recommend_adoptable_pet.class);
        ImageButton goPetShelterBtn = findViewById(R.id.goPetShelterBtn);
        startActivity(intent);
    }

    public void goCalendarPage(View view) {
        Intent intent = new Intent(this, calendar.class);
        ImageButton goCalendarBtn = findViewById(R.id.goCalendarBtn);
        startActivity(intent);
    }

    public void goProfilePage(View view) {
        Intent intent = new Intent(this, my_profile.class);
        ImageButton goProfileBtn = findViewById(R.id.goProfileBtn);
        startActivity(intent);
    }

    public void logout() {
        auth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(home.this, intro.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
