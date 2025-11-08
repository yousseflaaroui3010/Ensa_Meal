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
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ensa_meal.database.AppDatabase;
import com.example.ensa_meal.database.MealDao;
import com.example.ensa_meal.database.MealEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MainActivity - Main screen displaying meal categories with CRUD operations
 * Architecture: MVC + Room Database
 * - Model: Plat.java, MealEntity.java
 * - View: activity_main.xml, model_plat.xml
 * - Controller: MainActivity.java, AdapterMeals.java
 * - Database: Room (AppDatabase, MealDao)
 *
 * Features:
 * - CREATE: Add new meals via FAB + Dialog
 * - READ: Display all meals from local database
 * - UPDATE: Long-press to edit meal details
 * - DELETE: Swipe left/right to delete
 * - SEARCH: Real-time filtering with SearchView
 *
 * API Endpoint: https://www.themealdb.com/api/json/v1/1/categories.php (Initial sync)
 * Network Library: Volley
 * Image Library: Glide
 * Database: Room
 */
public class MainActivity extends AppCompatActivity implements AdapterMeals.OnItemLongClickListener {

    private static final String TAG = "MainActivity";
    private static final String API_URL = "https://www.themealdb.com/api/json/v1/1/categories.php";

    private AdapterMeals adapterMeals;
    private RecyclerView recyclerView;
    private ArrayList<Plat> arrayList;
    private ArrayList<Plat> fullList; // For search filtering
    private ProgressBar progressBar;
    private RequestQueue requestQueue;
    private FloatingActionButton fabAdd;

    // Room Database components
    private AppDatabase database;
    private MealDao mealDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        database = AppDatabase.getInstance(this);
        mealDao = database.mealDao();

        // Initialize views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup swipe to delete
        setupSwipeToDelete();

        // Setup FAB for adding meals
        setupFAB();

        // Load meals from database
        loadMealsFromDatabase();

