package com.example.veterinaryclinicmobileapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class intro extends AppCompatActivity {

    ImageButton petShelterDialogBox;
    ImageButton clinicDialogBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);

        clinicDialogBox = findViewById(R.id.clinic);
        petShelterDialogBox = findViewById(R.id.petshelter);

        clinicDialogBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClinicDialogBox();
            }
        });

        petShelterDialogBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPSDialogBox();
            }
        });
    }

    private void showClinicDialogBox() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.vet_dialog);

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.frame);

        Button yesBtn = dialog.findViewById(R.id.yesBtn);
        Button noBtn = dialog.findViewById(R.id.noBtn);

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCSCDialogBox();
            }
        });
        dialog.show();
    }

    private void showCSCDialogBox() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.vet_staff_security_code);

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.frame);

        EditText securityCode = dialog.findViewById(R.id.securityCode);
        Button doneBtn = dialog.findViewById(R.id.doneBtn);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correctSecurityCode = "vet123";
                String inputedSecurityCode = securityCode.getText().toString();

                if (!inputedSecurityCode.equals(correctSecurityCode)) {
                    Toast.makeText(intro.this, "Security code does not match", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), vet_login.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        dialog.show();
    }

    private void showPSDialogBox() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.staff_dialog);

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.frame);

        Button yesBtn = dialog.findViewById(R.id.yesBtn);
        Button noBtn = dialog.findViewById(R.id.noBtn);

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPSSCDialogBox();
            }
        });
        dialog.show();
    }

    private void showPSSCDialogBox() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.vet_staff_security_code);

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.frame);

        EditText securityCode = dialog.findViewById(R.id.securityCode);
        Button doneBtn = dialog.findViewById(R.id.doneBtn);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correctSecurityCode = "staff123";
                String inputedSecurityCode = securityCode.getText().toString();

                if (!inputedSecurityCode.equals(correctSecurityCode)) {
                    Toast.makeText(intro.this, "Security code does not match", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), staff_login.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        dialog.show();
    }

    public void goLoginPage(View view){
        Intent intent = new Intent(this, login.class);
        Button goLoginBtn = findViewById(R.id.goLoginBtn);
        startActivity(intent);
    }

    public void goRegisterPage(View view){
        Intent intent = new Intent(this, register.class);
        Button goRegisterBtn = findViewById(R.id.goRegisterBtn);
        startActivity(intent);
    }
}