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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class vet_profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    ImageButton vetProfileImage;
    Uri imageUri;
    EditText vetName, vetEmail, vetPhoneNumber, vetSpecialtyArea;
    Button saveBtn, backBtn;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser firebaseUser;
    FirebaseStorage storage;
    StorageReference storageRef;
    String currentVetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vet_profile);

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
        saveBtn = findViewById(R.id.saveBtn);
        backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> goBackHomePage());

        vetProfileImage.setOnClickListener(v -> openGallery());

        if (firebaseUser == null) {
            Intent intent = new Intent(vet_profile.this, login.class);
            startActivity(intent);
            finish();
        } else {
            loadVetDetails(firebaseUser.getEmail());
        }

        saveBtn.setOnClickListener(v -> {
            String newName = vetName.getText().toString();
            String newPhoneNumber = vetPhoneNumber.getText().toString();
            String newSpecialtyArea = vetSpecialtyArea.getText().toString();

            updateUserData(newName, newPhoneNumber, newSpecialtyArea);

            if (imageUri != null) {
                uploadProfileImage(currentVetId);
            }
        });
    }

    public void loadVetDetails(String email) {
        db.collection("veterinarian")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);

                            currentVetId = document.getString("id");
                            String name = document.getString("name");
                            String emailFetched = document.getString("email");
                            String phoneNumber = document.getString("phone_number");
                            String specialtyArea = document.getString("specialty_area");

                            vetName.setText(name);
                            vetEmail.setText(emailFetched);
                            vetPhoneNumber.setText(phoneNumber);
                            vetSpecialtyArea.setText(specialtyArea);

                            loadProfileImage(currentVetId);
                        } else {
                            Toast.makeText(vet_profile.this, "Profile not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(vet_profile.this, "Failed to fetch profile details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void loadProfileImage(String vetId) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        String fileExtension = getFileExtension(imageUri);
        String fileName = "images/" + vetId + "." + fileExtension;
        StorageReference imageRef = storageRef.child(fileName);

        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Picasso.get().load(uri).into(vetProfileImage);
        }).addOnFailureListener(exception -> {
            Log.e("ImageLoad", "Failed to load image: " + exception.getMessage());
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

    public void uploadProfileImage(String vetId) {
        if (imageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileExtension = getFileExtension(imageUri);
        String fileName = "images/" + vetId + "." + fileExtension;
        StorageReference imageRef = storageRef.child(fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(vet_profile.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                    loadProfileImage(vetId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(vet_profile.this, "Failed to update profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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

    public void updateUserData(String newName, String newPhoneNumber, String newSpecialtyArea) {
        db.collection("veterinarian")
                .whereEqualTo("email", firebaseUser.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String docId = task.getResult().getDocuments().get(0).getId();
                        db.collection("veterinarian")
                                .document(docId)
                                .update("name", newName, "phone_number", newPhoneNumber, "specialty_area", newSpecialtyArea)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(vet_profile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(vet_profile.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }

    public void goBackHomePage() {
        finish();
    }
}
