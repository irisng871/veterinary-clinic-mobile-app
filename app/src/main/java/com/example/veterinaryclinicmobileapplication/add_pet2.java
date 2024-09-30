package com.example.veterinaryclinicmobileapplication;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class add_pet2 extends AppCompatActivity {

    Uri imageUri;

    EditText petName, petWeight, petAllergy;

    String selectedGender, selectedNeutered;

    ImageButton pickDate;

    TextInputEditText petEstimatedBirthday, petEstimatedAge;

    Button doneBtn;

    Spinner petGender, petNeutered;

    ArrayAdapter<String> adapterForPetGender, adapterForPetNeutered;

    FirebaseAuth Auth;

    FirebaseFirestore db;

    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_pet2);

        petName = findViewById(R.id.name);
        petGender = findViewById(R.id.selectPetGender);
        petWeight = findViewById(R.id.weight);
        petEstimatedBirthday = findViewById(R.id.estimatedBirthday);
        pickDate = findViewById(R.id.pickDate);
        petEstimatedAge = findViewById(R.id.estimatedAge);
        petNeutered = findViewById(R.id.selectPetNeutered);
        petAllergy = findViewById(R.id.allergy);
        doneBtn = findViewById(R.id.doneBtn);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

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

        doneBtn.setOnClickListener(new View.OnClickListener() {
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

                String type = intent.getStringExtra("type");
                String breed = intent.getStringExtra("breed");

                // check empty
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(add_pet2.this, "Please enter your pet name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(weight)) {
                    Toast.makeText(add_pet2.this, "Please enter your pet weight", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(estimatedBirthday)) {
                    Toast.makeText(add_pet2.this, "Please enter your pet estimated birthday", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(estimatedAge)) {
                    Toast.makeText(add_pet2.this, "Please enter your pet estimated age", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(allergy)) {
                    Toast.makeText(add_pet2.this, "Please enter your pet allergy", Toast.LENGTH_SHORT).show();
                    return;
                }

                // check format
                if (!name.matches("[a-zA-Z\\s]+") || !allergy.matches("[a-zA-Z\\s]+")) {
                    Toast.makeText(add_pet2.this, "Name and allergy can only contain letters and spaces", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!weight.matches("\\d+")) {
                    Toast.makeText(add_pet2.this, "Weight and estimated age must be digit", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check gender and neutered
                if (gender.matches("Choose your pet gender")) {
                    Toast.makeText(add_pet2.this, "Please choose your pet gender.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (neutered.matches("Are your pet neutered ?")) {
                    Toast.makeText(add_pet2.this, "Please choose your pet neutered status.", Toast.LENGTH_SHORT).show();
                    return;
                }

                generateId(imageUri, type, breed, name, gender, weight, estimatedBirthday, estimatedAge, neutered, allergy);
            }
        });
    }

    private void openDatePicker() {
        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Open DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // When the user selects a date
                String formattedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);

                // Calculate the age
                int age = calculateAge(year, month, dayOfMonth);

                // Update the UI
                petEstimatedBirthday.setText(formattedDate);
                petEstimatedAge.setText(String.valueOf(age));
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private int calculateAge(int year, int month, int dayOfMonth) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, dayOfMonth);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

    private void generateId(Uri imageUri, String type, String breed, String name, String gender, String weight, String estimatedBirthday, String estimatedAge, String neutered, String allergy) {
        db.collection("pet")
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String newId = "PE1";
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String lastId = document.getString("id");
                            if (lastId != null && lastId.startsWith("PE")) {
                                int lastNumber = Integer.parseInt(lastId.substring(2));
                                newId = "PE" + (lastNumber + 1);
                            }
                        }
                        getPetOwnerIdAndSavePet (newId, type, breed, name, gender, weight, estimatedBirthday, estimatedAge, neutered, allergy);
                    }
                });
    }

    private void getPetOwnerIdAndSavePet(String newId, String type, String breed, String name, String gender, String weight, String estimatedBirthday, String estimatedAge, String neutered, String allergy) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("pet_owner").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String petOwnerId = documentSnapshot.getString("id");

                            savePetDetails(newId, type, breed, name, gender, weight, estimatedBirthday, estimatedAge, neutered, allergy, petOwnerId);
                        } else {
                            Toast.makeText(add_pet2.this, "Pet owner document not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(add_pet2.this, "Error fetching pet owner: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(add_pet2.this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }

    public void savePetDetails(String newId, String type, String breed, String name, String gender, String weight, String estimatedBirthday, String estimatedAge, String neutered, String allergy, String petOwnerId) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        Map<String, Object> petData = new HashMap<>();
        petData.put("pet_owner_id", petOwnerId);
        petData.put("id", newId);
        petData.put("type", type);
        petData.put("breed", breed);
        petData.put("name", name);
        petData.put("gender", gender);
        petData.put("weight", weight);
        petData.put("estimated_birthday", estimatedBirthday);
        petData.put("estimated_age", estimatedAge);
        petData.put("neutered", neutered);
        petData.put("allergy", allergy);

        if (imageUri != null) {
            // Determine file extension
            String fileExtension = getFileExtension(imageUri);
            String fileName = "images/" + petOwnerId + "/" + newId + "." + fileExtension;
            StorageReference imageRef = storageRef.child(fileName);

            // Upload the image
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Save image name to petData
                        petData.put("image", fileName);

                        // Save pet data to Firestore
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("pet")
                                .document(newId)
                                .set(petData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(add_pet2.this, "Pet details saved.", Toast.LENGTH_SHORT).show();

                                    // clear text input
                                    TextInputEditText nameText = findViewById(R.id.name);
                                    TextInputEditText weightNumberText = findViewById(R.id.weight);
                                    TextInputEditText estimatedBirthdayText = findViewById(R.id.estimatedBirthday);
                                    TextInputEditText estimatedAgeText = findViewById(R.id.estimatedAge);
                                    TextInputEditText allergyText = findViewById(R.id.allergy);

                                    nameText.getText().clear();
                                    weightNumberText.getText().clear();
                                    estimatedBirthdayText.getText().clear();
                                    estimatedAgeText.getText().clear();
                                    allergyText.getText().clear();

                                    Spinner genderSpinner = findViewById(R.id.selectPetGender);
                                    genderSpinner.setSelection(0);

                                    Spinner neuteredSpinner = findViewById(R.id.selectPetNeutered);
                                    neuteredSpinner.setSelection(0);

                                    // go my_pet
                                    Intent intent = new Intent(getApplicationContext(), my_pet.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(add_pet2.this, "Failed to save pet details: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(add_pet2.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            // No image selected, save data without image
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("pet")
                    .document(newId)
                    .set(petData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(add_pet2.this, "Pet details saved.", Toast.LENGTH_SHORT).show();

                        // clear text input
                        TextInputEditText nameText = findViewById(R.id.name);
                        TextInputEditText weightNumberText = findViewById(R.id.weight);
                        TextInputEditText estimatedBirthdayText = findViewById(R.id.estimatedBirthday);
                        TextInputEditText estimatedAgeText = findViewById(R.id.estimatedAge);
                        TextInputEditText allergyText = findViewById(R.id.allergy);

                        nameText.getText().clear();
                        weightNumberText.getText().clear();
                        estimatedBirthdayText.getText().clear();
                        estimatedAgeText.getText().clear();
                        allergyText.getText().clear();

                        Spinner genderSpinner = findViewById(R.id.selectPetGender);
                        genderSpinner.setSelection(0);

                        Spinner neuteredSpinner = findViewById(R.id.selectPetNeutered);
                        neuteredSpinner.setSelection(0);

                        // go my_pet
                        Intent intent = new Intent(getApplicationContext(), my_pet.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(add_pet2.this, "Failed to save pet details: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        String mimeType = contentResolver.getType(uri);

        if (mimeType != null) {
            switch (mimeType) {
                case "image/jpeg":
                    return "jpg";
                case "image/png":
                    return "png";
                case "image/gif":
                    return "gif";
                case "application/pdf":
                    return "pdf";
                default:
                    return "unknown";
            }
        }
        return "unknown";
    }

    public void goBackAddPet1Page(View view){
        Intent intent = new Intent(this, add_pet1.class);
        TextView ppCheckBox = findViewById(R.id.backBtn);
        startActivity(intent);
    }
}
