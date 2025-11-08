package com.example.ensa_meal.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room Database Instance
 * Singleton pattern ensures only one database instance exists
 *
 * Database Version 2:
 * - Added Favorites table for user's favorite meals with comments
 */
@Database(entities = {MealEntity.class, FavoriteEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;
    private static final String DATABASE_NAME = "ensa_meal_database";

    /**
     * Get DAO for meal operations
     */
    public abstract MealDao mealDao();

    /**
     * Get DAO for favorites operations
     */
    public abstract FavoriteDao favoriteDao();

    /**
     * Get singleton database instance
     * Thread-safe implementation with double-checked locking
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME
            )
            .allowMainThreadQueries() // For simplicity - use background threads in production
            .fallbackToDestructiveMigration() // Recreate DB on version changes
            .build();
        }
        return instance;
    }
}