        // Check if database is empty, then fetch from API
        if (mealDao.getMealCount() == 0) {
            fetchMealCategories();
        }
    }

    /**
     * Initialize all view components
     */
    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        // Note: ProgressBar would need to be added to layout (commented out for now)
        // progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Setup RecyclerView with adapter and layout manager
     */
    private void setupRecyclerView() {
        arrayList = new ArrayList<>();
        fullList = new ArrayList<>();
        adapterMeals = new AdapterMeals(arrayList, this, this);
        recyclerView.setAdapter(adapterMeals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true); // Performance optimization
    }

    /**
     * Setup Floating Action Button for adding new meals
     */
    private void setupFAB() {
        fabAdd.setOnClickListener(v -> showAddMealDialog());
    }

    /**
     * Setup swipe to delete functionality
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
                deleteMeal(position);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Fetch meal categories from TheMealDB API using Volley
     */
    private void fetchMealCategories() {
        // Show loading state (if ProgressBar exists in layout)
        // showLoading(true);

        // Initialize request queue
        requestQueue = Volley.newRequestQueue(this);

        // Create JSON request
        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET,
            API_URL,
            null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // Hide loading state
                    // showLoading(false);

                    handleApiResponse(response);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Hide loading state
                    // showLoading(false);

                    handleApiError(error);
                }
            }
        );

        // Add request to queue with timeout configuration
        request.setTag(TAG);
        requestQueue.add(request);
    }

    /**
     * Handle successful API response
     * Saves data to Room database
     * @param response JSON response from API
     */
    private void handleApiResponse(JSONObject response) {
        try {
            if (response.has("categories")) {
                JSONArray categoriesArray = response.getJSONArray("categories");

                List<MealEntity> mealEntities = new ArrayList<>();

                // Parse and create MealEntity objects
                for (int i = 0; i < categoriesArray.length(); i++) {
                    JSONObject category = categoriesArray.getJSONObject(i);

                    // Extract data with null safety
                    String id = category.optString("idCategory", "0");
                    String name = category.optString("strCategory", "Unknown");
                    String imageUrl = category.optString("strCategoryThumb", "");
                    String description = category.optString("strCategoryDescription", "No description available");

                    // Create MealEntity and add to list
                    MealEntity entity = new MealEntity(id, name, imageUrl, description);
                    mealEntities.add(entity);
                }

                // Save to database
                mealDao.insertAll(mealEntities);

                // Reload from database to update UI
                loadMealsFromDatabase();

                Log.d(TAG, "Successfully loaded and saved " + mealEntities.size() + " meal categories");
                Toast.makeText(this, "Synced " + mealEntities.size() + " categories from API", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Response doesn't contain 'categories' array");
                Toast.makeText(this, "Invalid response format", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
            Toast.makeText(this, "Error parsing data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Load all meals from Room database
     */
    private void loadMealsFromDatabase() {
        List<MealEntity> entities = mealDao.getAllMeals();
        arrayList.clear();
        fullList.clear();

        for (MealEntity entity : entities) {
            Plat plat = new Plat(entity.getId(), entity.getName(),
                    entity.getImageURL(), entity.getDescription());
            arrayList.add(plat);
            fullList.add(plat);
        }

        adapterMeals.notifyDataSetChanged();
        Log.d(TAG, "Loaded " + arrayList.size() + " meals from database");
    }

    /**
     * CREATE - Show dialog to add new meal
     */
    private void showAddMealDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Meal Category");

        // Inflate custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_meal, null);
        EditText editName = dialogView.findViewById(R.id.editMealName);
        EditText editImageUrl = dialogView.findViewById(R.id.editMealImageUrl);
        EditText editDescription = dialogView.findViewById(R.id.editMealDescription);

        builder.setView(dialogView);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = editName.getText().toString().trim();
            String imageUrl = editImageUrl.getText().toString().trim();
            String description = editDescription.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate unique ID
            String id = UUID.randomUUID().toString();

            // Create and save to database
            MealEntity newMeal = new MealEntity(id, name, imageUrl, description);
            mealDao.insert(newMeal);

            // Reload data
            loadMealsFromDatabase();
            Toast.makeText(this, "Meal added successfully", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * UPDATE - Show dialog to edit existing meal
     * Called from long-press on RecyclerView item
     */
    @Override
    public void onItemLongClick(int position) {
        Plat plat = arrayList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Meal Category");

        // Inflate custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_meal, null);
        EditText editName = dialogView.findViewById(R.id.editMealName);
        EditText editImageUrl = dialogView.findViewById(R.id.editMealImageUrl);
        EditText editDescription = dialogView.findViewById(R.id.editMealDescription);

        // Pre-fill with existing data
        editName.setText(plat.getName());
        editImageUrl.setText(plat.getImageURL());
        editDescription.setText(plat.getInstructions());

        builder.setView(dialogView);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String name = editName.getText().toString().trim();
            String imageUrl = editImageUrl.getText().toString().trim();
            String description = editDescription.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update in database
            MealEntity updatedMeal = new MealEntity(plat.getId(), name, imageUrl, description);
            mealDao.update(updatedMeal);

            // Reload data
            loadMealsFromDatabase();
            Toast.makeText(this, "Meal updated successfully", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * DELETE - Remove meal from database
     * Called from swipe gesture
     */
    private void deleteMeal(int position) {
        Plat plat = arrayList.get(position);

        // Create entity for deletion
        MealEntity entityToDelete = new MealEntity(plat.getId(), plat.getName(),
                plat.getImageURL(), plat.getInstructions());

        // Delete from database
        mealDao.delete(entityToDelete);

        // Remove from list
        arrayList.remove(position);
        fullList.remove(plat);
        adapterMeals.notifyItemRemoved(position);

        Toast.makeText(this, "Deleted: " + plat.getName(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Deleted meal: " + plat.getName());
    }

    /**
     * Handle API error response
     * @param error Volley error object
     */
    private void handleApiError(VolleyError error) {
        String errorMessage = "Network error occurred";

        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            errorMessage = "Error code: " + statusCode;
            Log.e(TAG, "Network error - Status Code: " + statusCode);
        } else if (error.getMessage() != null) {
            errorMessage = error.getMessage();
            Log.e(TAG, "Volley error: " + errorMessage);
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Show/hide loading indicator
     * @param show true to show, false to hide
     */
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * SEARCH - Filter meals by name
     */
    private void filterMeals(String query) {
        if (query == null || query.isEmpty()) {
            // Show all meals
            arrayList.clear();
            arrayList.addAll(fullList);
        } else {
            // Filter meals
            arrayList.clear();
            for (Plat plat : fullList) {
                if (plat.getName().toLowerCase().contains(query.toLowerCase())) {
                    arrayList.add(plat);
                }
            }
        }
        adapterMeals.notifyDataSetChanged();
        Log.d(TAG, "Filtered meals: " + arrayList.size() + " results for query: " + query);
    }

    /**
     * Create options menu with search functionality
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("Search meals...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterMeals(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterMeals(newText);
                return false;
            }
        });

        return true;
    }

    /**
     * Handle options menu item clicks
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_favorites) {
            // Open Favorites Activity
            Intent intent = new Intent(this, FavoritesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_refresh) {
            // Refresh from API
            fetchMealCategories();
            return true;
        } else if (id == R.id.action_clear_all) {
            // Clear all meals with confirmation
            new AlertDialog.Builder(this)
                    .setTitle("Clear All Meals")
                    .setMessage("Are you sure you want to delete all meals?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        mealDao.deleteAll();
                        loadMealsFromDatabase();
                        Toast.makeText(this, "All meals deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Cancel all pending requests when activity stops
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}