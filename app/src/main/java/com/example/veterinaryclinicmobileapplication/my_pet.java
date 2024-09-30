package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

    FirebaseAuth Auth;
    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_pet);

        Auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        petRecyclerView = findViewById(R.id.petRecyclerView);
        petRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        petList = new ArrayList<>();
        petAdapter = new PetAdapter(this, petList);
        petRecyclerView.setAdapter(petAdapter);

        loadPets();

        addBtn = findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), add_pet1.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loadPets() {
        String userId = Auth.getCurrentUser().getUid();

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

                                                Log.d("my_pet", "Pet ID: " + petId + ", Name: " + petName);

                                                // Loop through possible extensions to find an image
                                                String[] possibleExtensions = {"png", "jpg"};
                                                for (String extension : possibleExtensions) {
                                                    String fileName = "images/" + petOwnerId + "/" + petId + "." + extension;
                                                    StorageReference imageRef = storage.getReference().child(fileName);

                                                    // Load image URI and add pet to the list
                                                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                                        petList.add(new Pet(petId, petName, uri.toString()));
                                                        // Notify the adapter that data has changed
                                                        petAdapter.notifyDataSetChanged();
                                                    }).addOnFailureListener(e -> {
                                                        Log.e("my_pet", "Error getting image URL for pet: " + petName + " with extension: " + extension, e);
                                                    });
                                                }
                                            }

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
}
