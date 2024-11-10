package com.example.veterinaryclinicmobileapplication;

import android.app.Dialog;
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

public class staff_pet_profile extends AppCompatActivity {

    TextView petType, petBreed, petName, petGender, petWeight, petEstimatedBirthday, petEstimatedAge, petNeutered, petPersonality, petHealthStatus, petAllergy, petHistory;
    ImageButton profileImageButton, backBtn;
    Button editBtn, deleteBtn;
    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_pet_profile);

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
        editBtn = findViewById(R.id.editBtn);
        deleteBtn = findViewById(R.id.deleteBtn);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackStaffPetfolioPage());

        Intent intent = getIntent();
        String petId = intent.getStringExtra("id");

        if (petId != null) {
            loadPetDetails(petId);
        } else {
            Toast.makeText(this, "Pet ID is missing", Toast.LENGTH_SHORT).show();
        }

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (petId != null) {
                    Intent editIntent = new Intent(staff_pet_profile.this, staff_edit_pet_profile.class);
                    editIntent.putExtra("id", petId);
                    startActivity(editIntent);
                } else {
                    Toast.makeText(staff_pet_profile.this, "Pet ID is not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeletionDialogBox(petId);
            }
        });
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

                        // Try jpg first, then png
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

    public void showDeletionDialogBox(String petId) {
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
                deletePet(petId);
            }
        });

        dialog.show();
    }

    public void deletePet(String petId) {
        db.collection("adoptable_pet").document(petId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String petName = documentSnapshot.getString("name");
                        Log.d("staff_pet_profile", "Deleting pet: " + petName + " with ID: " + petId);

                        // Try deleting the PNG image first
                        String pngFileName = "images/" + petId + ".png";
                        StorageReference pngImageRef = storage.getReference().child(pngFileName);

                        pngImageRef.delete().addOnSuccessListener(aVoid -> {
                            Log.d("staff_pet_profile", "PNG image deleted successfully");
                            deletePetDocument(petId);
                        }).addOnFailureListener(e -> {
                            Log.e("staff_pet_profile", "PNG image not found, trying JPG...");

                            String jpgFileName = "images/" + petId + ".jpg";
                            StorageReference jpgImageRef = storage.getReference().child(jpgFileName);

                            jpgImageRef.delete().addOnSuccessListener(aVoid1 -> {
                                Log.d("staff_pet_profile", "JPG image deleted successfully");
                                deletePetDocument(petId);
                            }).addOnFailureListener(e1 -> {
                                Log.e("staff_pet_profile", "JPG image not found");
                                deletePetDocument(petId);
                            });
                        });

                    } else {
                        Toast.makeText(this, "Pet not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching pet details", Toast.LENGTH_SHORT).show();
                });
    }

    public void deletePetDocument(String petId) {
        db.collection("adoptable_pet").document(petId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Pet deleted successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, staff_petfolio.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting pet", Toast.LENGTH_SHORT).show();
                });
    }


    public void goBackStaffPetfolioPage(){
        finish();
    }
}
