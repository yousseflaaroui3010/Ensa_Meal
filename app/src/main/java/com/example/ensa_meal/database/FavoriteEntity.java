package com.example.ensa_meal.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Favorite Entity - Represents a meal added to favorites with optional comment
 * CRUD Operations:
 * - CREATE: Add meal to favorites (with or without comment)
 * - READ: View all favorites
 * - UPDATE: Edit comment on favorite
 * - DELETE: Remove from favorites
 */
@Entity(tableName = "favorites")
public class FavoriteEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "meal_id")
    private String mealId;

    @ColumnInfo(name = "meal_name")
    private String mealName;

    @ColumnInfo(name = "meal_image_url")
    private String mealImageUrl;

    @ColumnInfo(name = "meal_description")
    private String mealDescription;

    @ColumnInfo(name = "user_comment")
    private String userComment; // User's personal comment/note

    @ColumnInfo(name = "added_timestamp")
    private long addedTimestamp;

    public FavoriteEntity(@NonNull String mealId, String mealName, String mealImageUrl,
                          String mealDescription, String userComment) {
        this.mealId = mealId;
        this.mealName = mealName;
        this.mealImageUrl = mealImageUrl;
        this.mealDescription = mealDescription;
        this.userComment = userComment;
        this.addedTimestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    @NonNull
    public String getMealId() {
        return mealId;
    }

    public void setMealId(@NonNull String mealId) {
        this.mealId = mealId;
    }

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }

    public String getMealImageUrl() {
        return mealImageUrl;
    }

    public void setMealImageUrl(String mealImageUrl) {
        this.mealImageUrl = mealImageUrl;
    }

    public String getMealDescription() {
        return mealDescription;
    }

    public void setMealDescription(String mealDescription) {
        this.mealDescription = mealDescription;
    }

    public String getUserComment() {
        return userComment;
    }

    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }

    public long getAddedTimestamp() {
        return addedTimestamp;
    }

    public void setAddedTimestamp(long addedTimestamp) {
        this.addedTimestamp = addedTimestamp;
    }
}
