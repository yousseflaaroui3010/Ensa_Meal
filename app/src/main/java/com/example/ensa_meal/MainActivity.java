package com.example.ensa_meal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ensa_meal.database.AppDatabase;
import com.example.ensa_meal.database.FavoriteDao;
import com.example.ensa_meal.database.FavoriteEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AdapterMeals.OnItemClickListener {

    private static final String TAG = "MainActivity";
    private static final String API_URL = "https://www.themealdb.com/api/json/v1/1/search.php?s=";

    private AdapterMeals adapterMeals;
    private RecyclerView recyclerView;
    private ArrayList<Plat> arrayList;
    private ProgressBar progressBar;
    private RequestQueue requestQueue;
    private FavoriteDao favoriteDao;
    private SearchView searchView;
    private Button favoritesButton;
    private Set<String> favoriteMealIds; // To store IDs of favorite meals

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        AppDatabase database = AppDatabase.getInstance(this);
        favoriteDao = database.favoriteDao();

        // Initialize views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup Listeners
        setupListeners();

        // Load initial favorite IDs
        loadFavoriteMealIds();

        // Initial search (e.g., for "chicken")
        searchMeals("chicken");
    }

    /**
     * Initialize all view components
     */
    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.search_view);
        favoritesButton = findViewById(R.id.favorites_button);
    }

    /**
     * Setup RecyclerView with adapter and layout manager
     */
    private void setupRecyclerView() {
        arrayList = new ArrayList<>();
        favoriteMealIds = new HashSet<>(); // Initialize here, will be populated by loadFavoriteMealIds
        adapterMeals = new AdapterMeals(arrayList, this, this, favoriteMealIds);
        recyclerView.setAdapter(adapterMeals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true); // Performance optimization
    }

    /**
     * Setup listeners for search and favorites button
     */
    private void setupListeners() {
        favoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchMeals(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchMeals(newText);
                return false;
            }
        });
    }

    /**
     * Load favorite meal IDs from the database
     */
    private void loadFavoriteMealIds() {
        favoriteMealIds.clear();
        favoriteMealIds.addAll(favoriteDao.getFavoriteMealIds());
        if (adapterMeals != null) {
            adapterMeals.setFavoriteMealIds(favoriteMealIds);
        }
    }

    /**
     * Search for meals from TheMealDB API using Volley
     */
    private void searchMeals(String query) {
        if (query == null || query.isEmpty()) {
            return;
        }
        // Show loading state
        showLoading(true);

        // Initialize request queue
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }

        // Create JSON request
        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET,
            API_URL + query,
            null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // Hide loading state
                    showLoading(false);
                    handleApiResponse(response);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Hide loading state
                    showLoading(false);
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
     * @param response JSON response from API
     */
    private void handleApiResponse(JSONObject response) {
        try {
            arrayList.clear();
            if (response.has("meals") && !response.isNull("meals")) {
                JSONArray mealsArray = response.getJSONArray("meals");

                // Parse and create Plat objects
                for (int i = 0; i < mealsArray.length(); i++) {
                    JSONObject meal = mealsArray.getJSONObject(i);

                    // Extract data with null safety
                    String id = meal.optString("idMeal", "0");
                    String name = meal.optString("strMeal", "Unknown");
                    String imageUrl = meal.optString("strMealThumb", "");
                    String description = meal.optString("strInstructions", "No instructions available");

                    // Create Plat and add to list
                    Plat plat = new Plat(id, name, imageUrl, description);
                    arrayList.add(plat);
                }
                Log.d(TAG, "Successfully loaded " + arrayList.size() + " meals");

            } else {
                Log.d(TAG, "Response doesn't contain 'meals' array or it is null");
                Toast.makeText(this, "No meals found", Toast.LENGTH_SHORT).show();
            }
            adapterMeals.notifyDataSetChanged();
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
            Toast.makeText(this, "Error parsing data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(int position) {
        Plat plat = arrayList.get(position);
        Intent intent = new Intent(this, Instructions.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("MEAL", plat);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onToggleFavoriteClick(int position) {
        Plat plat = arrayList.get(position);
        String mealId = plat.getId();

        if (favoriteDao.isFavorite(mealId)) {
            // Meal is already a favorite, remove it
            favoriteDao.removeFromFavoritesById(mealId);
            favoriteMealIds.remove(mealId);
            Toast.makeText(this, plat.getName() + " removed from favorites", Toast.LENGTH_SHORT).show();
        } else {
            // Meal is not a favorite, add it
            FavoriteEntity favorite = new FavoriteEntity(
                    mealId,
                    plat.getName(),
                    plat.getImageURL(),
                    plat.getInstructions(),
                    "", // Default comment
                    0   // Default rating
            );
            favoriteDao.addToFavorites(favorite);
            favoriteMealIds.add(mealId);
            Toast.makeText(this, plat.getName() + " added to favorites", Toast.LENGTH_SHORT).show();
        }
        adapterMeals.setFavoriteMealIds(favoriteMealIds); // Update adapter with new favorite status
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

    @Override
    protected void onResume() {
        super.onResume();
        // Reload favorite IDs and update adapter when returning to this activity
        loadFavoriteMealIds();
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