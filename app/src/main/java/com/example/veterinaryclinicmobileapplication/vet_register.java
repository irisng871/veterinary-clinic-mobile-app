package com.example.veterinaryclinicmobileapplication;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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

    Button registerBtn;

    FirebaseAuth Auth;

    FirebaseFirestore db;

//    @Override
//    public void onStart() {
//        super.onStart();
//        FirebaseUser currentUser = Auth.getCurrentUser();
//        if (currentUser != null) {
//            Intent intent = new Intent(getApplicationContext(), vet_home.class);
//            startActivity(intent);
//            finish();
//        }
//    }

    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vet_register);

        Auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        vetProfileImage = findViewById(R.id.profileImage);
        vetName = findViewById(R.id.name);
        vetEmail = findViewById(R.id.email);
        vetPhoneNumber = findViewById(R.id.phoneNumber);
        vetSpecialtyArea = findViewById(R.id.specialtyArea);
        vetPassword = findViewById(R.id.password);

        registerBtn = findViewById(R.id.registerBtn);

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
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(vet_register.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(vet_register.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(vet_register.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(specialtyArea)) {
                    Toast.makeText(vet_register.this, "Please enter an specialty area", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(vet_register.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // check format
                if (!name.matches("[a-zA-Z\\s]+")) {
                    Toast.makeText(vet_register.this, "Name can only contain letters and spaces", Toast.LENGTH_SHORT).show();
                }

                if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    Toast.makeText(vet_register.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                }

                if (!phoneNumber.matches("\\d{10,11}")) {
                    Toast.makeText(vet_register.this, "Phone number must be 10 or 11 digits", Toast.LENGTH_SHORT).show();
                }

                if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$")) {
                    Toast.makeText(vet_register.this, "Password must be at least 6 characters long and include uppercase, lowercase, number, and special character.", Toast.LENGTH_SHORT).show();
                }

                // create vet with firebase authentication
                Auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(vet_register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = Auth.getCurrentUser();
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

    private void openGallery() {
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

    private void generateId(String name, String email, String phoneNumber, String specialtyArea, String authId) {
        db.collection("veterinarian")
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String newId = "VE1";
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String lastId = document.getString("id");
                                if (lastId != null && lastId.startsWith("VE")) {
                                    int lastNumber = Integer.parseInt(lastId.substring(2));
                                    newId = "VE" + (lastNumber + 1);
                                }
                            }
                            saveData(authId, newId, name, email, phoneNumber, specialtyArea);
                        }
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

    private void saveData(String authId, String id, String name, String email, String phoneNumber, String specialtyArea) {
        FirebaseUser firebaseUser = Auth.getCurrentUser();

        if (firebaseUser != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            Map<String, Object> vetData = new HashMap<>();
            vetData.put("id", id);
            vetData.put("name", name);
            vetData.put("email", email);
            vetData.put("phone_number", phoneNumber);
            vetData.put("specialty_area", specialtyArea);

            if (imageUri != null) {
                // determine file extension
                String fileExtension = getFileExtension(imageUri);
                String fileName = "images/" + authId + "." + fileExtension; // Unique file name with extension
                StorageReference imageRef = storageRef.child(fileName);

                // upload the image
                imageRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            // Save image name to vetData
                            vetData.put("image_name", fileName);

                            // Save vet data to Firestore
                            db.collection("veterinarian")
                                    .document(authId)
                                    .set(vetData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(vet_register.this, "Vet data saved.", Toast.LENGTH_SHORT).show();

                                        // clear text input
                                        TextInputEditText nameText = findViewById(R.id.name);
                                        TextInputEditText emailText = findViewById(R.id.email);
                                        TextInputEditText phoneNumberText = findViewById(R.id.phoneNumber);
                                        TextInputEditText specialtyAreaText = findViewById(R.id.specialtyArea);
                                        TextInputEditText passwordText = findViewById(R.id.password);

                                        vetProfileImage.setImageResource(R.drawable.ic_launcher_foreground);
                                        nameText.getText().clear();
                                        emailText.getText().clear();
                                        phoneNumberText.getText().clear();
                                        passwordText.getText().clear();
                                        specialtyAreaText.getText().clear();

                                        // go home
                                        Intent intent = new Intent(getApplicationContext(), vet_home.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(vet_register.this, "Failed to save vet data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(vet_register.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                // No image selected, save data without image
                db.collection("veterinarian")
                        .document(authId)
                        .set(vetData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(vet_register.this, "Vet data saved.", Toast.LENGTH_SHORT).show();

                            // clear text input
                            TextInputEditText nameText = findViewById(R.id.name);
                            TextInputEditText emailText = findViewById(R.id.email);
                            TextInputEditText phoneNumberText = findViewById(R.id.phoneNumber);
                            TextInputEditText specialtyAreaText = findViewById(R.id.specialtyArea);
                            TextInputEditText passwordText = findViewById(R.id.password);

                            vetProfileImage.setImageResource(R.drawable.ic_launcher_foreground);
                            nameText.getText().clear();
                            emailText.getText().clear();
                            phoneNumberText.getText().clear();
                            passwordText.getText().clear();
                            specialtyAreaText.getText().clear();

                            // go home
                            Intent intent = new Intent(getApplicationContext(), vet_home.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(vet_register.this, "Failed to save vet data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        } else {
            Toast.makeText(vet_register.this, "No authenticated vet found.", Toast.LENGTH_SHORT).show();
        }
    }

    public void goBackLoginPage(View view){
        Intent intent = new Intent(this, vet_login.class);
        Button backBtn = findViewById(R.id.backBtn);
        startActivity(intent);
    }
}
