package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class vet_home extends AppCompatActivity {

    FirebaseAuth Auth;

    FirebaseFirestore db;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vet_home);
    }

    public void goHomePage(View view){
        Intent intent = new Intent(this, home.class);
        ImageView goHomeBtn = findViewById(R.id.logo);
        startActivity(intent);
    }

    public void goProfilePage(View view){
        Intent intent = new Intent(this, vet_profile.class);
        ImageButton goProfileBtn = findViewById(R.id.goProfileBtn);
        startActivity(intent);
    }
}
