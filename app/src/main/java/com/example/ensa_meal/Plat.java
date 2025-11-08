package com.example.ensa_meal;

import java.io.Serializable;

public class Plat implements Serializable {
    String id,name,imageURL,instructions;

    public Plat(String id, String name, String imageURL, String instructions) {
        this.id = id;
        this.name = name;
        this.imageURL = imageURL;
        this.instructions = instructions;
    }

    public Plat() {
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

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
