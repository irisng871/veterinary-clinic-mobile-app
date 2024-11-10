package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class vet_patient_medical_record extends AppCompatActivity implements MedicalRecordAdapter.OnItemClickListener {

    RecyclerView patientMRRecyclerView;
    MedicalRecordAdapter medicalRecordAdapter;
    List<MedicalRecord> medicalRecordList;
    ImageButton backBtn;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vet_patient_medical_record);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = auth.getCurrentUser();

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackPatientDetailsPage());

        patientMRRecyclerView = findViewById(R.id.patientMRRecyclerView);
        patientMRRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        String petId = intent.getStringExtra("petId");

        medicalRecordList = new ArrayList<>();

        medicalRecordAdapter = new MedicalRecordAdapter(this, medicalRecordList, this);
        patientMRRecyclerView.setAdapter(medicalRecordAdapter);

        loadAppointments(petId);
    }

    @Override
    public void onItemClick(String appointmentId) {
        String petId = getIntent().getStringExtra("petId");
        checkMedicalRecord(appointmentId, petId);
    }

    public void loadAppointments(String petId) {
        if (firebaseUser != null) {
            String userEmail = firebaseUser.getEmail();

            db.collection("veterinarian")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful()) {
                            if (!userTask.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : userTask.getResult()) {
                                    String vetId = document.getString("id");
                                    Log.d("vet_patient_medical_record", "Vet ID: " + vetId);

                                    loadVeterinarianAppointments(petId, vetId);
                                }
                            } else {
                                Log.d("vet_patient_medical_record", "No veterinarian document found for this user.");
                            }
                        } else {
                            Log.e("vet_patient_medical_record", "Error getting veterinarian document: ", userTask.getException());
                        }
                    });
        } else {
            Log.d("vet_patient_medical_record", "No user is currently logged in.");
        }
    }

    public void loadVeterinarianAppointments(String petId, String vetId) {
        db.collection("booking")
                .whereEqualTo("pet_id", petId)
                .whereEqualTo("veterinarian_id", vetId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        medicalRecordList.clear();
                        QuerySnapshot querySnapshot = task.getResult();

                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Create list of QueryDocumentSnapshots
                            List<QueryDocumentSnapshot> bookingDocs = new ArrayList<>();

                            // Populate list with QueryDocumentSnapshots
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                bookingDocs.add(document);
                            }

                            for (QueryDocumentSnapshot document : bookingDocs) {
                                String appointmentId = document.getId();
                                String petName = document.getString("pet_name");
                                String date = document.getString("date");
                                String time = document.getString("time");

                                MedicalRecord medicalRecord = new MedicalRecord(appointmentId, petId, petName, date, time);
                                medicalRecordList.add(medicalRecord);
                            }

                            sortAppointmentsDescending();

                            medicalRecordAdapter.notifyDataSetChanged();
                        } else {
                            Log.d("vet_patient_medical_record", "No appointments found for this petId with the logged-in vetId");
                        }
                    } else {
                        Log.e("vet_patient_medical_record", "Error getting booking documents: ", task.getException());
                    }
                });
    }

    public void checkMedicalRecord(String appointmentId, String petId) {
        db.collection("medical_record")
                .whereEqualTo("appointment_id", appointmentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            Intent intent = new Intent(vet_patient_medical_record.this, vet_patient_medical_record_details.class);
                            intent.putExtra("appointmentId", appointmentId);
                            intent.putExtra("petId", petId);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(vet_patient_medical_record.this, vet_add_medical_record.class);
                            intent.putExtra("appointmentId", appointmentId);
                            intent.putExtra("petId", petId);
                            startActivity(intent);
                        }
                    } else {
                        Log.e("vet_patient_medical_record", "Error getting documents: ", task.getException());
                    }
                });
    }

    public void sortAppointmentsDescending() {
        medicalRecordList.sort((a1, a2) -> a2.getAppointmentId().compareTo(a1.getAppointmentId()));
    }

    public void goBackPatientDetailsPage() {
        finish();
    }
}
