package com.example.veterinaryclinicmobileapplication;

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
import android.util.Log;

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

public class staff_register extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    ImageButton staffProfileImage;
    Uri imageUri;
    EditText staffName, staffEmail, staffPhoneNumber, staffPassword;
    Button registerBtn, backBtn;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser firebaseUser;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = auth.getCurrentUser();

        staffProfileImage = findViewById(R.id.profileImage);
        staffName = findViewById(R.id.name);
        staffEmail = findViewById(R.id.email);
        staffPhoneNumber = findViewById(R.id.phoneNumber);
        staffPassword = findViewById(R.id.password);
        registerBtn = findViewById(R.id.registerBtn);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackIntroPage());

        staffProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name, email, phoneNumber, password;

                name = String.valueOf(staffName.getText());
                email = String.valueOf(staffEmail.getText());
                phoneNumber = String.valueOf(staffPhoneNumber.getText());
                password = String.valueOf(staffPassword.getText());

                // check empty
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(password)) {
                    Toast.makeText(staff_register.this, "Please enter all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // check format
                if (!name.matches("[a-zA-Z\\s]+")) {
                    Toast.makeText(staff_register.this, "Name can only contain letters and spaces", Toast.LENGTH_SHORT).show();
                }

                if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    Toast.makeText(staff_register.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                } else if (!email.endsWith("@staff.com")) {
                    Toast.makeText(staff_register.this, "Email must end with @staff.com", Toast.LENGTH_SHORT).show();
                }

                if (!phoneNumber.matches("\\d{10,11}")) {
                    Toast.makeText(staff_register.this, "Phone number must be 10 or 11 digits", Toast.LENGTH_SHORT).show();
                }

                if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$")) {
                    Toast.makeText(staff_register.this, "Min 6 characters (upper, lower, number, special character)"
                            , Toast.LENGTH_SHORT).show();
                }

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(staff_register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = auth.getCurrentUser();
                                    generateId(name, email, phoneNumber, firebaseUser.getUid());
                                } else {
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        Toast.makeText(staff_register.this, "Staff already exists", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(staff_register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
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
            staffProfileImage.setImageURI(imageUri);
        }
    }

    public void generateId(String name, String email, String phoneNumber, String authId) {
        db.collection("staff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int maxNumber = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String lastId = document.getString("id");
                            if (lastId != null && lastId.startsWith("ST")) {
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

                        String newId = "ST" + (maxNumber + 1);
                        Log.d("Staff", "Generated ID: " + newId);

                        saveData(newId, name, email, phoneNumber);
                    } else {
                        Log.e("Firestore Error", "Error retrieving staff documents", task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error retrieving data", e);
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

    public void saveData(String id, String name, String email, String phoneNumber) {
        if (firebaseUser != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            Map<String, Object> staffData = new HashMap<>();
            staffData.put("id", id);
            staffData.put("name", name);
            staffData.put("email", email);
            staffData.put("phone_number", phoneNumber);

            if (imageUri != null) {
                // Determine file extension
                String fileExtension = getFileExtension(imageUri);
                String fileName = "images/" + id + "." + fileExtension;
                StorageReference imageRef = storageRef.child(fileName);

                // Upload the image
                imageRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            staffData.put("image", fileName);

                            db.collection("staff")
                                    .add(staffData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(staff_register.this, "Staff data saved.", Toast.LENGTH_SHORT).show();

                                        TextInputEditText nameText = findViewById(R.id.name);
                                        TextInputEditText emailText = findViewById(R.id.email);
                                        TextInputEditText phoneNumberText = findViewById(R.id.phoneNumber);
                                        TextInputEditText passwordText = findViewById(R.id.password);

                                        staffProfileImage.setImageResource(R.drawable.ic_launcher_foreground);
                                        nameText.getText().clear();
                                        emailText.getText().clear();
                                        phoneNumberText.getText().clear();
                                        passwordText.getText().clear();

                                        Intent intent = new Intent(getApplicationContext(), staff_home.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(staff_register.this, "Failed to save staff data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(staff_register.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                // No image selected, save data without image
                db.collection("staff")
                        .add(staffData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(staff_register.this, "Staff data saved.", Toast.LENGTH_SHORT).show();

                            TextInputEditText nameText = findViewById(R.id.name);
                            TextInputEditText emailText = findViewById(R.id.email);
                            TextInputEditText phoneNumberText = findViewById(R.id.phoneNumber);
                            TextInputEditText passwordText = findViewById(R.id.password);

                            staffProfileImage.setImageResource(R.drawable.ic_launcher_foreground);
                            nameText.getText().clear();
                            emailText.getText().clear();
                            phoneNumberText.getText().clear();
                            passwordText.getText().clear();

                            Intent intent = new Intent(getApplicationContext(), staff_home.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(staff_register.this, "Failed to save staff data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        } else {
            Toast.makeText(staff_register.this, "No authenticated staff found.", Toast.LENGTH_SHORT).show();
        }
    }

    public void goBackIntroPage(){
        finish();
    }
}
