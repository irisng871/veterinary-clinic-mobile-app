package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class home extends AppCompatActivity {

    FirebaseAuth Auth;

    FirebaseFirestore db;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
    }

    public void goBookingPage(View view){
        Intent intent = new Intent(this, booking_select_vet.class);
        Button goBookingBtn = findViewById(R.id.bookingBtn);
        startActivity(intent);
    }

    public void goMyPetPage(View view){
        Intent intent = new Intent(this, my_pet.class);
        ImageButton goMyPetBtn = findViewById(R.id.goMyPetBtn);
        startActivity(intent);
    }

    public void goHomePage(View view){
        Intent intent = new Intent(this, home.class);
        ImageView goHomeBtn = findViewById(R.id.logo);
        startActivity(intent);
    }

    public void goCalendarPage(View view){
        Intent intent = new Intent(this, calendar.class);
        ImageButton goCalendarBtn = findViewById(R.id.goCalendarBtn);
        startActivity(intent);
    }

    public void goProfilePage(View view){
        Intent intent = new Intent(this, my_profile.class);
        ImageButton goProfileBtn = findViewById(R.id.goProfileBtn);
        startActivity(intent);
    }
}
