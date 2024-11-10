package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class vet_my_appointment extends AppCompatActivity {

    RecyclerView appointmentRecyclerView;
    AppointmentAdapter appointmentAdapter;
    List<Appointment> appointmentList;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vet_my_appointment);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = auth.getCurrentUser();

        appointmentRecyclerView = findViewById(R.id.appointmentRecyclerView);
        appointmentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        appointmentList = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(this, appointmentList, "vet");
        appointmentRecyclerView.setAdapter(appointmentAdapter);

        loadAppointments();
    }

    public void loadAppointments() {
        if (firebaseUser != null) {
            String userEmail = firebaseUser.getEmail();

            db.collection("veterinarian")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String vetId = document.getString("id");
                                fetchAppointmentsForVet(vetId);
                            }
                        } else {
                            Toast.makeText(this, "No veterinarian record found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Appointment", "Error fetching vet document: ", e));
        } else {
            Log.d("Appointment", "No user is currently logged in.");
        }
    }

    public void fetchAppointmentsForVet(String vetId) {
        db.collection("booking")
                .whereEqualTo("veterinarian_id", vetId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        appointmentList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String petId = document.getString("pet_id");
                            String date = document.getString("date");
                            String time = document.getString("time");
                            String bookingId = document.getId();

                            fetchPetName(petId, date, time, vetId, bookingId);
                        }
                        Log.d("Appointment", "Appointments loaded successfully.");
                    } else {
                        Log.e("Appointment", "Error getting documents: ", task.getException());
                    }
                });
    }

    public void fetchPetName(String petId, String date, String time, String vetId, String bookingId) {
        db.collection("pet")
                .document(petId)
                .get()
                .addOnSuccessListener(petDocument -> {
                    if (petDocument.exists()) {
                        String petName = petDocument.getString("name");

                        Appointment appointment = new Appointment(petName, date, time, vetId, bookingId);
                        appointmentList.add(appointment);

                        sortAppointmentsDescending();

                        appointmentAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("Appointment", "Pet document not found for petId: " + petId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Appointment", "Error fetching pet document: ", e));
    }

    public void sortAppointmentsDescending() {
        appointmentList.sort((a1, a2) -> a2.getAppointmentId().compareTo(a1.getAppointmentId()));
    }

    public void goMyPatientPage(View view) {
        Intent intent = new Intent(this, vet_my_patient.class);
        ImageView goMyPatientBtn = findViewById(R.id.goPatientBtn);
        startActivity(intent);
    }

    public void goMyAppointmentPage(View view) {
        Intent intent = new Intent(this, vet_my_appointment.class);
        ImageView goMyAppointmentBtn = findViewById(R.id.goAppointmentBtn);
        startActivity(intent);
    }

    public void goHomePage(View view) {
        Intent intent = new Intent(this, vet_home.class);
        ImageView goHomeBtn = findViewById(R.id.logo);
        startActivity(intent);
    }

    public void goProfilePage(View view) {
        Intent intent = new Intent(this, vet_profile.class);
        ImageButton goProfileBtn = findViewById(R.id.goProfileBtn);
        startActivity(intent);
    }
}
