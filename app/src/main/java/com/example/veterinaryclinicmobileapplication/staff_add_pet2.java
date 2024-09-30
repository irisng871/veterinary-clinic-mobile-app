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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;

public class staff_add_pet2 extends AppCompatActivity {

    Uri imageUri;

    EditText petName, petWeight;

    String selectedType, selectedBreed;

    String selectedGender, selectedNeutered;

    ImageButton pickDate;

    TextInputEditText petEstimatedBirthday, petEstimatedAge;

    Button nxtBtn;

    Spinner petGender, petNeutered;

    ArrayAdapter<String> adapterForPetGender, adapterForPetNeutered;

    FirebaseFirestore db;

    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_add_pet2);

        petName = findViewById(R.id.name);
        petGender = findViewById(R.id.selectPetGender);
        petWeight = findViewById(R.id.weight);
        petEstimatedBirthday = findViewById(R.id.estimatedBirthday);
        pickDate = findViewById(R.id.pickDate);
        petEstimatedAge = findViewById(R.id.estimatedAge);
        petNeutered = findViewById(R.id.selectPetNeutered);
        nxtBtn = findViewById(R.id.nxtBtn);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

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

        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        nxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name, weight, estimatedBirthday, estimatedAge;

                name = String.valueOf(petName.getText());
                weight = String.valueOf(petWeight.getText());
                estimatedBirthday = String.valueOf(petEstimatedBirthday.getText());
                estimatedAge = String.valueOf(petEstimatedAge.getText());

                String gender = selectedGender;
                String neutered = selectedNeutered;

                Intent intent = getIntent();
                String imageUriString = intent.getStringExtra("image");
                if (imageUriString != null) {
                    imageUri = Uri.parse(imageUriString);
                }

                selectedType = intent.getStringExtra("type");
                selectedBreed = intent.getStringExtra("breed");

                // check empty
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(staff_add_pet2.this, "Please enter the pet name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(weight)) {
                    Toast.makeText(staff_add_pet2.this, "Please enter the pet weight", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(estimatedBirthday)) {
                    Toast.makeText(staff_add_pet2.this, "Please enter the pet estimated birthday", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(estimatedAge)) {
                    Toast.makeText(staff_add_pet2.this, "Please enter the pet estimated age", Toast.LENGTH_SHORT).show();
                    return;
                }

                // check format
                if (!name.matches("[a-zA-Z\\s]+")) {
                    Toast.makeText(staff_add_pet2.this, "Name can only contain letters and spaces", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!weight.matches("^\\d+(\\.\\d{1})?$")) {
                    Toast.makeText(staff_add_pet2.this, "Weight and estimated age must be digit", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check gender and neutered
                if (gender.matches("Choose your pet gender")) {
                    Toast.makeText(staff_add_pet2.this, "Please choose the pet gender.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (neutered.matches("Are your pet neutered ?")) {
                    Toast.makeText(staff_add_pet2.this, "Please choose the pet neutered status.", Toast.LENGTH_SHORT).show();
                    return;
                }

                passToStaffAddPet3();
            }
        });
    }

    private void openDatePicker() {
        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Open DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // When the user selects a date
                String formattedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);

                // Calculate the age
                int age = calculateAge(year, month, dayOfMonth);

                // Update the UI
                petEstimatedBirthday.setText(formattedDate);
                petEstimatedAge.setText(String.valueOf(age));
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private int calculateAge(int year, int month, int dayOfMonth) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, dayOfMonth);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

    public void passToStaffAddPet3() {
        String type = selectedType;
        String breed = selectedBreed;
        String name = ((EditText) findViewById(R.id.name)).getText().toString();
        String gender = ((Spinner) findViewById(R.id.selectPetGender)).getSelectedItem().toString();
        String weight = ((EditText) findViewById(R.id.weight)).getText().toString();
        String estimatedBirthday = ((EditText) findViewById(R.id.estimatedBirthday)).getText().toString();
        String estimatedAge = ((EditText) findViewById(R.id.estimatedAge)).getText().toString();
        String neutered = ((Spinner) findViewById(R.id.selectPetNeutered)).getSelectedItem().toString();

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


    public void goBackAddPet1Page(View view){
        Intent intent = new Intent(this, staff_add_pet1.class);
        TextView ppCheckBox = findViewById(R.id.backBtn);
        startActivity(intent);
    }
}
