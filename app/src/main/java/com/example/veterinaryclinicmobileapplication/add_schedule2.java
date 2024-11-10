package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class add_schedule2 extends AppCompatActivity {

    Spinner petSpinner;
    EditText scheduleTitle, scheduleActivity;
    Button addBtn;
    ImageButton backBtn;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    FirebaseFirestore db;
    FirebaseStorage storage;
    Map<String, String> petIdMap;
    String petOwnerId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_schedule2);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        petSpinner = findViewById(R.id.selectPetName);
        scheduleTitle = findViewById(R.id.title);
        scheduleActivity = findViewById(R.id.activity);
        addBtn = findViewById(R.id.addBtn);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackCalendarPage());

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title, activity, selectedPetId;

                title = String.valueOf(scheduleTitle.getText());
                activity = String.valueOf(scheduleActivity.getText());
                selectedPetId = getSelectedPetId();

                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(add_schedule2.this, "Please enter the title", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(activity)) {
                    Toast.makeText(add_schedule2.this, "Please enter the activity", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = getIntent();
                String date = intent.getStringExtra("selectedDate");
                String time = intent.getStringExtra("selectedTime");

                generateId(selectedPetId, date, time, title, activity);
            }
        });

        firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            db.collection("pet_owner").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            petOwnerId = documentSnapshot.getString("id");
                            if (petOwnerId != null) {
                                fetchPets(petOwnerId);
                            } else {
                                Toast.makeText(this, "Pet owner ID not found.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Pet owner not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load pet owner data.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
        }
    }

    public void fetchPets(String petOwnerId) {
        db.collection("pet")
                .whereEqualTo("pet_owner_id", petOwnerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            List<String> petNames = new ArrayList<>();
                            petIdMap = new HashMap<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String petName = document.getString("name");
                                String petId = document.getId();

                                if (petName != null) {
                                    petNames.add(petName);
                                    petIdMap.put(petName, petId);
                                } else {
                                    Log.e("Firestore", "Pet document missing 'name' field: " + document.getId());
                                }
                            }

                            if (!petNames.isEmpty()) {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, petNames);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                petSpinner.setAdapter(adapter);
                            } else {
                                Toast.makeText(this, "No pets found. Please add a pet.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "No pets found. Please add a pet.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to load pets.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public String getSelectedPetId() {
        String selectedPetName = petSpinner.getSelectedItem() != null ? petSpinner.getSelectedItem().toString() : null;
        String petId = petIdMap.get(selectedPetName);
        Log.d("Booking", "Selected Pet Name: " + selectedPetName + ", Pet ID: " + petId);
        return petId;
    }

    public void generateId(String selectedPetId, String date, String time, String title, String activity) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            db.collection("pet_owner").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String petOwnerId = documentSnapshot.getString("id");
                            if (petOwnerId != null) {
                                db.collection("calendar").document(petOwnerId).get()
                                        .addOnSuccessListener(document -> {
                                            if (document.exists()) {
                                                // Get schedule array
                                                List<Map<String, Object>> schedules = (List<Map<String, Object>>) document.get("schedule");
                                                String newId = "CS1";

                                                // Find last ID from the schedule array
                                                if (schedules != null && !schedules.isEmpty()) {
                                                    for (Map<String, Object> schedule : schedules) {
                                                        String lastId = (String) schedule.get("id");
                                                        if (lastId != null && lastId.startsWith("CS")) {
                                                            int lastNumber = Integer.parseInt(lastId.substring(2));
                                                            newId = "CS" + (lastNumber + 1);
                                                        }
                                                    }
                                                }
                                                getPetOwnerIdAndAddSchedule(newId, selectedPetId, date, time, title, activity);
                                            } else {
                                                getPetOwnerIdAndAddSchedule("CS1", selectedPetId,  date, time, title, activity);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(add_schedule2.this, "Error fetching calendar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(add_schedule2.this, "pet_owner_id not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(add_schedule2.this, "Pet owner document not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(add_schedule2.this, "Error fetching pet owner: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(add_schedule2.this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }


    public void getPetOwnerIdAndAddSchedule(String newId, String selectedPetId, String date, String time, String title, String activity) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("pet_owner").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String petOwnerId = documentSnapshot.getString("id");

                            if (petOwnerId != null) {
                                DocumentReference petOwnerRef = db.collection("calendar").document(petOwnerId);
                                petOwnerRef.get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        if (!task.getResult().exists()) {
                                            Map<String, Object> scheduleEntry = new HashMap<>();
                                            scheduleEntry.put("id", newId);
                                            scheduleEntry.put("pet_id", selectedPetId);
                                            scheduleEntry.put("date", date);
                                            scheduleEntry.put("time", time);
                                            scheduleEntry.put("title", title);
                                            scheduleEntry.put("activity", activity);

                                            Map<String, Object> newPetOwnerData = new HashMap<>();
                                            newPetOwnerData.put("schedule", FieldValue.arrayUnion(scheduleEntry));

                                            petOwnerRef.set(newPetOwnerData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(add_schedule2.this, "Schedule successfully added", Toast.LENGTH_LONG).show();

                                                        TextInputEditText titleText = findViewById(R.id.title);
                                                        TextInputEditText activityText = findViewById(R.id.activity);

                                                        titleText.getText().clear();
                                                        activityText.getText().clear();

                                                        Intent intent = new Intent(getApplicationContext(), home.class);
                                                        startActivity(intent);
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(add_schedule2.this, "Error creating document: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    });

                                        } else {
                                            addSchedule(newId, selectedPetId, date, time, title, activity, petOwnerId);
                                        }
                                    } else {
                                        Toast.makeText(add_schedule2.this, "Error fetching calendar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(add_schedule2.this, "pet_owner_id not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(add_schedule2.this, "Pet owner document not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(add_schedule2.this, "Error fetching pet owner: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(add_schedule2.this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }

    public void addSchedule(String newId, String selectedPetId, String date, String time, String title, String activity, String petOwnerId) {
        Map<String, Object> scheduleEntry = new HashMap<>();
        scheduleEntry.put("id", newId);
        scheduleEntry.put("pet_id", selectedPetId);
        scheduleEntry.put("date", date);
        scheduleEntry.put("time", time);
        scheduleEntry.put("title", title);
        scheduleEntry.put("activity", activity);

        DocumentReference petOwnerRef = db.collection("calendar").document(petOwnerId);

        petOwnerRef.update("schedule", FieldValue.arrayUnion(scheduleEntry))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(add_schedule2.this, "Schedule successfully added", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getApplicationContext(), calendar.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(add_schedule2.this, "Error adding schedule: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    public void goBackCalendarPage() {
        finish();
    }
}
