package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class vet_edit_medical_record_details extends AppCompatActivity {

    TextInputEditText petDiagnosis, petMedication, petAction, petRemarks, petPaymentAmount;
    Button editBtn;
    FirebaseFirestore db;
    String appointmentId, petId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vet_edit_medical_record_details);

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("appointmentId")) {
            appointmentId = intent.getStringExtra("appointmentId");
            petId = intent.getStringExtra("petId");
        } else {
            Toast.makeText(this, "No Appointment ID found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        petDiagnosis = findViewById(R.id.diagnosis);
        petMedication = findViewById(R.id.medication);
        petAction = findViewById(R.id.action);
        petRemarks = findViewById(R.id.remarks);
        petPaymentAmount = findViewById(R.id.paymentAmount);
        editBtn = findViewById(R.id.editBtn);

        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());

        loadMedicalRecordData();

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMedicalRecord();
            }
        });
    }

    public void loadMedicalRecordData() {
        Query recordQuery = db.collection("medical_record").whereEqualTo("appointment_id", appointmentId);
        recordQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentReference recordRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                recordRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String diagnosis = documentSnapshot.getString("diagnosis");
                        String medication = documentSnapshot.getString("medication");
                        String action = documentSnapshot.getString("action");
                        String remarks = documentSnapshot.getString("remarks");
                        String paymentAmount = documentSnapshot.getString("payment_amount");

                        petDiagnosis.setText(diagnosis);
                        petMedication.setText(medication);
                        petAction.setText(action);
                        petRemarks.setText(remarks);
                        petPaymentAmount.setText(paymentAmount);
                    } else {
                        Toast.makeText(vet_edit_medical_record_details.this, "Medical record not found.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(vet_edit_medical_record_details.this, "No medical record found for this appointment.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(vet_edit_medical_record_details.this, "Error loading record: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    public void updateMedicalRecord() {
        String diagnosis = petDiagnosis.getText().toString();
        String medication = petMedication.getText().toString();
        String action = petAction.getText().toString();
        String remarks = petRemarks.getText().toString();
        String paymentAmount = petPaymentAmount.getText().toString();

        if (diagnosis.isEmpty() || medication.isEmpty() || action.isEmpty() || paymentAmount.isEmpty()) {
            Toast.makeText(this, "Please fill out all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!paymentAmount.matches("^\\d+\\.\\d{2}$")) {
            Toast.makeText(this, "Payment amount must be a valid number with two decimal places", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> medicalRecordData = new HashMap<>();
        medicalRecordData.put("diagnosis", diagnosis);
        medicalRecordData.put("medication", medication);
        medicalRecordData.put("action", action);
        medicalRecordData.put("remarks", remarks);
        medicalRecordData.put("payment_amount", paymentAmount);

        db.collection("medical_record")
                .whereEqualTo("appointment_id", appointmentId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentReference recordRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        recordRef.update(medicalRecordData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(vet_edit_medical_record_details.this, "Medical record updated successfully.", Toast.LENGTH_SHORT).show();
                                    Intent editIntent = new Intent(vet_edit_medical_record_details.this, vet_patient_medical_record_details.class);
                                    editIntent.putExtra("appointmentId", appointmentId);
                                    editIntent.putExtra("petId", petId);
                                    startActivity(editIntent);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(vet_edit_medical_record_details.this, "Error updating record: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                });
                    } else {
                        Toast.makeText(vet_edit_medical_record_details.this, "No medical record found for this appointment.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(vet_edit_medical_record_details.this, "Error updating record: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}