package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class recommend_adoptable_pet extends AppCompatActivity {

    RecyclerView recyclerView;
    RecommendPetAdapter adapter;
    List<RecommendPet> petList;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    String selectedType, selectedBreed, selectedGender, selectedNeutered;
    ImageButton filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommend_adoptable_pet);

        recyclerView = findViewById(R.id.recommendAdoptablePetRecyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        petList = new ArrayList<>();
        adapter = new RecommendPetAdapter(this, petList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        filter = findViewById(R.id.filter);
        filter.setOnClickListener(v -> goFilterPage());

        selectedType = getIntent().getStringExtra("type");
        selectedBreed = getIntent().getStringExtra("breed");
        selectedGender = getIntent().getStringExtra("gender");
        selectedNeutered = getIntent().getStringExtra("neutered");

        Log.d("IntentData", "Type: " + selectedType + ", Breed: " + selectedBreed + ", Gender: " + selectedGender + ", Neutered: " + selectedNeutered);

        if (selectedType != null || selectedBreed != null || selectedGender != null || selectedNeutered != null) {
            Log.d("Action", "Loading filtered pets");
            loadFilteredPets(selectedType, selectedBreed, selectedGender, selectedNeutered);
        } else {
            Log.d("Action", "Loading user pets and recommendations");
            loadUserPetsAndRecommendations();
        }
    }

    public void loadUserPetsAndRecommendations() {
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            db.collection("pet_owner").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String petOwnerId = documentSnapshot.getString("id");
                            Log.d("PetOwnerID", "Current pet owner ID: " + petOwnerId);
                            loadUserPets(petOwnerId);
                        } else {
                            Toast.makeText(this, "Pet owner document does not exist.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error getting pet owner: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadUserPets(String petOwnerId) {
        db.collection("pet")
                .whereEqualTo("pet_owner_id", petOwnerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        HashSet<String> petTypes = new HashSet<>();
                        int petCount = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String petType = document.getString("type");
                            if (petType != null) {
                                petTypes.add(petType.toLowerCase());
                            }
                            petCount++;
                        }

                        if (petCount > 0) {
                            if (petTypes.contains("cat") && petTypes.contains("dog")) {
                                loadAllAdoptablePets();
                            } else if (petTypes.contains("cat")) {
                                loadAdoptablePetsByType("cat");
                            } else if (petTypes.contains("dog")) {
                                loadAdoptablePetsByType("dog");
                            }
                        } else {
                            Toast.makeText(this, "No pets found for user.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error getting user's pets: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void loadAllAdoptablePets() {
        db.collection("adoptable_pet")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        petList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            RecommendPet pet = document.toObject(RecommendPet.class);
                            String petId = pet.getId();

                            String jpgFileName = "images/" + petId + ".jpg";
                            String pngFileName = "images/" + petId + ".png";

                            StorageReference jpgImageRef = FirebaseStorage.getInstance().getReference().child(jpgFileName);
                            StorageReference pngImageRef = FirebaseStorage.getInstance().getReference().child(pngFileName);

                            jpgImageRef.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        pet.setImageUrl(uri.toString());
                                        petList.add(pet);
                                        adapter.notifyItemInserted(petList.size() - 1);
                                    }).addOnFailureListener(e -> {
                                        // If .jpg fails, try .png
                                        pngImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                            pet.setImageUrl(uri.toString());
                                            petList.add(pet);
                                            adapter.notifyItemInserted(petList.size() - 1);
                                        }).addOnFailureListener(e2 -> {
                                            Log.e("MyPetActivity", "Error getting image URL for pet: " + pet.getName() + " in both jpg and png formats.", e2);
                                            petList.add(pet); // Add pet without image
                                            adapter.notifyItemInserted(petList.size() - 1);
                                        });
                                    });
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Error getting all adoptable pets: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void loadAdoptablePetsByType(String petType) {
        db.collection("adoptable_pet")
                .whereEqualTo("type", petType)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        petList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            RecommendPet pet = document.toObject(RecommendPet.class);
                            String petId = pet.getId();

                            String jpgFileName = "images/" + petId + ".jpg";
                            String pngFileName = "images/" + petId + ".png";

                            StorageReference jpgImageRef = FirebaseStorage.getInstance().getReference().child(jpgFileName);
                            StorageReference pngImageRef = FirebaseStorage.getInstance().getReference().child(pngFileName);

                            // Start fetching the image URLs
                            jpgImageRef.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        pet.setImageUrl(uri.toString());
                                        petList.add(pet);
                                        adapter.notifyItemInserted(petList.size() - 1);
                                    }).addOnFailureListener(e -> {
                                        // If .jpg fails, try .png
                                        pngImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                            pet.setImageUrl(uri.toString());
                                            petList.add(pet);
                                            adapter.notifyItemInserted(petList.size() - 1);
                                        }).addOnFailureListener(e2 -> {
                                            Log.e("MyPetActivity", "Error getting image URL for pet: " + pet.getName() + " in both jpg and png formats.", e2);
                                            petList.add(pet); // Add pet without image
                                            adapter.notifyItemInserted(petList.size() - 1);
                                        });
                                    });
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Error getting adoptable pets: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void loadFilteredPets(String type, String breed, String gender, String neutered) {
        Query query = db.collection("adoptable_pet");

        if (type != null) {
            query = query.whereEqualTo("type", type);
        }

        if (breed != null) {
            query = query.whereEqualTo("breed", breed);
        }

        if (gender != null && !gender.equals("Choose the pet gender")) {
            query = query.whereEqualTo("gender", gender);
        }

        if (neutered != null && !neutered.equals("Are the pet neutered?")) {
            query = query.whereEqualTo("neutered", neutered);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                petList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    RecommendPet pet = document.toObject(RecommendPet.class);
                    String petId = pet.getId();

                    String jpgFileName = "images/" + petId + ".jpg";
                    String pngFileName = "images/" + petId + ".png";

                    StorageReference jpgImageRef = FirebaseStorage.getInstance().getReference().child(jpgFileName);
                    StorageReference pngImageRef = FirebaseStorage.getInstance().getReference().child(pngFileName);

                    jpgImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        pet.setImageUrl(uri.toString());
                        petList.add(pet);
                        adapter.notifyItemInserted(petList.size() - 1);
                    }).addOnFailureListener(e -> {
                        // If .jpg fails, try .png
                        pngImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            pet.setImageUrl(uri.toString());
                            petList.add(pet);
                            adapter.notifyItemInserted(petList.size() - 1);
                        }).addOnFailureListener(e2 -> {
                            Log.e("MyPetActivity", "Error getting image URL for pet: " + pet.getName() + " in both jpg and png formats.", e2);
                            petList.add(pet);
                            adapter.notifyItemInserted(petList.size() - 1);
                        });
                    });
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error getting filtered adoptable pets: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goFilterPage() {
        Intent intent = new Intent(this, filter.class);
        startActivity(intent);
    }

    public void goHomePage(View view) {
        Intent intent = new Intent(this, home.class);
        ImageButton goHomeBtn = findViewById(R.id.goHomeBtn);
        startActivity(intent);
    }

    public void goMyPetPage(View view) {
        Intent intent = new Intent(this, my_pet.class);
        ImageButton goMyPetBtn = findViewById(R.id.goMyPetBtn);
        startActivity(intent);
    }

    public void goRecommendAdoptablePetPage(View view) {
        Intent intent = new Intent(this, recommend_adoptable_pet.class);
        ImageButton goPetShelterBtn = findViewById(R.id.goPetShelterBtn);
        startActivity(intent);
    }

    public void goCalendarPage(View view) {
        Intent intent = new Intent(this, calendar.class);
        ImageButton goCalendarBtn = findViewById(R.id.goCalendarBtn);
        startActivity(intent);
    }

    public void goProfilePage(View view) {
        Intent intent = new Intent(this, my_profile.class);
        ImageButton goProfileBtn = findViewById(R.id.goProfileBtn);
        startActivity(intent);
    }
}
