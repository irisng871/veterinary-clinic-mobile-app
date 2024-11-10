package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class filter extends AppCompatActivity implements BreedAdapter.OnBreedClickListener {

    ImageButton backBtn;
    Button doneBtn;
    Spinner selectPetType, petGender, petNeutered;
    String selectedType, selectedBreed, selectedGender, selectedNeutered;
    ArrayAdapter<String> adapterForPetType, adapterForPetGender, adapterForPetNeutered;
    RecyclerView breedRecyclerView;
    RelativeLayout selectedBreedLayout;
    BreedAdapter breedAdapter;
    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        selectPetType = findViewById(R.id.selectPetType);
        breedRecyclerView = findViewById(R.id.breedRecyclerView);
        doneBtn = findViewById(R.id.doneBtn);
        petGender = findViewById(R.id.selectPetGender);
        petNeutered = findViewById(R.id.selectPetNeutered);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackHomePage());

        String[] typeSelectionOptions = {"Choose the pet type", "cat", "dog"};
        adapterForPetType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeSelectionOptions);
        adapterForPetType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectPetType.setAdapter(adapterForPetType);

        selectPetType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = typeSelectionOptions[position];
                if (!selectedType.equals("Choose the pet type")) {
                    fetchBreeds(selectedType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        setupSpinners();

        doneBtn.setOnClickListener(v -> passToRecommendAdoptablePet());
    }

    public void fetchBreeds(String petType) {
        String formattedPetType = petType.toLowerCase();

        db.collection("pet_type").document(formattedPetType).collection("breed").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Breed> breedList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String breedName = document.getString("name");
                        String breedImageName = document.getString("image");

                        if (breedName != null && breedImageName != null) {
                            StorageReference imageRef = storage.getReference().child("pet_type/" + formattedPetType + "/" + breedImageName);

                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                breedList.add(new Breed(breedName, uri.toString()));

                                breedList.sort((breed1, breed2) -> breed1.getName().compareToIgnoreCase(breed2.getName()));

                                if (breedAdapter != null) {
                                    breedAdapter.notifyDataSetChanged();
                                }
                            }).addOnFailureListener(e -> {});
                        }
                    }

                    breedRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
                    breedAdapter = new BreedAdapter(this, breedList, this);
                    breedRecyclerView.setAdapter(breedAdapter);
                })
                .addOnFailureListener(e -> {});
    }

    public void setupSpinners() {
        String[] genderSelectionOptions = {"Choose the pet gender", "male", "female"};
        adapterForPetGender = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderSelectionOptions);
        adapterForPetGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        petGender.setAdapter(adapterForPetGender);

        petGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGender = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        String[] neuteredSelectionOptions = {"Are the pet neutered?", "yes", "no"};
        adapterForPetNeutered = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, neuteredSelectionOptions);
        adapterForPetNeutered.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        petNeutered.setAdapter(adapterForPetNeutered);

        petNeutered.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedNeutered = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onBreedClick(int position) {
        if (selectedBreedLayout != null) {
            selectedBreedLayout.setBackgroundColor(Color.TRANSPARENT);
        }

        RecyclerView.ViewHolder viewHolder = breedRecyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            selectedBreedLayout = viewHolder.itemView.findViewById(R.id.breedSelection);
            if (selectedBreedLayout != null) {
                selectedBreedLayout.setBackgroundColor(Color.parseColor("#DADADA"));
            }
        }

        if (breedAdapter != null) {
            selectedBreed = breedAdapter.getBreedNameAt(position);
        }
    }

    public void passToRecommendAdoptablePet() {
        Intent intent = new Intent(this, recommend_adoptable_pet.class);

        intent.putExtra("type", selectedType);
        intent.putExtra("breed", selectedBreed);
        intent.putExtra("gender", selectedGender);
        intent.putExtra("neutered", selectedNeutered);

        startActivity(intent);
    }

    private void goBackHomePage() {
        finish();
    }
}
