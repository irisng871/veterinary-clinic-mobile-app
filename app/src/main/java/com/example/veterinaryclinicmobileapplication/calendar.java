package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public class calendar extends AppCompatActivity {

    CalendarView date;
    long selectedDate;
    int selectedHour = 8;  // Default hour
    int selectedMinute = 0; // Default minute
    TimePicker time;
    Button nxtBtn;
    FirebaseAuth Auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        nxtBtn = findViewById(R.id.nxtBtn);

        date.setMinDate(System.currentTimeMillis());

        time.setHour(selectedHour);
        time.setMinute(selectedMinute);

        // CalendarView date selection listener
        date.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Store the selected date
            selectedDate = new GregorianCalendar(year, month, dayOfMonth).getTimeInMillis();
        });

        // TimePicker time selection listener
        time.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            // Update selectedHour and selectedMinute when the time is changed
            selectedHour = hourOfDay;
            selectedMinute = minute;

            // Restrict time selection between 8:00 AM and 6:00 PM
            if ((hourOfDay < 8 || (hourOfDay == 8 && minute < 0)) || (hourOfDay > 18 || (hourOfDay == 18 && minute > 0))) {
                // If out of range, reset to 8:00 AM
                time.setHour(8);
                time.setMinute(0);
                selectedHour = 8;  // Reset selectedHour
                selectedMinute = 0; // Reset selectedMinute
            }
        });

        nxtBtn.setOnClickListener(v -> {
            // Format the selected date into a readable string
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = sdf.format(selectedDate);

            // Format the selected time to a 12-hour format (e.g., "3:00 AM")
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s",
                    (selectedHour % 12 == 0 ? 12 : selectedHour % 12),
                    selectedMinute,
                    (selectedHour >= 12 ? "PM" : "AM"));

            // Pass formatted date and time to the next activity
            Intent intent = new Intent(calendar.this, add_schedule.class);
            intent.putExtra("selectedDate", formattedDate);  // Now a String
            intent.putExtra("selectedTime", formattedTime);
            startActivity(intent);
        });
    }

    public void goMyPetPage(View view){
        Intent intent = new Intent(this, my_pet.class);
        startActivity(intent);
    }

    public void goHomePage(View view){
        Intent intent = new Intent(this, home.class);
        startActivity(intent);
    }

    public void goProfilePage(View view){
        Intent intent = new Intent(this, my_profile.class);
        startActivity(intent);
    }
}