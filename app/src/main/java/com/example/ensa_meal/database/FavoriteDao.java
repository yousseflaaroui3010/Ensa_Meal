package com.example.ensa_meal.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Favorite DAO - Data Access Object for Favorites
 * CRUD Operations for Favorites Management
 */
@Dao
public interface FavoriteDao {

    /**
     * CREATE - Add meal to favorites
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addToFavorites(FavoriteEntity favorite);

    /**
     * READ - Get all favorites ordered by timestamp (newest first)
     */
    @Query("SELECT * FROM favorites ORDER BY added_timestamp DESC")
    List<FavoriteEntity> getAllFavorites();

    /**
     * READ - Check if meal is in favorites
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE meal_id = :mealId)")
    boolean isFavorite(String mealId);

    /**
     * READ - Get specific favorite
     */
    @Query("SELECT * FROM favorites WHERE meal_id = :mealId LIMIT 1")
    FavoriteEntity getFavoriteById(String mealId);

    /**
     * UPDATE - Update comment on favorite
     */
    @Query("UPDATE favorites SET user_comment = :comment WHERE meal_id = :mealId")
    void updateComment(String mealId, String comment);

    /**
     * UPDATE - Update rating on favorite
     */
    @Query("UPDATE favorites SET user_rating = :rating WHERE meal_id = :mealId")
    void updateRating(String mealId, float rating);

    /**
     * UPDATE - Alternative using entity
     */
    @Update
    void updateFavorite(FavoriteEntity favorite);

    /**
     * DELETE - Remove from favorites
     */
    @Delete
    void removeFromFavorites(FavoriteEntity favorite);

    /**
     * DELETE - Remove by ID
     */
    @Query("DELETE FROM favorites WHERE meal_id = :mealId")
    void removeFromFavoritesById(String mealId);

    /**
     * DELETE ALL - Clear all favorites
     */
    @Query("DELETE FROM favorites")
    void clearAllFavorites();

    /**
     * COUNT - Get total favorites count
     */
    @Query("SELECT COUNT(*) FROM favorites")
    int getFavoritesCount();

    /**
     * SEARCH - Search favorites by name
     */
    @Query("SELECT * FROM favorites WHERE meal_name LIKE '%' || :query || '%' ORDER BY added_timestamp DESC")
    List<FavoriteEntity> searchFavorites(String query);
}
