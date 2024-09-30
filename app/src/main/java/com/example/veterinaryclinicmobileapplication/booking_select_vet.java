package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;

public class booking_select_vet extends AppCompatActivity implements VetAdapter.OnVetClickListener {

    Button nxtBtn;
    RecyclerView vetRecyclerView;
    VetAdapter vetAdapter;
    FirebaseFirestore db;
    FirebaseStorage storage;
    ArrayList<Vet> vetList;
    String selectedVetName = null; // To store the selected vet name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_select_vet);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        nxtBtn = findViewById(R.id.nxtBtn);
        vetRecyclerView = findViewById(R.id.vetRecyclerView);
        vetRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        vetList = new ArrayList<>();
        fetchAllVets();

        nxtBtn.setOnClickListener(v -> {
            if (selectedVetName != null) {
                Log.d("SelectedVet", "Selected veterinarian: " + selectedVetName); // Debug log
                Intent intent = new Intent(booking_select_vet.this, booking.class);
                intent.putExtra("selectedVetName", selectedVetName);
                startActivity(intent);
            } else {
                Toast.makeText(booking_select_vet.this, "Please select a veterinarian.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAllVets() {
        db.collection("veterinarian").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalVets = queryDocumentSnapshots.size();
                    final int[] fetchedCount = {0}; // Counter for fetched images

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String vetId = document.getId(); // Get the document ID
                        String vetName = document.getString("name");
                        String[] possibleExtensions = {"png", "jpg"};

                        for (String extension : possibleExtensions) {
                            String fileName = "images/" + vetId + "." + extension;
                            StorageReference imageRef = storage.getReference().child(fileName);

                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                vetList.add(new Vet(vetId, vetName, uri.toString()));
                                fetchedCount[0]++; // Increment fetched count
                                if (fetchedCount[0] == totalVets * possibleExtensions.length) {
                                    vetAdapter = new VetAdapter(this, vetList, this);
                                    vetRecyclerView.setAdapter(vetAdapter);
                                }
                            }).addOnFailureListener(e -> {
                                Log.e("FetchVets", "Error getting image URL for vet: " + vetName + " with extension: " + extension, e);
                                fetchedCount[0]++; // Increment regardless of success
                                if (fetchedCount[0] == totalVets * possibleExtensions.length) {
                                    vetAdapter = new VetAdapter(this, vetList, this);
                                    vetRecyclerView.setAdapter(vetAdapter);
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FetchVets", "Error fetching vets", e);
                });
    }

    @Override
    public void onVetClick(String vetName) {
        selectedVetName = vetName; // Store the selected vet name when clicked
    }

    // Navigation buttons
    public void goMyPetPage(View view) {
        Intent intent = new Intent(this, my_pet.class);
        startActivity(intent);
    }

    public void goHomePage(View view) {
        Intent intent = new Intent(this, home.class);
        startActivity(intent);
    }

    public void goCalendarPage(View view) {
        Intent intent = new Intent(this, calendar.class);
        startActivity(intent);
    }

    public void goProfilePage(View view) {
        Intent intent = new Intent(this, my_profile.class);
        startActivity(intent);
    }
}
