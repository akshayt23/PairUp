package com.rubberduck.pairup.model;

public class Trouser {
    private int id;
    private String imagePath;

    public Trouser(int id, String imagePath) {
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
