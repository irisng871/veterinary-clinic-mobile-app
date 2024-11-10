package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class vet_add_medical_record extends AppCompatActivity {

    EditText petDiagnosis, petMedication, petAction, petRemarks, petPaymentAmount;
    Button addBtn;
    ImageButton backBtn;
    FirebaseAuth Auth;
    FirebaseFirestore db;
    String appointmentId, petId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vet_add_medical_record);

        Auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        petDiagnosis = findViewById(R.id.diagnosis);
        petMedication = findViewById(R.id.medication);
        petAction = findViewById(R.id.action);
        petRemarks = findViewById(R.id.remarks);
        petPaymentAmount = findViewById(R.id.paymentAmount);
        addBtn = findViewById(R.id.addBtn);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackVetPatientMedicalRecordPage());

        appointmentId = getIntent().getStringExtra("appointmentId");
        petId = getIntent().getStringExtra("petId");

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appointmentId == null) {
                    Toast.makeText(vet_add_medical_record.this, "Appointment ID is null", Toast.LENGTH_SHORT).show();
                    return;
                }

                String diagnosis, medication, action, paymentAmount;

                diagnosis = String.valueOf(petDiagnosis.getText());
                medication = String.valueOf(petMedication.getText());
                action = String.valueOf(petAction.getText());
                paymentAmount = String.valueOf(petPaymentAmount.getText());

                if (TextUtils.isEmpty(diagnosis) || TextUtils.isEmpty(action) || TextUtils.isEmpty(medication) || TextUtils.isEmpty(paymentAmount)) {
                    Toast.makeText(vet_add_medical_record.this, "Please enter all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                generateMedicalRecordId();
            }
        });
    }

    public void generateMedicalRecordId() {
        db.collection("medical_record")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int maxNumber = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String lastId = document.getString("id");
                            if (lastId != null && lastId.startsWith("MR")) {
                                String numberPart = lastId.substring(2);
                                try {
                                    int number = Integer.parseInt(numberPart);
                                    if (number > maxNumber) {
                                        maxNumber = number;
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("ID Parsing Error", "Error parsing ID: " + lastId, e);
                                }
                            }
                        }

                        String newId = "MR" + (maxNumber + 1);
                        Log.d("MedicalRecord", "Generated ID: " + newId);

                        addMedicalRecord(newId);
                    } else {
                        Log.e("MedicalRecord", "Error generating medical record ID", task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MedicalRecord", "Error retrieving data", e);
                });
    }

    public void addMedicalRecord(String newId) {
        String diagnosis = petDiagnosis.getText().toString().trim();
        String medication = petMedication.getText().toString().trim();
        String action = petAction.getText().toString().trim();
        String remarks = petRemarks.getText().toString().trim();
        String paymentAmount = petPaymentAmount.getText().toString().trim();

        if (!paymentAmount.matches("^\\d+\\.\\d{2}$")) {
            Toast.makeText(this, "Payment amount must be a valid number with two decimal places (X.XX)", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> medicalRecord = new HashMap<>();
        medicalRecord.put("id", newId);
        medicalRecord.put("diagnosis", diagnosis);
        medicalRecord.put("medication", medication);
        medicalRecord.put("action", action);
        medicalRecord.put("remarks", remarks);
        medicalRecord.put("payment_amount", paymentAmount);
        medicalRecord.put("appointment_id", appointmentId);

        db.collection("medical_record")
                .document(newId)
                .set(medicalRecord)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(vet_add_medical_record.this, "Medical record added successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(vet_add_medical_record.this, vet_patient_medical_record_details.class);
                    intent.putExtra("appointmentId", appointmentId);
                    intent.putExtra("petId", petId);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(vet_add_medical_record.this, "Error adding medical record: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void goBackVetPatientMedicalRecordPage() {
        finish();
    }
}
