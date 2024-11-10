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

public class my_profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    ImageButton userProfileImage;
    Uri imageUri;
    EditText userName, userEmail, userPhoneNumber, userEmergencyContact, userEmergencyPhoneNumber;
    Button saveBtn, backBtn;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser firebaseUser;
    FirebaseStorage storage;
    StorageReference storageRef;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = auth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        userProfileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.name);
        userEmail = findViewById(R.id.email);
        userPhoneNumber = findViewById(R.id.phoneNumber);
        userEmergencyContact = findViewById(R.id.emergencyContact);
        userEmergencyPhoneNumber = findViewById(R.id.emergencyPhoneNumber);
        saveBtn = findViewById(R.id.saveBtn);
        backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> goBackHomePage());

        userProfileImage.setOnClickListener(v -> openGallery());

        if (firebaseUser == null) {
            Intent intent = new Intent(my_profile.this, login.class);
            startActivity(intent);
            finish();
        } else {
            loadUserData();
        }

        saveBtn.setOnClickListener(v -> {
            String newName = userName.getText().toString();
            String newPhoneNumber = userPhoneNumber.getText().toString();
            String newEmergencyContact = userEmergencyContact.getText().toString();
            String newEmergencyPhoneNumber = userEmergencyPhoneNumber.getText().toString();

            updateUserData(newName, newPhoneNumber, newEmergencyContact, newEmergencyPhoneNumber);
        });
    }

    public void loadUserData() {
        db.collection("pet_owner")
                .document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            userName.setText(document.getString("name"));
                            userEmail.setText(document.getString("email"));
                            userPhoneNumber.setText(document.getString("phone_number"));
                            userEmergencyContact.setText(document.getString("emergency_contact"));
                            userEmergencyPhoneNumber.setText(document.getString("emergency_phone_number"));

                            loadProfileImage(firebaseUser.getUid());
                        } else {
                            Toast.makeText(my_profile.this, "Document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(my_profile.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void loadProfileImage(String userId) {
        String[] possibleExtensions = {"jpg", "png", "jpeg"};

        for (String extension : possibleExtensions) {
            String fileName = "images/" + userId + "." + extension;
            StorageReference imageRef = storageRef.child(fileName);

            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Picasso.get().load(uri).into(userProfileImage);
            }).addOnFailureListener(exception -> {
                Log.e("ImageLoad", "Failed to load image with extension: " + extension);
            });
        }
    }

    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            userProfileImage.setImageURI(imageUri);

            if (firebaseUser != null) {
                uploadProfileImage(firebaseUser.getUid());
            }
        }
    }

    public void uploadProfileImage(String userId) {
        if (imageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileExtension = getFileExtension(imageUri);
        String fileName = "images/" + userId + "." + fileExtension;
        StorageReference imageRef = storageRef.child(fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(my_profile.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                    loadProfileImage(userId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(my_profile.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                });
    }

    public String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        String mimeType = contentResolver.getType(uri);

        if (mimeType != null) {
            switch (mimeType) {
                case "image/jpeg":
                    return "jpeg";
                case "image/jpg":
                    return "jpg";
                case "image/png":
                    return "png";
                default:
                    return "unknown";
            }
        }
        return "unknown";
    }

    public void updateUserData(String newName, String newPhoneNumber, String newEmergencyContact, String newEmergencyPhoneNumber) {
        if (firebaseUser != null) {
            DocumentReference documentReference = db.collection("pet_owner")
                    .document(firebaseUser.getUid());

            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("name", newName);
            updatedData.put("phone_number", newPhoneNumber);
            updatedData.put("emergency_contact", newEmergencyContact);
            updatedData.put("emergency_phone_number", newEmergencyPhoneNumber);

            documentReference.update(updatedData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(my_profile.this, "New data updated successfully", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(my_profile.this, "Failed to update new data", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    public void goBackHomePage() {
        finish();
    }
}
