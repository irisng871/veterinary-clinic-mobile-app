package com.example.veterinaryclinicmobileapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;

public class staff_add_pet2 extends AppCompatActivity {

    Uri imageUri;
    EditText petName, petWeight;
    String type, breed;
    String selectedGender, selectedNeutered;
    ImageButton pickDate;
    TextInputEditText petEstimatedBirthday, petEstimatedAge;
    Button nxtBtn;
    ImageButton backBtn;
    Spinner petGender, petNeutered;
    ArrayAdapter<String> adapterForPetGender, adapterForPetNeutered;
    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_add_pet2);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        petName = findViewById(R.id.name);
        petGender = findViewById(R.id.selectPetGender);
        petWeight = findViewById(R.id.weight);
        petEstimatedBirthday = findViewById(R.id.estimatedBirthday);
        pickDate = findViewById(R.id.pickDate);
        petEstimatedAge = findViewById(R.id.estimatedAge);
        petNeutered = findViewById(R.id.selectPetNeutered);
        nxtBtn = findViewById(R.id.nxtBtn);
        backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> goBackAddPet1Page());

        Intent intent = getIntent();
        String imageUriString = intent.getStringExtra("image");
        if (imageUriString != null) {
            imageUri = Uri.parse(imageUriString);
        }

        type = intent.getStringExtra("type");
        breed = intent.getStringExtra("breed");

        setupSpinners();

        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMonthYearPicker();
            }
        });

        nxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    passToStaffAddPet3();
                }
            }
        });
    }

    public void setupSpinners() {
        String[] genderSelectionOptions = {"Choose the pet gender", "male", "female"};
        adapterForPetGender = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderSelectionOptions);
        adapterForPetGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        petGender.setAdapter(adapterForPetGender);

        petGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGender = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        String[] neuteredSelectionOptions = {"Are the pet neutered ?", "yes", "no"};
        adapterForPetNeutered = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, neuteredSelectionOptions);
        adapterForPetNeutered.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        petNeutered.setAdapter(adapterForPetNeutered);

        petNeutered.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedNeutered = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void openMonthYearPicker() {
        month_year_picker monthYearPicker = new month_year_picker(this, (month, year) -> {
            petEstimatedBirthday.setText(month + " " + year);
            int selectedMonth = getMonthFromString(month);
            int selectedYearInt = Integer.parseInt(year);
            int[] calculatedAge = calculateAge(selectedYearInt, selectedMonth);
            String formattedAge = formatAge(calculatedAge[0], calculatedAge[1]);
            petEstimatedAge.setText(formattedAge);
        });
        monthYearPicker.show();
    }

    public boolean checkValidation() {
        String name = petName.getText().toString();
        String weight = petWeight.getText().toString();
        String estimatedBirthday = petEstimatedBirthday.getText().toString();
        String estimatedAge = petEstimatedAge.getText().toString();

        // Check for empty fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(weight) || TextUtils.isEmpty(estimatedBirthday)) {
            Toast.makeText(this, "Please enter all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check format
        if (!name.matches("[a-zA-Z\\s]+")) {
            Toast.makeText(this, "Name can only contain letters and spaces", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!weight.matches("^\\d+(\\.\\d{1})?$")) {
            Toast.makeText(this, "Weight must be a valid number", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check gender and neutered
        if (selectedGender.equals("Choose the pet gender")) {
            Toast.makeText(this, "Please choose the pet gender.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedNeutered.equals("Are the pet neutered ?")) {
            Toast.makeText(this, "Please choose the pet neutered status.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void passToStaffAddPet3() {
        String name = petName.getText().toString();
        String gender = selectedGender;
        String weight = petWeight.getText().toString();
        String estimatedBirthday = petEstimatedBirthday.getText().toString();
        String estimatedAge = petEstimatedAge.getText().toString();
        String neutered = selectedNeutered;

        Intent intent = new Intent(this, staff_add_pet3.class);

        if (imageUri != null) {
            intent.putExtra("image", imageUri.toString());
        }
        intent.putExtra("type", type);
        intent.putExtra("breed", breed);
        intent.putExtra("name", name);
        intent.putExtra("gender", gender);
        intent.putExtra("weight", weight);
        intent.putExtra("estimated_birthday", estimatedBirthday);
        intent.putExtra("estimated_age", estimatedAge);
        intent.putExtra("neutered", neutered);

        startActivity(intent);
    }

    public int getMonthFromString(String month) {
        switch (month) {
            case "January": return 0;
            case "February": return 1;
            case "March": return 2;
            case "April": return 3;
            case "May": return 4;
            case "June": return 5;
            case "July": return 6;
            case "August": return 7;
            case "September": return 8;
            case "October": return 9;
            case "November": return 10;
            case "December": return 11;
            default: return -1;
        }
    }

    public int[] calculateAge(int year, int month) {
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH);
        int ageInYears = currentYear - year;
        int ageInMonths = currentMonth - month;
        if (ageInMonths < 0) {
            ageInYears--;
            ageInMonths += 12;
        }
        return new int[]{ageInYears, ageInMonths};
    }

    public String formatAge(int years, int months) {
        if (years > 0) {
            return years + " years " + months + " months";
        } else {
            return months + " months";
        }
    }

    public void goBackAddPet1Page() {
        Intent intent = new Intent(this, staff_add_pet1.class);
        startActivity(intent);
    }
}
