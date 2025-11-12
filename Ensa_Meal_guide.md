# Building Ensa Meal: A Complete From-Scratch Guide

Welcome, Youssef. I'm Davinci, and I'll be your guide on this journey.

Over my 35 years building software systems, I've learned that true understanding comes not from memorizing code, but from grasping the **why** behind every decision. By the time you complete this guide, you won't just have built an Android app—you'll understand it so deeply that you could rebuild it with your eyes closed and defend every architectural choice to your professors.

Ensa Meal is deceptively simple on the surface: three screens that let users browse meals, save favorites, and chat with an AI about what to cook. But beneath that simplicity lies a sophisticated architecture that demonstrates professional Android development practices. You'll work with REST APIs, local databases, real-time AI integration, and modern Android patterns—all while learning the principles that separate hobbyist code from production-grade software.

This guide assumes you're intelligent but new to Android. I'll explain everything, from why we structure folders a certain way to why specific code patterns exist. Think of this as learning not just to cook a recipe, but understanding the chemistry of cooking itself.

Let's begin.

---

## Part 1: Understanding What We're Building

Before touching any code, let's establish a mental model of Ensa Meal.

### The Three Screens

**Screen 1: Meal Browser (MainActivity)**
- Displays a scrollable grid of meals from the internet
- Users can search for specific dishes
- Each meal card is clickable to see full details
- Think of this as the "storefront window"—visually appealing, easy to browse

**Screen 2: Favorites (FavoritesActivity)**
- Shows only meals the user has saved
- Users can add personal notes and ratings
- Swipe-to-delete functionality
- This is the "personal cookbook"—curated and customizable

**Screen 3: AI Chat (AIChatActivity)**
- A conversational interface powered by AI
- The AI can see your favorite meals
- Ask "What should I cook?" and it suggests from YOUR favorites, not random meals
- Supports multiple languages, including Moroccan Darija
- This is your "smart cooking assistant"

### The Data Flow

Here's how information moves through your app:

```
TheMealDB API → Your App → User sees meals
                    ↓
              User saves favorite
                    ↓
              Room Database (local storage on device)
                    ↓
              AI reads favorites from database
                    ↓
              AI suggests meals via Groq API
```

Understanding this flow is crucial. Data enters from the internet, gets stored locally, and is read by multiple parts of your app. This is a **three-tier architecture**: presentation (what users see), business logic (how the app works), and data (where information lives).

### Why This Architecture Matters

When professors ask about your design choices, here's your answer:

"I separated concerns into three layers. The UI layer handles display and user interaction. The data layer manages persistence and network calls. The business logic connects them. This separation means I can change how data is stored without touching the UI, or redesign the UI without touching data access—a principle called **loose coupling** that makes code maintainable."

Now that you understand the big picture, let's build it.

---

## Part 2: Setting Up Your Development Environment

### Installing Android Studio

1. Download Android Studio from developer.android.com
2. During installation, select these components:
   - Android SDK (the tools to build Android apps)
   - Android SDK Platform (specific Android version tools)
   - Android Virtual Device (emulator to test your app)

**Why Android Studio?** It's the official IDE from Google, meaning it has the best integration with Android tools. Think of it as a chef's kitchen—everything you need is in one place.

### Creating the Project

1. Open Android Studio
2. Select "New Project"
3. Choose "Empty Views Activity"

**Why "Empty Views Activity"?** This template gives you a basic app structure without cluttering it with example code. You start with a blank canvas.

4. Configure your project:
   - **Name**: Ensa_Meal (with underscore, not hyphen—Android naming convention)
   - **Package name**: com.example.ensa_meal (reverse domain notation, a Java convention)
   - **Save location**: Choose a location you'll remember
   - **Language**: Java (stable, widely documented, and what this guide uses)
   - **Minimum SDK**: API 24 (covers 95% of active Android devices)

5. Click "Finish"

Android Studio will now create your project structure. This takes 1-2 minutes as it downloads necessary files.

### Understanding the Project Structure

When the process completes, you'll see this folder structure:

```
Ensa_Meal/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/ensa_meal/
│   │       │   └── MainActivity.java
│   │       ├── res/
│   │       │   └── layout/
│   │       │       └── activity_main.xml
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
└── settings.gradle.kts
```

**Let me explain each piece:**

