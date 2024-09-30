package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

public class booking extends AppCompatActivity {

    CalendarView date;
    long selectedDate;
    Button bookBtn;
    FirebaseAuth Auth;
    FirebaseFirestore db;

    // List of predefined time slots for the day
    ArrayList<String> timeSlots;
    TextView slotDisplay; // To display the slots

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking);

        String selectedVetName = getIntent().getStringExtra("selectedVetName");

        if (selectedVetName != null) {
            Log.d("Booking", "Selected veterinarian received: " + selectedVetName);
        } else {
            Log.e("Booking", "No veterinarian name received.");
        }

        date = findViewById(R.id.date);
        bookBtn = findViewById(R.id.bookBtn);
        slotDisplay = findViewById(R.id.slotDisplay); // Assuming there's a TextView to show the slots

        // Initialize predefined time slots for the day
        timeSlots = new ArrayList<>();
        timeSlots.add("9:00am");
        timeSlots.add("10:30am");
        timeSlots.add("12:00pm");
        timeSlots.add("1:30pm");
        timeSlots.add("3:00pm");
        timeSlots.add("4:30pm");
        timeSlots.add("6:00pm");

        date.setMinDate(System.currentTimeMillis());

        // CalendarView date selection listener
        date.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Store the selected date
            selectedDate = new GregorianCalendar(year, month, dayOfMonth).getTimeInMillis();

            // Display slots for the selected date
            displayAvailableSlots();
        });

        bookBtn.setOnClickListener(v -> {
            // Format the selected date into a readable string
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = sdf.format(selectedDate);

            // Confirm booking process here (e.g., save to Firestore)
            Toast.makeText(this, "Booked for " + formattedDate, Toast.LENGTH_SHORT).show();
        });
    }

    // Method to display available slots for the selected date
    private void displayAvailableSlots() {
        StringBuilder slots = new StringBuilder();
        for (String slot : timeSlots) {
            slots.append(slot).append("\n");
        }
        slotDisplay.setText(slots.toString()); // Update the TextView with available slots
    }
}