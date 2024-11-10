package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class my_pet extends AppCompatActivity {

    Button addBtn;
    RecyclerView petRecyclerView;
    PetAdapter petAdapter;
    List<Pet> petList;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_pet);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        petRecyclerView = findViewById(R.id.petRecyclerView);
        petRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        petList = new ArrayList<>();
        petAdapter = new PetAdapter(this, petList);
        petRecyclerView.setAdapter(petAdapter);

        loadPets();

        addBtn = findViewById(R.id.addBtn);
        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), add_pet1.class);
            startActivity(intent);
            finish();
        });
    }

    public void loadPets() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("pet_owner").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String petOwnerId = documentSnapshot.getString("id");

                        db.collection("pet")
                                .whereEqualTo("pet_owner_id", petOwnerId)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        QuerySnapshot querySnapshot = task.getResult();
                                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                            Log.d("my_pet", "Number of pets found: " + querySnapshot.size());

                                            for (QueryDocumentSnapshot petDocument : querySnapshot) {
                                                String petId = petDocument.getString("id");
                                                String petName = petDocument.getString("name");

                                                Pet pet = new Pet(petId, petName, null, petOwnerId);
                                                petList.add(pet);

                                                loadPetImage(pet);
                                            }
                                            petAdapter.notifyDataSetChanged();
                                        } else {
                                            Log.d("my_pet", "No pets found for this owner.");
                                        }
                                    } else {
                                        Log.e("my_pet", "Error getting pets: ", task.getException());
                                    }
                                });
                    } else {
                        Log.d("my_pet", "No pet_owner document found for user.");
                    }
                })
                .addOnFailureListener(e -> Log.e("my_pet", "Error getting pet_owner: ", e));
    }

    public void loadPetImage(Pet pet) {
        String petOwnerId = pet.getPetOwnerId();
        String petId = pet.getId();

        String[] possibleExtensions = {"png", "jpg"};
        for (String extension : possibleExtensions) {
            String fileName = "images/" + petOwnerId + "/" + petId + "." + extension;
            StorageReference imageRef = storage.getReference().child(fileName);

            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                pet.setImageUrl(uri.toString());
                petAdapter.notifyDataSetChanged();
            }).addOnFailureListener(e -> {
                Log.e("my_pet", "Error getting image URL for pet: " + pet.getName() + " with extension: " + extension, e);
            });
        }
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
