package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class staff_login extends AppCompatActivity {
    EditText userEmail, userPassword;
    Button loginBtn, backBtn;
    FirebaseAuth auth;
    FirebaseFirestore db;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userEmail = findViewById(R.id.email);
        userPassword = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackIntroPage());

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password;

                email = String.valueOf(userEmail.getText());
                password = String.valueOf(userPassword.getText());

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(staff_login.this, "Please enter your email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean isStaff = email.endsWith("@staff.com");

                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    if (isStaff) {
                                        Intent intent = new Intent(getApplicationContext(), staff_home.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(staff_login.this, "Only staff are allow to login", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(staff_login.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    public void goRegisterPage(View view){
        Intent intent = new Intent(this, staff_register.class);
        TextView goRegisterBtn = findViewById(R.id.goRegisterBtn);
        startActivity(intent);
    }

    public void goBackIntroPage(){
        finish();
    }
}
