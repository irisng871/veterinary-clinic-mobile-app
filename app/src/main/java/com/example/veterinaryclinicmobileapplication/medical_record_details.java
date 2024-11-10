package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class medical_record_details extends AppCompatActivity {

    TextView petName, petDate, petTime, petDiagnosis, petAction, petMedication, petRemarks, petPaymentAmount;
    ImageButton backBtn;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medical_record_details);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        petName = findViewById(R.id.name);
        petDate = findViewById(R.id.date);
        petTime = findViewById(R.id.time);
        petDiagnosis = findViewById(R.id.diagnosis);
        petAction = findViewById(R.id.action);
        petMedication = findViewById(R.id.medication);
        petRemarks = findViewById(R.id.remarks);
        petPaymentAmount = findViewById(R.id.paymentAmount);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackAppointmentDetailsPage());

        Intent intent = getIntent();
        String appointmentId = intent.getStringExtra("appointmentId");
        String petId = intent.getStringExtra("petId");

        if (appointmentId != null) {
            fetchMedicalRecordDetails(appointmentId);
            fetchAppointmentDetails(appointmentId);
        } else {
            Log.d("medical_record_details", "No Appointment ID provided");
        }

        if (petId != null) {
            fetchPetName(petId);
        } else {
            Log.d("medical_record_details", "No Pet ID provided");
        }
    }

    public void fetchMedicalRecordDetails(String appointmentId) {
        db.collection("medical_record")
                .whereEqualTo("appointment_id", appointmentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String diagnosis = document.getString("diagnosis");
                                String action = document.getString("action");
                                String medication = document.getString("medication");
                                String remarks = document.getString("remarks");
                                String paymentAmount = document.getString("payment_amount");

                                petDiagnosis.setText(diagnosis);
                                petAction.setText(action);
                                petMedication.setText(medication);
                                petRemarks.setText(remarks);
                                petPaymentAmount.setText(paymentAmount);

                                break;
                            }
                        } else {
                            Log.d("medical_record_details", "No medical record found");
                        }
                    } else {
                        Log.d("medical_record_details", "Error fetching medical record", task.getException());
                    }
                });
    }

    public void fetchAppointmentDetails(String appointmentId) {
        db.collection("booking")
                .document(appointmentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String date = documentSnapshot.getString("date");
                        String time = documentSnapshot.getString("time");

                        petDate.setText(date != null ? date : "N/A");
                        petTime.setText(time != null ? time : "N/A");
                    } else {
                        Log.d("medical_record_details", "No booking document found for appointmentId: " + appointmentId);
                    }
                })
                .addOnFailureListener(e -> Log.e("medical_record_details", "Error fetching booking details", e));
    }

    public void fetchPetName(String petId) {
        db.collection("pet")
                .document(petId)
                .get()
                .addOnSuccessListener(petDocument -> {
                    if (petDocument.exists()) {
                        String name = petDocument.getString("name");
                        Log.d("fetchPetName", "Fetched pet name: " + name);
                        petName.setText(name != null ? name : "Unknown Pet");
                    } else {
                        Log.d("medical_record_details", "Pet document not found for petId: " + petId);
                    }
                })
                .addOnFailureListener(e -> Log.e("medical_record_details", "Error fetching pet document: ", e));
    }

    public void goBackAppointmentDetailsPage() {
        finish();
    }
}
