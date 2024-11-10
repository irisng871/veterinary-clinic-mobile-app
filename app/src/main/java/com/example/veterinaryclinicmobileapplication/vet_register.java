package com.example.veterinaryclinicmobileapplication;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class vet_register extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    ImageButton vetProfileImage;
    Uri imageUri;
    EditText vetName, vetEmail, vetPhoneNumber, vetSpecialtyArea, vetPassword;
    Button registerBtn, backBtn;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser firebaseUser;
    FirebaseStorage storage;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vet_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = auth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        vetProfileImage = findViewById(R.id.profileImage);
        vetName = findViewById(R.id.name);
        vetEmail = findViewById(R.id.email);
        vetPhoneNumber = findViewById(R.id.phoneNumber);
        vetSpecialtyArea = findViewById(R.id.specialtyArea);
        vetPassword = findViewById(R.id.password);
        registerBtn = findViewById(R.id.registerBtn);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackLoginPage());

        vetProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name, email, phoneNumber, specialtyArea, password;

                name = String.valueOf(vetName.getText());
                email = String.valueOf(vetEmail.getText());
                phoneNumber = String.valueOf(vetPhoneNumber.getText());
                specialtyArea = String.valueOf(vetSpecialtyArea.getText());
                password = String.valueOf(vetPassword.getText());

                // check empty
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phoneNumber)
                        || TextUtils.isEmpty(specialtyArea) || TextUtils.isEmpty(password)) {
                    Toast.makeText(vet_register.this, "Please enter all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // check format
                if (!name.matches("[a-zA-Z\\.\\s]+")) {
                    Toast.makeText(vet_register.this, "Name can only contain letters, spaces, and dot", Toast.LENGTH_SHORT).show();
                }

                if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    Toast.makeText(vet_register.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                } else if (!email.endsWith("@vet.com")) {
                    Toast.makeText(vet_register.this, "Email must end with @vet.com", Toast.LENGTH_SHORT).show();
                }

                if (!phoneNumber.matches("\\d{10,11}")) {
                    Toast.makeText(vet_register.this, "Phone number must be 10 or 11 digits", Toast.LENGTH_SHORT).show();
                }

                if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$")) {
                    Toast.makeText(vet_register.this, "Min 6 characters (upper, lower, number, special character)"
                            , Toast.LENGTH_SHORT).show();
                }

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(vet_register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = auth.getCurrentUser();
                                    generateId(name, email, phoneNumber, specialtyArea, firebaseUser.getUid());
                                } else {
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        Toast.makeText(vet_register.this, "Vet already exists", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(vet_register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
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
            vetProfileImage.setImageURI(imageUri);
        }
    }

    public void generateId(String name, String email, String phoneNumber, String specialtyArea, String id) {
        db.collection("veterinarian")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int maxNumber = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String lastId = document.getString("id");
                            if (lastId != null && lastId.startsWith("VE")) {
                                String numberPart = lastId.substring(2);
                                try {
                                    int number = Integer.parseInt(numberPart);
                                    if (number > maxNumber) {
                                        maxNumber = number;
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("ID Parsing Error", "Error parsing vet ID: " + lastId, e);
                                }
                            }
                        }

                        String newId = "VE" + (maxNumber + 1);
                        Log.d("New Vet ID", newId);

                        saveData(newId, id, name, email, phoneNumber, specialtyArea);
                    } else {
                        Log.e("Firestore Error", "Error getting vet documents", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore Error", "Error retrieving vet data", e));
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

    public void saveData(String vetId, String id, String name, String email, String phoneNumber, String specialtyArea) {
        Map<String, Object> vetData = new HashMap<>();
        vetData.put("id", id);
        vetData.put("name", name);
        vetData.put("email", email);
        vetData.put("phone_number", phoneNumber);
        vetData.put("specialty_area", specialtyArea);

        if (imageUri != null) {
            String fileExtension = getFileExtension(imageUri);
            String fileName = "images/" + vetId + "." + fileExtension;
            StorageReference imageRef = storageRef.child(fileName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        vetData.put("image", fileName);

                        db.collection("veterinarian")
                                .add(vetData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(vet_register.this, "Vet data saved.", Toast.LENGTH_SHORT).show();

                                    TextInputEditText vetName = findViewById(R.id.name);
                                    TextInputEditText vetEmail = findViewById(R.id.email);
                                    TextInputEditText vetPhoneNumber = findViewById(R.id.phoneNumber);
                                    TextInputEditText vetSpecialtyArea = findViewById(R.id.password);
                                    TextInputEditText vetPassword = findViewById(R.id.password);

                                    vetProfileImage.setImageResource(R.drawable.ic_launcher_foreground);
                                    vetName.getText().clear();
                                    vetEmail.getText().clear();
                                    vetPhoneNumber.getText().clear();
                                    vetSpecialtyArea.getText().clear();
                                    vetPassword.getText().clear();

                                    Intent intent = new Intent(getApplicationContext(), vet_home.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(vet_register.this, "Failed to save vet data: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(vet_register.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            // No image selected, save data without image
            db.collection("veterinarian")
                    .add(vetData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(vet_register.this, "Vet data saved.", Toast.LENGTH_SHORT).show();

                        TextInputEditText vetName = findViewById(R.id.name);
                        TextInputEditText vetEmail = findViewById(R.id.email);
                        TextInputEditText vetPhoneNumber = findViewById(R.id.phoneNumber);
                        TextInputEditText vetSpecialtyArea = findViewById(R.id.password);
                        TextInputEditText vetPassword = findViewById(R.id.password);

                        vetProfileImage.setImageResource(R.drawable.ic_launcher_foreground);
                        vetName.getText().clear();
                        vetEmail.getText().clear();
                        vetPhoneNumber.getText().clear();
                        vetSpecialtyArea.getText().clear();
                        vetPassword.getText().clear();

                        Intent intent = new Intent(getApplicationContext(), vet_home.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(vet_register.this, "Failed to save vet data: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    public void goBackLoginPage(){
        finish();
    }
}
