package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentResolver;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class pet_profile extends AppCompatActivity {

    TextView petType, petBreed, petName, petGender, petWeight, petEstimatedBirthday, petEstimatedAge, petNeutered, petAllergy;

    ImageButton profileImageButton;

    Uri imageUri;

    Button editBtn, deleteBtn;

    FirebaseFirestore db;

    FirebaseAuth Auth;

    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pet_profile);

        Auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        petType = findViewById(R.id.type);
        petBreed = findViewById(R.id.breed);
        petName = findViewById(R.id.name);
        petGender = findViewById(R.id.petGender);
        petWeight = findViewById(R.id.weight);
        petEstimatedBirthday = findViewById(R.id.estimatedBirthday);
        petEstimatedAge = findViewById(R.id.estimatedAge);
        petNeutered = findViewById(R.id.neutered);
        petAllergy = findViewById(R.id.allergy);
        profileImageButton = findViewById(R.id.profileImage);
        editBtn = findViewById(R.id.editBtn);
        deleteBtn = findViewById(R.id.deleteBtn);

        Intent intent = getIntent();
        String petId = intent.getStringExtra("id");
        String petOwnerId = intent.getStringExtra("petOwnerId");

        if (petId != null) {
            loadPetDetails(petId);
        } else {
            Toast.makeText(this, "Pet ID is missing", Toast.LENGTH_SHORT).show();
        }

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (petId != null) {
                    Intent editIntent = new Intent(pet_profile.this, edit_pet_profile.class);
                    editIntent.putExtra("id", petId);
                    startActivity(editIntent);
                } else {
                    Toast.makeText(pet_profile.this, "Pet ID is not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePet(petId);
            }
        });
    }

    public void loadPetDetails(String petId) {
        db.collection("pet").document(petId).get()
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
                        String allergy = documentSnapshot.getString("allergy");
                        String petOwnerId = documentSnapshot.getString("pet_owner_id");

                        petType.setText(type);
                        petBreed.setText(breed);
                        petName.setText(name);
                        petGender.setText(gender);
                        petWeight.setText(weight);
                        petEstimatedBirthday.setText(estimatedBirthday);
                        petEstimatedAge.setText(estimatedAge);
                        petNeutered.setText(neutered);
                        petAllergy.setText(allergy);

                        // Construct file names for both jpg and png
                        String jpgFileName = "images/" + petOwnerId + "/" + petId + ".jpg";
                        String pngFileName = "images/" + petOwnerId + "/" + petId + ".png";

                        StorageReference jpgImageRef = storage.getReference().child(jpgFileName);
                        StorageReference pngImageRef = storage.getReference().child(pngFileName);

                        // Attempt to get the .jpg image first
                        jpgImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Load image into profileImageButton using Glide
                            Glide.with(this).load(uri).into(profileImageButton);
                        }).addOnFailureListener(e -> {
                            // If .jpg fails, try .png
                            pngImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Load image into profileImageButton using Glide
                                Glide.with(this).load(uri).into(profileImageButton);
                            }).addOnFailureListener(e2 -> {
                                Log.e("pet_profile", "Error getting image URL for pet with ID: " + petId, e2);
                                // Handle the case where both images are not found (e.g., set a placeholder image)
                            });
                        });
                    } else {
                        Toast.makeText(this, "Pet not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error getting pet details", Toast.LENGTH_SHORT).show());
    }

    public void deletePet(String petId) {
        // Get the current user ID
        String userId = Auth.getCurrentUser().getUid();

        // Retrieve the pet owner ID based on the current user ID
        db.collection("pet_owner").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String petOwnerId = documentSnapshot.getString("id");
                        if (petOwnerId != null && !petOwnerId.isEmpty()) {
                            // Proceed with image deletion and pet document deletion
                            proceedWithDeletion(petId, petOwnerId);
                        } else {
                            Log.e("DeletePet", "Pet owner ID is null or empty.");
                            Toast.makeText(this, "Pet owner ID is invalid.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("DeletePet", "Pet owner document does not exist for user ID: " + userId);
                        Toast.makeText(this, "Pet owner not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DeletePet", "Error retrieving pet owner document: " + e.getMessage());
                    Toast.makeText(this, "Error retrieving pet owner.", Toast.LENGTH_SHORT).show();
                });
    }

    private void proceedWithDeletion(String petId, String petOwnerId) {
        String jpgFileName = "images/" + petOwnerId + "/" + petId + ".jpg";
        String pngFileName = "images/" + petOwnerId + "/" + petId + ".png";

        StorageReference jpgImageRef = storage.getReference().child(jpgFileName);
        StorageReference pngImageRef = storage.getReference().child(pngFileName);

        // Try deleting JPG first
        jpgImageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("DeletePet", "JPG image deleted successfully.");
                    deletePetDocument(petId); // Delete the pet document after successful deletion
                })
                .addOnFailureListener(e -> {
                    Log.e("DeletePet", "Error deleting JPG image: " + e.getMessage());
                    // If JPG fails, try PNG
                    pngImageRef.delete()
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d("DeletePet", "PNG image deleted successfully.");
                                deletePetDocument(petId); // Delete the pet document after successful deletion
                            })
                            .addOnFailureListener(e1 -> {
                                Log.e("DeletePet", "Error deleting PNG image: " + e1.getMessage());
                                Toast.makeText(this, "No images found or could not be deleted. Pet not deleted.", Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void deletePetDocument(String petId) {
        db.collection("pet").document(petId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Pet deleted successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, my_pet.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("DeletePet", "Error deleting pet document: " + e.getMessage());
                    Toast.makeText(this, "Error deleting pet document", Toast.LENGTH_SHORT).show();
                });
    }

    public void goBackMyPetPage(View view){
        Intent intent = new Intent(this, my_pet.class);
        ImageButton backBtn = findViewById(R.id.backBtn);
        startActivity(intent);
    }
}
