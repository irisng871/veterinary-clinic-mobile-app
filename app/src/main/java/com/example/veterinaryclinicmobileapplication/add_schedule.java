package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class add_schedule extends AppCompatActivity {

    EditText scheduleTitle, scheduleActivity;

    Button addBtn;

    FirebaseAuth Auth;

    FirebaseFirestore db;

    FirebaseStorage storage;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_schedule);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        scheduleTitle = findViewById(R.id.title);
        scheduleActivity = findViewById(R.id.activity);
        addBtn = findViewById(R.id.addBtn);
        
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title, activity;

                title = String.valueOf(scheduleTitle.getText());
                activity = String.valueOf(scheduleActivity.getText());
                
                Intent intent = getIntent();
                String date = intent.getStringExtra("selectedDate");
                String time = intent.getStringExtra("selectedTime");
                
                generateId(date, time, title, activity);
            }
        });
    }

    private void generateId(String date, String time, String title, String activity) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            db.collection("pet_owner").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String petOwnerId = documentSnapshot.getString("id");
                            if (petOwnerId != null) {
                                // Fetch the schedules for this pet owner
                                db.collection("calendar").document(petOwnerId).get()
                                        .addOnSuccessListener(document -> {
                                            if (document.exists()) {
                                                // Get the schedule array
                                                List<Map<String, Object>> schedules = (List<Map<String, Object>>) document.get("schedule");
                                                String newId = "CS1"; // Default ID

                                                // Find the last ID from the schedule array
                                                if (schedules != null && !schedules.isEmpty()) {
                                                    for (Map<String, Object> schedule : schedules) {
                                                        String lastId = (String) schedule.get("id");
                                                        if (lastId != null && lastId.startsWith("CS")) {
                                                            int lastNumber = Integer.parseInt(lastId.substring(2));
                                                            newId = "CS" + (lastNumber + 1);
                                                        }
                                                    }
                                                }
                                                // Now add the schedule with the new ID
                                                getPetOwnerIdAndAddSchedule(newId, date, time, title, activity);
                                            } else {
                                                // If no document exists, create the first schedule
                                                getPetOwnerIdAndAddSchedule("CS1", date, time, title, activity);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(add_schedule.this, "Error fetching calendar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(add_schedule.this, "pet_owner_id not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(add_schedule.this, "Pet owner document not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(add_schedule.this, "Error fetching pet owner: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(add_schedule.this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }


    public void getPetOwnerIdAndAddSchedule(String newId, String date, String time, String title, String activity) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // First, fetch the pet_owner_id from the pet_owner collection using the UID
            db.collection("pet_owner").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Get the pet_owner_id from the document
                            String petOwnerId = documentSnapshot.getString("id");

                            if (petOwnerId != null) {
                                // Now that we have pet_owner_id, check if the document exists in the calendar collection
                                DocumentReference petOwnerRef = db.collection("calendar").document(petOwnerId);
                                petOwnerRef.get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        if (!task.getResult().exists()) {
                                            // If document doesn't exist, create a new one with initial schedule array
                                            Map<String, Object> scheduleEntry = new HashMap<>();
                                            scheduleEntry.put("id", newId);
                                            scheduleEntry.put("date", date);
                                            scheduleEntry.put("time", time);
                                            scheduleEntry.put("title", title);
                                            scheduleEntry.put("activity", activity);

                                            // Create new document with the schedule field
                                            Map<String, Object> newPetOwnerData = new HashMap<>();
                                            newPetOwnerData.put("schedule", FieldValue.arrayUnion(scheduleEntry));

                                            petOwnerRef.set(newPetOwnerData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(add_schedule.this, "Schedule successfully added", Toast.LENGTH_LONG).show();

                                                        TextInputEditText titleText = findViewById(R.id.title);
                                                        TextInputEditText activityText = findViewById(R.id.activity);

                                                        titleText.getText().clear();
                                                        activityText.getText().clear();

                                                        Intent intent = new Intent(getApplicationContext(), home.class);
                                                        startActivity(intent);
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(add_schedule.this, "Error creating document: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    });

                                        } else {
                                            // If document exists, just update the schedule
                                            addSchedule(newId, date, time, title, activity, petOwnerId);
                                        }
                                    } else {
                                        Toast.makeText(add_schedule.this, "Error fetching calendar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(add_schedule.this, "pet_owner_id not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(add_schedule.this, "Pet owner document not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(add_schedule.this, "Error fetching pet owner: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(add_schedule.this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }

    public void addSchedule(String newId, String date, String time, String title, String activity, String petOwnerId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> scheduleEntry = new HashMap<>();
        scheduleEntry.put("id", newId);
        scheduleEntry.put("date", date);
        scheduleEntry.put("time", time);
        scheduleEntry.put("title", title);
        scheduleEntry.put("activity", activity);

        DocumentReference petOwnerRef = db.collection("calendar").document(petOwnerId);

        petOwnerRef.update("schedule", FieldValue.arrayUnion(scheduleEntry))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(add_schedule.this, "Schedule successfully added", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(add_schedule.this, "Error adding schedule: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


}
