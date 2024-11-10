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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class vet_patient_details extends AppCompatActivity {
    TextView petName, petType, petBreed, petGender, petWeight, petEstimatedBirthday, petEstimatedAge;
    ImageButton backBtn;
    Button viewAllMedicalRecordBtn;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vet_patient_details);

        db = FirebaseFirestore.getInstance();

        petName = findViewById(R.id.name);
        petType = findViewById(R.id.type);
        petBreed = findViewById(R.id.breed);
        petGender = findViewById(R.id.gender);
        petWeight = findViewById(R.id.weight);
        petEstimatedBirthday = findViewById(R.id.estimatedBirthday);
        petEstimatedAge = findViewById(R.id.estimatedAge);
        backBtn = findViewById(R.id.backBtn);
        viewAllMedicalRecordBtn = findViewById(R.id.viewAllMedicalRecordBtn);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackMyPatientPage());

        Intent intent = getIntent();
        String petId = intent.getStringExtra("petId");

        fetchPetDetails(petId);

        viewAllMedicalRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(vet_patient_details.this, vet_patient_medical_record.class);
                intent.putExtra("petId", petId);
                startActivity(intent);
            }
        });
    }

    public void fetchPetDetails(String petIdValue) {
        db.collection("pet")
                .document(petIdValue)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                petName.setText(document.getString("name"));
                                petType.setText(document.getString("type"));
                                petBreed.setText(document.getString("breed"));
                                petGender.setText(document.getString("gender"));
                                petWeight.setText(document.getString("weight"));
                                petEstimatedBirthday.setText(document.getString("estimated_birthday"));
                                petEstimatedAge.setText(document.getString("estimated_age"));
                            } else {
                                Log.d("vet_patient_details", "No such pet document");
                            }
                        } else {
                            Log.d("vet_patient_details", "Get pet failed with ", task.getException());
                        }
                    }
                });
    }

    public void goBackMyPatientPage() {
        finish();
    }
}
