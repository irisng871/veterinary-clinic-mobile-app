package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
    ImageButton backBtn;
    RecyclerView vetRecyclerView;
    VetAdapter vetAdapter;
    FirebaseFirestore db;
    FirebaseStorage storage;
    ArrayList<Vet> vetList;
    String selectedVetName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_select_vet);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        nxtBtn = findViewById(R.id.nxtBtn);
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goHomePage());

        vetRecyclerView = findViewById(R.id.vetRecyclerView);
        vetRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        vetList = new ArrayList<>();
        fetchAllVets();

        nxtBtn.setOnClickListener(v -> {
            if (selectedVetName != null) {
                Log.d("SelectedVet", "Selected veterinarian: " + selectedVetName);
                Intent intent = new Intent(booking_select_vet.this, booking.class);
                intent.putExtra("selectedVetName", selectedVetName);
                startActivity(intent);
            } else {
                Toast.makeText(booking_select_vet.this, "Please select a veterinarian", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void fetchAllVets() {
        db.collection("veterinarian").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalVets = queryDocumentSnapshots.size();
                    final int[] fetchedCount = {0};

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String vetId = document.getString("id");
                        String vetName = document.getString("name");
                        String vetSpecialtyArea = document.getString("specialty_area");
                        String[] possibleExtensions = {"png", "jpg"};

                        // Loop to check the image existence
                        for (String extension : possibleExtensions) {
                            String fileName = "images/" + vetId + "." + extension;
                            StorageReference imageRef = storage.getReference().child(fileName);

                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                vetList.add(new Vet(vetId, vetName, uri.toString(), vetSpecialtyArea));
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
        selectedVetName = vetName;
    }

    public void goHomePage() {
        finish();
    }
}
