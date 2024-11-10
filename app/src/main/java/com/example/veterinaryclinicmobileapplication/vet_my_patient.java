package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class vet_my_patient extends AppCompatActivity {

    RecyclerView patientRecyclerView;
    PatientAdapter patientAdapter;
    List<Patient> patientList;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser firebaseUser;
    String vetId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vet_my_patient);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = auth.getCurrentUser();

        patientRecyclerView = findViewById(R.id.patientRecyclerView);
        patientRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        patientList = new ArrayList<>();
        patientAdapter = new PatientAdapter(this, patientList);
        patientRecyclerView.setAdapter(patientAdapter);

        loadPatients();
    }

    public void loadPatients() {
        if (firebaseUser != null) {
            String userEmail = firebaseUser.getEmail();

            db.collection("veterinarian")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d("Patients", "Firestore query was successful.");
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                vetId = document.getString("id");
                                Log.d("Patients", "Vet ID: " + vetId);
                                fetchBookingsForVet(vetId);
                            }
                        } else {
                            Log.d("Patients", "No veterinarian document found for this user.");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Patients", "Error fetching vet document: ", e));
        } else {
            Log.d("Patients", "No user is currently logged in.");
        }
    }

    public void fetchBookingsForVet(String vetId) {
        db.collection("booking")
                .whereEqualTo("veterinarian_id", vetId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        patientList.clear();
                        Set<String> uniquePetIds = new HashSet<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String petId = document.getString("pet_id");

                            if (!uniquePetIds.contains(petId)) {
                                uniquePetIds.add(petId);
                                fetchPatientDetails(petId);
                            }
                        }
                        Log.d("Patients", "Patients loaded successfully.");
                    } else {
                        Log.e("Patients", "Error getting documents: ", task.getException());
                    }
                });
    }

    public void fetchPatientDetails(String petId) {
        db.collection("pet")
                .document(petId)
                .get()
                .addOnSuccessListener(petDocument -> {
                    if (petDocument.exists()) {
                        String petName = petDocument.getString("name");

                        Patient patient = new Patient(petId, petName);
                        patientList.add(patient);

                        patientAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("Patients", "Pet document not found for petId: " + petId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Patients", "Error fetching pet document: ", e));
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
