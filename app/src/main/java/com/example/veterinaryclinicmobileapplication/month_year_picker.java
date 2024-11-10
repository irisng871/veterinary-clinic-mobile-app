package com.example.veterinaryclinicmobileapplication;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import java.util.Calendar;

public class month_year_picker {

    Dialog dialog;
    Spinner monthSpinner, yearSpinner;
    Button okButton;
    OnDateSelectedListener listener;

    public month_year_picker(Context context, OnDateSelectedListener listener) {
        this.listener = listener;
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.month_year_picker);

        monthSpinner = dialog.findViewById(R.id.monthSpinner);
        yearSpinner = dialog.findViewById(R.id.yearSpinner);
        okButton = dialog.findViewById(R.id.okButton);

        setupSpinners();

        okButton.setOnClickListener(view -> {
            String selectedMonth = monthSpinner.getSelectedItem().toString();
            String selectedYear = yearSpinner.getSelectedItem().toString();
            listener.onDateSelected(selectedMonth, selectedYear);
            dialog.dismiss();
        });
    }

    public void show() {
        dialog.show();
    }

    private void setupSpinners() {
        String[] months = new String[]{
                "January", "February", "March",
                "April", "May", "June",
                "July", "August", "September",
                "October", "November", "December"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(dialog.getContext(),
                android.R.layout.simple_spinner_item, months);
        monthSpinner.setAdapter(monthAdapter);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[50];
        for (int i = 0; i < years.length; i++) {
            years[i] = String.valueOf(currentYear - i);
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(dialog.getContext(),
                android.R.layout.simple_spinner_item, years);
        yearSpinner.setAdapter(yearAdapter);
    }

    public interface OnDateSelectedListener {
        void onDateSelected(String month, String year);
    }
}
