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

public class add_schedule1 extends AppCompatActivity {

    CalendarView date;
    long selectedDate;
    int selectedHour = 8;
    int selectedMinute = 0;
    TimePicker time;
    Button nxtBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_schedule1);

        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        nxtBtn = findViewById(R.id.nxtBtn);

        date.setMinDate(System.currentTimeMillis());

        time.setHour(selectedHour);
        time.setMinute(selectedMinute);

        // CalendarView date selection
        date.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Store selected date
            selectedDate = new GregorianCalendar(year, month, dayOfMonth).getTimeInMillis();
        });

        // TimePicker time selection
        time.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            // Update selectedHour and selectedMinute when time is changed
            selectedHour = hourOfDay;
            selectedMinute = minute;

            if ((hourOfDay < 8 || (hourOfDay == 8 && minute < 0)) || (hourOfDay > 18 || (hourOfDay == 18 && minute > 0))) {
                time.setHour(8);
                time.setMinute(0);
                selectedHour = 8;
                selectedMinute = 0;
            }
        });

        nxtBtn.setOnClickListener(v -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = sdf.format(selectedDate);

            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s",
                    (selectedHour % 12 == 0 ? 12 : selectedHour % 12),
                    selectedMinute,
                    (selectedHour >= 12 ? "PM" : "AM"));

            Intent intent = new Intent(add_schedule1.this, add_schedule2.class);
            intent.putExtra("selectedDate", formattedDate);
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