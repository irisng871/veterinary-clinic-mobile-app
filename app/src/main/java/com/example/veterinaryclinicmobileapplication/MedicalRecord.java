package com.example.veterinaryclinicmobileapplication;

public class MedicalRecord {
    private String appointmentId;
    private String petId;
    private String petName;
    private String date;
    private String time;

    public MedicalRecord(String appointmentId, String petId, String petName, String date, String time) {
        this.appointmentId = appointmentId;
        this.petId = petId;
        this.petName = petName;
        this.date = date;
        this.time = time;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getPetId() {
        return petId;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
