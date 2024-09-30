package com.example.veterinaryclinicmobileapplication;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class vet_profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    ImageButton vetProfileImage;

    Uri imageUri;

    EditText vetName, vetEmail, vetPhoneNumber, vetSpecialtyArea;

    Button saveBtn;

    FirebaseAuth Auth;

    FirebaseFirestore db;

    FirebaseUser vet;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vet_profile);

        Auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        vet = Auth.getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        vetProfileImage = findViewById(R.id.profileImage);
        vetName = findViewById(R.id.name);
        vetEmail = findViewById(R.id.email);
        vetPhoneNumber = findViewById(R.id.phoneNumber);
        vetSpecialtyArea = findViewById(R.id.specialtyArea);
        saveBtn = findViewById(R.id.saveBtn);

        vetProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        if (vet == null) {
            Intent intent = new Intent(vet_profile.this, login.class);
            startActivity(intent);
            finish();
        } else {
            db.collection("veterinarian")
                    .document(vet.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    String name = document.getString("name");
                                    String email = document.getString("email");
                                    String phoneNumber = document.getString("phone_number");
                                    String specialtyArea = document.getString("specialty_area");

                                    vetName.setText(name);
                                    vetEmail.setText(email);
                                    vetPhoneNumber.setText(phoneNumber);
                                    vetSpecialtyArea.setText(specialtyArea);

                                    // Load profile image from Firebase Storage
                                    String id = vet.getUid();

                                    // Assuming the image is stored with a known file extension
                                    String[] possibleExtensions = {"jpg", "png", "gif"};

                                    for (String extension : possibleExtensions) {
                                        String fileName = "images/" + id + "." + extension;
                                        StorageReference imageRef = storageRef.child(fileName);

                                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                            Picasso.get().load(uri).into(vetProfileImage);
                                        }).addOnFailureListener(exception -> {
                                            // Log or handle the failure silently; continue to try the next extension
                                        });
                                    }
                                } else {
                                    Toast.makeText(vet_profile.this, "Document does not exist", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(vet_profile.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = ((TextInputEditText) findViewById(R.id.name)).getText().toString();
                String newPhoneNumber = ((TextInputEditText) findViewById(R.id.phoneNumber)).getText().toString();
                String newSpecialtyArea = ((TextInputEditText) findViewById(R.id.specialtyArea)).getText().toString();

                updateUserData (newName, newPhoneNumber, newSpecialtyArea);
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            vetProfileImage.setImageURI(imageUri);

            if (vet != null) {
                String id = vet.getUid();
                String fileExtension = getFileExtension(imageUri);
                String fileName = "images/" + id + "." + fileExtension;
                StorageReference imageRef = storageRef.child(fileName);

                imageRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            Toast.makeText(vet_profile.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(vet_profile.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                        });
            }
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

    public void updateUserData (String newName, String newPhoneNumber, String newSpecialtyArea) {
        if (vet != null) {
            DocumentReference documentReference = db.collection("veterinarian")
                    .document(vet.getUid());

            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("name", newName);
            updatedData.put("phone_number", newPhoneNumber);
            updatedData.put("specialty_rea", newSpecialtyArea);

            documentReference.update(updatedData)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(vet_profile.this, "New data updated successfully", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(vet_profile.this, "Failed to update new data", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    public void goBackHomePage(View view){
        Intent intent = new Intent(this, home.class);
        Button backBtn = findViewById(R.id.backBtn);
        startActivity(intent);
    }
}