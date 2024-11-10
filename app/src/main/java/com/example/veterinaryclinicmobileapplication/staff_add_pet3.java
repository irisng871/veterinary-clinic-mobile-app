package com.example.veterinaryclinicmobileapplication;

import android.app.DatePickerDialog;
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

public class staff_add_pet3 extends AppCompatActivity {

    Uri imageUri;
    EditText petPersonality, petHealthStatus, petAllergy, petHistory;
    Button doneBtn;
    ImageButton backBtn;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_add_pet3);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        petPersonality = findViewById(R.id.personality);
        petHealthStatus = findViewById(R.id.healthStatus);
        petAllergy = findViewById(R.id.allergy);
        petHistory = findViewById(R.id.history);
        doneBtn = findViewById(R.id.doneBtn);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackStaffAddPet2Page());

        Intent intent = getIntent();
        String imageUriString = intent.getStringExtra("image");
        if (imageUriString != null) {
            imageUri = Uri.parse(imageUriString);
        }

        String type = intent.getStringExtra("type");
        String breed = intent.getStringExtra("breed");
        String name = intent.getStringExtra("name");
        String gender = intent.getStringExtra("gender");
        String weight = intent.getStringExtra("weight");
        String estimatedBirthday = intent.getStringExtra("estimated_birthday");
        String estimatedAge = intent.getStringExtra("estimated_age");
        String neutered = intent.getStringExtra("neutered");

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String personality, healthStatus, allergy, history;

                personality = String.valueOf(petPersonality.getText());
                healthStatus = String.valueOf(petHealthStatus.getText());
                allergy = String.valueOf(petAllergy.getText());
                history = String.valueOf(petHistory.getText());

                // check empty
                if (TextUtils.isEmpty(personality) || TextUtils.isEmpty(healthStatus) || TextUtils.isEmpty(allergy) || TextUtils.isEmpty(history)) {
                    Toast.makeText(staff_add_pet3.this, "Please enter all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                generateId(type, breed, name, gender, weight, estimatedBirthday, estimatedAge, neutered, personality, healthStatus, allergy, history);
            }
        });
    }

    public void generateId(String type, String breed, String name, String gender, String weight, String estimatedBirthday, String estimatedAge, String neutered, String personality, String healthStatus, String allergy, String history) {
        db.collection("adoptable_pet")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int maxNumber = 0; // To track the highest number found

                        // Loop through each document to find the highest ID
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String lastId = document.getString("id");
                            if (lastId != null && lastId.startsWith("AP")) {
                                String numberPart = lastId.substring(2);
                                try {
                                    int number = Integer.parseInt(numberPart);
                                    // Update maxNumber if this number is greater
                                    if (number > maxNumber) {
                                        maxNumber = number;
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("ID Parsing Error", "Error parsing ID: " + lastId, e);
                                }
                            }
                        }

                        // Generate the new ID by incrementing the maxNumber found
                        String newId = "AP" + (maxNumber + 1);
                        Log.d("New ID", newId);
                        savePetDetails(newId, type, breed, name, gender, weight, estimatedBirthday, estimatedAge, neutered, personality, healthStatus, allergy, history);
                    } else {
                        Log.e("Firestore Error", "Error getting documents", task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error retrieving data", e);
                });
    }

    public void savePetDetails(String newId, String type, String breed, String name, String gender, String weight, String estimatedBirthday, String estimatedAge, String neutered, String personality, String healthStatus, String allergy, String history) {
        Map<String, Object> petData = new HashMap<>();
        petData.put("id", newId);
        petData.put("type", type);
        petData.put("breed", breed);
        petData.put("name", name);
        petData.put("gender", gender);
        petData.put("weight", weight);
        petData.put("estimated_birthday", estimatedBirthday);
        petData.put("estimated_age", estimatedAge);
        petData.put("neutered", neutered);
        petData.put("personality", personality);
        petData.put("health_status", healthStatus);
        petData.put("allergy", allergy);
        petData.put("history", history);

        if (imageUri != null) {
            String fileExtension = getFileExtension(imageUri);
            String fileName = "images/" + newId + "." + fileExtension;
            StorageReference imageRef = storageRef.child(fileName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        petData.put("image", fileName);

                        db.collection("adoptable_pet")
                                .document(newId)
                                .set(petData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(staff_add_pet3.this, "Pet details saved.", Toast.LENGTH_SHORT).show();

                                    // clear text input
                                    TextInputEditText personalityText = findViewById(R.id.personality);
                                    TextInputEditText healthStatusText = findViewById(R.id.healthStatus);
                                    TextInputEditText allergyText = findViewById(R.id.allergy);
                                    TextInputEditText historyAgeText = findViewById(R.id.history);

                                    personalityText.getText().clear();
                                    healthStatusText.getText().clear();
                                    allergyText.getText().clear();
                                    historyAgeText.getText().clear();

                                    // go staff_petfolio
                                    Intent intent = new Intent(getApplicationContext(), staff_petfolio.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(staff_add_pet3.this, "Failed to save pet details: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(staff_add_pet3.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            // No image selected, save data without image
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("adoptable_pet")
                    .document(newId)
                    .set(petData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(staff_add_pet3.this, "Pet details saved.", Toast.LENGTH_SHORT).show();

                        // clear text input
                        TextInputEditText personalityText = findViewById(R.id.personality);
                        TextInputEditText healthStatusText = findViewById(R.id.healthStatus);
                        TextInputEditText allergyText = findViewById(R.id.allergy);
                        TextInputEditText historyAgeText = findViewById(R.id.history);

                        personalityText.getText().clear();
                        healthStatusText.getText().clear();
                        allergyText.getText().clear();
                        historyAgeText.getText().clear();

                        // go staff_petfolio
                        Intent intent = new Intent(getApplicationContext(), staff_petfolio.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(staff_add_pet3.this, "Failed to save pet details: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

    public void goBackStaffAddPet2Page(){
        finish();
    }
}
