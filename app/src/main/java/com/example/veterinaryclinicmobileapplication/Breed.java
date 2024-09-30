package com.example.veterinaryclinicmobileapplication;

public class Breed {
    private String name;
    private String imageUrl;

    public Breed(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
