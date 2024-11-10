package com.example.veterinaryclinicmobileapplication;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class privacyPolicy extends AppCompatActivity {

    ImageButton backBtn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_policy);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackRegisterPage());
    }

    public void goBackRegisterPage(){
        finish();
    }
}
