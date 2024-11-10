package com.example.veterinaryclinicmobileapplication;

public class Appointment {
    private String petName;
    private String date;
    private String time;
    private String vetId;
    private String appointmentId;

    public Appointment(String petName, String date, String time, String vetId, String appointmentId) {
        this.petName = petName;
        this.date = date;
        this.time = time;
        this.vetId = vetId;
        this.appointmentId = appointmentId;
    }

    public String getPetName() {
        return petName;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getVetId() {
        return vetId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }
}
