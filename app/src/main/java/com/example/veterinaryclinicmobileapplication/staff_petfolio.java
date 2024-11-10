package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class staff_petfolio extends AppCompatActivity {

    Button addBtn;
    RecyclerView petRecyclerView;
    AdoptablePetAdapter adoptablePetAdapter;
    List<AdoptablePet> petList;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_petfolio);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        petRecyclerView = findViewById(R.id.petRecyclerView);
        petRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        petList = new ArrayList<>();
        adoptablePetAdapter = new AdoptablePetAdapter(this, petList);
        petRecyclerView.setAdapter(adoptablePetAdapter);

        loadPets();

        addBtn = findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), staff_add_pet1.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void loadPets() {
        db.collection("adoptable_pet")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            Log.d("MyPetActivity", "Number of pets found: " + querySnapshot.size());

                            for (QueryDocumentSnapshot petDocument : querySnapshot) {
                                String petId = petDocument.getString("id");
                                String petName = petDocument.getString("name");

                                Log.d("MyPetActivity", "Pet ID: " + petId + ", Name: " + petName);

                                // Try jpg first, then png
                                String jpgFileName = "images/" + petId + ".jpg";
                                String pngFileName = "images/" + petId + ".png";

                                StorageReference jpgImageRef = storage.getReference().child(jpgFileName);
                                StorageReference pngImageRef = storage.getReference().child(pngFileName);

                                // Attempt to get the .jpg image first
                                jpgImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    // Add pet with .jpg image URL
                                    petList.add(new AdoptablePet(petId, petName, uri.toString()));
                                    petRecyclerView.getAdapter().notifyDataSetChanged();
                                }).addOnFailureListener(e -> {
                                    // If .jpg fails, try .png
                                    pngImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        // Add pet with .png image URL
                                        petList.add(new AdoptablePet(petId, petName, uri.toString()));
                                        petRecyclerView.getAdapter().notifyDataSetChanged();
                                    }).addOnFailureListener(e2 -> {
                                        Log.e("MyPetActivity", "Error getting image URL for pet: " + petName + " in both jpg and png formats.", e2);
                                        petList.add(new AdoptablePet(petId, petName, null));
                                        petRecyclerView.getAdapter().notifyDataSetChanged();
                                    });
                                });
                            }
                            petRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
                            petRecyclerView.setAdapter(new AdoptablePetAdapter(this, petList));

                        } else {
                            Log.d("MyPetActivity", "No pets found.");
                        }
                    } else {
                        Log.e("MyPetActivity", "Error getting pets: ", task.getException());
                    }
                });
    }

    public void goHomePage(View view){
        Intent intent = new Intent(this, staff_home.class);
        startActivity(intent);
    }

    public void goPetfolioPage(View view){
        Intent intent = new Intent(this, staff_petfolio.class);
        startActivity(intent);
    }

    public void goProfilePage(View view){
        Intent intent = new Intent(this, staff_profile.class);
        startActivity(intent);
    }
}
