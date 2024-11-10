package com.example.veterinaryclinicmobileapplication;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class MonthYearValueFormatter extends ValueFormatter {

    private final String[] labels;

    public MonthYearValueFormatter(String[] labels) {
        this.labels = labels;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        int index = (int) value;

        if (index >= 0 && index < labels.length) {
            return labels[index];
        }
        return "";
    }
}
