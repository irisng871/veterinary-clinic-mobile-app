package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class forgotPassword extends AppCompatActivity {

    EditText email;
    Button doneBtn;
    ImageButton backBtn;
    FirebaseAuth auth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        doneBtn = findViewById(R.id.doneBtn);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackLoginPage());

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = email.getText().toString();

                if (TextUtils.isEmpty(Email)) {
                    Toast.makeText(forgotPassword.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!Email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    Toast.makeText(forgotPassword.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                }

                auth.sendPasswordResetEmail(Email)
                        .addOnCompleteListener(forgotPassword.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(forgotPassword.this, "Reset password email is sent successfully", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(getApplicationContext(), login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(forgotPassword.this, "Failed to send email", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }

    private void goBackLoginPage() {
        finish();
    }
}
