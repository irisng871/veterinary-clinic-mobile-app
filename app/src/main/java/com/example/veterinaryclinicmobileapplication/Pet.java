package com.example.veterinaryclinicmobileapplication;

public class Pet {
    private String id;
    private String name;
    private String imageUrl;
    private String petOwnerId;

    public Pet(String id, String name, String imageUrl, String petOwnerId) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.petOwnerId = petOwnerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPetOwnerId() {
        return petOwnerId;
    }

    public void setPetOwnerId(String petOwnerId) {
        this.petOwnerId = petOwnerId;
    }
}

