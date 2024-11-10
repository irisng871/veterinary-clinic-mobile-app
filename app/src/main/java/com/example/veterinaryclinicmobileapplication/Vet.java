package com.example.veterinaryclinicmobileapplication;

public class Vet {
    private String id;
    private String name;
    private String imageUrl;
    private String specialtyArea;

    public Vet(String id, String name, String imageUrl, String specialtyArea) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.specialtyArea = specialtyArea;
    }

    public String getid() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSpecialtyArea() {
        return specialtyArea;
    }
}


