package com.example.ensa_meal;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ensa_meal.database.AppDatabase;
import com.example.ensa_meal.database.FavoriteDao;
import com.example.ensa_meal.database.FavoriteEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * FavoritesActivity - Display and manage favorite meals
 *
 * CRUD Operations:
 * - CREATE: Add meals to favorites (done from MainActivity/Instructions)
 * - READ: Display all favorites with comments
 * - UPDATE: Edit comments on favorites
 * - DELETE: Remove from favorites (swipe or click)
 * - SEARCH: Filter favorites by name
 */
public class FavoritesActivity extends AppCompatActivity implements FavoritesAdapter.OnFavoriteActionListener {

    private static final String TAG = "FavoritesActivity";

    private RecyclerView recyclerViewFavorites;
    private FavoritesAdapter favoritesAdapter;
    private ArrayList<FavoriteEntity> favoritesList;
    private ArrayList<FavoriteEntity> fullList;
    private TextView emptyView;

    private AppDatabase database;
    private FavoriteDao favoriteDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Favorites");
        }

        // Initialize database
        database = AppDatabase.getInstance(this);
        favoriteDao = database.favoriteDao();

        // Initialize views
        recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites);
        emptyView = findViewById(R.id.emptyFavoritesText);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup swipe to delete
        setupSwipeToDelete();

        // Load favorites
        loadFavorites();
    }

    private void setupRecyclerView() {
        favoritesList = new ArrayList<>();
        fullList = new ArrayList<>();
        favoritesAdapter = new FavoritesAdapter(favoritesList, this, this);
        recyclerViewFavorites.setAdapter(favoritesAdapter);
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFavorites.setHasFixedSize(true);
    }

    /**
     * READ - Load all favorites from database
     */
    private void loadFavorites() {
        List<FavoriteEntity> entities = favoriteDao.getAllFavorites();
        favoritesList.clear();
        fullList.clear();
        favoritesList.addAll(entities);
        fullList.addAll(entities);

        favoritesAdapter.notifyDataSetChanged();

        // Show/hide empty view
        if (favoritesList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerViewFavorites.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerViewFavorites.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "Loaded " + favoritesList.size() + " favorites");
    }

    /**
     * UPDATE - Edit comment and rating on favorite
     */
    @Override
    public void onEditFavorite(int position) {
        FavoriteEntity favorite = favoritesList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Favorite");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_favorite, null);
        EditText editComment = dialogView.findViewById(R.id.editComment);
        RatingBar editRating = dialogView.findViewById(R.id.editRating);

        // Pre-fill with existing comment and rating
        if (favorite.getUserComment() != null && !favorite.getUserComment().isEmpty()) {
            editComment.setText(favorite.getUserComment());
        }
        editRating.setRating(favorite.getUserRating());

        builder.setView(dialogView);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String comment = editComment.getText().toString().trim();
            float rating = editRating.getRating();

            // Update comment and rating in database
            favoriteDao.updateComment(favorite.getMealId(), comment);
            favoriteDao.updateRating(favorite.getMealId(), rating);

            // Update local object
            favorite.setUserComment(comment);
            favorite.setUserRating(rating);
            favoritesAdapter.notifyItemChanged(position);

            Toast.makeText(this, "Favorite updated", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Updated favorite: " + favorite.getMealName());
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Navigate to meal details
     */
    @Override
    public void onViewDetails(int position) {
        FavoriteEntity favorite = favoritesList.get(position);

        // Create Plat object from favorite
        Plat plat = new Plat(favorite.getMealId(), favorite.getMealName(),
                favorite.getMealImageUrl(), favorite.getMealDescription());

        Intent intent = new Intent(this, Instructions.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("MEAL", plat);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * Setup swipe to delete
     */
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                deleteFavorite(position);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewFavorites);
    }

    /**
     * DELETE - Remove from favorites
     */
    private void deleteFavorite(int position) {
        FavoriteEntity favorite = favoritesList.get(position);

        // Delete from database
        favoriteDao.removeFromFavorites(favorite);

        // Remove from lists
        favoritesList.remove(position);
        fullList.remove(favorite);
        favoritesAdapter.notifyItemRemoved(position);

        Toast.makeText(this, "Removed: " + favorite.getMealName(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Removed favorite: " + favorite.getMealName());

        // Check if list is empty
        if (favoritesList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerViewFavorites.setVisibility(View.GONE);
        }
    }

    /**
     * SEARCH - Filter favorites
     */
    private void filterFavorites(String query) {
        if (query == null || query.isEmpty()) {
            favoritesList.clear();
            favoritesList.addAll(fullList);
        } else {
            favoritesList.clear();
            for (FavoriteEntity favorite : fullList) {
                if (favorite.getMealName().toLowerCase().contains(query.toLowerCase())) {
                    favoritesList.add(favorite);
                }
            }
        }
        favoritesAdapter.notifyDataSetChanged();
        Log.d(TAG, "Filtered favorites: " + favoritesList.size() + " results");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favorites_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search_favorites);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("Search favorites...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterFavorites(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFavorites(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_clear_favorites) {
            // Clear all favorites with confirmation
            new AlertDialog.Builder(this)
                    .setTitle("Clear All Favorites")
                    .setMessage("Remove all meals from favorites?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        favoriteDao.clearAllFavorites();
                        loadFavorites();
                        Toast.makeText(this, "All favorites cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload favorites when returning to this activity
        loadFavorites();
    }
}
