package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class vet_appointment_details extends AppCompatActivity {

    TextView appointmentId, petId, petName, date, time, petOwnerName;
    Button rejectBtn, acceptBtn;
    ImageButton backBtn;
    String currentStatus;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vet_appointment_details);

        db = FirebaseFirestore.getInstance();

        appointmentId = findViewById(R.id.appointmentId);
        petId = findViewById(R.id.id);
        petName = findViewById(R.id.name);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        petOwnerName = findViewById(R.id.petOwnerName);
        rejectBtn = findViewById(R.id.rejectBtn);
        acceptBtn = findViewById(R.id.acceptBtn);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goMyAppointmentPage());

        Intent intent = getIntent();
        String bookingId = intent.getStringExtra("appointmentId");

        fetchAppointmentDetails(bookingId);

        rejectBtn.setOnClickListener(v -> updateAppointmentStatus("Reject"));
        acceptBtn.setOnClickListener(v -> updateAppointmentStatus("Accept"));
    }

    public void fetchAppointmentDetails(String bookingId) {
        db.collection("booking")
                .document(bookingId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            appointmentId.setText(document.getString("id"));
                            petId.setText(document.getString("pet_id"));
                            date.setText(document.getString("date"));
                            time.setText(document.getString("time"));

                            String petIdValue = document.getString("pet_id");
                            fetchPetDetails(petIdValue);

                            String petOwnerId = document.getString("pet_owner_id");
                            fetchPetOwnerDetails(petOwnerId);

                            // Show/hide buttons
                            currentStatus = document.getString("status");
                            if ("Pending".equals(currentStatus)) {
                                acceptBtn.setVisibility(View.VISIBLE);
                                rejectBtn.setVisibility(View.VISIBLE);
                            } else {
                                acceptBtn.setVisibility(View.GONE);
                                rejectBtn.setVisibility(View.GONE);
                            }
                        } else {
                            Log.d("vet_appointment_details", "No such document");
                        }
                    } else {
                        Log.d("vet_appointment_details", "Get failed with ", task.getException());
                    }
                });
    }

    public void fetchPetDetails(String petId) {
        db.collection("pet")
                .document(petId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                petName.setText(document.getString("name"));
                            } else {
                                Log.d("vet_appointment_details", "No such pet document");
                            }
                        } else {
                            Log.d("vet_appointment_details", "Get pet failed with ", task.getException());
                        }
                    }
                });
    }

    public void fetchPetOwnerDetails(String petOwnerId) {
        db.collection("pet_owner")
                .document(petOwnerId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                petOwnerName.setText(document.getString("name"));
                            } else {
                                Log.d("vet_appointment_details", "No such pet owner document");
                            }
                        } else {
                            Log.d("vet_appointment_details", "Get pet owner failed with ", task.getException());
                        }
                    }
                });
    }

    public void updateAppointmentStatus(String status) {
        String appointmentIdValue = appointmentId.getText().toString();

        db.collection("booking")
                .document(appointmentIdValue)
                .update("status", status)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Hide buttons after update status
                        acceptBtn.setVisibility(View.GONE);
                        rejectBtn.setVisibility(View.GONE);
                        currentStatus = status;
                    } else {
                        Log.d("vet_appointment_details", "Failed to update status: ", task.getException());
                    }
                });
    }

    public void goMyAppointmentPage() {
        finish();
    }
}
