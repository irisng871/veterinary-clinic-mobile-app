package com.example.veterinaryclinicmobileapplication;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class staff_profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    ImageButton staffProfileImage;
    Uri imageUri;
    EditText staffName, staffEmail, staffPhoneNumber;
    Button saveBtn, backBtn;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser firebaseUser;
    FirebaseStorage storage;
    StorageReference storageRef;
    String staffId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = auth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        staffProfileImage = findViewById(R.id.profileImage);
        staffName = findViewById(R.id.name);
        staffEmail = findViewById(R.id.email);
        staffPhoneNumber = findViewById(R.id.phoneNumber);
        saveBtn = findViewById(R.id.saveBtn);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackHomePage());

        staffProfileImage.setOnClickListener(v -> openGallery());

        if (firebaseUser == null) {
            Intent intent = new Intent(staff_profile.this, login.class);
            startActivity(intent);
            finish();
        } else {
            loadStaffDetails(firebaseUser.getEmail());
        }

        saveBtn.setOnClickListener(v -> {
            String newName = staffName.getText().toString();
            String newPhoneNumber = staffPhoneNumber.getText().toString();

            updateUserData(newName, newPhoneNumber);

            if (imageUri != null) {
                uploadProfileImage(staffId);
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadStaffDetails(String email) {
        db.collection("staff")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);

                            String name = document.getString("name");
                            String emailFetched = document.getString("email");
                            String phoneNumber = document.getString("phone_number");
                            staffId = document.getString("id");

                            staffName.setText(name);
                            staffEmail.setText(emailFetched);
                            staffPhoneNumber.setText(phoneNumber);

                            loadProfileImage(staffId);
                        } else {
                            Toast.makeText(staff_profile.this, "Profile not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(staff_profile.this, "Failed to fetch profile details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void loadProfileImage(String id) {
        String fileExtension = getFileExtension(imageUri);
        String fileName = "images/" + id + "." + fileExtension;
        StorageReference imageRef = storageRef.child(fileName);

        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Picasso.get()
                    .load(uri)
                    .fit()
                    .centerCrop()
                    .into(staffProfileImage);
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
            staffProfileImage.setImageURI(imageUri);
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

    public void updateUserData(String newName, String newPhoneNumber) {
        if (firebaseUser != null) {
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("name", newName);
            updatedData.put("phone_number", newPhoneNumber);

            db.collection("staff")
                    .whereEqualTo("email", firebaseUser.getEmail())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String docId = task.getResult().getDocuments().get(0).getId();
                            db.collection("staff")
                                    .document(docId)
                                    .update(updatedData)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(staff_profile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(staff_profile.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });
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
                    Toast.makeText(staff_profile.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                    loadProfileImage(userId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(staff_profile.this, "Failed to update profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void goBackHomePage() {
        finish();
    }
}
