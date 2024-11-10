package com.example.veterinaryclinicmobileapplication;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class calendar extends AppCompatActivity {

    private static final String TAG = "CalendarActivity";
    private static final int REQUEST_CODE_EDIT_SCHEDULE = 1;
    CalendarView calendarView;
    TextView scheduleDetails;
    FirebaseAuth auth;
    FirebaseFirestore db;
    String selectedDate;
    String scheduleId;
    int scheduleIndex;
    Button editBtn, deleteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        calendarView = findViewById(R.id.date);
        scheduleDetails = findViewById(R.id.scheduleDetails);

        editBtn = findViewById(R.id.editBtn);
        editBtn.setOnClickListener(v -> goEditSchedulePage());

        deleteBtn = findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(v -> showDeletionDialogBox(scheduleId));

        // Get current date
        Calendar calendar = Calendar.getInstance();
        selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));

        calendarView.setDate(calendar.getTimeInMillis(), true, true);
        Log.d(TAG, "Initial selected date: " + selectedDate);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                Log.d(TAG, "Selected date: " + selectedDate);
                fetchScheduleForSelectedDate();
            }
        });

        fetchScheduleForSelectedDate();
    }

    public void fetchScheduleForSelectedDate() {
        String userId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Fetching schedules for selected date: " + selectedDate + " for user ID: " + userId);

        db.collection("pet_owner").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String petOwnerId = documentSnapshot.getString("id");
                        fetchSchedulesForCalendar(petOwnerId);
                    } else {
                        Toast.makeText(calendar.this, "No pet owner found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching pet owner ID: ", e);
                });
    }

    public void fetchSchedulesForCalendar(String petOwnerId) {
        db.collection("calendar").document(petOwnerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<Map<String, Object>> schedules = (List<Map<String, Object>>) document.get("schedule");

                            if (schedules != null && !schedules.isEmpty()) {
                                boolean scheduleFound = false;
                                StringBuilder details = new StringBuilder();

                                for (int i = 0; i < schedules.size(); i++) { // Track index
                                    Map<String, Object> schedule = schedules.get(i);
                                    String scheduleDate = (String) schedule.get("date");

                                    if (scheduleDate.equals(selectedDate)) {
                                        scheduleFound = true;
                                        scheduleId = (String) schedule.get("id");
                                        scheduleIndex = i;
                                        String title = (String) schedule.get("title");
                                        String activity = (String) schedule.get("activity");
                                        String time = (String) schedule.get("time");
                                        String petId = (String) schedule.get("pet_id");

                                        if (petId != null && !petId.isEmpty()) {
                                            fetchPetName(petId, details, title, activity, time);
                                        } else {
                                            details.append("Title: ").append(title).append("\n\n")
                                                    .append("Activity: ").append(activity).append("\n\n")
                                                    .append("Time: ").append(time).append("\n\n")
                                                    .append("Pet ID is missing.\n");
                                            scheduleDetails.setText(details.toString());
                                        }
                                    }
                                }

                                // Update the UI to display details
                                if (scheduleFound) {
                                    scheduleDetails.setVisibility(View.VISIBLE);
                                    findViewById(R.id.editBtn).setVisibility(View.VISIBLE);
                                    findViewById(R.id.deleteBtn).setVisibility(View.VISIBLE);
                                    Log.d(TAG, "Schedules found for pet owner ID: " + petOwnerId);
                                } else {
                                    scheduleDetails.setText("No schedules for this date.");
                                    scheduleDetails.setVisibility(View.VISIBLE);
                                    findViewById(R.id.editBtn).setVisibility(View.GONE);
                                    findViewById(R.id.deleteBtn).setVisibility(View.GONE);
                                    Log.d(TAG, "No schedules found for date: " + selectedDate);
                                }
                            } else {
                                scheduleDetails.setText("No schedules for this date.");
                                scheduleDetails.setVisibility(View.VISIBLE);
                                findViewById(R.id.editBtn).setVisibility(View.GONE);
                                findViewById(R.id.deleteBtn).setVisibility(View.GONE);
                                Log.d(TAG, "No schedules found in document for pet owner ID: " + petOwnerId);
                            }
                        } else {
                            scheduleDetails.setText("No schedules for this date.");
                            scheduleDetails.setVisibility(View.VISIBLE);
                            findViewById(R.id.editBtn).setVisibility(View.GONE);
                            findViewById(R.id.deleteBtn).setVisibility(View.GONE);
                            Log.d(TAG, "No document found for pet owner ID: " + petOwnerId);
                        }
                    } else {
                        Log.e(TAG, "Query failed: ", task.getException());
                        Toast.makeText(calendar.this, "Failed to load schedule.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void fetchPetName(String petId, StringBuilder details, String title, String activity, String time) {
        db.collection("pet").document(petId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot petDocument = task.getResult();
                        if (petDocument.exists()) {
                            String petName = petDocument.getString("name");
                            details.append("Title: ").append(title).append("\n\n")
                                    .append("Activity: ").append(activity).append("\n\n")
                                    .append("Time: ").append(time).append("\n\n")
                                    .append("Pet Name: ").append(petName).append("\n");
                            scheduleDetails.setText(details.toString());
                        } else {
                            details.append("Title: ").append(title).append("\n\n")
                                    .append("Activity: ").append(activity).append("\n\n")
                                    .append("Time: ").append(time).append("\n\n")
                                    .append("Pet name not found.\n");
                            scheduleDetails.setText(details.toString());
                        }
                    } else {
                        details.append("Title: ").append(title).append("\n\n")
                                .append("Activity: ").append(activity).append("\n\n")
                                .append("Time: ").append(time).append("\n\n")
                                .append("Error fetching pet name.\n");
                        scheduleDetails.setText(details.toString());
                    }
                });
    }

    public void showDeletionDialogBox(String scheduleId) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.deletion);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.frame);

        Button yesBtn = dialog.findViewById(R.id.yesBtn);
        Button noBtn = dialog.findViewById(R.id.noBtn);

        noBtn.setOnClickListener(v -> dialog.dismiss());

        yesBtn.setOnClickListener(v -> {
            dialog.dismiss();
            deleteSchedule(scheduleId);
        });

        dialog.show();
    }

    public void deleteSchedule(String scheduleId) {
        String userId = auth.getCurrentUser().getUid();

        db.collection("pet_owner").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String petOwnerId = documentSnapshot.getString("id");

                        db.collection("calendar").document(petOwnerId)
                                .get()
                                .addOnSuccessListener(calendarDocument -> {
                                    if (calendarDocument.exists()) {
                                        List<Map<String, Object>> schedules = (List<Map<String, Object>>) calendarDocument.get("schedule");

                                        if (schedules != null) {
                                            for (Map<String, Object> schedule : schedules) {
                                                if (scheduleId.equals(schedule.get("id"))) {
                                                    schedules.remove(schedule);
                                                    break;
                                                }
                                            }

                                            db.collection("calendar").document(petOwnerId)
                                                    .update("schedule", schedules)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(calendar.this, "Schedule deleted.", Toast.LENGTH_SHORT).show();
                                                        // Refresh
                                                        fetchSchedulesForCalendar(petOwnerId);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(calendar.this, "Failed to delete schedule.", Toast.LENGTH_SHORT).show();
                                                    });
                                        } else {
                                            Log.e(TAG, "No schedules found in document for pet owner ID: " + petOwnerId);
                                        }
                                    } else {
                                        Log.e(TAG, "No schedule document found for pet owner ID: " + petOwnerId);
                                    }
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Error fetching schedule document: ", e));
                    } else {
                        Log.e(TAG, "No pet owner document found for user ID: " + userId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching pet owner ID: ", e));
    }

    public void goEditSchedulePage() {
        Intent intent = new Intent(calendar.this, edit_schedule.class);
        intent.putExtra("scheduleId", scheduleId);
        startActivityForResult(intent, REQUEST_CODE_EDIT_SCHEDULE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_SCHEDULE && resultCode == RESULT_OK) {
            fetchScheduleForSelectedDate();
        }
    }

    public void goAddSchedulePage(View view) {
        Intent intent = new Intent(this, add_schedule1.class);
        startActivity(intent);
    }

    public void goHomePage(View view) {
        Intent intent = new Intent(this, home.class);
        ImageButton goHomeBtn = findViewById(R.id.goHomeBtn);
        startActivity(intent);
    }

    public void goMyPetPage(View view) {
        Intent intent = new Intent(this, my_pet.class);
        ImageButton goMyPetBtn = findViewById(R.id.goMyPetBtn);
        startActivity(intent);
    }

    public void goRecommendAdoptablePetPage(View view) {
        Intent intent = new Intent(this, recommend_adoptable_pet.class);
        ImageButton goPetShelterBtn = findViewById(R.id.goPetShelterBtn);
        startActivity(intent);
    }

    public void goCalendarPage(View view) {
        Intent intent = new Intent(this, calendar.class);
        ImageButton goCalendarBtn = findViewById(R.id.goCalendarBtn);
        startActivity(intent);
    }

    public void goProfilePage(View view) {
        Intent intent = new Intent(this, my_profile.class);
        ImageButton goProfileBtn = findViewById(R.id.goProfileBtn);
        startActivity(intent);
    }
}
