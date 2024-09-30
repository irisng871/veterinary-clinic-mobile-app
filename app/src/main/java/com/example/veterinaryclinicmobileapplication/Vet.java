package com.example.veterinaryclinicmobileapplication;

public class Vet {
    private String vetId;
    private String vetName;
    private String imageUrl;

    // Constructor
    public Vet(String vetId, String vetName, String imageUrl) {
        this.vetId = vetId;
        this.vetName = vetName;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getVetId() {
        return vetId;
    }

    public String getVetName() {
        return vetName;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}


