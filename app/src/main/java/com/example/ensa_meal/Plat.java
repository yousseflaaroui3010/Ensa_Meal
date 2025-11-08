package com.example.ensa_meal;

import java.io.Serializable;

/**
 * Plat (Meal Category) Model Class
 * Represents a meal category from TheMealDB API
 * Implements Serializable to allow passing between activities via Intent
 *
 * Data Structure:
 * - id: Category ID from API (idCategory)
 * - name: Category name (strCategory)
 * - imageURL: Thumbnail image URL (strCategoryThumb)
 * - instructions: Category description (strCategoryDescription)
 */
public class Plat implements Serializable {
    private static final long serialVersionUID = 1L; // Version control for Serializable

    private String id;
    private String name;
    private String imageURL;
    private String instructions;

    /**
     * Constructor with all fields
     * @param id Category ID
     * @param name Category name
     * @param imageURL Image URL
     * @param instructions Category description
     */
    public Plat(String id, String name, String imageURL, String instructions) {
        this.id = id;
        this.name = name;
        this.imageURL = imageURL;
        this.instructions = instructions;
    }

    /**
     * Default constructor
     */
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
