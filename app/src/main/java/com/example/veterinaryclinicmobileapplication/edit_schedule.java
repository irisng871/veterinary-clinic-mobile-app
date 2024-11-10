package com.example.veterinaryclinicmobileapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class edit_schedule extends AppCompatActivity {

    private static final String TAG = "EditScheduleActivity";
    EditText titleEditText, activityEditText;
    Button editBtn;
    ImageButton backBtn;
    FirebaseAuth auth;
    FirebaseFirestore db;
    String scheduleId, petOwnerId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_schedule);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        titleEditText = findViewById(R.id.title);
        activityEditText = findViewById(R.id.activity);

        editBtn = findViewById(R.id.editBtn);
        editBtn.setOnClickListener(v -> editSchedule());

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());

        scheduleId = getIntent().getStringExtra("scheduleId");

        if (scheduleId == null) {
            Toast.makeText(this, "Invalid schedule data.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        getUserIdAndLoadSchedule();
    }

    public void getUserIdAndLoadSchedule() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("pet_owner").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        petOwnerId = documentSnapshot.getString("id");
                        Log.d(TAG, "Pet Owner ID: " + petOwnerId);
                        loadScheduleDetails();
                    } else {
                        Toast.makeText(edit_schedule.this, "No pet owner document found.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching pet owner document: ", e);
                    Toast.makeText(edit_schedule.this, "Failed to load pet owner document.", Toast.LENGTH_SHORT).show();
                });
    }

    public void loadScheduleDetails() {
        db.collection("calendar").document(petOwnerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "Document retrieved: " + documentSnapshot.getData());
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> schedules = (List<Map<String, Object>>) documentSnapshot.get("schedule");

                        if (schedules != null) {
                            boolean found = false;
                            for (Map<String, Object> schedule : schedules) {
                                Log.d(TAG, "Checking schedule with ID: " + schedule.get("id"));
                                if (scheduleId.equals(schedule.get("id"))) {
                                    titleEditText.setText((String) schedule.get("title"));
                                    activityEditText.setText((String) schedule.get("activity"));
                                    found = true;
                                    break; // Exit loop once the schedule is found
                                }
                            }
                            if (!found) {
                                Toast.makeText(edit_schedule.this, "Schedule not found.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(edit_schedule.this, "No schedules found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(edit_schedule.this, "No schedule document found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(edit_schedule.this, "Failed to load schedule details.", Toast.LENGTH_SHORT).show();
                });
    }

    public void editSchedule() {
        String title = titleEditText.getText().toString().trim();
        String activity = activityEditText.getText().toString().trim();

        if (title.isEmpty() || activity.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("calendar").document(petOwnerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> schedules = (List<Map<String, Object>>) documentSnapshot.get("schedule");

                        if (schedules != null) {
                            boolean scheduleFound = false;

                            // Loop through the schedules to find the one to edit
                            for (int i = 0; i < schedules.size(); i++) {
                                Map<String, Object> schedule = schedules.get(i);

                                if (scheduleId.equals(schedule.get("id"))) {
                                    schedule.put("title", title);
                                    schedule.put("activity", activity);
                                    scheduleFound = true;
                                    break;
                                }
                            }

                            if (scheduleFound) {
                                db.collection("calendar").document(petOwnerId)
                                        .update("schedule", schedules)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(edit_schedule.this, "Schedule updated successfully.", Toast.LENGTH_SHORT).show();
                                            setResult(RESULT_OK);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(edit_schedule.this, "Failed to update schedule.", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(edit_schedule.this, "Schedule not found for editing.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(edit_schedule.this, "No schedules found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(edit_schedule.this, "No schedule document found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(edit_schedule.this, "Failed to load schedule document.", Toast.LENGTH_SHORT).show();
                });
    }
}