**app/**: This is your application module. Everything specific to your app lives here.

**src/main/java/**: Your Java code goes here. Think of this as the "brain" folder.

**MainActivity.java**: The entry point of your app. When users tap your app icon, this code runs first.

**res/**: Resources—everything that's not code. Images, layouts, text strings, colors.

**layout/**: XML files that define how screens look. Think of XML as a blueprint, and Java as the construction crew that brings it to life.

**AndroidManifest.xml**: The master configuration file. It tells Android: "Here's what my app can do, what permissions it needs, and what screens exist."

**build.gradle.kts**: Your build configuration. This is where you list dependencies (libraries your app uses) and configure build settings. The `.kts` extension means it uses Kotlin DSL, but you're still writing a Java app—this file is just configuration.

### The Gradle Build System

Gradle is your build automation tool. Think of it like a recipe that tells the computer: "Here's how to transform my source code into an installable app."

When you add a dependency (like a library for loading images), you add it to `app/build.gradle.kts`, and Gradle downloads it and includes it in your app. This is better than manually downloading JAR files and managing them yourself.

---

## Part 3: Building Screen 1 - The Meal Browser

### Step 3.1: Adding Dependencies

Open `app/build.gradle.kts` and find the `dependencies` block. Add these lines:

```kotlin
dependencies {
    // Existing dependencies will already be here
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")

    // Add these new ones:
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
```

**Why each dependency?**

**Volley**: A networking library from Google. When you want to fetch meal data from the internet, Volley handles the HTTP request/response cycle. Without it, you'd write hundreds of lines of networking code yourself.

**Glide**: An image loading library. When you have a URL like "https://example.com/meal.jpg", Glide downloads it, caches it, and displays it in your app. Without Glide, you'd handle threading, caching, and memory management manually—complex and error-prone.

**Room**: Google's database library. It provides a clean API over SQLite (Android's built-in database). When users save favorite meals, Room stores them locally on the device.

**OkHttp**: An advanced HTTP client. We'll use this for the AI chat because it gives us more control than Volley (like custom headers for authentication).

After adding these, click "Sync Now" at the top of the file. Gradle will download these libraries.

### Step 3.2: Adding Internet Permission

Open `app/src/main/AndroidManifest.xml` and add this line inside the `<manifest>` tag (not inside `<application>`):

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

**Why?** Android is security-conscious. Apps must explicitly declare permissions they need. Without this line, your app can't access the internet—network calls would silently fail.

**Security note**: This permission is granted automatically at install time (it's not "dangerous" in Android's classification), so users won't see a popup. They see it in the app's permission list in Settings.

### Step 3.3: Creating the Plat Model Class

In Android, a **model** is a class that represents data. Your meal data comes from the API as JSON. You need a Java class to hold that data.

Right-click `com.example.ensa_meal` folder → New → Java Class → Name it `Plat`

**Why "Plat"?** It's French for "dish" or "meal". Naming is arbitrary, but being consistent matters.

Write this code in `app/src/main/java/com/example/ensa_meal/Plat.java`:

```java
package com.example.ensa_meal;

public class Plat {
    private String id;
    private String nom;
    private String photo;
    private String description;

    public Plat(String id, String nom, String photo, String description) {
        this.id = id;
        this.nom = nom;
        this.photo = photo;
        this.description = description;
    }

    // Getters
    public String getId() { return id; }
    public String getNom() { return nom; }
    public String getPhoto() { return photo; }
    public String getDescription() { return description; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPhoto(String photo) { this.photo = photo; }
    public void setDescription(String description) { this.description = description; }
}
```

**Understanding this code:**

**private fields**: The data is private—other classes can't directly access it. This is **encapsulation**, a core principle in object-oriented programming.

**Constructor**: The method that runs when you create a new `Plat` object: `new Plat("123", "Pizza", "url", "Delicious")`.

**Getters/Setters**: Public methods to read and modify the private fields. This gives you control—you could add validation in a setter, like checking if a URL is valid before setting it.

**Why this structure?** When professors ask "Why not just make the fields public?", here's your answer: "Encapsulation allows me to change internal implementation without breaking code that uses this class. If I later decide IDs must be integers instead of Strings, I only change this class—not every place that uses it."

### Step 3.4: Designing the Main Layout

Open `app/src/main/res/layout/activity_main.xml`. This is the visual blueprint for your home screen.

Replace everything with:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <SearchView
        android:id="@+id/search_view"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:queryHint="Search meals..."
        android:iconifiedByDefault="false"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toStartOf="@+id/ai_button"
        app:layout_constraintHorizontal_weight="1" />

    <Button
        android:id="@+id/ai_button"
        android:layout_width="wrap_content"
        android:layout_height="0dp"
        android:text="AI"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintEnd_toStartOf="@+id/favorites_button"
        app:layout_constraintStart_toEndOf="@+id/search_view"
        app:layout_constraintBottom_toBottomOf="@+id/search_view" />

    <Button
        android:id="@+id/favorites_button"
        android:layout_width="wrap_content"
        android:layout_height="0dp"
        android:text="⭐"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@+id/ai_button"
        app:layout_constraintBottom_toBottomOf="@+id/search_view" />

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycler_view"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginTop="8dp"
        app:layout_constraintTop_toBottomOf="@+id/search_view"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <ProgressBar
        android:id="@+id/progress_bar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:visibility="gone"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Understanding this XML:**

**ConstraintLayout**: Think of it as a magnetic board where you pin elements relative to each other. "Put this button to the right of that search bar." This is more flexible than older layouts like LinearLayout.

**SearchView**: A pre-built search box. Users type here to filter meals.

**RecyclerView**: This will display your meal grid. It's called "RecyclerView" because it recycles views—when you scroll, it reuses UI elements instead of creating new ones. This makes scrolling smooth even with thousands of items.

**ProgressBar**: A loading spinner. We set `visibility="gone"` initially (completely hidden). When loading data, we'll make it visible.

**android:id="@+id/..."**: These are IDs you'll use in Java to reference these UI elements. `@+id` creates a new ID; `@id` references an existing one.

**Constraints**: Lines like `app:layout_constraintTop_toBottomOf="@+id/search_view"` mean "Position my top edge below the search view's bottom edge." ConstraintLayout requires every view to be constrained in both horizontal and vertical directions.

### Step 3.5: Creating the Meal Card Layout

Each meal needs its own card design. Create a new layout file:

Right-click `res/layout` → New → Layout Resource File → Name it `item_plat.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="8dp">

        <ImageView
            android:id="@+id/plat_image"
            android:layout_width="match_parent"
            android:layout_height="200dp"
            android:scaleType="centerCrop"
            android:contentDescription="Meal image" />

        <TextView
            android:id="@+id/plat_name"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="Meal Name"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="@android:color/black" />

        <RatingBar
            android:id="@+id/plat_rating"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:numStars="5"
            android:stepSize="0.5"
            android:rating="0"
            android:isIndicator="true"
            style="?android:attr/ratingBarStyleSmall" />

    </LinearLayout>
</androidx.cardview.widget.CardView>
```

**Understanding this layout:**

**CardView**: Creates a card with rounded corners and shadow (elevation). This is Material Design—Google's design language for Android.

**LinearLayout**: Arranges children vertically (orientation="vertical"). Simple but effective for stacking image, text, and rating.

**ImageView scaleType="centerCrop"**: When the image aspect ratio doesn't match the ImageView dimensions, this crops the image to fill the space without distorting it.

**RatingBar**: Shows stars. `isIndicator="true"` means users can't interact with it on this screen (it's read-only). In the favorites screen, we'll make it editable.

### Step 3.6: Creating the Adapter

Now we connect our data (meals) to our RecyclerView. This requires understanding a crucial Android pattern.

Right-click `com.example.ensa_meal` → New → Java Class → Name it `AdapterMeals`

**What is an Adapter?** Think of it as a translator. You have data (a list of meals) and a view (RecyclerView). The adapter takes each meal from your list and creates a visual card for it. When you scroll, the adapter intelligently recycles old cards to show new data—this is what makes scrolling smooth.

**The Pattern**: RecyclerView.Adapter uses three key methods:
1. **onCreateViewHolder**: "Create a blank card template"
2. **onBindViewHolder**: "Fill this card with meal #5's data"
3. **getItemCount**: "How many cards total?"

Write this in `app/src/main/java/com/example/ensa_meal/AdapterMeals.java`:

```java
package com.example.ensa_meal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Set;

public class AdapterMeals extends RecyclerView.Adapter<AdapterMeals.Holder> {
    private final ArrayList<Plat> plats;
    private final Context context;
    private final OnItemClickListener clickListener;
    private Set<String> favoriteMealIds;

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(int position);
        void onToggleFavoriteClick(int position);
    }

    public AdapterMeals(ArrayList<Plat> plats, Context context, OnItemClickListener clickListener, Set<String> favoriteMealIds) {
        this.plats = plats;
        this.context = context;
        this.clickListener = clickListener;
        this.favoriteMealIds = favoriteMealIds;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.model_plat, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Plat p = plats.get(position);
        holder.tId.setText(p.getId());
        holder.tName.setText(p.getName());

        // Load image with Glide
        Glide.with(context).load(p.getImageURL()).into(holder.image);

        // Show favorite star if this meal is favorited
        if (favoriteMealIds.contains(p.getId())) {
            holder.favoriteIcon.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.favoriteIcon.setImageResource(android.R.drawable.btn_star_big_off);
        }

        // Click on card to view details
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(holder.getAdapterPosition());
            }
        });

        // Click star to toggle favorite
        holder.favoriteIcon.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onToggleFavoriteClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return plats.size();
    }

    public void setFavoriteMealIds(Set<String> favoriteMealIds) {
        this.favoriteMealIds = favoriteMealIds;
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView tId, tName;
        ImageView favoriteIcon;

        public Holder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageView);
            tId = itemView.findViewById(R.id.modelId);
            tName = itemView.findViewById(R.id.modelName);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon);
        }
    }
}
```

**Breaking down this code:**

**LayoutInflater**: This takes your XML layout and converts it into actual View objects in memory. Think of it as a 3D printer that reads blueprints and creates physical objects.

**ViewHolder Pattern**: The `Holder` class caches view references. Without this, every time you scroll, Android would call `findViewById` repeatedly—slow. With ViewHolder, you find views once and reuse them. This is a massive performance optimization.

**Glide.with(context).load(url).into(imageView)**: This single line does:
1. Downloads the image from the URL
2. Caches it on disk so it doesn't re-download
3. Loads it into memory efficiently
4. Displays it in the ImageView
5. Handles threading automatically

Without Glide, this would be 50+ lines of complex code.

**OnItemClickListener Interface**: This is the **Observer pattern**. The adapter doesn't know what to do when a meal is clicked—MainActivity decides that. The interface creates a contract: "Whoever uses this adapter must implement these methods." This is called **dependency inversion**—high-level modules (MainActivity) aren't dependent on low-level modules (Adapter).

**When professors ask about patterns**: "I used the ViewHolder pattern for performance optimization and the Observer pattern for loose coupling between adapter and activity."

### Step 3.7: Implementing MainActivity

Now we bring everything together. Open `app/src/main/java/com/example/ensa_meal/MainActivity.java` and replace everything with:

```java
package com.example.ensa_meal;

import android.content.Intent;
import android.os.Bundle;
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

    private static final String API_URL = "https://www.themealdb.com/api/json/v1/1/search.php?s=";

    private AdapterMeals adapterMeals;
    private RecyclerView recyclerView;
    private ArrayList<Plat> arrayList;
    private ProgressBar progressBar;
    private RequestQueue requestQueue;
    private FavoriteDao favoriteDao;
    private SearchView searchView;
    private Button favoritesButton, aiButton;
    private Set<String> favoriteMealIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        AppDatabase database = AppDatabase.getInstance(this);
        favoriteDao = database.favoriteDao();

        initializeViews();
        setupRecyclerView();
        setupListeners();
        loadFavoriteMealIds();

        // Initial search
        searchMeals("chicken");
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.search_view);
        favoritesButton = findViewById(R.id.favorites_button);
        aiButton = findViewById(R.id.ai_button);
    }

    private void setupRecyclerView() {
        arrayList = new ArrayList<>();
        favoriteMealIds = new HashSet<>();
        adapterMeals = new AdapterMeals(arrayList, this, this, favoriteMealIds);
        recyclerView.setAdapter(adapterMeals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true); // Performance optimization
    }

    private void setupListeners() {
        favoritesButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
        });

        aiButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AIChatActivity.class));
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

    private void loadFavoriteMealIds() {
        favoriteMealIds.clear();
        favoriteMealIds.addAll(favoriteDao.getFavoriteMealIds());
        if (adapterMeals != null) {
            adapterMeals.setFavoriteMealIds(favoriteMealIds);
        }
    }

    private void searchMeals(String query) {
        if (query == null || query.isEmpty()) {
            return;
        }

        showLoading(true);

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }

        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET,
            API_URL + query,
            null,
            response -> {
                showLoading(false);
                handleApiResponse(response);
            },
            error -> {
                showLoading(false);
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
            }
        );

        requestQueue.add(request);
    }

    private void handleApiResponse(JSONObject response) {
        try {
            arrayList.clear();
            if (response.has("meals") && !response.isNull("meals")) {
                JSONArray mealsArray = response.getJSONArray("meals");

                for (int i = 0; i < mealsArray.length(); i++) {
                    JSONObject meal = mealsArray.getJSONObject(i);
                    String id = meal.optString("idMeal", "0");
                    String name = meal.optString("strMeal", "Unknown");
                    String imageUrl = meal.optString("strMealThumb", "");
                    String description = meal.optString("strInstructions", "");

                    Plat plat = new Plat(id, name, imageUrl, description);
                    arrayList.add(plat);
                }
            } else {
                Toast.makeText(this, "No meals found", Toast.LENGTH_SHORT).show();
            }
            adapterMeals.notifyDataSetChanged();
        } catch (JSONException e) {
            Toast.makeText(this, "Error parsing data", Toast.LENGTH_LONG).show();
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
            favoriteDao.removeFromFavoritesById(mealId);
            favoriteMealIds.remove(mealId);
            Toast.makeText(this, plat.getName() + " removed", Toast.LENGTH_SHORT).show();
        } else {
            FavoriteEntity favorite = new FavoriteEntity(
                mealId, plat.getName(), plat.getImageURL(),
                plat.getInstructions(), "", 0
            );
            favoriteDao.addToFavorites(favorite);
            favoriteMealIds.add(mealId);
            Toast.makeText(this, plat.getName() + " added", Toast.LENGTH_SHORT).show();
        }
        adapterMeals.setFavoriteMealIds(favoriteMealIds);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoriteMealIds(); // Refresh when returning to this screen
    }
}
```

**Understanding this Activity:**

**onCreate() lifecycle**: This method runs when the activity is created. It's where you initialize everything. Android has a lifecycle: `onCreate → onStart → onResume → onPause → onStop → onDestroy`. Understanding this is crucial.

**findViewById**: Connects Java code to XML views. `R.id.recyclerView` references the RecyclerView you defined in `activity_main.xml`. `R` is auto-generated by Android—it's a resource index.

**LinearLayoutManager**: Tells RecyclerView to arrange items vertically, like a list. You could use `GridLayoutManager` for a grid, or `StaggeredGridLayoutManager` for Pinterest-style layouts.

**Volley's Request Queue**: Volley manages a queue of network requests. Multiple requests can execute concurrently. The queue handles threading, retries, and caching automatically.

**Lambda expressions**: `v -> startActivity(...)` is shorthand for:
```java
new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        startActivity(...);
    }
}
```
This is Java 8 syntax that makes code cleaner.

**JSON Parsing**: `response.getJSONArray("meals")` navigates the JSON structure. If the API returns `{"meals": [...]}`, this gets that array. `optString` is safer than `getString`—it returns a default value if the key doesn't exist, preventing crashes.

**Activity Stack**: When you call `startActivity(intent)`, Android pushes the new activity onto a stack. Pressing back pops it off, returning you to the previous activity.

**When professors ask about threading**: "I used Volley for networking because it handles background threading automatically. Network operations run off the main thread, so the UI stays responsive. Volley's callbacks (onResponse, onErrorResponse) return results on the main thread, where I can safely update the UI."

---

## Part 4: Building the Detail Screen

When users tap a meal card, they should see full details. This requires a new activity.

### Step 4.1: Create the Detail Activity Class

Right-click `com.example.ensa_meal` → New → Java Class → Name it `Instructions`

Write this in `app/src/main/java/com/example/ensa_meal/Instructions.java`:

```java
package com.example.ensa_meal;

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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ensa_meal.database.AppDatabase;
import com.example.ensa_meal.database.FavoriteDao;
import com.example.ensa_meal.database.FavoriteEntity;

public class Instructions extends AppCompatActivity {
    private ImageView imageView;
    private TextView IDmeal, Name, Inst;
    private Plat currentPlat;
    private FavoriteDao favoriteDao;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        // Initialize database
        AppDatabase database = AppDatabase.getInstance(this);
        favoriteDao = database.favoriteDao();

        // Initialize views
        imageView = findViewById(R.id.imageInst);
        IDmeal = findViewById(R.id.IdInst);
        Name = findViewById(R.id.NameInstr);
        Inst = findViewById(R.id.Instr_Inst);

        // Retrieve meal from intent
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            Plat plat;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                plat = bundle.getSerializable("MEAL", Plat.class);
            } else {
                plat = (Plat) bundle.getSerializable("MEAL");
            }

            if (plat != null) {
                currentPlat = plat;
                isFavorite = favoriteDao.isFavorite(plat.getId());

                IDmeal.setText(plat.getId());
                Name.setText(plat.getName());
                Inst.setText(plat.getInstructions());

                Glide.with(this)
                    .load(plat.getImageURL())
                    .into(imageView);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.instructions_menu, menu);
        MenuItem favoriteItem = menu.findItem(R.id.action_add_favorite);

        if (isFavorite) {
            favoriteItem.setIcon(android.R.drawable.star_on);
            favoriteItem.setTitle("Remove from Favorites");
        } else {
            favoriteItem.setIcon(android.R.drawable.star_off);
            favoriteItem.setTitle("Add to Favorites");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_favorite) {
            if (isFavorite) {
                removeFromFavorites();
            } else {
                showAddToFavoritesDialog();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddToFavoritesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add to Favorites");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_favorite, null);
        EditText editComment = dialogView.findViewById(R.id.editComment);
        RatingBar editRating = dialogView.findViewById(R.id.editRating);

        builder.setView(dialogView);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String comment = editComment.getText().toString().trim();
            float rating = editRating.getRating();

            FavoriteEntity favorite = new FavoriteEntity(
                currentPlat.getId(), currentPlat.getName(),
                currentPlat.getImageURL(), currentPlat.getInstructions(),
                comment.isEmpty() ? null : comment, rating
            );

            favoriteDao.addToFavorites(favorite);
            isFavorite = true;
            invalidateOptionsMenu();

            Toast.makeText(this, "Added to Favorites!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void removeFromFavorites() {
        new AlertDialog.Builder(this)
            .setTitle("Remove from Favorites")
            .setMessage("Remove this meal from your favorites?")
            .setPositiveButton("Yes", (dialog, which) -> {
                favoriteDao.removeFromFavoritesById(currentPlat.getId());
                isFavorite = false;
                invalidateOptionsMenu();
                Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
```

**Understanding this code:**

**Serializable**: The `Plat` class needs to implement `Serializable` to be passed between activities. Add `implements Serializable` to your Plat class definition. Serializable converts objects to byte streams for transmission.

**Build.VERSION.SDK_INT**: Android API changes over time. Android 13+ requires type-safe `getSerializable()`. This code handles both old and new Android versions—critical for compatibility.

**Options Menu**: The action bar at the top can have menu items. `onCreateOptionsMenu` inflates a menu from XML. `onOptionsItemSelected` handles clicks.

**AlertDialog**: A popup dialog. `AlertDialog.Builder` uses the **Builder pattern**—you chain method calls to configure the dialog, then call `show()`. This makes code readable.

**invalidateOptionsMenu()**: Tells Android to redraw the options menu. When you add/remove a favorite, the star icon needs to update—this method triggers that.

### Step 4.2: Create the Detail Layout

Create `app/src/main/res/layout/activity_instructions.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp">

        <ImageView
            android:id="@+id/imageInst"
            android:layout_width="0dp"
            android:layout_height="250dp"
            android:scaleType="centerCrop"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent" />

        <TextView
            android:id="@+id/IdInst"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:textSize="12sp"
            android:textColor="#888"
            app:layout_constraintTop_toBottomOf="@id/imageInst"
            app:layout_constraintStart_toStartOf="parent" />

        <TextView
            android:id="@+id/NameInstr"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:textSize="24sp"
            android:textStyle="bold"
            android:textColor="@android:color/black"
            app:layout_constraintTop_toBottomOf="@id/IdInst"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent" />

        <TextView
            android:id="@+id/Instr_Inst"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:textSize="16sp"
            android:lineSpacingExtra="4dp"
            app:layout_constraintTop_toBottomOf="@id/NameInstr"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent" />

    </androidx.constraintlayout.widget.ConstraintLayout>
</ScrollView>
```

**Why ScrollView?** Instructions can be long. ScrollView makes the entire layout scrollable—users can swipe up to see more text.

**fillViewport="true"**: Makes the ScrollView's child fill the available height even if content is short. This prevents layout issues on large screens.

### Step 4.3: Create the Dialog Layout

Create `app/src/main/res/layout/dialog_edit_favorite.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="20dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Rate this meal:"
        android:textSize="14sp"
        android:layout_marginBottom="8dp" />

    <RatingBar
        android:id="@+id/editRating"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:numStars="5"
        android:stepSize="0.5"
        android:layout_gravity="center_horizontal"
        android:layout_marginBottom="16dp" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Add your comment or note:"
        android:textSize="14sp"
        android:layout_marginBottom="8dp" />

    <EditText
        android:id="@+id/editComment"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="e.g., My favorite meal!"
        android:inputType="textMultiLine"
        android:lines="3"
        android:gravity="top|start"
        android:padding="12dp" />

</LinearLayout>
```

**inputType="textMultiLine"**: Allows users to type multiple lines. `textCapSentences` would auto-capitalize first letters.

**gravity="top|start"**: Aligns text to top-left. Without this, text starts centered, which looks odd for multi-line input.

### Step 4.4: Register the Activity

Open `app/src/main/AndroidManifest.xml` and add this inside the `<application>` tag:

```xml
<activity
    android:name=".Instructions"
    android:exported="false" />
```

**Why register activities?** Android needs to know all activities at install time. The manifest is the contract between your app and the Android system.

**exported="false"**: Means other apps can't launch this activity. Only your app can. This is a security feature—prevents malicious apps from directly opening your screens.

---

## Part 5: Building the Database Layer

Now we implement local storage. When users save favorites, they persist even after closing the app. This requires Room database.

### Step 5.1: Create the Entity

Right-click `com.example.ensa_meal` → New → Package → Name it `database`

Inside `database` package, create `FavoriteEntity.java`:

```java
package com.example.ensa_meal.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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
    private String userComment;

    @ColumnInfo(name = "user_rating")
    private float userRating;

    @ColumnInfo(name = "added_timestamp")
    private long addedTimestamp;

    public FavoriteEntity(@NonNull String mealId, String mealName,
                          String mealImageUrl, String mealDescription,
                          String userComment, float userRating) {
        this.mealId = mealId;
        this.mealName = mealName;
        this.mealImageUrl = mealImageUrl;
        this.mealDescription = mealDescription;
        this.userComment = userComment;
        this.userRating = userRating;
        this.addedTimestamp = System.currentTimeMillis();
    }

    // Getters and setters
    @NonNull
    public String getMealId() { return mealId; }
    public void setMealId(@NonNull String mealId) { this.mealId = mealId; }

    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }

    public String getMealImageUrl() { return mealImageUrl; }
    public void setMealImageUrl(String mealImageUrl) { this.mealImageUrl = mealImageUrl; }

    public String getMealDescription() { return mealDescription; }
    public void setMealDescription(String mealDescription) { this.mealDescription = mealDescription; }

    public String getUserComment() { return userComment; }
    public void setUserComment(String userComment) { this.userComment = userComment; }

    public float getUserRating() { return userRating; }
    public void setUserRating(float userRating) { this.userRating = userRating; }

    public long getAddedTimestamp() { return addedTimestamp; }
    public void setAddedTimestamp(long addedTimestamp) { this.addedTimestamp = addedTimestamp; }
}
```

**Understanding Room Annotations:**

**@Entity(tableName = "favorites")**: Tells Room this class represents a database table. Room generates SQL like `CREATE TABLE favorites (...)`.

**@PrimaryKey**: The unique identifier. No two favorites can have the same mealId. This is enforced at the database level.

**@ColumnInfo(name = "...")**: Maps Java field to database column. Without this, Room uses the field name directly. Explicit naming is clearer.

**@NonNull**: Tells Room this field can't be null. Room enforces this—trying to insert a null mealId throws an exception.

**System.currentTimeMillis()**: Returns current time in milliseconds since January 1, 1970 (Unix epoch). This timestamp lets you sort favorites by when they were added.

**Why separate Entity from Plat?** Different concerns. `Plat` is network data. `FavoriteEntity` is database data with extra fields like comments and timestamps. This is **separation of concerns**.

### Step 5.2: Create the DAO

Inside `database` package, create `FavoriteDao.java`:

```java
package com.example.ensa_meal.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addToFavorites(FavoriteEntity favorite);

    @Query("SELECT * FROM favorites ORDER BY added_timestamp DESC")
    List<FavoriteEntity> getAllFavorites();

    @Query("SELECT meal_id FROM favorites")
    List<String> getFavoriteMealIds();

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE meal_id = :mealId)")
    boolean isFavorite(String mealId);

    @Query("UPDATE favorites SET user_comment = :comment WHERE meal_id = :mealId")
    void updateComment(String mealId, String comment);

    @Query("UPDATE favorites SET user_rating = :rating WHERE meal_id = :mealId")
    void updateRating(String mealId, float rating);

    @Update
    void updateFavorite(FavoriteEntity favorite);

    @Delete
    void removeFromFavorites(FavoriteEntity favorite);

    @Query("DELETE FROM favorites WHERE meal_id = :mealId")
    void removeFromFavoritesById(String mealId);

    @Query("DELETE FROM favorites")
    void clearAllFavorites();
}
```

**What is a DAO?** Data Access Object—a pattern that abstracts database operations. You define an interface, and Room generates the implementation at compile time. This is **code generation**—you write clean interfaces, Room writes the messy SQL.

**@Insert(onConflict = REPLACE)**: If you try to insert a favorite that already exists (same primary key), it replaces the old one instead of crashing.

**@Query**: You write SQL, Room verifies it at compile time. If your SQL is invalid, your code won't compile—this catches errors early.

**ORDER BY added_timestamp DESC**: Sorts favorites newest-first. `DESC` means descending (highest to lowest). This makes recently added favorites appear at the top.

**EXISTS(SELECT 1 ...)**: SQL idiom for "does this exist?". Returns true/false. More efficient than counting rows.

**When professors ask about database**: "I used Room, Google's ORM for SQLite. Room provides compile-time verification of SQL queries, type-safe DAO methods, and automatic object mapping. This reduces boilerplate and prevents runtime SQL errors."

### Step 5.3: Create the Database Class

Inside `database` package, create `AppDatabase.java`:

```java
package com.example.ensa_meal.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {FavoriteEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;
    private static final String DATABASE_NAME = "ensa_meal_database";

    public abstract FavoriteDao favoriteDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                DATABASE_NAME
            )
            .allowMainThreadQueries() // For simplicity - use background threads in production
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
}
```

**Understanding this pattern:**

**Singleton Pattern**: Only one database instance exists. `synchronized` ensures thread-safety—if two threads try to create the database simultaneously, one waits. Without this, you could create multiple database connections—wasteful and dangerous.

**Room.databaseBuilder()**: Creates the database. Takes context (to access storage), class reference (to know what to build), and name (the file name on disk).

**allowMainThreadQueries()**: Dangerous but convenient for learning. Production apps should use background threads for database operations. Room can block the main thread, freezing your UI.

**fallbackToDestructiveMigration()**: If the database schema changes (e.g., you add a column), Room can't migrate automatically. This tells Room to delete the old database and create a new one. Users lose data, but it's fine for development.

**context.getApplicationContext()**: Uses application context, not activity context. Activity contexts can leak memory if held after the activity is destroyed. Application context lives as long as your app—safe for singletons.

**When professors ask about design patterns**: "I implemented the Singleton pattern for the database to ensure only one instance exists, preventing race conditions and resource waste. I also used the DAO pattern to separate business logic from data access logic."

---

## Part 6: Building Screen 2 - Favorites

The favorites screen shows saved meals with comments and ratings. Users can edit or delete favorites.

### Step 6.1: Create the Favorites Adapter

Create `FavoritesAdapter.java`:

```java
package com.example.ensa_meal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ensa_meal.database.FavoriteEntity;

import java.util.ArrayList;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private final ArrayList<FavoriteEntity> favorites;
    private final Context context;
    private final OnFavoriteActionListener listener;

    public interface OnFavoriteActionListener {
        void onEditFavorite(int position);
        void onViewDetails(int position);
        void onDeleteFavorite(int position);
    }

    public FavoritesAdapter(ArrayList<FavoriteEntity> favorites, Context context,
                            OnFavoriteActionListener listener) {
        this.favorites = favorites;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteEntity favorite = favorites.get(position);

        holder.mealName.setText(favorite.getMealName());

        if (favorite.getUserComment() != null && !favorite.getUserComment().isEmpty()) {
            holder.comment.setText(favorite.getUserComment());
            holder.comment.setVisibility(View.VISIBLE);
        } else {
            holder.comment.setText("No comment yet. Tap to add.");
            holder.comment.setAlpha(0.5f);
        }

        holder.ratingBar.setRating(favorite.getUserRating());

        // Calculate time ago
        long diff = System.currentTimeMillis() - favorite.getAddedTimestamp();
        long hours = diff / (1000 * 60 * 60);
        long days = hours / 24;

        String timeAgo;
        if (days > 0) {
            timeAgo = days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            timeAgo = hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else {
            timeAgo = "Just now";
        }
        holder.timestamp.setText(timeAgo);

        Glide.with(context)
            .load(favorite.getMealImageUrl())
            .into(holder.mealImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetails(holder.getAdapterPosition());
        });

        holder.btnEditFavorite.setOnClickListener(v -> {
            if (listener != null) listener.onEditFavorite(holder.getAdapterPosition());
        });

        holder.btnDeleteFavorite.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteFavorite(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView mealImage;
        TextView mealName, comment, timestamp;
        ImageButton btnEditFavorite, btnDeleteFavorite;
        RatingBar ratingBar;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            mealImage = itemView.findViewById(R.id.favoriteMealImage);
            mealName = itemView.findViewById(R.id.favoriteMealName);
            comment = itemView.findViewById(R.id.favoriteComment);
            timestamp = itemView.findViewById(R.id.favoriteTimestamp);
            btnEditFavorite = itemView.findViewById(R.id.btnEditFavorite);
            btnDeleteFavorite = itemView.findViewById(R.id.btnDeleteFavorite);
            ratingBar = itemView.findViewById(R.id.favoriteRating);
        }
    }
}
```

**Time calculation logic**: Converts milliseconds to days/hours. `diff / (1000 * 60 * 60)` converts milliseconds → seconds → minutes → hours. This gives users context: "added 2 days ago."

**setAlpha(0.5f)**: Makes the placeholder text semi-transparent, visually indicating it's not real data.

### Step 6.2: Create Favorites Activity

Create `FavoritesActivity.java`:

```java
package com.example.ensa_meal;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
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

public class FavoritesActivity extends AppCompatActivity implements FavoritesAdapter.OnFavoriteActionListener {

    private RecyclerView recyclerViewFavorites;
    private FavoritesAdapter favoritesAdapter;
    private ArrayList<FavoriteEntity> favoritesList;
    private TextView emptyView;
    private FavoriteDao favoriteDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Favorites");
        }

        AppDatabase database = AppDatabase.getInstance(this);
        favoriteDao = database.favoriteDao();

        recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites);
        emptyView = findViewById(R.id.emptyFavoritesText);

        setupRecyclerView();
        setupSwipeToDelete();
        loadFavorites();
    }

    private void setupRecyclerView() {
        favoritesList = new ArrayList<>();
        favoritesAdapter = new FavoritesAdapter(favoritesList, this, this);
        recyclerViewFavorites.setAdapter(favoritesAdapter);
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadFavorites() {
        List<FavoriteEntity> entities = favoriteDao.getAllFavorites();
        favoritesList.clear();
        favoritesList.addAll(entities);
        favoritesAdapter.notifyDataSetChanged();

        if (favoritesList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerViewFavorites.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerViewFavorites.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEditFavorite(int position) {
        FavoriteEntity favorite = favoritesList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Favorite");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_favorite, null);
        EditText editComment = dialogView.findViewById(R.id.editComment);
        RatingBar editRating = dialogView.findViewById(R.id.editRating);

        if (favorite.getUserComment() != null && !favorite.getUserComment().isEmpty()) {
            editComment.setText(favorite.getUserComment());
        }
        editRating.setRating(favorite.getUserRating());

        builder.setView(dialogView);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String comment = editComment.getText().toString().trim();
            float rating = editRating.getRating();

            favoriteDao.updateComment(favorite.getMealId(), comment);
            favoriteDao.updateRating(favorite.getMealId(), rating);

            favorite.setUserComment(comment);
            favorite.setUserRating(rating);
            favoritesAdapter.notifyItemChanged(position);

            Toast.makeText(this, "Favorite updated", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onViewDetails(int position) {
        FavoriteEntity favorite = favoritesList.get(position);

        Plat plat = new Plat(favorite.getMealId(), favorite.getMealName(),
                favorite.getMealImageUrl(), favorite.getMealDescription());

        Intent intent = new Intent(this, Instructions.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("MEAL", plat);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onDeleteFavorite(int position) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Favorite")
            .setMessage("Are you sure?")
            .setPositiveButton("Yes", (dialog, which) -> deleteFavorite(position))
            .setNegativeButton("No", null)
            .show();
    }

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

    private void deleteFavorite(int position) {
        FavoriteEntity favorite = favoritesList.get(position);
        favoriteDao.removeFromFavorites(favorite);
        favoritesList.remove(position);
        favoritesAdapter.notifyItemRemoved(position);
        Toast.makeText(this, "Removed: " + favorite.getMealName(), Toast.LENGTH_SHORT).show();

        if (favoritesList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerViewFavorites.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
```

**Understanding swipe-to-delete:**

**ItemTouchHelper**: A utility class that adds swipe and drag-and-drop to RecyclerView. You provide a callback defining what happens on swipe/move.

**SimpleCallback(0, LEFT | RIGHT)**: First parameter is drag directions (0 = no dragging). Second is swipe directions—`LEFT | RIGHT` means users can swipe either way.

**onMove()**: Called when dragging items to reorder. We return false because we don't support reordering.

**onSwiped()**: Called when user completes a swipe. We delete the item here.

**notifyItemRemoved(position)**: Tells the adapter "item at position X was removed." RecyclerView animates the deletion smoothly. If you used `notifyDataSetChanged()`, the entire list would refresh—no animation.

**When professors ask about UX**: "I implemented swipe-to-delete using ItemTouchHelper. This provides intuitive gestural interaction—users can quickly remove items without extra taps. The animated deletion gives visual feedback, improving user experience."

### Step 6.3: Create Layouts

Create `app/src/main/res/layout/activity_favorites.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerViewFavorites"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <TextView
        android:id="@+id/emptyFavoritesText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="No favorites yet.\nAdd meals from the home screen!"
        android:textSize="18sp"
        android:textAlignment="center"
        android:visibility="gone"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

Create `app/src/main/res/layout/item_favorite.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="12dp">

        <ImageView
            android:id="@+id/favoriteMealImage"
            android:layout_width="80dp"
            android:layout_height="80dp"
            android:scaleType="centerCrop"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintStart_toStartOf="parent" />

        <TextView
            android:id="@+id/favoriteMealName"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="12dp"
            android:textSize="18sp"
            android:textStyle="bold"
            android:textColor="@android:color/black"
            app:layout_constraintTop_toTopOf="@id/favoriteMealImage"
            app:layout_constraintStart_toEndOf="@id/favoriteMealImage"
            app:layout_constraintEnd_toStartOf="@id/btnEditFavorite" />

        <TextView
            android:id="@+id/favoriteComment"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="12dp"
            android:layout_marginTop="4dp"
            android:textSize="14sp"
            android:maxLines="2"
            android:ellipsize="end"
            app:layout_constraintTop_toBottomOf="@id/favoriteMealName"
            app:layout_constraintStart_toEndOf="@id/favoriteMealImage"
            app:layout_constraintEnd_toStartOf="@id/btnEditFavorite" />

        <RatingBar
            android:id="@+id/favoriteRating"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="12dp"
            android:layout_marginTop="4dp"
            android:numStars="5"
            android:stepSize="0.5"
            android:isIndicator="true"
            style="?android:attr/ratingBarStyleSmall"
            app:layout_constraintTop_toBottomOf="@id/favoriteComment"
            app:layout_constraintStart_toEndOf="@id/favoriteMealImage" />

        <TextView
            android:id="@+id/favoriteTimestamp"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:textSize="12sp"
            android:textColor="#888"
            app:layout_constraintTop_toTopOf="@id/favoriteRating"
            app:layout_constraintBottom_toBottomOf="@id/favoriteRating"
            app:layout_constraintStart_toEndOf="@id/favoriteRating" />

        <ImageButton
            android:id="@+id/btnEditFavorite"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:layout_marginEnd="4dp"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:src="@android:drawable/ic_menu_edit"
            android:contentDescription="Edit favorite"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintEnd_toStartOf="@id/btnDeleteFavorite" />

        <ImageButton
            android:id="@+id/btnDeleteFavorite"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:src="@android:drawable/ic_menu_delete"
            android:contentDescription="Delete favorite"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintEnd_toEndOf="parent" />

    </androidx.constraintlayout.widget.ConstraintLayout>
</androidx.cardview.widget.CardView>
```

**?attr/selectableItemBackgroundBorderless**: This is a theme attribute. It creates a ripple effect when the button is clicked—visual feedback that feels modern. The `?` means "look this up in the current theme."

**ellipsize="end"**: If text is too long for the space, it adds "..." at the end. Combined with `maxLines="2"`, this prevents comments from taking too much space.

### Step 6.4: Register the Activity

Add to `AndroidManifest.xml`:

```xml
<activity
    android:name=".FavoritesActivity"
    android:exported="false"
    android:parentActivityName=".MainActivity" />
```

**parentActivityName**: Creates an "Up" button in the action bar. When users tap it, they return to MainActivity. Android handles the navigation—you don't write code for this.

---

You've now completed the first six parts! You have a working meal browser, detail screen, database layer, and favorites screen. The remaining parts (AI Chat implementation and presentation guidance) follow similar patterns.

**Key concepts you've learned:**
- Three-tier architecture (UI, business logic, data)
- RecyclerView with Adapter and ViewHolder patterns
- Room database with Entity, DAO, and Singleton patterns
- Volley for networking with automatic threading
- Glide for image loading and caching
- Intent-based navigation between activities
- Material Design principles (CardView, elevation, ripples)
- Swipe gestures with ItemTouchHelper
- Observer pattern with click listeners
- Lifecycle methods (onCreate, onResume, etc.)

When presenting, focus on **why** you made each choice. Professors care less about syntax and more about understanding architectural decisions.

---

## Part 7: Building Screen 3 - The AI Chat

The AI chat is the crown jewel of your app. Users can ask cooking questions in any language, and the AI responds intelligently—drawing from their personal favorites list.

### Step 7.1: Understanding the AI Architecture

Before coding, grasp how this works:

```
User types question → AIChatActivity → OkHttp request → Groq API
                                            ↓
                                    AI processes with:
                                    - COSTAR system prompt
                                    - User's favorites context
                                    - Conversation history
                                            ↓
                                    AI response → Display to user
```

**Why Groq?** Groq provides fast inference on open-source LLMs like Llama 3.3. It's free for development and respects your API usage patterns.

**Why OkHttp instead of Volley?** Groq requires custom headers (Bearer authentication) and POST requests with JSON bodies. OkHttp gives us fine-grained control. Volley is simpler but less flexible.

### Step 7.2: Securing the API Key

**Critical security practice**: Never hardcode API keys in source code. They'd be visible in version control and decompiled APKs.

**Solution**: Use BuildConfig to inject the key at build time from a gitignored file.

Open the root `local.properties` file (it's gitignored by default) and add:

```properties
GROQ_API_KEY=your_groq_api_key_here
```

**Getting your Groq API key:**
1. Visit https://console.groq.com
2. Sign up (free)
3. Navigate to API Keys
4. Create a new key
5. Copy it to `local.properties`

Open `app/build.gradle.kts` and add this inside the `android` block, within `defaultConfig`:

```kotlin
android {
    defaultConfig {
        // Existing config...

        // Load API key from local.properties
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(FileInputStream(localPropertiesFile))
        }

        buildConfigField("String", "GROQ_API_KEY",
            "\"${properties.getProperty("GROQ_API_KEY", "")}\"")
    }

    buildFeatures {
        buildConfig = true
    }
}
```

**How this works**: Gradle reads `local.properties` at build time and generates a `BuildConfig.java` class with a `GROQ_API_KEY` constant. In your code, you reference `BuildConfig.GROQ_API_KEY`—the actual key never appears in source code.

**When professors ask about security**: "I used BuildConfig injection to keep API keys out of source control. The key is read from a gitignored local.properties file at build time. This prevents credential leakage in version control or decompiled APKs—a standard security practice for mobile apps."

### Step 7.3: Create the AI Chat Layout

Create `app/src/main/res/layout/activity_ai_chat.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <TextView
        android:id="@+id/ai_title"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="AI Meal Assistant"
        android:textSize="24sp"
        android:textStyle="bold"
        android:gravity="center"
        android:paddingBottom="16dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <TextView
        android:id="@+id/ai_subtitle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Ask me anything about meals and recipes"
        android:textSize="14sp"
        android:gravity="center"
        android:paddingBottom="16dp"
        android:alpha="0.7"
        app:layout_constraintTop_toBottomOf="@id/ai_title"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <ScrollView
        android:id="@+id/scroll_view"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_marginBottom="8dp"
        app:layout_constraintTop_toBottomOf="@id/ai_subtitle"
        app:layout_constraintBottom_toTopOf="@id/input_container"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <TextView
            android:id="@+id/chat_text_view"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="16sp"
            android:lineSpacingExtra="4dp"
            android:padding="8dp"
            android:text="Welcome! Ask me about any meal or recipe." />
    </ScrollView>

    <LinearLayout
        android:id="@+id/input_container"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <EditText
            android:id="@+id/question_input"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:hint="Ask about a meal..."
            android:inputType="textCapSentences"
            android:maxLines="3"
            android:padding="12dp" />

        <Button
            android:id="@+id/send_button"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:text="Ask" />
    </LinearLayout>

    <ProgressBar
        android:id="@+id/progress_bar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:visibility="gone"
        app:layout_constraintTop_toTopOf="@id/scroll_view"
        app:layout_constraintBottom_toBottomOf="@id/scroll_view"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Layout structure**: A classic chat UI. Title at top, scrollable chat area in middle, input and button at bottom. The ScrollView expands to fill available space (`0dp` height with constraints top and bottom).

### Step 7.4: Implementing AIChatActivity

Create `AIChatActivity.java`:

```java
package com.example.ensa_meal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.example.ensa_meal.database.AppDatabase;
import com.example.ensa_meal.database.FavoriteDao;
import com.example.ensa_meal.database.FavoriteEntity;

public class AIChatActivity extends AppCompatActivity {

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    private EditText questionInput;
    private Button sendButton;
    private TextView chatTextView;
    private ProgressBar progressBar;
    private ScrollView scrollView;
    private OkHttpClient client;
    private StringBuilder chatHistory;
    private List<JSONObject> conversationHistory;
    private FavoriteDao favoriteDao;
    private List<FavoriteEntity> userFavorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupListeners();

        client = new OkHttpClient();
        chatHistory = new StringBuilder();
        conversationHistory = new ArrayList<>();

        AppDatabase database = AppDatabase.getInstance(this);
        favoriteDao = database.favoriteDao();
        loadFavorites();
    }

    private void initializeViews() {
        questionInput = findViewById(R.id.question_input);
        sendButton = findViewById(R.id.send_button);
        chatTextView = findViewById(R.id.chat_text_view);
        progressBar = findViewById(R.id.progress_bar);
        scrollView = findViewById(R.id.scroll_view);
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> {
            String question = questionInput.getText().toString().trim();
            if (!question.isEmpty()) {
                sendQuestionToAI(question);
            } else {
                Toast.makeText(this, "Please enter a question", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendQuestionToAI(String question) {
        if (BuildConfig.GROQ_API_KEY == null || BuildConfig.GROQ_API_KEY.isEmpty()) {
            Toast.makeText(this, "Please add your Groq API key in local.properties", Toast.LENGTH_LONG).show();
            appendToChat("Error: API key not configured. Add GROQ_API_KEY to local.properties\n\n");
            return;
        }

        showLoading(true);
        sendButton.setEnabled(false);

        appendToChat("You: " + question + "\n\n");
        storeUserMessage(question);
        questionInput.setText("");

        try {
            JSONObject requestBody = buildRequestBody(question);

            Request request = new Request.Builder()
                .url(GROQ_API_URL)
                .addHeader("Authorization", "Bearer " + BuildConfig.GROQ_API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        sendButton.setEnabled(true);
                        Toast.makeText(AIChatActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        appendToChat("Error: Could not connect to AI service\n\n");
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();

                    runOnUiThread(() -> {
                        showLoading(false);
                        sendButton.setEnabled(true);

                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonResponse = new JSONObject(responseBody);
                                String answer = extractAnswer(jsonResponse);
                                appendToChat("AI: " + answer + "\n\n");
                                storeAssistantResponse(answer);
                            } catch (JSONException e) {
                                Toast.makeText(AIChatActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                                appendToChat("Error: Invalid response from AI\n\n");
                            }
                        } else {
                            String errorMsg = "API Error: " + response.code();
                            if (response.code() == 401) {
                                errorMsg = "Invalid API key. Check local.properties";
                                appendToChat("Error: Invalid or expired API key.\n\nCheck GROQ_API_KEY in local.properties file\n\n");
                            } else {
                                appendToChat("Error: AI service returned error code " + response.code() + "\n\n");
                            }
                            Toast.makeText(AIChatActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        } catch (JSONException e) {
            showLoading(false);
            sendButton.setEnabled(false);
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONObject buildRequestBody(String question) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "llama-3.3-70b-versatile");

        JSONArray messages = new JSONArray();

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", buildCostarPrompt());
        messages.put(systemMessage);

        for (JSONObject msg : conversationHistory) {
            messages.put(msg);
        }

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.3);
        requestBody.put("top_p", 0.9);
        requestBody.put("max_tokens", 500);

        return requestBody;
    }

    private String buildCostarPrompt() {
        String favoritesContext = buildFavoritesContext();

        return "# CONTEXT\n" +
                "You are a cooking assistant in Ensa Meal app. Users ask cooking questions. You have conversation history.\n\n" +

                "# USER'S FAVORITE MEALS\n" +
                favoritesContext + "\n" +
                "You can suggest what to cook from these favorites when asked.\n\n" +

                "# OBJECTIVE\n" +
                "Answer cooking questions briefly and simply. Remember previous messages for follow-up questions. " +
                "Help users decide what to cook from their favorites.\n\n" +

                "# STYLE\n" +
                "Simple words. Easy to understand. Like texting a friend. No fancy language.\n\n" +

                "# TONE\n" +
                "Friendly and helpful. Quick and direct.\n\n" +

                "# AUDIENCE\n" +
                "Anyone who cooks. Keep it simple.\n\n" +

                "# CRITICAL LANGUAGE RULE\n" +
                "ALWAYS respond in the SAME LANGUAGE the user writes in:\n" +
                "- English question → English answer\n" +
                "- Moroccan Darija question → Moroccan Darija answer\n" +
                "- French question → French answer\n" +
                "- Arabic question → Arabic answer\n" +
                "Match the user's language EXACTLY. This is mandatory.\n\n" +

                "# RESPONSE FORMAT\n" +
                "Keep answers VERY brief:\n\n" +

                "For GREETINGS (Hi, Hello, Salam, etc):\n" +
                "Just say: 'Hi! How can I help you?'\n" +
                "Nothing more.\n\n" +

                "For FAVORITES QUESTIONS (What should I cook? What to make? etc):\n" +
                "Suggest 2-3 meals from their favorites list.\n" +
                "Example: 'Try making [meal 1] or [meal 2].'\n" +
                "Keep it short.\n\n" +

                "For INGREDIENTS questions:\n" +
                "1. ingredient one\n" +
                "2. ingredient two\n" +
                "3. ingredient three\n" +
                "That's it. No extra text.\n\n" +

                "For RECIPE/STEPS questions:\n" +
                "1. brief step\n" +
                "2. brief step\n" +
                "3. brief step\n" +
                "Max 5-6 steps. Short sentences.\n\n" +

                "For TIME/TEMPERATURE questions:\n" +
                "Direct answer in 1 sentence. Example: 'Cook for 20 minutes at 180°C.'\n\n" +

                "For TIPS/SUBSTITUTIONS:\n" +
                "1-2 sentences max. Direct and simple.\n\n" +

                "For FOLLOW-UP questions:\n" +
                "Reference previous topic briefly, then answer in 1-2 sentences.\n\n" +

                "RULES:\n" +
                "- When user asks what to cook, suggest from their favorites\n" +
                "- No long explanations\n" +
                "- No professional cooking terms unless necessary\n" +
                "- No 'hope this helps' or extra fluff\n" +
                "- Get straight to the point\n" +
                "- Use numbers for lists\n" +
                "- Maximum 4-5 sentences for any answer\n" +
                "- Respond in user's language (English, Darija, French, Arabic, etc)";
    }

    private String extractAnswer(JSONObject response) throws JSONException {
        JSONArray choices = response.getJSONArray("choices");
        if (choices.length() > 0) {
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            return message.getString("content");
        }
        return "No response from AI";
    }

    private void appendToChat(String text) {
        chatHistory.append(text);
        chatTextView.setText(chatHistory.toString());
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void storeUserMessage(String message) {
        try {
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", message);
            conversationHistory.add(userMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void storeAssistantResponse(String response) {
        try {
            JSONObject assistantMsg = new JSONObject();
            assistantMsg.put("role", "assistant");
            assistantMsg.put("content", response);
            conversationHistory.add(assistantMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadFavorites() {
        userFavorites = favoriteDao.getAllFavorites();
    }

    private String buildFavoritesContext() {
        if (userFavorites == null || userFavorites.isEmpty()) {
            return "User has no favorite meals saved yet.";
        }

        StringBuilder context = new StringBuilder("User's favorite meals:\n");
        for (int i = 0; i < userFavorites.size(); i++) {
            FavoriteEntity fav = userFavorites.get(i);
            context.append((i + 1)).append(". ").append(fav.getMealName());
            if (fav.getUserComment() != null && !fav.getUserComment().isEmpty()) {
                context.append(" (Note: ").append(fav.getUserComment()).append(")");
            }
            context.append("\n");
        }
        return context.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
```

**Understanding this implementation:**

### The COSTAR Framework

**What is COSTAR?** A prompt engineering framework that structures AI instructions. Each letter represents a section:

- **C**ontext: "You are a cooking assistant"
- **O**bjective: "Answer cooking questions briefly"
- **S**tyle: "Simple words, like texting a friend"
- **T**one: "Friendly and helpful"
- **A**udience: "Anyone who cooks"
- **R**esponse: "Format rules for different question types"

**Why use a framework?** LLMs perform better with structured prompts. Random instructions produce inconsistent results. COSTAR creates a "personality" for the AI—consistent, predictable, aligned with your app's goals.

### Conversation History

Notice `conversationHistory` stores all messages. When you send a new question, the entire conversation is sent to the AI. This gives **context**:

```
You: "What should I cook?"
AI: "Try making Chicken Teriyaki."
You: "How long does it take?"  ← AI knows "it" refers to Teriyaki
AI: "About 25 minutes."
```

Without history, the AI wouldn't know what "it" means.

### Favorites Integration

`buildFavoritesContext()` loads user favorites and injects them into the system prompt. When someone asks "What should I cook?", the AI suggests from **their** favorites, not random meals. This is personalization.

```
User's favorite meals:
1. Teriyaki Chicken (Note: Easy weeknight dinner)
2. Spaghetti Carbonara
3. Beef Tacos (Note: Kids love this)
```

The AI sees this and suggests: "Try making Teriyaki Chicken or Beef Tacos."

### Multilingual Support

The prompt contains:
```
CRITICAL LANGUAGE RULE
ALWAYS respond in the SAME LANGUAGE the user writes in
```

If the user types in Moroccan Darija: "شنو نطيب؟" (What should I cook?), the AI responds in Darija. This works because modern LLMs are multilingual—they detect input language and mirror it.

### API Parameters

```java
requestBody.put("temperature", 0.3);
requestBody.put("top_p", 0.9);
requestBody.put("max_tokens", 500);
```

**temperature**: Controls randomness. 0.3 is low—responses are focused and consistent. High values (0.8+) make responses creative but unpredictable.

**top_p**: Nucleus sampling. 0.9 means "consider the top 90% probable tokens." This balances quality and diversity.

**max_tokens**: Maximum response length. 500 tokens ≈ 375 words. Limits cost and keeps answers concise.

**When professors ask about AI integration**: "I integrated the Groq API using OkHttp for HTTP requests. I implemented the COSTAR prompting framework to structure AI behavior—defining context, objectives, style, tone, and response format. I pass conversation history to maintain context across messages, and I dynamically inject the user's favorites into the system prompt for personalization. The AI supports multiple languages through prompt engineering—I instruct it to mirror the user's language exactly."

### Step 7.5: Register the Activity

Add to `AndroidManifest.xml`:

```xml
<activity
    android:name=".AIChatActivity"
    android:exported="false"
    android:parentActivityName=".MainActivity" />
```

### Step 7.6: Make Plat Serializable

The `Plat` class must implement `Serializable`. Open `Plat.java` and ensure it looks like this:

```java
package com.example.ensa_meal;

import java.io.Serializable;

public class Plat implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String imageURL;
    private String instructions;

    public Plat(String id, String name, String imageURL, String instructions) {
        this.id = id;
        this.name = name;
        this.imageURL = imageURL;
        this.instructions = instructions;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getImageURL() { return imageURL; }
    public String getInstructions() { return instructions; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}
```

**serialVersionUID**: A version control mechanism for Serializable classes. If you change the class structure, old serialized objects might not deserialize correctly. This ID helps Java detect incompatibilities.

---

## Part 8: Testing and Running Your App

You've written the code. Now let's make it run.

### Step 8.1: Building the Project

Before running, ensure your code compiles.

**In Android Studio:**
1. Click **Build** → **Make Project** (or press Ctrl+F9 / Cmd+F9)
2. Watch the **Build** tab at the bottom
3. If errors appear, read them carefully—Android Studio highlights the file and line

**Common build errors:**

**Missing imports**: If you see "Cannot resolve symbol," you forgot an import. Press Alt+Enter on the red text, select "Import class."

**Syntax errors**: Missing semicolons, unmatched braces. The error message shows the line number.

**Gradle sync failed**: Click **File** → **Sync Project with Gradle Files**. This downloads dependencies and regenerates build files.

### Step 8.2: Running on Emulator

**Setting up an emulator:**

1. Click **Tools** → **Device Manager**
2. Click **Create Device**
3. Choose a phone (Pixel 5 is good—modern, common screen size)
4. Select a system image:
   - **API 33** (Android 13) is recommended
   - Click **Download** if not already downloaded
5. Click **Finish**

**Running your app:**

1. Click the green **Run** button (▶) at the top
2. Select your emulator from the list
3. Click **OK**

The emulator will boot (takes 30-60 seconds first time), then your app installs and launches.

**Emulator tips:**

- **Take a snapshot**: After booting, save state so next launch is instant
- **Use hardware acceleration**: Settings → Performance → Graphics: Hardware - GLES 2.0
- **Rotate**: Ctrl+F11 / Cmd+Left (test landscape mode)

### Step 8.3: Running on Physical Device

Testing on real hardware reveals issues emulators miss (performance, touch responsiveness, network conditions).

**Enable Developer Options on your phone:**

1. **Settings** → **About Phone**
2. Tap **Build Number** 7 times rapidly
3. You'll see "You are now a developer!"

**Enable USB Debugging:**

1. **Settings** → **Developer Options**
2. Toggle **USB Debugging** on
3. Connect phone to computer via USB
4. Phone shows "Allow USB debugging?" → Tap **Allow**

**Install your app:**

1. In Android Studio, your phone appears in the device dropdown
2. Click **Run** (▶)
3. App installs and launches on your phone

**If phone doesn't appear:**
- Try a different USB cable (some are charge-only)
- Ensure USB mode is "File Transfer" not "Charge Only"
- Install your phone manufacturer's USB drivers (Google, Samsung, etc.)

### Step 8.4: Testing Scenarios

Systematically test all features:

**Test 1: Browse Meals**
- Launch app
- See meal cards load from API
- Verify images display correctly
- Try search: "chicken" → see chicken meals

**Test 2: View Meal Details**
- Tap a meal card
- Detail screen opens with full description
- Verify image, name, instructions display

**Test 3: Add to Favorites**
- In detail screen, tap star icon in menu
- Add rating and comment
- Verify "Added to Favorites" toast

**Test 4: View Favorites List**
- Tap ⭐ button on home screen
- See your favorited meal
- Verify rating and comment appear
- Check timestamp shows "Just now"

**Test 5: Edit Favorite**
- In favorites list, tap edit button
- Change comment and rating
- Save → verify changes persist

**Test 6: Delete Favorite (Swipe)**
- In favorites list, swipe meal card left
- Meal disappears with animation

**Test 7: Delete Favorite (Button)**
- Tap delete button
- Confirm in dialog
- Meal removed

**Test 8: AI Chat - Basic Question**
- Tap "AI" button on home screen
- Type: "How do I cook pasta?"
- Verify AI responds with brief steps

**Test 9: AI Chat - Favorites Suggestion**
- Add some favorites first
- In AI chat, ask: "What should I cook?"
- Verify AI suggests from YOUR favorites

**Test 10: AI Chat - Moroccan Darija**
- Type: "شنو نطيب اليوم؟"
- Verify AI responds in Darija

**Test 11: AI Chat - Conversation Memory**
- Ask: "How do I make tacos?"
- AI gives recipe
- Ask: "How long does it take?"
- Verify AI knows "it" = tacos

**Test 12: Navigation**
- Test back button from each screen
- Test up button in action bar
- Ensure you return to correct screens

**Test 13: Rotation**
- Rotate device in each screen
- Verify no crashes
- Verify data persists (don't lose typed text)

### Step 8.5: Debugging Common Issues

**Problem: App crashes on launch**

**Solution**: Check Logcat for stack trace:
1. Open **Logcat** tab at bottom
2. Filter by your package name: `com.example.ensa_meal`
3. Find lines with `E/` (error) or `AndroidRuntime: FATAL EXCEPTION`
4. The stack trace shows which line crashed

Common causes:
- `NullPointerException`: You called a method on null. Add null checks.
- `NetworkOnMainThreadException`: You did networking on the main thread. (Volley/OkHttp handle this, so this means you bypassed them.)

**Problem: Images don't load**

**Cause**: Glide requires internet permission.

**Solution**: Verify `<uses-permission android:name="android.permission.INTERNET" />` is in `AndroidManifest.xml`.

**Problem: Database is empty after restart**

**Cause**: You're using `.fallbackToDestructiveMigration()` and changed the database schema.

**Solution**: Uninstall the app, then reinstall. Or remove `.fallbackToDestructiveMigration()` and implement proper migrations (advanced topic).

**Problem: AI chat says "API key not configured"**

**Cause**: `local.properties` doesn't have `GROQ_API_KEY` or Gradle didn't sync.

**Solution**:
1. Verify `GROQ_API_KEY=your_key_here` is in `local.properties`
2. Rebuild: **Build** → **Rebuild Project**
3. Check `BuildConfig.GROQ_API_KEY` is accessible in code

**Problem: AI responds in English when I type Darija**

**Cause**: The model might not recognize your Darija dialect, or the prompt needs refinement.

**Solution**: This is a limitation of the model's training data. Llama 3.3 has good multilingual support but Darija support varies. Try standard Moroccan Arabic instead.

**Problem: Search doesn't work**

**Cause**: TheMealDB API might be down, or you have no internet.

**Solution**:
1. Test internet: Open browser on emulator/device
2. Test API directly: Visit `https://www.themealdb.com/api/json/v1/1/search.php?s=chicken` in browser
3. Check Logcat for Volley errors

---

## Part 9: Preparing for Your Presentation

You've built a sophisticated app. Now, sell it to your professors.

### Step 9.1: Understanding What to Emphasize

Professors evaluate three things:

1. **Technical depth**: Do you understand the code, or did you copy-paste?
2. **Architectural reasoning**: Can you justify your design choices?
3. **Problem-solving**: How did you handle challenges?

They care less about "Look, it works!" and more about "Why did you build it this way?"

### Step 9.2: Presentation Structure

**Opening (30 seconds)**

"I'm presenting Ensa Meal, an Android application that combines meal discovery, personalized favorites management, and AI-powered cooking assistance. The app demonstrates three-tier architecture, RESTful API integration, local data persistence, and real-time AI interaction."

**Architecture Overview (2 minutes)**

Draw this diagram on the board or slide:

```
┌─────────────────────────────────────────────┐
│           PRESENTATION LAYER                │
│  MainActivity │ FavoritesActivity │ AIChatActivity
└─────────────┬───────────────────────────────┘
              │
┌─────────────▼───────────────────────────────┐
│         BUSINESS LOGIC LAYER                │
│  Adapters │ Click Listeners │ Validation    │
└─────────────┬───────────────────────────────┘
              │
┌─────────────▼───────────────────────────────┐
│            DATA LAYER                       │
│  TheMealDB API │ Room Database │ Groq API   │
└─────────────────────────────────────────────┘
```

**Explain each layer:**

"The presentation layer handles UI and user interactions. The business logic layer processes data and manages workflows. The data layer interfaces with external APIs and local storage. This separation enables maintainability—I can change the database without touching UI code."

**Key Features Demo (5 minutes)**

**Feature 1: Meal Discovery**

*Show searching for meals*

"I integrated the TheMealDB API using Volley for HTTP requests. Volley handles background threading automatically—network calls don't block the main thread. The adapter uses the ViewHolder pattern for efficient scrolling—views are recycled instead of recreated."

**Feature 2: Favorites Management**

*Show adding, editing, deleting favorites*

"Favorites persist locally using Room, Google's ORM over SQLite. Room provides compile-time SQL verification—invalid queries fail at build time, not runtime. I implemented the DAO pattern to abstract database operations. The Singleton pattern ensures only one database instance exists, preventing resource waste."

*Show swipe-to-delete*

"I implemented swipe-to-delete using ItemTouchHelper. This provides intuitive gestural interaction—users expect swipe gestures in modern apps."

**Feature 3: AI Chat**

*Show AI conversation*

"The AI chat integrates the Groq API using OkHttp. I implemented the COSTAR prompting framework—Context, Objective, Style, Tone, Audience, Response. This structures the AI's behavior. The AI has conversation memory—it remembers previous messages, enabling natural follow-ups."

*Show favorites integration*

"The AI reads the user's favorites from the database and incorporates them into its context. When asked 'What should I cook?', it suggests from the user's saved meals, not random recipes. This is dynamic personalization."

*Show multilingual support*

"The AI supports multiple languages through prompt engineering. I instruct it to mirror the user's language—if they ask in Moroccan Darija, it responds in Darija. This works because modern LLMs are multilingual."

### Step 9.3: Answering Tough Questions

**Q: "Why did you use Volley instead of Retrofit?"**

**A**: "Volley is simpler for basic GET requests with JSON responses. Retrofit is more powerful but has a steeper learning curve. For this app's requirements—straightforward API calls with automatic JSON parsing—Volley's simplicity was appropriate. However, if the app scales and requires more complex API interactions like multipart uploads or RxJava integration, I'd migrate to Retrofit."

**Q: "Your database queries run on the main thread. Why?"**

**A**: "I used `.allowMainThreadQueries()` for development simplicity. In production, I'd use Room's built-in support for LiveData or Kotlin coroutines to observe database changes asynchronously. This keeps the UI responsive. The queries in this app are fast—small datasets, indexed primary keys—so main thread queries don't cause noticeable lag. But it's not scalable."

**Q: "How do you handle API key security?"**

**A**: "I use BuildConfig injection from Gradle. The API key is stored in `local.properties`, which is gitignored. At build time, Gradle reads this file and generates a `BuildConfig` class with the key as a constant. This prevents the key from appearing in version control or being decompiled from the APK. For production apps, I'd use Android's EncryptedSharedPreferences or a secure backend endpoint that proxies API requests."

**Q: "What if the user has no internet?"**

**A**: "Currently, the app requires internet for meal discovery and AI chat. To handle offline scenarios, I'd implement caching. Volley has built-in HTTP caching—responses are stored on disk. For the database, favorites already work offline. To improve this, I'd cache meal search results in Room and display them when offline, with a banner indicating 'Viewing cached data'."

**Q: "How do you ensure the AI doesn't hallucinate meal suggestions not in the user's favorites?"**

**A**: "The prompt explicitly lists the user's favorites and instructs the AI to suggest only from this list. LLMs generally follow explicit constraints. However, hallucination is possible. To prevent this, I could validate AI responses on the client side—check if suggested meals exist in the favorites list, and if not, prompt the AI again or display an error."

**Q: "Why Java instead of Kotlin?"**

**A**: "Java has more learning resources and a longer history in Android development. For someone new to Android, Java's explicit syntax makes concepts clearer. Kotlin is more concise and modern—features like null safety, coroutines, and extension functions are powerful. If continuing this project, I'd migrate to Kotlin for better productivity and Android Jetpack integration."

### Step 9.4: Emphasizing Learning Outcomes

Conclude with reflection:

"Building this app taught me more than syntax—I learned **why** patterns exist. The Singleton pattern prevents resource waste. The ViewHolder pattern optimizes scrolling. The DAO pattern separates concerns. These aren't arbitrary rules; they're solutions to real problems that emerged as apps grew complex. Understanding the problems they solve means I can apply them appropriately, not just blindly follow tutorials."

### Step 9.5: Demo Flow Checklist

**Before presenting:**
- [ ] App installed on device or emulator
- [ ] Device connected to internet
- [ ] Some meals already added to favorites
- [ ] Groq API key valid and working
- [ ] Screen brightness at 100% (for visibility)
- [ ] Airplane mode off
- [ ] Do Not Disturb on (avoid interruptions)

**Demo sequence:**
1. Launch app → Show meal cards loading
2. Search for "pasta" → Show results
3. Tap a meal → Show detail screen
4. Add to favorites with comment → Show toast
5. Navigate to favorites → Show saved meal
6. Edit favorite → Change rating
7. Swipe to delete → Show animation
8. Add another favorite (you need meals for AI)
9. Open AI chat → Ask "What should I cook?"
10. Show AI suggests YOUR favorites
11. Ask follow-up: "How do I make [meal]?"
12. Show AI remembers context
13. Ask in Darija: "شنو نطيب؟"
14. Show AI responds in Darija

**Time management**: Practice the demo. Aim for 5-7 minutes of demo, leaving time for questions.

### Step 9.6: The Most Important Slide

If you make slides, include this one:

**Lessons Learned:**

1. **Architecture matters**: Separation of concerns makes code maintainable
2. **Patterns solve problems**: Singleton, Observer, ViewHolder, DAO, Builder—each addresses a specific issue
3. **UX is hard**: Smooth scrolling, loading states, error messages—small details matter
4. **APIs are unpredictable**: Handle failures gracefully, validate responses, don't trust external services
5. **Security is not optional**: API keys, user data, permissions—think like an attacker

This shows maturity—you're not just a code typist, you're a thinker.

---

## Conclusion

You've journeyed from zero to a complete Android app. You understand:

- **Android fundamentals**: Activities, layouts, RecyclerView, intents
- **Networking**: REST APIs, JSON parsing, asynchronous requests
- **Persistence**: Room database, SQL, DAOs
- **UI patterns**: Adapters, ViewHolders, click listeners
- **Advanced features**: AI integration, prompt engineering, multilingual support
- **Professional practices**: Security, error handling, code organization

But more importantly, you understand **why**. You can defend every line of code. When professors probe, you won't panic—you know the reasoning.

**Final advice**: Projects are never "done." You could add features forever—push notifications, meal planning, grocery lists, social sharing. But done is better than perfect. Ship what works, then iterate.

Your presentation is not about showing a flawless app. It's about demonstrating that you can think architecturally, solve problems systematically, and learn continuously.

Go into that presentation room with confidence. You didn't just build an app. You built understanding.

And that's what they're really grading.

*Now go make them proud.*

— Davinci

---

## Part 10: Building and Generating APK for Submission

You've tested your app on devices and emulators. Now you need to create an installable APK file to submit to your professors or share with others.

### Step 10.1: Understanding Build Types

Android has two main build types:

**Debug Build:**
- Used during development
- Includes debugging information
- Signed with a debug keystore (auto-generated)
- Larger file size
- Not optimized
- What you've been running until now

**Release Build:**
- For production/distribution
- Optimized and obfuscated code
- Signed with your own keystore
- Smaller file size
- Better performance
- What you give to professors/users

**Why the difference?** Debug builds include extra information to help developers troubleshoot (line numbers, variable names, logging). Release builds strip this out, making them faster and harder to reverse-engineer.

### Step 10.2: Creating a Keystore

Before creating a release APK, you need a **keystore**—a file containing cryptographic keys that prove you're the app's creator.

**Important**: Keep this keystore safe. If you lose it, you can never update your app on the Play Store. It's your app's identity.

**Create your keystore via command line:**

Open Terminal (Mac/Linux) or Command Prompt (Windows) and run:

```bash
keytool -genkey -v -keystore ensa_meal_keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias ensa_meal_key
```

**What this command does:**

- `keytool`: Java's key management utility
- `-genkey`: Generate a new key pair
- `-keystore ensa_meal_keystore.jks`: Output file name
- `-keyalg RSA`: Use RSA encryption algorithm
- `-keysize 2048`: 2048-bit key (secure standard)
- `-validity 10000`: Valid for 10,000 days (~27 years)
- `-alias ensa_meal_key`: Nickname for this key

**You'll be prompted for:**

1. **Keystore password**: Choose a strong password (e.g., `MySecurePass123!`)
2. **Key password**: Can be same as keystore password
3. **Name**: Your name (e.g., "Youssef")
4. **Organizational Unit**: Your department (e.g., "Computer Science")
5. **Organization**: Your university (e.g., "ENSA")
6. **City**: Your city
7. **State**: Your state/region
8. **Country code**: Two-letter code (e.g., "MA" for Morocco)

After answering, it creates `ensa_meal_keystore.jks` in your current directory.

**Store this file securely!** Move it to a safe location. Never commit it to git.

### Step 10.3: Configuring Gradle for Release

Open `app/build.gradle.kts` and add signing configuration:

```kotlin
android {
    // ... existing config

    signingConfigs {
        create("release") {
            storeFile = file("/path/to/your/ensa_meal_keystore.jks")
            storePassword = "your_keystore_password"
            keyAlias = "ensa_meal_key"
            keyPassword = "your_key_password"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

**Replace:**
- `/path/to/your/ensa_meal_keystore.jks` with actual path
- `your_keystore_password` with your actual password
- `your_key_password` with your actual key password

**Understanding the configuration:**

**isMinifyEnabled = true**: Enables code shrinking. ProGuard/R8 removes unused code, making the APK smaller.

**isShrinkResources = true**: Removes unused resources (images, layouts you imported but never used).

**proguardFiles**: ProGuard rules that tell R8 what to obfuscate and what to keep.

**signingConfig**: Uses your keystore to sign the APK.

**Security Warning**: Hardcoding passwords in `build.gradle.kts` is bad practice for real projects. For your school project, it's acceptable. In production, you'd use environment variables or Android Studio's signing configuration UI.

### Step 10.4: ProGuard Rules

ProGuard obfuscates your code—renames classes and methods to make reverse-engineering harder. But it can break things if it renames classes that reflection or libraries depend on.

Open (or create) `app/proguard-rules.pro` and add:

```proguard
# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.**

# Keep Gson/JSON serialization classes
-keepclassmembers class ** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Plat model (used with Serializable)
-keep class com.example.ensa_meal.Plat { *; }
-keep class com.example.ensa_meal.database.** { *; }

# Keep OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Keep Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Keep line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
```

**What these rules do:**

**-keep**: Don't obfuscate or remove these classes
**-dontwarn**: Ignore warnings about missing classes (they're optional dependencies)
**-keepattributes**: Keep certain attributes (like line numbers for crash reports)

**When professors ask about ProGuard**: "ProGuard is a code shrinker and obfuscator. It removes unused code, making the APK smaller and faster. It also renames classes and methods, making reverse-engineering harder. However, it can break reflection or serialization, so I added keep rules for Room database entities, serializable classes, and third-party libraries like Glide and OkHttp."

### Step 10.5: Generating the Release APK

**In Android Studio:**

1. Click **Build** → **Generate Signed Bundle / APK**
2. Select **APK** (not Bundle—unless submitting to Play Store)
3. Click **Next**
4. **Key store path**: Browse to your `.jks` file
5. **Key store password**: Enter your keystore password
6. **Key alias**: `ensa_meal_key`
7. **Key password**: Enter your key password
8. Click **Next**
9. **Destination folder**: Choose where to save (default is `app/release/`)
10. **Build Variants**: Select `release`
11. **Signature Versions**: Check both V1 and V2
12. Click **Finish**

Android Studio builds your APK. When done, you'll see a notification with a link to the APK location.

**Via Command Line** (alternative):

```bash
./gradlew assembleRelease
```

The APK will be at `app/build/outputs/apk/release/app-release.apk`.

### Step 10.6: Testing the Release APK

Before submitting, test the APK thoroughly. Release builds behave differently than debug builds.

**Install on device:**

```bash
adb install app/build/outputs/apk/release/app-release.apk
```

Or drag the APK to an emulator.

**What to test:**

1. **All features work**: Go through your 13 test scenarios from Part 8
2. **No crashes**: Release builds have different optimizations
3. **API keys work**: Ensure BuildConfig still injects correctly
4. **Database persists**: Close and reopen app, verify favorites remain
5. **Performance**: Release should be faster and smoother than debug

**Common Release Build Issues:**

**Problem: App crashes on startup**

**Cause**: ProGuard removed a class you need.

**Solution**: Check Logcat for `ClassNotFoundException` or `MethodNotFoundException`. Add a `-keep` rule for that class in `proguard-rules.pro`.

**Problem: API calls fail**

**Cause**: ProGuard obfuscated JSON model classes.

**Solution**: Add `-keep class com.example.ensa_meal.Plat { *; }` to ProGuard rules.

**Problem: Database queries fail**

**Cause**: Room entities were obfuscated.

**Solution**: Already handled if you added the Room keep rules from Step 10.4.

### Step 10.7: Reducing APK Size

Professors appreciate efficiency. A smaller APK is better.

**Check current size:**

The APK in `app/build/outputs/apk/release/` shows its size. Typical range: 5-15 MB.

**Optimization techniques:**

**1. Enable code shrinking** (already done in Step 10.3)

**2. Remove unused resources:**

In `app/build.gradle.kts`:

```kotlin
buildTypes {
    release {
        isShrinkResources = true  // Already added
    }
}
```

**3. Use WebP images instead of PNG:**

Android Studio can convert images: Right-click image → Convert to WebP. WebP is 25-35% smaller than PNG with similar quality.

**4. Exclude unused language resources:**

If your app doesn't need every language, exclude them:

```kotlin
android {
    defaultConfig {
        resourceConfigurations += listOf("en", "ar", "fr")  // Only include English, Arabic, French
    }
}
```

**5. Enable APK splitting** (advanced):

Generate separate APKs for different screen densities or architectures. This is overkill for a school project, but good to know.

### Step 10.8: Preparing for Submission

**Create a submission package:**

1. **The APK**: `app-release.apk`
2. **Installation instructions**: A simple text file

Create `INSTALL_INSTRUCTIONS.txt`:

```
# Ensa Meal - Installation Instructions

## Requirements:
- Android device with API 24+ (Android 7.0 or newer)
- Internet connection (for meal API and AI features)

## Installation Steps:

### On Android Device:
1. Transfer app-release.apk to your device
2. Open the APK file
3. If prompted, enable "Install from Unknown Sources" in Settings
4. Tap "Install"
5. Open Ensa Meal from your app drawer

### On Emulator:
1. Drag and drop app-release.apk onto the emulator window
2. App installs automatically
3. Open from app drawer

## Using the App:
1. Browse meals on the home screen
2. Tap any meal to view details
3. Add meals to favorites (star icon in detail screen)
4. View favorites by tapping ⭐ button
5. Chat with AI by tapping "AI" button
   - Note: AI requires Groq API key (configured during development)

## Features:
- Meal browsing with search
- Favorites management (add, edit, delete)
- Swipe-to-delete in favorites
- AI cooking assistant
- Multilingual support (English, Darija, French, Arabic)
- Personalized meal suggestions from your favorites

## Author:
Youssef - [Your University] - [Date]
```

**Create a ZIP file:**

- `app-release.apk`
- `INSTALL_INSTRUCTIONS.txt`
- (Optional) `README.md` with project overview

Name it: `Ensa_Meal_Youssef_[Date].zip`

### Step 10.9: Understanding APK Signing (For Interview Questions)

**Q: "What is APK signing and why is it necessary?"**

**A**: "APK signing is a cryptographic process that proves the app's authenticity. When you sign an APK with your keystore, it creates a digital signature. Android verifies this signature during installation. If someone modifies your APK after you sign it, the signature becomes invalid and Android refuses to install it. This prevents tampering. Additionally, apps signed with different keys are treated as different apps—you can't update an app with an APK signed by a different key. This ensures only the original developer can publish updates."

**Q: "What's the difference between V1 and V2 signing?"**

**A**: "V1 (JAR signing) is the original scheme, signing each file individually. V2 (APK Signature Scheme) signs the entire APK as a single block, which is faster to verify and more secure—it protects the entire APK structure, not just individual files. V2 was introduced in Android 7.0. For maximum compatibility, we enable both—V2 for modern devices, V1 as fallback for older devices."

### Step 10.10: Final Checklist Before Submission

- [ ] Release APK builds successfully
- [ ] APK installs on physical device
- [ ] APK installs on emulator
- [ ] All features work in release build
- [ ] No crashes during testing
- [ ] API key configured correctly
- [ ] App performance is smooth
- [ ] APK size is reasonable (<20 MB)
- [ ] Installation instructions included
- [ ] Files packaged in ZIP
- [ ] File named clearly (name_project_date)

---

## Part 11: Future Enhancements and Next Steps

You've built a complete, functional app. But software is never "done." Here's how to take it further.

### Step 11.1: Architectural Improvements

**Current Architecture**: You used a simple three-tier architecture. It works, but there are more sophisticated patterns.

#### Migrate to MVVM (Model-View-ViewModel)

**Problem with current approach**: Activities contain business logic, making them hard to test. UI and logic are tightly coupled.

**MVVM Solution**: Separate concerns:
- **Model**: Data (Plat, FavoriteEntity)
- **View**: UI (Activities, XML)
- **ViewModel**: Business logic (between Model and View)

**Benefits**:
- Testable: Test ViewModels without launching Activities
- Lifecycle-aware: ViewModels survive configuration changes (rotation)
- Clean: Activities only handle UI updates

**Example refactor**:

Instead of MainActivity directly calling `searchMeals()`, you'd have:

```java
public class MainViewModel extends ViewModel {
    private MutableLiveData<List<Plat>> mealsLiveData = new MutableLiveData<>();

    public LiveData<List<Plat>> getMeals() {
        return mealsLiveData;
    }

    public void searchMeals(String query) {
        // Network call logic here
        // Update mealsLiveData when done
    }
}
```

MainActivity observes `mealsLiveData`:

```java
viewModel.getMeals().observe(this, meals -> {
    adapter.updateData(meals);
});
```

**When to use**: Medium to large apps. For your project's scope, current architecture is appropriate. But mentioning MVVM in your presentation shows awareness.

#### Clean Architecture

**Even more separation**: Domain layer (business rules), Data layer (repositories), Presentation layer.

**Overkill for this project**, but useful for enterprise apps.

### Step 11.2: Performance Optimizations

#### 1. Database Operations on Background Thread

Currently: `.allowMainThreadQueries()` blocks the UI.

**Solution**: Use LiveData or Kotlin Coroutines:

```java
// Room automatically runs queries on background thread when returning LiveData
@Query("SELECT * FROM favorites")
LiveData<List<FavoriteEntity>> getAllFavorites();
```

In Activity:

```java
favoriteDao.getAllFavorites().observe(this, favorites -> {
    adapter.updateData(favorites);
});
```

**Benefit**: UI never freezes waiting for database.

#### 2. Pagination for Meal List

Currently: Load all meals at once.

**Problem**: If API returns 100+ meals, loading takes time and uses memory.

**Solution**: Paging library—load 20 meals at a time, load more as user scrolls.

#### 3. Image Caching Strategy

Glide already caches images, but you can tune it:

```java
Glide.with(context)
    .load(url)
    .diskCacheStrategy(DiskCacheStrategy.ALL)  // Cache original and resized
    .thumbnail(0.1f)  // Show low-res thumbnail while loading
    .into(imageView);
```

**Benefit**: Faster loading, less bandwidth usage.

#### 4. LazyColumn vs RecyclerView

If migrating to Jetpack Compose (modern Android UI), replace RecyclerView with LazyColumn—it's more efficient and easier to use.

### Step 11.3: Feature Enhancements

#### 1. Offline Mode

**Goal**: Browse cached meals without internet.

**Implementation**:
- Store API responses in Room database
- When offline, query database instead of API
- Show banner: "Viewing cached data"

```java
if (isNetworkAvailable()) {
    fetchFromAPI();
} else {
    loadFromCache();
    showOfflineBanner();
}
```

#### 2. Meal Planning

**Feature**: Users plan meals for the week.

**Implementation**:
- New table: `meal_plan` with columns `day`, `meal_id`, `meal_type` (breakfast/lunch/dinner)
- Calendar view showing planned meals
- Drag-and-drop from favorites to calendar

**Use case**: "What should I cook Wednesday?" → AI suggests based on meal plan.

#### 3. Shopping List

**Feature**: Generate shopping list from favorite meals' ingredients.

**Implementation**:
- Parse recipe instructions for ingredients (or use detailed API endpoint)
- Store in `shopping_list` table
- Checkbox list UI
- Share list via SMS/WhatsApp

#### 4. Social Sharing

**Feature**: Share favorite meals with friends.

**Implementation**:
- Deep links: `ensaMeal://meal/52772` opens meal detail
- Share button creates link + image
- Firebase Dynamic Links for robust cross-platform sharing

#### 5. Nutrition Information

**Feature**: Show calories, protein, carbs for meals.

**Implementation**:
- TheMealDB doesn't have nutrition data
- Integrate Nutritionix API or Edamam API
- Display nutrition facts in detail screen

#### 6. Voice Input for AI

**Feature**: Speak questions instead of typing.

**Implementation**:
- Android's SpeechRecognizer
- Convert speech to text
- Send to AI as usual

```java
SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
speechRecognizer.startListening(intent);
```

**Benefit**: Hands-free cooking assistance.

#### 7. Push Notifications

**Feature**: "Time to cook dinner! Try Beef Tacos tonight."

**Implementation**:
- Firebase Cloud Messaging
- Schedule notifications based on meal times
- AI generates personalized suggestions

### Step 11.4: UI/UX Improvements

#### 1. Dark Mode

**Implementation**:
- Create `res/values-night/colors.xml`
- Define dark theme colors
- Use `?attr/colorPrimary` instead of hardcoded colors

**Benefit**: Easy on eyes, saves battery on OLED screens.

#### 2. Animations

**Add polish**:
- Shared element transitions between screens
- FAB (Floating Action Button) for adding favorites
- Lottie animations for loading states

```java
// Shared element transition
ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
    this,
    imageView,
    "mealImage"
);
startActivity(intent, options.toBundle());
```

#### 3. Material You / Material 3

**Upgrade**: Use Material 3 components (dynamic color, rounded everything).

**Implementation**: Update to Material 3 library, redesign with new components.

#### 4. Swipe Gestures Everywhere

- Swipe between tabs
- Pull-to-refresh meal list
- Swipe meal cards in main screen to quick-add to favorites

### Step 11.5: Backend Integration

Currently: Direct API calls from app.

**Limitations**:
- API keys exposed (even with BuildConfig, they're in the APK)
- Can't add custom features (like user accounts)
- No cross-device sync

**Solution**: Build your own backend.

#### Option 1: Firebase

**Pros**: No code, easy setup
**Features**:
- Authentication (user accounts)
- Firestore (cloud database)
- Cloud Functions (serverless code)
- Cloud Storage (store images)

**Use case**: Users log in, favorites sync across devices.

#### Option 2: Custom Backend (Node.js + Express)

**Pros**: Full control
**Implementation**:
- Node.js server with Express
- PostgreSQL or MongoDB database
- RESTful API
- JWT authentication

**API endpoints**:
- `POST /auth/register` - Create account
- `POST /auth/login` - Login
- `GET /favorites` - Get user's favorites
- `POST /favorites` - Add favorite
- `GET /ai/chat` - Proxy AI requests (hide API key)

**Benefit**: API keys stay on server, users have accounts, data syncs.

### Step 11.6: Testing Improvements

#### Unit Tests

Test individual components in isolation:

```java
@Test
public void testPlatGetters() {
    Plat plat = new Plat("1", "Pizza", "url", "Delicious");
    assertEquals("Pizza", plat.getName());
    assertEquals("1", plat.getId());
}
```

#### Integration Tests

Test Room database queries:

```java
@Test
public void testAddFavorite() {
    FavoriteEntity fav = new FavoriteEntity("1", "Pizza", "url", "desc", "note", 5);
    favoriteDao.addToFavorites(fav);

    List<FavoriteEntity> favorites = favoriteDao.getAllFavorites();
    assertEquals(1, favorites.size());
}
```

#### UI Tests (Espresso)

Test UI interactions:

```java
@Test
public void testAddToFavorites() {
    onView(withId(R.id.recycler_view))
        .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

    onView(withId(R.id.action_add_favorite))
        .perform(click());

    onView(withText("Added to Favorites"))
        .check(matches(isDisplayed()));
}
```

**Why test?** Catch bugs early, confidence when refactoring, professional practice.

### Step 11.7: Security Hardening

#### 1. Network Security Configuration

Force HTTPS, prevent man-in-the-middle attacks:

Create `res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

In `AndroidManifest.xml`:

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config">
```

**Benefit**: App only communicates over HTTPS.

#### 2. Certificate Pinning

**Problem**: Attacker could use fake certificates.

**Solution**: Pin specific certificates:

```java
OkHttpClient client = new OkHttpClient.Builder()
    .certificatePinner(new CertificatePinner.Builder()
        .add("api.groq.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .build())
    .build();
```

Get the hash from the certificate.

**Benefit**: App only trusts specific certificates, blocking MITM attacks.

#### 3. SQL Injection Prevention

**Your app is safe** because Room uses parameterized queries. But if you wrote raw SQL:

```java
// UNSAFE
database.execSQL("SELECT * FROM favorites WHERE id = " + userId);

// SAFE
database.query("SELECT * FROM favorites WHERE id = ?", new String[]{userId});
```

### Step 11.8: Deployment to Play Store (Advanced)

If you want to publish:

**Requirements**:
- Google Play Developer account ($25 one-time fee)
- Privacy policy URL
- App icons (multiple sizes)
- Screenshots (phone, tablet, TV)
- Store listing (description, graphics)

**Steps**:
1. Create developer account
2. Generate **App Bundle** (not APK): `./gradlew bundleRelease`
3. Upload to Play Console
4. Fill out store listing
5. Submit for review (takes 1-3 days)

**Benefit**: Anyone can install from Play Store. Adds credibility.

### Step 11.9: Learning Resources

**To deepen Android knowledge:**

**Official**:
- **Android Developer Guides**: developer.android.com/guide
- **Android Codelabs**: codelabs.developers.google.com (hands-on tutorials)
- **Android Architecture Samples**: github.com/android/architecture-samples

**Courses**:
- **Google's Android Basics in Kotlin**: Free, comprehensive
- **Udacity Android Nanodegree**: Paid, in-depth
- **Philipp Lackner YouTube**: Modern Android (Jetpack Compose, MVVM)

**Books**:
- *Android Programming: The Big Nerd Ranch Guide* - Beginner-friendly
- *Effective Java* by Joshua Bloch - Java best practices

**Communities**:
- r/androiddev on Reddit
- Android Developers Discord
- Stack Overflow

**Next Steps**:
1. **Learn Kotlin**: Modern Android uses Kotlin. More concise, safer than Java.
2. **Jetpack Compose**: Declarative UI framework (like React/SwiftUI). Future of Android UI.
3. **Coroutines & Flow**: Better async programming than callbacks.
4. **Dagger/Hilt**: Dependency injection for large apps.
5. **GraphQL**: Modern API alternative to REST.

### Step 11.10: Contributing to Open Source

**Build credibility:**

Find Android projects on GitHub, fix bugs, add features, submit pull requests.

**Good starter projects**:
- Beginner-friendly projects labeled "good first issue"
- Apps you use daily (check if they're open source)
- Libraries like Glide, OkHttp, Room (improve documentation)

**Benefits**:
- Learn from experienced developers
- Build portfolio
- Network with community
- Looks great on resume

### Step 11.11: Final Thoughts

You've completed a journey from zero to a fully functional Android app. You understand not just **what** to code, but **why** each piece exists.

**The real skill you've gained isn't Android development—it's the ability to learn any technology systematically**. The patterns you learned (MVC, MVVM, Singleton, Observer) apply everywhere. The problem-solving approach (understand requirements, architect a solution, implement iteratively, test thoroughly) is universal.

**What separates good developers from great ones:**

- **Good developers** write code that works.
- **Great developers** write code that's maintainable, testable, and scalable.

You're on your way to being great.

**Remember**:
- Every expert was once a beginner
- Every large project started small
- Every bug you fix teaches you something
- Every line of code makes you better

Your professors will see more than an app. They'll see someone who can **think**, **design**, and **build**.

**One last piece of advice**: After your presentation, don't abandon this project. Add one feature from Part 11. Polish it. Make it yours. Then build something new. The best way to learn is to keep building.

**The path forward:**
1. Present confidently
2. Iterate on feedback
3. Learn Kotlin
4. Explore Jetpack Compose
5. Build your next app
6. Contribute to open source
7. Never stop learning

**You've got this.**

When you walk into that presentation room, remember: you built something from nothing. You took an idea and made it real. You solved problems, debugged crashes, integrated APIs, and created an experience.

That's not just development. That's creation.

And you're a creator now.

*Go forth and build amazing things.*

— Davinci

---

## Appendix: Quick Reference

### Project Structure
```
Ensa_Meal/
├── app/src/main/
│   ├── java/com/example/ensa_meal/
│   │   ├── MainActivity.java
│   │   ├── FavoritesActivity.java
│   │   ├── AIChatActivity.java
│   │   ├── Instructions.java
│   │   ├── Plat.java
│   │   ├── AdapterMeals.java
│   │   ├── FavoritesAdapter.java
│   │   └── database/
│   │       ├── AppDatabase.java
│   │       ├── FavoriteEntity.java
│   │       └── FavoriteDao.java
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml
│   │   │   ├── activity_favorites.xml
│   │   │   ├── activity_ai_chat.xml
│   │   │   ├── activity_instructions.xml
│   │   │   ├── model_plat.xml
│   │   │   ├── item_favorite.xml
│   │   │   └── dialog_edit_favorite.xml
│   │   └── values/
│   │       ├── strings.xml
│   │       └── colors.xml
│   └── AndroidManifest.xml
├── build.gradle.kts
└── local.properties (gitignored)
```

### Key Dependencies
```kotlin
// Networking
implementation("com.android.volley:volley:1.2.1")
implementation("com.squareup.okhttp3:okhttp:4.12.0")

// Database
implementation("androidx.room:room-runtime:2.6.1")
annotationProcessor("androidx.room:room-compiler:2.6.1")

// Image Loading
implementation("com.github.bumptech.glide:glide:4.16.0")

// UI
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("com.google.android.material:material:1.9.0")
```

### Design Patterns Used
- **Singleton**: AppDatabase
- **ViewHolder**: RecyclerView optimization
- **Observer**: Click listeners, LiveData
- **DAO**: Database access abstraction
- **Builder**: AlertDialog, OkHttp Request
- **Adapter**: RecyclerView adapters
- **MVC**: Model (Plat, FavoriteEntity), View (XML), Controller (Activities)

### API Endpoints
- **TheMealDB**: `https://www.themealdb.com/api/json/v1/1/search.php?s={query}`
- **Groq**: `https://api.groq.com/openai/v1/chat/completions`

### Common Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on device
adb install app/build/outputs/apk/release/app-release.apk

# View logs
adb logcat | grep "com.example.ensa_meal"

# Generate keystore
keytool -genkey -v -keystore ensa_meal_keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias ensa_meal_key
```

---

**End of Guide**

*This guide was crafted with 35 years of experience and an IQ of 264, but written with the heart of a teacher who remembers being a student.*

*May your code compile, your apps inspire, and your presentations impress.*

*— Davinci*
