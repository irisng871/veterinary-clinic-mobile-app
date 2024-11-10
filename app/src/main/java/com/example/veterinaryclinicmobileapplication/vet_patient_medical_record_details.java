package com.example.veterinaryclinicmobileapplication;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class vet_patient_medical_record_details extends AppCompatActivity {

    TextView petName, petDate, petTime, petDiagnosis, petAction, petMedication, petRemarks, petPaymentAmount;
    ImageButton backBtn;
    Button editBtn, deleteBtn;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vet_patient_medical_record_details);

        db = FirebaseFirestore.getInstance();

        petName = findViewById(R.id.name);
        petDate = findViewById(R.id.date);
        petTime = findViewById(R.id.time);
        petDiagnosis = findViewById(R.id.diagnosis);
        petAction = findViewById(R.id.action);
        petMedication = findViewById(R.id.medication);
        petRemarks = findViewById(R.id.remarks);
        petPaymentAmount = findViewById(R.id.paymentAmount);
        editBtn = findViewById(R.id.editBtn);
        deleteBtn = findViewById(R.id.deleteBtn);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackMedicalRecordListPage());

        Intent intent = getIntent();
        String appointmentId = intent.getStringExtra("appointmentId");
        String petId = intent.getStringExtra("petId");

        if (appointmentId != null) {
            fetchMedicalRecordDetails(appointmentId);
            fetchAppointmentDetails(appointmentId);
        } else {
            Log.d("vet_patient_medical_record_details", "No Appointment ID provided");
        }

        if (petId != null) {
            fetchPetName(petId);
        } else {
            Log.d("vet_patient_medical_record_details", "No Pet ID provided");
        }

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appointmentId != null) {
                    Intent editIntent = new Intent(vet_patient_medical_record_details.this, vet_edit_medical_record_details.class);
                    editIntent.putExtra("appointmentId", appointmentId);
                    editIntent.putExtra("petId", petId);
                    startActivity(editIntent);
                } else {
                    Toast.makeText(vet_patient_medical_record_details.this, "Appointment ID is not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeletionDialogBox(appointmentId);
            }
        });
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
                            Log.d("vet_patient_medical_record_details", "No medical record found");
                        }
                    } else {
                        Log.d("vet_patient_medical_record_details", "Error fetching medical record", task.getException());
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
                        Log.d("vet_patient_medical_record_details", "No booking document found for appointmentId: " + appointmentId);
                    }
                })
                .addOnFailureListener(e -> Log.e("vet_patient_medical_record_details", "Error fetching booking details", e));
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
                        Log.d("vet_patient_medical_record_details", "Pet document not found for petId: " + petId);
                    }
                })
                .addOnFailureListener(e -> Log.e("vet_patient_medical_record_details", "Error fetching pet document: ", e));
    }

    public void showDeletionDialogBox(String appointmentId) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.deletion);

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
                dialog.dismiss();
                deleteMedicalRecord(appointmentId);
            }
        });
        dialog.show();
    }

    public void deleteMedicalRecord(String appointmentId) {
        db.collection("medical_record")
                .whereEqualTo("appointment_id", appointmentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String documentId = document.getId();
                            db.collection("medical_record").document(documentId).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(vet_patient_medical_record_details.this, "Medical record deleted", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("vet_patient_medical_record_details", "Error deleting medical record", e);
                                    });
                        }
                    } else {
                        Log.d("vet_patient_medical_record_details", "No medical record found to delete");
                    }
                });
    }

    public void goBackMedicalRecordListPage() {
        finish();
    }
}
