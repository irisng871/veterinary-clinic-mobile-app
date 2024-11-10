package com.example.veterinaryclinicmobileapplication;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    String selectedGender, selectedNeutered, type, breed;
    ImageButton pickDate, backBtn;
    TextInputEditText petEstimatedBirthday, petEstimatedAge;
    Button doneBtn;
    Spinner petGender, petNeutered;
    ArrayAdapter<String> adapterForPetGender, adapterForPetNeutered;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_pet2);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        petName = findViewById(R.id.name);
        petGender = findViewById(R.id.selectPetGender);
        petWeight = findViewById(R.id.weight);
        petEstimatedBirthday = findViewById(R.id.estimatedBirthday);
        pickDate = findViewById(R.id.pickDate);
        petEstimatedAge = findViewById(R.id.estimatedAge);
        petNeutered = findViewById(R.id.selectPetNeutered);
        petAllergy = findViewById(R.id.allergy);

        Intent intent = getIntent();
        String imageUriString = intent.getStringExtra("image");
        if (imageUriString != null) {
            imageUri = Uri.parse(imageUriString);
        }

        type = intent.getStringExtra("type");
        breed = intent.getStringExtra("breed");

        doneBtn = findViewById(R.id.doneBtn);
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackAddPet1Page());

        pickDate.setOnClickListener(v -> openMonthYearPicker());
        doneBtn.setOnClickListener(v -> validateAndSavePet());

        setupSpinners();
    }

    public void setupSpinners() {
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
            public void onNothingSelected(AdapterView<?> parent) {}
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
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void openMonthYearPicker() {
        month_year_picker monthYearPicker = new month_year_picker(this, (month, year) -> {
            petEstimatedBirthday.setText(month + " " + year);
            int selectedMonth = getMonthFromString(month);
            int selectedYearInt = Integer.parseInt(year);
            int[] calculatedAge = calculateAge(selectedYearInt, selectedMonth);
            String formattedAge = formatAge(calculatedAge[0], calculatedAge[1]);
            petEstimatedAge.setText(formattedAge);
        });
        monthYearPicker.show();
    }

    public void validateAndSavePet() {
        String name = petName.getText().toString();
        String weight = petWeight.getText().toString();
        String estimatedBirthday = petEstimatedBirthday.getText().toString();
        String estimatedAge = petEstimatedAge.getText().toString();
        String allergy = petAllergy.getText().toString();

        // Check empty
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(weight) || TextUtils.isEmpty(allergy) || TextUtils.isEmpty(estimatedBirthday)) {
            Toast.makeText(this, "Please enter all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check format
        if (!name.matches("[a-zA-Z\\s]+")) {
            Toast.makeText(this, "Name can only contain letters and spaces", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!weight.matches("^\\d+(\\.\\d{1})?$")) {
            Toast.makeText(this, "Weight must be a valid number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check gender and neutered
        if (selectedGender.equals("Choose the pet gender")) {
            Toast.makeText(this, "Please choose the pet gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedNeutered.equals("Are the pet neutered ?")) {
            Toast.makeText(this, "Please choose the pet neutered status", Toast.LENGTH_SHORT).show();
            return;
        }

        generateId(type, breed, name, selectedGender, weight, estimatedBirthday, estimatedAge, selectedNeutered, allergy);
    }

    public void generateId(String type, String breed, String name, String gender, String weight, String estimatedBirthday, String estimatedAge, String neutered, String allergy) {
        db.collection("pet")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int maxNumber = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String lastId = document.getString("id");
                            if (lastId != null && lastId.startsWith("PE")) {
                                String numberPart = lastId.substring(2);
                                try {
                                    int number = Integer.parseInt(numberPart);
                                    if (number > maxNumber) {
                                        maxNumber = number;
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("ID Parsing Error", "Error parsing ID: " + lastId, e);
                                }
                            }
                        }

                        String newId = "PE" + (maxNumber + 1);
                        Log.d("New ID", newId);
                        getPetOwnerIdAndSavePet(newId, type, breed, name, gender, weight, estimatedBirthday, estimatedAge, neutered, allergy);
                    } else {
                        Log.e("Firestore Error", "Error getting documents", task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error retrieving data", e);
                });
    }

    public void getPetOwnerIdAndSavePet(String newId, String type, String breed, String name, String gender, String weight, String estimatedBirthday, String estimatedAge, String neutered, String allergy) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
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
            String fileExtension = getFileExtension(imageUri);
            String fileName = "images/" + petOwnerId + "/" + newId + "." + fileExtension;
            StorageReference imageRef = storageRef.child(fileName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        petData.put("image", fileName);

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

    public int getMonthFromString(String month) {
        switch (month) {
            case "January": return 0;
            case "February": return 1;
            case "March": return 2;
            case "April": return 3;
            case "May": return 4;
            case "June": return 5;
            case "July": return 6;
            case "August": return 7;
            case "September": return 8;
            case "October": return 9;
            case "November": return 10;
            case "December": return 11;
            default: return -1;
        }
    }

    public int[] calculateAge(int year, int month) {
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH);
        int ageInYears = currentYear - year;
        int ageInMonths = currentMonth - month;
        if (ageInMonths < 0) {
            ageInYears--;
            ageInMonths += 12;
        }
        return new int[]{ageInYears, ageInMonths};
    }

    public String formatAge(int years, int months) {
        if (years > 0) {
            return years + " years " + months + " months";
        } else {
            return months + " months";
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

    public void goBackAddPet1Page() {
        finish();
    }
}
