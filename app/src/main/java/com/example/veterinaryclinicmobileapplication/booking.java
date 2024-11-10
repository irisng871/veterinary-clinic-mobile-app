package com.example.veterinaryclinicmobileapplication;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class booking extends AppCompatActivity {

    CalendarView date;
    long selectedDate;
    Button bookBtn;
    ImageButton backBtn;
    Spinner petSpinner;
    GridLayout slotGrid;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser firebaseUser;
    ArrayList<String> timeSlots;
    Map<String, String> petIdMap;
    String selectedVetId, petOwnerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = auth.getCurrentUser();

        String selectedVetName = getIntent().getStringExtra("selectedVetName");
        if (selectedVetName != null) {
            Log.d("Booking", "Selected veterinarian received: " + selectedVetName);
            fetchVetId(selectedVetName);
        } else {
            Log.e("Booking", "No veterinarian name received.");
        }

        date = findViewById(R.id.date);
        bookBtn = findViewById(R.id.bookBtn);
        petSpinner = findViewById(R.id.selectPetName);
        slotGrid = findViewById(R.id.slotGrid);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackBookingSelectVetPage());

        timeSlots = new ArrayList<>();
        timeSlots.add("9:00am");
        timeSlots.add("10:30am");
        timeSlots.add("12:00pm");
        timeSlots.add("1:30pm");
        timeSlots.add("3:00pm");
        timeSlots.add("4:30pm");
        timeSlots.add("6:00pm");

        displayAvailableSlots(timeSlots.toArray(new String[0]), new ArrayList<>());

        date.setMinDate(System.currentTimeMillis());

        // CalendarView date selection
        date.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = new GregorianCalendar(year, month, dayOfMonth).getTimeInMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String selectedDateString = sdf.format(selectedDate);

            // Fetch booked slots and update available slots
            fetchBookedSlots(selectedDateString, selectedVetId, bookedSlots -> {
                // Display slots for selected date excluding booked ones
                displayAvailableSlots(timeSlots.toArray(new String[0]), bookedSlots);
            });
        });

        bookBtn.setOnClickListener(v -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = sdf.format(selectedDate);

            String selectedPetId = getSelectedPetId();
            String selectedTimeSlot = getSelectedTimeSlot();

            if (formattedDate != null && selectedTimeSlot != null) {
                generateId(selectedVetId, selectedPetId, formattedDate, selectedTimeSlot, petOwnerId);
                Toast.makeText(this, "Booked for " + formattedDate + " with pet ID: " + selectedPetId, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please select a date and a time slot", Toast.LENGTH_SHORT).show();
            }
        });

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

    public interface FetchSlotsCallback {
        void onFetch(List<String> bookedSlots);
    }

    public void fetchBookedSlots(String selectedDate, String vetId, FetchSlotsCallback callback) {
        db.collection("booked_slots")
                .whereEqualTo("date", selectedDate)
                .whereEqualTo("vet_id", vetId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> bookedSlots = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String timeSlot = document.getString("time");
                        if (timeSlot != null) {
                            bookedSlots.add(timeSlot);
                        }
                    }
                    callback.onFetch(bookedSlots);
                })
                .addOnFailureListener(e -> {
                    Log.e("Booking", "Error fetching booked slots: ", e);
                    Toast.makeText(this, "Failed to fetch booked slots.", Toast.LENGTH_SHORT).show();
                });
    }

    public void displayAvailableSlots(String[] timeSlots, List<String> bookedSlots) {
        slotGrid.removeAllViews();

        for (String slot : timeSlots) {
            if (bookedSlots.contains(slot)) {
                continue;
            }

            Button btn = new Button(this);
            btn.setText(slot);
            btn.setTextColor(Color.WHITE);
            btn.setBackgroundColor(Color.parseColor("#252E63"));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(10, 10, 10, 10);
            btn.setLayoutParams(params);

            btn.setOnClickListener(view -> {
                // Deselect all other buttons
                for (int i = 0; i < slotGrid.getChildCount(); i++) {
                    Button otherBtn = (Button) slotGrid.getChildAt(i);
                    otherBtn.setBackgroundColor(Color.parseColor("#252E63"));
                    otherBtn.setTag(null); // Clear tag for deselected buttons
                }
                // Select the clicked button
                btn.setBackgroundColor(Color.parseColor("#5465cc"));
                btn.setTag("selected"); // Set tag for selected button
            });

            slotGrid.addView(btn);
        }
    }

    public void fetchVetId(String vetName) {
        db.collection("veterinarian")
                .whereEqualTo("name", vetName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            selectedVetId = document.getString("id");
                            Log.d("Booking", "Veterinarian ID fetched: " + selectedVetId);
                        }
                    } else {
                        Toast.makeText(this, "Failed to load veterinarian ID.", Toast.LENGTH_SHORT).show();
                    }
                });
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

    public String getSelectedTimeSlot() {
        for (int i = 0; i < slotGrid.getChildCount(); i++) {
            Button button = (Button) slotGrid.getChildAt(i);
            if ("selected".equals(button.getTag())) {
                return button.getText().toString();
            }
        }
        return null;
    }

    public void generateId(String selectedVetId, String selectedPetId, String formattedDate, String selectedTimeSlot, String petOwnerId) {
        db.collection("booking")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int maxNumber = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String lastId = document.getString("id");
                            if (lastId != null && lastId.startsWith("BO")) {
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

                        String newId = "BO" + (maxNumber + 1);
                        Log.d("Booking", "Generated ID: " + newId);

                        saveBooking(newId, selectedVetId, selectedPetId, petOwnerId, formattedDate, selectedTimeSlot);
                    } else {
                        Log.e("Booking", "Error generating booking ID", task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Booking", "Error retrieving data", e);
                });
    }

    public void saveBooking(String bookingId, String vetId, String petId, String petOwnerId, String date, String timeSlot) {
        Map<String, Object> booking = new HashMap<>();
        booking.put("id", bookingId);
        booking.put("veterinarian_id", vetId);
        booking.put("pet_id", petId);
        booking.put("pet_owner_id", petOwnerId);
        booking.put("date", date);
        booking.put("time", timeSlot);
        booking.put("status", "Pending");

        db.collection("booking")
                .document(bookingId)
                .set(booking)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Booking successful!", Toast.LENGTH_SHORT).show();

                    deleteTimeSlot(date, timeSlot, vetId);

                    Intent intent = new Intent(getApplicationContext(), home.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save booking.", Toast.LENGTH_SHORT).show();
                });
    }

    public void deleteTimeSlot(String date, String timeSlot, String vetId) {
        Map<String, Object> bookedSlot = new HashMap<>();
        bookedSlot.put("date", date);
        bookedSlot.put("time", timeSlot);
        bookedSlot.put("vet_id", vetId);

        db.collection("booked_slots")
                .add(bookedSlot)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Booking", "Booked slot added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Booking", "Error adding booked slot: ", e);
                });
    }

    public void goBackBookingSelectVetPage() {
        finish();
    }
}