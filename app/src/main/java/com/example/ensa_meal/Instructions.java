package com.example.ensa_meal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ensa_meal.database.AppDatabase;
import com.example.ensa_meal.database.FavoriteDao;
import com.example.ensa_meal.database.FavoriteEntity;

/**
 * Instructions Activity - Display detailed meal category information
 * Handles both old and new Android API versions for Serializable data retrieval
 *
 * Features:
 * - View meal details
 * - Add to Favorites (CREATE operation)
 * - Remove from Favorites if already added
 */
public class Instructions extends AppCompatActivity {
    private ImageView imageView;
    private TextView IDmeal, Name, Inst;
    private Plat currentPlat;

    // Favorites database
    private AppDatabase database;
    private FavoriteDao favoriteDao;
    private boolean isFavorite = false;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        // Initialize database
        database = AppDatabase.getInstance(this);
        favoriteDao = database.favoriteDao();

        // Initialize views
        imageView = findViewById(R.id.imageInst);
        IDmeal = findViewById(R.id.IdInst);
        Name = findViewById(R.id.NameInstr);
        Inst = findViewById(R.id.Instr_Inst);

        // Retrieve meal data from intent
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            // Use API-level appropriate method to retrieve Serializable
            Plat plat;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ (API 33+) - Use type-safe method
                plat = bundle.getSerializable("MEAL", Plat.class);
            } else {
                // Android 12 and below - Use legacy method
                plat = (Plat) bundle.getSerializable("MEAL");
            }

            // Display meal data with null safety
            if (plat != null) {
                currentPlat = plat; // Store for favorites

                // Check if already in favorites
                isFavorite = favoriteDao.isFavorite(plat.getId());

                IDmeal.setText(plat.getId() != null ? plat.getId() : "N/A");
                Name.setText(plat.getName() != null ? plat.getName() : "Unknown");
                Inst.setText(plat.getInstructions() != null ? plat.getInstructions() : "No description available");

                // Load image with Glide
                Glide.with(this)
                    .load(plat.getImageURL())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageView);
            } else {
                Toast.makeText(this, "Error: Meal data not found", Toast.LENGTH_SHORT).show();
                finish(); // Close activity if no data
            }
        } else {
            Toast.makeText(this, "Error: No data received", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.instructions_menu, menu);

        // Update favorite icon based on current state
        MenuItem favoriteItem = menu.findItem(R.id.action_add_favorite);
        if (isFavorite) {
            favoriteItem.setIcon(android.R.drawable.btn_star_big_on);
            favoriteItem.setTitle("Remove from Favorites");
        } else {
            favoriteItem.setIcon(android.R.drawable.btn_star_big_off);
            favoriteItem.setTitle("Add to Favorites");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_favorite) {
            if (isFavorite) {
                // Remove from favorites (DELETE)
                removeFromFavorites();
            } else {
                // Add to favorites (CREATE)
                showAddToFavoritesDialog();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * CREATE - Add to Favorites with optional comment
     */
    private void showAddToFavoritesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add to Favorites");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_comment, null);
        EditText editComment = dialogView.findViewById(R.id.editComment);

        builder.setView(dialogView);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String comment = editComment.getText().toString().trim();

            // Create favorite entity
            FavoriteEntity favorite = new FavoriteEntity(
                    currentPlat.getId(),
                    currentPlat.getName(),
                    currentPlat.getImageURL(),
                    currentPlat.getInstructions(),
                    comment.isEmpty() ? null : comment
            );

            // Add to favorites database
            favoriteDao.addToFavorites(favorite);

            isFavorite = true;
            invalidateOptionsMenu(); // Refresh menu icon

            Toast.makeText(this, "Added to Favorites!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * DELETE - Remove from Favorites
     */
    private void removeFromFavorites() {
        new AlertDialog.Builder(this)
                .setTitle("Remove from Favorites")
                .setMessage("Remove this meal from your favorites?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    favoriteDao.removeFromFavoritesById(currentPlat.getId());
                    isFavorite = false;
                    invalidateOptionsMenu(); // Refresh menu icon
                    Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}