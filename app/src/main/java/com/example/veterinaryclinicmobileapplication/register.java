package com.example.veterinaryclinicmobileapplication;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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

public class register extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    ImageButton userProfileImage;

    Uri imageUri;

    EditText userName, userEmail, userPhoneNumber, userPassword, userEmergencyContact, userEmergencyPhoneNumber;

    Button registerBtn;

    CheckBox ppCheckBox, touCheckBox;

    FirebaseAuth Auth;

    FirebaseFirestore db;

//    @Override
//    public void onStart() {
//        super.onStart();
//        FirebaseUser currentUser = Auth.getCurrentUser();
//        if (currentUser != null) {
//            Intent intent = new Intent(getApplicationContext(), home.class);
//            startActivity(intent);
//            finish();
//        }
//    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        Auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userProfileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.name);
        userEmail = findViewById(R.id.email);
        userPhoneNumber = findViewById(R.id.phoneNumber);
        userPassword = findViewById(R.id.password);
        userEmergencyContact = findViewById(R.id.emergencyContact);
        userEmergencyPhoneNumber = findViewById(R.id.emergencyPhoneNumber);

        ppCheckBox = findViewById(R.id.ppCheckBox);
        touCheckBox = findViewById(R.id.touCheckBox);
        registerBtn = findViewById(R.id.registerBtn);

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name, email, phoneNumber, password, emergencyContact, emergencyPhoneNumber;

                name = String.valueOf(userName.getText());
                email = String.valueOf(userEmail.getText());
                phoneNumber = String.valueOf(userPhoneNumber.getText());
                password = String.valueOf(userPassword.getText());
                emergencyContact = String.valueOf(userEmergencyContact.getText());
                emergencyPhoneNumber = String.valueOf(userEmergencyPhoneNumber.getText());

                // check empty
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(register.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(register.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(register.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(register.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(emergencyContact)) {
                    Toast.makeText(register.this, "Please enter an emergency contact", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(emergencyPhoneNumber)) {
                    Toast.makeText(register.this, "Please enter an emergency phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // check format
                if (!name.matches("[a-zA-Z\\s]+") || !emergencyContact.matches("[a-zA-Z\\s]+")) {
                    Toast.makeText(register.this, "Name can only contain letters and spaces", Toast.LENGTH_SHORT).show();
                }

                if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    Toast.makeText(register.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                }

                if (!phoneNumber.matches("\\d{10,11}") || !emergencyPhoneNumber.matches("\\d{10,11}")) {
                    Toast.makeText(register.this, "Phone number must be 10 or 11 digits", Toast.LENGTH_SHORT).show();
                }

                if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$")) {
                    Toast.makeText(register.this, "Password must be at least 6 characters long and include uppercase, lowercase, number, and special character.", Toast.LENGTH_SHORT).show();
                }

                // check emergency contact
                if (name.matches(emergencyContact)) {
                    Toast.makeText(register.this, "Name and emergency contact must be different.", Toast.LENGTH_SHORT).show();
                }

                if (phoneNumber.matches(emergencyPhoneNumber)) {
                    Toast.makeText(register.this, "Phone number and emergency phone number must be different.", Toast.LENGTH_SHORT).show();
                }

                // tick checkbox
                if (!ppCheckBox.isChecked() || (!touCheckBox.isChecked())) {
                    Toast.makeText(register.this, "You must agree to the privacy policy and terms of use", Toast.LENGTH_SHORT).show();
                    return;
                }

                // create user with firebase authentication
                Auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = Auth.getCurrentUser();
                                    generateId(name, email, phoneNumber, emergencyContact, emergencyPhoneNumber, firebaseUser.getUid());
                                } else {
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        Toast.makeText(register.this, "User already exists", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
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

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            userProfileImage.setImageURI(imageUri);
        }
    }

    private void generateId(String name, String email, String phoneNumber, String emergencyContact, String emergencyPhoneNumber, String authId) {
        db.collection("pet_owner")
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String newId = "PO1";
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String lastId = document.getString("id");
                            if (lastId != null && lastId.startsWith("PO")) {
                                int lastNumber = Integer.parseInt(lastId.substring(2));
                                newId = "PO" + (lastNumber + 1);
                            }
                        }
                        saveData(authId, newId, name, email, phoneNumber, emergencyContact, emergencyPhoneNumber);
                    }
                });
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

    private void saveData(String authId, String id, String name, String email, String phoneNumber, String emergencyContact, String emergencyPhoneNumber) {
        FirebaseUser firebaseUser = Auth.getCurrentUser();

        if (firebaseUser != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", id);
            userData.put("name", name);
            userData.put("email", email);
            userData.put("phone_number", phoneNumber);
            userData.put("emergency_contact", emergencyContact);
            userData.put("emergency_phone_number", emergencyPhoneNumber);

            if (imageUri != null) {
                // determine file extension
                String fileExtension = getFileExtension(imageUri);
                String fileName = "images/" + authId + "." + fileExtension;
                StorageReference imageRef = storageRef.child(fileName);

                // upload the image
                imageRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            // Save image name to userData
                            userData.put("image_name", fileName);

                            // Save user data to Firestore
                            db.collection("pet_owner")
                                    .document(authId)
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(register.this, "User data saved.", Toast.LENGTH_SHORT).show();

                                        // clear text input
                                        TextInputEditText nameText = findViewById(R.id.name);
                                        TextInputEditText emailText = findViewById(R.id.email);
                                        TextInputEditText phoneNumberText = findViewById(R.id.phoneNumber);
                                        TextInputEditText passwordText = findViewById(R.id.password);
                                        TextInputEditText emergencyContactText = findViewById(R.id.emergencyContact);
                                        TextInputEditText emergencyPhoneNumberText = findViewById(R.id.emergencyPhoneNumber);

                                        userProfileImage.setImageResource(R.drawable.ic_launcher_foreground);
                                        nameText.getText().clear();
                                        emailText.getText().clear();
                                        phoneNumberText.getText().clear();
                                        passwordText.getText().clear();
                                        emergencyContactText.getText().clear();
                                        emergencyPhoneNumberText.getText().clear();

                                        // go home
                                        Intent intent = new Intent(getApplicationContext(), home.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(register.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(register.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                // No image selected, save data without image
                db.collection("pet_owner")
                        .document(authId)
                        .set(userData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(register.this, "User data saved.", Toast.LENGTH_SHORT).show();

                            // clear text input
                            TextInputEditText nameText = findViewById(R.id.name);
                            TextInputEditText emailText = findViewById(R.id.email);
                            TextInputEditText phoneNumberText = findViewById(R.id.phoneNumber);
                            TextInputEditText passwordText = findViewById(R.id.password);
                            TextInputEditText emergencyContactText = findViewById(R.id.emergencyContact);
                            TextInputEditText emergencyPhoneNumberText = findViewById(R.id.emergencyPhoneNumber);

                            userProfileImage.setImageResource(R.drawable.ic_launcher_foreground);
                            nameText.getText().clear();
                            emailText.getText().clear();
                            phoneNumberText.getText().clear();
                            passwordText.getText().clear();
                            emergencyContactText.getText().clear();
                            emergencyPhoneNumberText.getText().clear();

                            // go home
                            Intent intent = new Intent(getApplicationContext(), home.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(register.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        } else {
            Toast.makeText(register.this, "No authenticated user found.", Toast.LENGTH_SHORT).show();
        }
    }

    public void goPrivacyPolicyPage(View view){
        Intent intent = new Intent(this, privacyPolicy.class);
        TextView ppCheckBox = findViewById(R.id.ppCheckBox);
        startActivity(intent);
    }

    public void goTermOfUsePage(View view){
        Intent intent = new Intent(this, termOfUse.class);
        TextView touCheckBox = findViewById(R.id.touCheckBox);
        startActivity(intent);
    }

    public void goBackIntroPage(View view){
        Intent intent = new Intent(this, intro.class);
        Button backBtn = findViewById(R.id.backBtn);
        startActivity(intent);
    }
}
