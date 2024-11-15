package com.example.veterinaryclinicmobileapplication;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class staff_add_pet1 extends AppCompatActivity implements BreedAdapter.OnBreedClickListener {

    private static final int PICK_IMAGE_REQUEST = 1;
    ImageButton petProfileImage, backBtn;
    Button nextBtn;
    Uri imageUri;
    Spinner selectPetType;
    String selectedType, selectedBreed;
    ArrayAdapter<String> adapterForPetType;
    RecyclerView breedRecyclerView;
    RelativeLayout selectedBreedLayout;
    BreedAdapter breedAdapter;
    FirebaseFirestore db;
    FirebaseStorage storage;
    RelativeLayout breedSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_add_pet1);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        petProfileImage = findViewById(R.id.profileImage);
        selectPetType = findViewById(R.id.selectPetType);
        breedRecyclerView = findViewById(R.id.breedRecyclerView);
        nextBtn = findViewById(R.id.nextBtn);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackPetfolioPage());

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

        petProfileImage.setOnClickListener(v -> openGallery());
        nextBtn.setOnClickListener(v -> passToStaffAddPet2());
        backBtn.setOnClickListener(v -> goBackPetfolioPage());

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passToStaffAddPet2();
            }
        });
    }

    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            petProfileImage.setImageURI(imageUri);
        }
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

    public void passToStaffAddPet2() {
        if (selectedType == null || selectedType.equals("Choose your pet type")) {
            Toast.makeText(this, "Please select a pet type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedBreed == null) {
            Toast.makeText(this, "Please select a pet breed", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, staff_add_pet2.class);

        if (imageUri != null) {
            intent.putExtra("image", imageUri.toString());
        }
        intent.putExtra("type", selectedType);
        intent.putExtra("breed", selectedBreed);

        startActivity(intent);
    }

    public void goBackPetfolioPage() {
        finish();
    }
}
