package com.example.ensa_meal.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object (DAO) for Meal Categories
 * Defines all database operations for CRUD functionality
 */
@Dao
public interface MealDao {

    /**
     * INSERT - Add a new meal category
     * OnConflictStrategy.REPLACE: If ID exists, replace it
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MealEntity meal);

    /**
     * INSERT MULTIPLE - Add multiple meal categories
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MealEntity> meals);

    /**
     * UPDATE - Modify an existing meal category
     */
    @Update
    void update(MealEntity meal);

    /**
     * DELETE - Remove a meal category
     */
    @Delete
    void delete(MealEntity meal);

    /**
     * DELETE ALL - Clear all meal categories
     */
    @Query("DELETE FROM meal_categories")
    void deleteAll();

    /**
     * READ ALL - Get all meal categories ordered by name
     */
    @Query("SELECT * FROM meal_categories ORDER BY name ASC")
    List<MealEntity> getAllMeals();

    /**
     * READ BY ID - Get a specific meal category
     */
    @Query("SELECT * FROM meal_categories WHERE id = :mealId LIMIT 1")
    MealEntity getMealById(String mealId);

    /**
     * SEARCH - Find meals by name (case-insensitive)
     */
    @Query("SELECT * FROM meal_categories WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    List<MealEntity> searchMealsByName(String searchQuery);

    /**
     * COUNT - Get total number of meals
     */
    @Query("SELECT COUNT(*) FROM meal_categories")
    int getMealCount();
}
