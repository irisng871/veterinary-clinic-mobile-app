package com.example.veterinaryclinicmobileapplication;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class recommend_adoptable_pet_details extends AppCompatActivity {

    TextView petType, petBreed, petName, petGender, petWeight, petEstimatedBirthday, petEstimatedAge, petNeutered, petPersonality, petHealthStatus, petAllergy, petHistory;
    ImageButton profileImageButton, backBtn;
    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommend_adoptable_pet_details);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        petType = findViewById(R.id.type);
        petBreed = findViewById(R.id.breed);
        petName = findViewById(R.id.name);
        petGender = findViewById(R.id.gender);
        petWeight = findViewById(R.id.weight);
        petEstimatedBirthday = findViewById(R.id.estimatedBirthday);
        petEstimatedAge = findViewById(R.id.estimatedAge);
        petNeutered = findViewById(R.id.neutered);
        petPersonality = findViewById(R.id.personality);
        petHealthStatus = findViewById(R.id.healthStatus);
        petAllergy = findViewById(R.id.allergy);
        petHistory = findViewById(R.id.history);
        profileImageButton = findViewById(R.id.profileImage);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackStaffPetfolioPage());

        Intent intent = getIntent();
        String petId = intent.getStringExtra("id");

        if (petId != null) {
            loadPetDetails(petId);
        } else {
            Toast.makeText(this, "Pet ID is missing", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadPetDetails(String petId) {
        db.collection("adoptable_pet").document(petId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String type = documentSnapshot.getString("type");
                        String breed = documentSnapshot.getString("breed");
                        String name = documentSnapshot.getString("name");
                        String gender = documentSnapshot.getString("gender");
                        String weight = documentSnapshot.getString("weight");
                        String estimatedBirthday = documentSnapshot.getString("estimated_birthday");
                        String estimatedAge = documentSnapshot.getString("estimated_age");
                        String neutered = documentSnapshot.getString("neutered");
                        String personality = documentSnapshot.getString("personality");
                        String healthStatus = documentSnapshot.getString("health_status");
                        String allergy = documentSnapshot.getString("allergy");
                        String history = documentSnapshot.getString("history");

                        petType.setText(type);
                        petBreed.setText(breed);
                        petName.setText(name);
                        petGender.setText(gender);
                        petWeight.setText(weight);
                        petEstimatedBirthday.setText(estimatedBirthday);
                        petEstimatedAge.setText(estimatedAge);
                        petNeutered.setText(neutered);
                        petPersonality.setText(personality);
                        petHealthStatus.setText(healthStatus);
                        petAllergy.setText(allergy);
                        petHistory.setText(history);

                        String jpgFileName = "images/" + petId + ".jpg";
                        String pngFileName = "images/" + petId + ".png";

                        StorageReference jpgImageRef = storage.getReference().child(jpgFileName);
                        StorageReference pngImageRef = storage.getReference().child(pngFileName);

                        // Try loading the .jpg image first
                        jpgImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Glide.with(this).load(uri).into(profileImageButton);
                        }).addOnFailureListener(e -> {
                            // If .jpg fails, try loading the .png image
                            pngImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                Glide.with(this).load(uri).into(profileImageButton);
                            }).addOnFailureListener(e2 -> {
                                Log.e("staff_pet_profile", "Error loading image in both jpg and png formats.", e2);
                            });
                        });

                    } else {
                        Toast.makeText(this, "Pet not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error getting pet details", Toast.LENGTH_SHORT).show());
    }

    public void goBackStaffPetfolioPage(){
        finish();
    }
}
