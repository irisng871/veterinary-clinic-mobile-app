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

public class staff_profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    ImageButton staffProfileImage;

    Uri imageUri;

    EditText staffName, staffEmail, staffPhoneNumber;

    Button saveBtn;

    FirebaseAuth Auth;

    FirebaseFirestore db;

    FirebaseUser staff;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_profile);

        Auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        staff = Auth.getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        staffProfileImage = findViewById(R.id.profileImage);
        staffName = findViewById(R.id.name);
        staffEmail = findViewById(R.id.email);
        staffPhoneNumber = findViewById(R.id.phoneNumber);
        saveBtn = findViewById(R.id.saveBtn);

        staffProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        if (staff == null) {
            Intent intent = new Intent(staff_profile.this, login.class);
            startActivity(intent);
            finish();
        } else {
            db.collection("staff")
                    .document(staff.getUid())
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
                                    String emergencyContact = document.getString("emergency_contact");
                                    String emergencyPhoneNumber = document.getString("emergency_phone_number");

                                    staffName.setText(name);
                                    staffEmail.setText(email);
                                    staffPhoneNumber.setText(phoneNumber);

                                    // Load profile image from Firebase Storage
                                    String id = staff.getUid();

                                    // Assuming the image is stored with a known file extension
                                    String[] possibleExtensions = {"jpg", "png", "gif"};

                                    for (String extension : possibleExtensions) {
                                        String fileName = "images/" + id + "." + extension;
                                        StorageReference imageRef = storageRef.child(fileName);

                                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                            Picasso.get().load(uri).into(staffProfileImage);
                                        }).addOnFailureListener(exception -> {
                                            // Log or handle the failure silently; continue to try the next extension
                                        });
                                    }
                                } else {
                                    Toast.makeText(staff_profile.this, "Document does not exist", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(staff_profile.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = ((TextInputEditText) findViewById(R.id.name)).getText().toString();
                String newPhoneNumber = ((TextInputEditText) findViewById(R.id.phoneNumber)).getText().toString();
                String newEmergencyContact = ((TextInputEditText) findViewById(R.id.emergencyContact)).getText().toString();
                String newEmergencyPhoneNumber = ((TextInputEditText) findViewById(R.id.emergencyPhoneNumber)).getText().toString();

                updateUserData (newName, newPhoneNumber, newEmergencyContact, newEmergencyPhoneNumber);
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
            staffProfileImage.setImageURI(imageUri);

            if (staff != null) {
                String id = staff.getUid();
                String fileExtension = getFileExtension(imageUri);
                String fileName = "images/" + id + "." + fileExtension;
                StorageReference imageRef = storageRef.child(fileName);

                imageRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            Toast.makeText(staff_profile.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(staff_profile.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
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

    public void updateUserData (String newName, String newPhoneNumber, String newEmergencyContact, String newEmergencyPhoneNumber) {
        if (staff != null) {
            DocumentReference documentReference = db.collection("staff")
                    .document(staff.getUid());

            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("name", newName);
            updatedData.put("phone_number", newPhoneNumber);
            updatedData.put("emergency_contact", newEmergencyContact);
            updatedData.put("emergency_phone_number", newEmergencyPhoneNumber);

            documentReference.update(updatedData)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(staff_profile.this, "New data updated successfully", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(staff_profile.this, "Failed to update new data", Toast.LENGTH_LONG).show();
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