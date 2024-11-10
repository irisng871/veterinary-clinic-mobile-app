package com.example.veterinaryclinicmobileapplication;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class edit_pet_profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    Uri imageUri;
    ImageButton profileImageButton, pickDate, backBtn;
    EditText petName, petWeight, petAllergy;
    String selectedGender, selectedNeutered, petId;
    TextInputEditText petEstimatedBirthday, petEstimatedAge;
    Button editBtn;
    Spinner petGender, petNeutered;
    ArrayAdapter<String> adapterForPetGender, adapterForPetNeutered;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_pet_profile);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        Intent intent = getIntent();
        String petId = intent.getStringExtra("id");

        petName = findViewById(R.id.name);
        petGender = findViewById(R.id.selectPetGender);
        petWeight = findViewById(R.id.weight);
        petEstimatedBirthday = findViewById(R.id.estimatedBirthday);
        pickDate = findViewById(R.id.pickDate);
        petEstimatedAge = findViewById(R.id.estimatedAge);
        petNeutered = findViewById(R.id.selectPetNeutered);
        petAllergy = findViewById(R.id.allergy);
        editBtn = findViewById(R.id.editBtn);
        profileImageButton = findViewById(R.id.profileImage);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackPetProfilePage());

        profileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        String[] genderSelectionOptions = {"Choose your pet gender", "male", "female"};
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

        String[] neuteredSelectionOptions = {"Are your pet neutered ?", "yes", "no"};
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

        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        if (petId != null) {
            loadPetDetails(petId);
        } else {
            Toast.makeText(this, "Pet ID is missing", Toast.LENGTH_SHORT).show();
        }

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name, weight, estimatedBirthday, estimatedAge, allergy;

                name = String.valueOf(petName.getText());
                weight = String.valueOf(petWeight.getText());
                estimatedBirthday = String.valueOf(petEstimatedBirthday.getText());
                estimatedAge = String.valueOf(petEstimatedAge.getText());
                allergy = String.valueOf(petAllergy.getText());

                String gender = selectedGender;
                String neutered = selectedNeutered;

                Intent intent = getIntent();
                String imageUriString = intent.getStringExtra("image");
                if (imageUriString != null) {
                    imageUri = Uri.parse(imageUriString);
                }

                // Check empty
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(weight) || TextUtils.isEmpty(allergy) || TextUtils.isEmpty(estimatedBirthday)) {
                    Toast.makeText(edit_pet_profile.this, "Please enter all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check format
                if (!name.matches("[a-zA-Z\\s]+")) {
                    Toast.makeText(edit_pet_profile.this, "Name and allergy can only contain letters and spaces", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!weight.matches("\\d+(\\.\\d+)?")) {
                    Toast.makeText(edit_pet_profile.this, "Weight must be a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check gender and neutered
                if (gender.matches("Choose your pet gender")) {
                    Toast.makeText(edit_pet_profile.this, "Please choose your pet gender.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (neutered.matches("Are your pet neutered ?")) {
                    Toast.makeText(edit_pet_profile.this, "Please choose your pet neutered status.", Toast.LENGTH_SHORT).show();
                    return;
                }

                updatePetData(petId, name, gender, weight, estimatedBirthday, estimatedAge, neutered, allergy);
            }
        });
    }

    public void loadPetDetails(String petId) {
        db.collection("pet").document(petId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String gender = documentSnapshot.getString("gender");
                        String weight = documentSnapshot.getString("weight");
                        String estimatedBirthday = documentSnapshot.getString("estimated_birthday");
                        String estimatedAge = documentSnapshot.getString("estimated_age");
                        String neutered = documentSnapshot.getString("neutered");
                        String allergy = documentSnapshot.getString("allergy");
                        String petOwnerId = documentSnapshot.getString("pet_owner_id");

                        petName.setText(name);
                        petWeight.setText(weight);
                        petEstimatedBirthday.setText(estimatedBirthday);
                        petEstimatedAge.setText(estimatedAge);
                        petAllergy.setText(allergy);

                        if (gender != null) {
                            int genderPosition = adapterForPetGender.getPosition(gender);
                            petGender.setSelection(genderPosition);
                        }

                        if (neutered != null) {
                            int neuteredPosition = adapterForPetNeutered.getPosition(neutered);
                            petNeutered.setSelection(neuteredPosition);
                        }

                        String jpgFileName = "images/" + petOwnerId + "/" + petId + ".jpg";
                        String pngFileName = "images/" + petOwnerId + "/" + petId + ".png";

                        // Try loading .jpg image first
                        StorageReference jpgImageRef = storage.getReference().child(jpgFileName);
                        jpgImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Picasso.get().load(uri).into(profileImageButton);
                        }).addOnFailureListener(e -> {
                            // If .jpg fails, try loading .png image
                            StorageReference pngImageRef = storage.getReference().child(pngFileName);
                            pngImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                Picasso.get().load(uri).into(profileImageButton);
                            }).addOnFailureListener(e1 -> {
                                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                            });
                        });
                    } else {
                        Toast.makeText(this, "Pet not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error getting pet details", Toast.LENGTH_SHORT).show());
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
            profileImageButton.setImageURI(imageUri);

            if (petId != null) {
                String fileExtension = getFileExtension(imageUri);
                String fileName = "images/" + petId + "." + fileExtension;
                StorageReference imageRef = storageRef.child(fileName);

                imageRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            Toast.makeText(edit_pet_profile.this, "Pet profile image updated", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(edit_pet_profile.this, "Failed to update pet profile image", Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }

    public void openDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String formattedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);

                int age = calculateAge(year, month, dayOfMonth);

                petEstimatedBirthday.setText(formattedDate);
                petEstimatedAge.setText(String.valueOf(age));
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    public int calculateAge(int year, int month, int dayOfMonth) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, dayOfMonth);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

    public void updatePetData (String petId, String newName, String newGender, String newWeight, String newEstimatedBirthday, String newEstimatedAge, String newNeutered, String newAllergy) {
        if (petId != null) {
            DocumentReference documentReference = db.collection("pet")
                    .document(petId);

            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("name", newName);
            updatedData.put("gender", newGender);
            updatedData.put("weight", newWeight);
            updatedData.put("estimated_birthday", newEstimatedBirthday);
            updatedData.put("estimated_age", newEstimatedAge);
            updatedData.put("neutered", newNeutered);
            updatedData.put("allergy", newAllergy);

            documentReference.update(updatedData)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(edit_pet_profile.this, "New data updated successfully", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(getApplicationContext(), my_pet.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(edit_pet_profile.this, "Failed to update new data", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    public String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        String mimeType = contentResolver.getType(uri);

        if (mimeType != null) {
            switch (mimeType) {
                case "image/jpeg":
                    return "jpeg";
                case "image/png":
                    return "png";
                case "image/jpg":
                    return "jpg";
                default:
                    return "unknown";
            }
        }
        return "unknown";
    }

    public void goBackPetProfilePage() {
        finish();
    }
}
