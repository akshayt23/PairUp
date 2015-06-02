package com.rubberduck.pairup.model;

public class Shirt {
    private int id;
    private String imagePath;

    public Shirt(int id, String imagePath) {
        this.id = id;
        this.imagePath = imagePath;
    }

    public int getId() {
        return id;
    }

    public String getImagePath() {
        return imagePath;
    }
}
