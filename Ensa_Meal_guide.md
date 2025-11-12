# Ensa Meal - Complete Project Guide

## Table of Contents
1. Project Overview
2. Technologies and Architecture
3. Database Structure
4. API Integration
5. User Interface and Navigation
6. Core Features
7. AI Integration
8. Design Choices

---

## 1. Project Overview

### What is Ensa Meal?
Ensa Meal is an Android application that helps users discover, save, and learn about different meals and recipes. Users can search for meals, view detailed instructions, save favorites with personal notes and ratings, and ask an AI assistant about cooking questions.

### Main Features
- Search meals from TheMealDB API
- View detailed meal instructions and images
- Add meals to favorites with ratings and comments
- Manage favorites (view, edit, delete)
- Ask AI questions about cooking and recipes
- Time tracking for when favorites were added

---

## 2. Technologies and Architecture

### Programming Language
**Java** - The entire app is written in Java, which is Android's primary programming language.

### Architecture Pattern
The app follows a **layered architecture** with three main layers:

1. **UI Layer (Activities)** - What users see and interact with
2. **Data Layer (Database)** - Where data is stored locally
3. **Network Layer (API)** - How we get data from the internet

```
┌─────────────────────┐
│   UI Activities     │ ← Users interact here
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│   Database (Room)   │ ← Data stored here
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│   Network (API)     │ ← Data fetched here
└─────────────────────┘
```

### Key Libraries

**Room Database**
- Why: Provides easy database management for Android
- How: Converts Java objects to SQLite database tables automatically
- File: `app/src/main/java/com/example/ensa_meal/database/AppDatabase.java`

**Volley**
- Why: Handles network requests to TheMealDB API
- How: Sends HTTP requests and receives JSON responses
- File: `app/src/main/java/com/example/ensa_meal/MainActivity.java:134`

**Glide**
- Why: Loads and caches images efficiently
- How: Downloads images from URLs and displays them in ImageViews
- Files: Used in all adapter classes

**OkHttp**
- Why: Makes API calls to Groq AI service
- How: Sends HTTP requests with JSON payloads
- File: `app/src/main/java/com/example/ensa_meal/AIChatActivity.java`

---

## 3. Database Structure

### Database Technology: Room
Room is Google's recommended database library for Android. It sits on top of SQLite (Android's built-in database) and makes it easier to work with.

### Database Configuration
**File**: `app/src/main/java/com/example/ensa_meal/database/AppDatabase.java`

The database is created as a **Singleton**, meaning only one instance exists throughout the app. This prevents multiple database connections and saves memory.

```java
private static AppDatabase instance;

public static synchronized AppDatabase getInstance(Context context) {
    if (instance == null) {
        instance = Room.databaseBuilder(...)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build();
    }
    return instance;
}
```

**Key Settings:**
- `allowMainThreadQueries()` - Lets us access database on main thread (normally not recommended for production)
- `fallbackToDestructiveMigration()` - Recreates database if version changes

### Table 1: MealEntity
**File**: `app/src/main/java/com/example/ensa_meal/database/MealEntity.java`
**Table Name**: `meal_categories`
**Purpose**: Stores meal information from API

| Column | Type | Description |
|--------|------|-------------|
| id | String | Unique meal ID (Primary Key) |
| name | String | Meal name |
| imageURL | String | URL to meal image |
| description | String | Meal instructions |
| timestamp | long | When meal was added |

### Table 2: FavoriteEntity
**File**: `app/src/main/java/com/example/ensa_meal/database/FavoriteEntity.java`
**Table Name**: `favorites`
**Purpose**: Stores user's favorite meals with personal data

| Column | Type | Description |
|--------|------|-------------|
| mealId | String | Meal ID (Primary Key) |
| mealName | String | Meal name |
| mealImageUrl | String | URL to meal image |
| mealDescription | String | Meal instructions |
| userComment | String | User's personal comment |
| userRating | float | User's rating (0-5 stars) |
| addedTimestamp | long | When added to favorites |

### Data Access Objects (DAOs)
DAOs are interfaces that define how to interact with the database.

**FavoriteDao** (`app/src/main/java/com/example/ensa_meal/database/FavoriteDao.java`)
- `addToFavorites()` - Adds a meal to favorites
- `getAllFavorites()` - Gets all favorite meals
- `removeFromFavoritesById()` - Deletes a favorite
- `isFavorite()` - Checks if meal is favorited
- `getFavoriteMealIds()` - Gets list of favorite IDs
- `updateFavorite()` - Updates comment and rating

Room automatically generates the SQL code for these methods.

---

## 4. API Integration

### What is an API?
An API (Application Programming Interface) is a way for different programs to communicate. In our case, TheMealDB provides an API that lets us get meal data over the internet.

### TheMealDB API
**Base URL**: `https://www.themealdb.com/api/json/v1/1/`
**Endpoint Used**: `search.php?s={query}`
**File**: `app/src/main/java/com/example/ensa_meal/MainActivity.java:37`

### How the API Call Works

**Step 1: User Types in Search**
- User enters text in SearchView
- SearchView triggers `onQueryTextChange()`
- File: `MainActivity.java:113`

**Step 2: Create Request**
```java
String url = API_URL + query;  // Example: "...search.php?s=chicken"
JsonObjectRequest request = new JsonObjectRequest(
    Request.Method.GET,    // GET method (requesting data)
    url,                   // Where to send request
    null,                  // No request body needed
    response -> handleApiResponse(response),  // Success callback
    error -> handleApiError(error)           // Error callback
);
```
File: `MainActivity.java:147`

**Step 3: Volley Sends Request**
Volley is a library that handles the network communication. It:
- Creates HTTP connection
- Sends request to server
- Waits for response
- Returns result to our callbacks

**Step 4: Receive JSON Response**
The API returns data in JSON format:
```json
{
  "meals": [
    {
      "idMeal": "52772",
      "strMeal": "Teriyaki Chicken",
      "strMealThumb": "https://...",
      "strInstructions": "Preheat oven..."
    }
  ]
}
```

**Step 5: Parse Response**
File: `MainActivity.java:178`
```java
JSONArray mealsArray = response.getJSONArray("meals");
for (int i = 0; i < mealsArray.length(); i++) {
    JSONObject meal = mealsArray.getJSONObject(i);
    String id = meal.optString("idMeal");
    String name = meal.optString("strMeal");
    // Create Plat object...
}
```

**Step 6: Display in RecyclerView**
- Create `Plat` objects from JSON
- Add to ArrayList
- Call `notifyDataSetChanged()` to update UI

### Error Handling
File: `MainActivity.java:252`
- Checks for network errors
- Shows user-friendly error messages
- Logs errors for debugging

---

## 5. User Interface and Navigation

### Activity Flow
```
MainActivity (Home)
    │
    ├─→ Instructions (Meal Details)
    │
    ├─→ FavoritesActivity (Favorite Meals)
    │
    └─→ AIChatActivity (AI Assistant)
```

### MainActivity
**File**: `app/src/main/java/com/example/ensa_meal/MainActivity.java`
**Layout**: `app/src/main/res/layout/activity_main.xml`

**What it does:**
- Entry point of the app
- Shows search bar at top
- Displays meal list in RecyclerView
- Has AI and Favorites buttons

**Key Components:**
1. **SearchView** - User types meal name here
2. **RecyclerView** - Scrollable list of meals
3. **AI Button** - Opens AI chat
4. **Favorites Button** - Opens favorites page
5. **ProgressBar** - Shows loading indicator

**How navigation works from MainActivity to Instructions:**

When user taps a meal:
1. `onItemClick(position)` is called (File: `MainActivity.java:212`)
2. Get the `Plat` object at that position
3. Create an `Intent` (Android's way of navigating between screens)
4. Put the `Plat` data into a `Bundle` (data container)
5. Start `Instructions` activity
6. Android transitions to new screen

```java
Intent intent = new Intent(this, Instructions.class);
Bundle bundle = new Bundle();
bundle.putSerializable("MEAL", plat);  // Plat implements Serializable
intent.putExtras(bundle);
startActivity(intent);  // Android handles the transition
```

The transition happens because:
- Android's `ActivityManager` creates the new activity
- The old activity is paused and moved to background
- The new activity is created and displayed
- Data is passed via the Intent extras

### Instructions Activity
**File**: `app/src/main/java/com/example/ensa_meal/Instructions.java`
**Layout**: `app/src/main/res/layout/activity_instructions.xml`

**What it does:**
- Shows meal details (image, name, instructions)
- Lets user add to favorites
- Displays if meal is already favorited

**How it receives data:**
```java
Intent intent = getIntent();
Bundle bundle = intent.getExtras();
// For Android 13+ (API 33)
plat = bundle.getSerializable("MEAL", Plat.class);
// For older Android versions
plat = (Plat) bundle.getSerializable("MEAL");
```
File: `Instructions.java:42`

**Why different handling for Android versions?**
Android 13 changed how Serializable objects are retrieved for better type safety.

### FavoritesActivity
**File**: `app/src/main/java/com/example/ensa_meal/FavoritesActivity.java`
**Layout**: `app/src/main/res/layout/activity_favorites.xml`

**What it does:**
- Shows all favorite meals
- Displays user comments and ratings
- Shows "time ago" (e.g., "2 days ago")
- Allows editing and deleting favorites
- Has swipe-to-delete feature

**Swipe to Delete Feature:**
File: `FavoritesActivity.java:78`

Uses `ItemTouchHelper` which detects swipe gestures:
```java
new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

    public boolean onMove(...) { return false; }

    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        deleteFavorite(position);  // Delete the swiped item
    }
}).attachToRecyclerView(recyclerView);
```

When user swipes left or right, `onSwiped()` is triggered and the item is deleted.

### AIChatActivity
**File**: `app/src/main/java/com/example/ensa_meal/AIChatActivity.java`
**Layout**: `app/src/main/res/layout/activity_ai_chat.xml`

**What it does:**
- Chat interface for AI questions
- User types questions about cooking
- AI responds with brief answers
- Shows conversation history

---

## 6. Core Features

### Feature 1: Search Meals

**How it works:**
1. User types in SearchView
2. `onQueryTextChange()` triggered on every character
3. Makes API call with search query
4. Receives JSON response
5. Parses data into `Plat` objects
6. Updates RecyclerView

**Why real-time search?**
Provides better user experience - results appear as you type.

### Feature 2: RecyclerView Display

**What is RecyclerView?**
RecyclerView is Android's efficient way to display scrollable lists. It only creates views for items currently visible on screen, recycling views as user scrolls.

**How AdapterMeals works:**
File: `app/src/main/java/com/example/ensa_meal/AdapterMeals.java`

1. **ViewHolder Pattern**: Holds references to views in each list item
```java
class ViewHolder {
    ImageView image;
    TextView tId;
    TextView tName;
    ImageView favoriteIcon;
}
```

2. **onCreateViewHolder()**: Creates view for each item
3. **onBindViewHolder()**: Fills view with data
```java
holder.tName.setText(plat.getName());
holder.tId.setText(plat.getId());
Glide.with(context).load(plat.getImageURL()).into(holder.image);
```

4. **Click Listeners**: Handles user taps
```java
holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
holder.favoriteIcon.setOnClickListener(v -> listener.onToggleFavoriteClick(position));
```

### Feature 3: Add to Favorites

**From MainActivity:**
File: `MainActivity.java:222`

When star icon clicked:
1. Check if already favorite using `favoriteDao.isFavorite(mealId)`
2. If favorite: Remove from database
3. If not favorite: Add to database with default values
4. Update UI to reflect new state

**From Instructions:**
File: `Instructions.java:87`

Shows dialog with:
- EditText for user comment
- RatingBar for rating (0-5 stars)
- Save button

When saved:
1. Create `FavoriteEntity` with user's comment and rating
2. Insert into database
3. Update UI

### Feature 4: Manage Favorites

**View All Favorites:**
File: `FavoritesActivity.java:124`
```java
List<FavoriteEntity> favorites = favoriteDao.getAllFavorites();
```
Room executes: `SELECT * FROM favorites`

**Edit Favorite:**
Shows dialog with current values pre-filled. On save:
```java
favoriteDao.updateFavorite(mealId, newComment, newRating);
```
Room executes: `UPDATE favorites SET userComment=?, userRating=? WHERE mealId=?`

**Delete Favorite:**
Shows confirmation dialog. On confirm:
```java
favoriteDao.removeFromFavoritesById(mealId);
```
Room executes: `DELETE FROM favorites WHERE mealId=?`

**Time Display:**
File: `FavoritesAdapter.java:123`

Calculates time difference between now and when favorite was added:
```java
long diff = currentTime - addedTime;
if (diff < minute) return "Just now";
if (diff < hour) return minutes + " minutes ago";
if (diff < day) return hours + " hours ago";
return days + " days ago";
```

### Feature 5: Search in Favorites

File: `FavoritesActivity.java:134`

Filters the favorites list based on search text:
```java
String lowerCaseQuery = query.toLowerCase();
for (FavoriteEntity fav : allFavorites) {
    if (fav.getMealName().toLowerCase().contains(lowerCaseQuery)) {
        filtered.add(fav);
    }
}
```

---

## 7. AI Integration

### Technology: Groq API

**What is Groq?**
Groq provides fast AI inference using the Llama 3.3 70B model. It's an open-source large language model that can answer questions about cooking.

**File**: `app/src/main/java/com/example/ensa_meal/AIChatActivity.java`

### How AI Chat Works

**Step 1: User Enters Question**
User types question and clicks Ask button.

**Step 2: Store User Message in Conversation History**
File: `AIChatActivity.java:90`

Before sending to API, the user's message is stored in `conversationHistory` list. This maintains context across multiple exchanges.

```java
private void storeUserMessage(String message) {
    JSONObject userMsg = new JSONObject();
    userMsg.put("role", "user");
    userMsg.put("content", message);
    conversationHistory.add(userMsg);
}
```

**Step 3: Build API Request with Full Context**
File: `AIChatActivity.java:152`

Creates JSON request with ENTIRE conversation history:
```json
{
  "model": "llama-3.3-70b-versatile",
  "messages": [
    {
      "role": "system",
      "content": "# CONTEXT\nYou are an AI cooking assistant..."
    },
    {
      "role": "user",
      "content": "How do I make pasta?"
    },
    {
      "role": "assistant",
      "content": "To make pasta, boil water..."
    },
    {
      "role": "user",
      "content": "What sauce goes with it?"
    }
  ],
  "temperature": 0.3,
  "top_p": 0.9,
  "max_tokens": 500
}
```

**What these parameters mean:**
- `model`: Which AI model to use (Llama 3.3 70B)
- `messages`: Full conversation history (system + all previous exchanges)
- `temperature`: Creativity level (0.3 = focused and consistent)
- `top_p`: Nucleus sampling (0.9 = high quality responses)
- `max_tokens`: Maximum response length

**Why Conversation Memory Matters:**
The AI remembers previous questions and answers, allowing natural follow-up questions like:
- "What about the sauce?" (knows you're still talking about pasta)
- "How long should I cook it?" (remembers the ingredient)
- "Can I substitute that?" (knows what ingredient you mean)

### COSTAR Prompting Framework

The system prompt uses the COSTAR framework for optimal AI responses:

**C - Context**: AI knows it's in Ensa Meal app, users can search meals and save favorites
**O - Objective**: Help with cooking questions, remember conversation history
**S - Style**: Conversational yet informative, simple language, step-by-step guidance
**T - Tone**: Friendly, helpful, patient - like a knowledgeable friend
**A - Audience**: Home cooks of all skill levels
**R - Response Format**: Concise (2-8 sentences), bullet points for lists, reference previous messages

File: `AIChatActivity.java:178`

**Step 4: Send Request with OkHttp**
File: `AIChatActivity.java:96`

```java
Request request = new Request.Builder()
    .url(GROQ_API_URL)
    .addHeader("Authorization", "Bearer " + BuildConfig.GROQ_API_KEY)
    .addHeader("Content-Type", "application/json")
    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
    .build();

client.newCall(request).enqueue(callback);
```

**Why OkHttp?**
OkHttp handles HTTP requests asynchronously, preventing UI freezing while waiting for response.

**Step 5: Receive and Parse Response**
File: `AIChatActivity.java:110`

Response format:
```json
{
  "choices": [
    {
      "message": {
        "content": "To make pasta, boil water..."
      }
    }
  ]
}
```

Extract answer:
```java
JSONArray choices = response.getJSONArray("choices");
JSONObject message = choices.getJSONObject(0).getJSONObject("message");
String answer = message.getString("content");
```

**Step 6: Store Assistant Response**
File: `AIChatActivity.java:126`

After receiving the AI's response, it's stored in conversation history for future context:

```java
private void storeAssistantResponse(String response) {
    JSONObject assistantMsg = new JSONObject();
    assistantMsg.put("role", "assistant");
    assistantMsg.put("content", response);
    conversationHistory.add(assistantMsg);
}
```

This creates a continuous conversation flow:
- User message → stored
- AI response → stored
- Next user message can reference previous context
- AI can say "For that pasta we discussed..." or "As I mentioned earlier..."

**Step 7: Display in Chat**
File: `AIChatActivity.java:226`

Appends to chat history and scrolls to bottom:
```java
chatHistory.append("You: " + question + "\n\n");
chatHistory.append("AI: " + answer + "\n\n");
chatTextView.setText(chatHistory.toString());
scrollView.fullScroll(View.FOCUS_DOWN);
```

### Why Use AI?

The university included AI lessons, so adding an AI assistant demonstrates:
1. Understanding of API integration
2. Knowledge of modern AI tools
3. Practical application of machine learning
4. Enhanced user experience

---

## 8. Design Choices

### Color Scheme
**File**: `app/src/main/res/values/colors.xml`

Colors chosen for:
- **Readability**: High contrast between text and background
- **Consistency**: Same colors used throughout app
- **Material Design**: Follows Google's design guidelines

### Layout Choices

**ConstraintLayout**
Used in all activities because:
- Flexible positioning of elements
- Better performance than nested layouts
- Responsive to different screen sizes

**RecyclerView vs ListView**
RecyclerView chosen because:
- Better performance (view recycling)
- Built-in animations
- Flexible layout managers

### User Experience Decisions

**Real-time Search**
Updates results as user types - faster than waiting for submit button.

**Star Icon for Favorites**
Universal symbol users recognize immediately.

**Swipe to Delete**
Common mobile pattern - feels natural to users.

**Progress Bars**
Shows user something is happening during network requests.

**Toast Messages**
Quick feedback for actions (added to favorites, deleted, etc.)

**Time Ago Display**
More user-friendly than absolute timestamps.

### Performance Optimizations

**Image Caching with Glide**
- Downloads images once
- Stores in cache
- Reuses cached images
- Saves bandwidth and loading time

**ViewHolder Pattern**
- Prevents redundant `findViewById()` calls
- Improves scrolling performance

**Singleton Database**
- One instance across app
- Prevents memory leaks
- Faster access

**Request Cancellation**
File: `MainActivity.java:289`
```java
protected void onStop() {
    requestQueue.cancelAll(TAG);
}
```
Cancels pending API requests when user leaves screen - saves bandwidth.

---

## Technical Concepts Explained

### What is an Intent?
An Intent is Android's way to navigate between screens or send data. Think of it like an envelope containing:
- **Destination**: Which activity to open
- **Data**: Information to pass (via Bundle)
- **Action**: What to do when arriving

### What is a Bundle?
A Bundle is a container for key-value pairs. Like a dictionary:
```java
bundle.put("MEAL", platObject);  // Store
plat = bundle.get("MEAL");       // Retrieve
```

### What is Serializable?
Serializable converts objects to bytes so they can be:
- Stored in Bundles
- Passed between activities
- Saved to disk

File: `app/src/main/java/com/example/ensa_meal/Plat.java:6`

### What is Volley RequestQueue?
RequestQueue manages network requests:
- Queues requests
- Executes them efficiently
- Caches responses
- Handles retries

### What is Room @Dao?
@Dao (Data Access Object) is an interface Room uses to generate database code:
```java
@Dao
public interface FavoriteDao {
    @Insert
    void addToFavorites(FavoriteEntity favorite);  // Room generates SQL
}
```

### What is @Entity?
@Entity marks a class as a database table:
```java
@Entity(tableName = "favorites")
public class FavoriteEntity {
    @PrimaryKey
    private String mealId;  // Becomes table column
}
```

### What is Callback?
A callback is code that runs when an async operation completes:
```java
volleyRequest.enqueue(new Callback() {
    void onSuccess() { }  // Runs when request succeeds
    void onError() { }    // Runs when request fails
});
```

---

## Common Professor Questions & Answers

### Q: How does the app show meal details after clicking?

**Answer:**
1. User taps meal in MainActivity RecyclerView
2. AdapterMeals detects click via `onItemClick()` interface callback
3. MainActivity receives callback at line 212
4. Creates Intent with Instructions activity as destination
5. Puts Plat object in Bundle (Plat implements Serializable)
6. Calls `startActivity(intent)`
7. Android ActivityManager handles transition:
   - Pauses MainActivity
   - Creates Instructions activity
   - Calls Instructions.onCreate()
8. Instructions retrieves Plat from Intent extras
9. Displays meal data in UI components

**Key files:**
- Click detection: `AdapterMeals.java:70`
- Navigation trigger: `MainActivity.java:212`
- Data reception: `Instructions.java:35`

### Q: Which part handles the API communication?

**Answer:**
Volley library handles all API communication.

**Process:**
1. Create JsonObjectRequest with URL
2. Add success and error listeners (callbacks)
3. Add request to RequestQueue
4. Volley opens HTTP connection on background thread
5. Receives response
6. Parses JSON automatically
7. Calls success callback on main thread with JSONObject
8. MainActivity parses JSONObject and updates UI

**File**: `MainActivity.java:147-171`

**Why Volley?**
- Automatic thread management
- Built-in caching
- Request prioritization
- Simple API

### Q: How is the database managed?

**Answer:**
Room library manages the database.

**Architecture:**
- **AppDatabase**: Singleton that creates and maintains database connection
- **Entities**: Java classes representing tables (@Entity)
- **DAOs**: Interfaces defining database operations (@Dao)

**How it works:**
1. Room uses annotations to understand structure
2. At compile time, Room generates SQL and implementation code
3. At runtime, calling DAO methods executes generated SQL
4. Room handles object-to-row conversion automatically

**Example:**
```java
@Insert
void addToFavorites(FavoriteEntity entity);
```
Room generates:
```sql
INSERT INTO favorites (mealId, mealName, ...) VALUES (?, ?, ...)
```

**Files:**
- Database: `database/AppDatabase.java`
- Entity: `database/FavoriteEntity.java`
- DAO: `database/FavoriteDao.java`

### Q: Why use RecyclerView instead of ScrollView?

**Answer:**
RecyclerView is optimized for long lists:

**ViewHolder Pattern:**
- Only creates views for visible items
- Reuses views as user scrolls
- Calls `onBindViewHolder()` to update recycled views

**Example:**
List of 1000 items:
- ScrollView: Creates 1000 views (slow, high memory)
- RecyclerView: Creates ~10 views (fast, low memory)

**Other benefits:**
- Built-in item animations
- Swipe and drag support
- Different layout managers (grid, linear, staggered)

### Q: How does the rating system work?

**Answer:**
RatingBar is an Android widget that displays stars.

**Properties:**
- `numStars`: How many stars (default 5)
- `rating`: Current value (0-5)
- `stepSize`: Increment (0.5 for half stars, 1.0 for full stars)

**Storage:**
Rating stored as float in database:
```java
private float userRating;
```

**Display:**
```java
ratingBar.setRating(favorite.getUserRating());
```

**Retrieve:**
```java
float rating = ratingBar.getRating();
```

**Files:**
- Input: `dialog_edit_favorite.xml`
- Display: `FavoritesAdapter.java:93`
- Storage: `FavoriteEntity.java:26`

### Q: What happens when user swipes to delete?

**Answer:**
ItemTouchHelper detects swipe gestures.

**Process:**
1. User swipes item left or right
2. ItemTouchHelper detects swipe direction
3. Calls `onSwiped(viewHolder, direction)`
4. Gets position from viewHolder
5. Shows confirmation dialog
6. If confirmed, deletes from database
7. Removes from adapter's list
8. Calls `notifyItemRemoved()` to animate removal

**File**: `FavoritesActivity.java:78-105`

**Why confirmation dialog?**
Prevents accidental deletions - better UX.

### Q: How does the AI integration work?

**Answer:**
Uses Groq API with OkHttp for HTTP requests. Implements conversation memory to maintain context.

**Flow:**
1. User enters question
2. Store user message in `conversationHistory` list
3. Build JSON request with system prompt + ALL previous messages + new question
4. Send POST request to Groq API with auth header
5. Groq processes with Llama 3.3 70B model
6. Returns JSON response with AI answer
7. Parse response and extract answer
8. Store AI response in `conversationHistory`
9. Display in chat interface

**Conversation Memory:**
```java
private List<JSONObject> conversationHistory = new ArrayList<>();
```

Each message (user and AI) is stored as JSONObject with role and content. The entire history is sent with each request, allowing the AI to reference previous exchanges.

**Example:**
- User: "How do I make pasta?"
- AI: "Boil water, add salt, cook pasta 8-10 minutes"
- User: "What sauce goes with it?" ← AI remembers we're talking about pasta

**Why asynchronous?**
Network requests must run on background thread to prevent UI freezing. OkHttp's `enqueue()` handles this automatically.

**File**: `AIChatActivity.java`

### Q: What is COSTAR and why is it used?

**Answer:**
COSTAR is a prompting framework that structures AI system prompts for better responses.

**Framework Breakdown:**
- **C - Context**: AI knows it's in Ensa Meal app, background info
- **O - Objective**: What AI should achieve (help with cooking, remember context)
- **S - Style**: How to write (conversational, simple, step-by-step)
- **T - Tone**: Emotional quality (friendly, helpful, patient)
- **A - Audience**: Who it's for (home cooks, all skill levels)
- **R - Response**: Output structure (concise, bullet points, reference history)

**Why COSTAR?**
- Consistent response quality
- Clear AI behavior expectations
- Better context retention
- More focused and relevant answers
- Professional approach to prompt engineering

**Parameters:**
- `temperature: 0.3` - Low creativity, more focused and consistent
- `top_p: 0.9` - Nucleus sampling for quality responses
- `max_tokens: 500` - Reasonable response length

**File**: `AIChatActivity.java:178`

### Q: What is the purpose of ProgressBar?

**Answer:**
Provides visual feedback during loading.

**States:**
- `VISIBLE`: Shows spinning animation
- `GONE`: Completely hidden (doesn't take space)

**Usage:**
```java
progressBar.setVisibility(View.VISIBLE);  // Show
progressBar.setVisibility(View.GONE);     // Hide
```

**When shown:**
- API requests
- Database operations
- Image loading

**Why important?**
Users need feedback that app is working, not frozen.

---

## Project File Structure

```
app/src/main/
├── java/com/example/ensa_meal/
│   ├── MainActivity.java (Home screen with search and meal list)
│   ├── FavoritesActivity.java (Manage favorites)
│   ├── Instructions.java (Meal details)
│   ├── AIChatActivity.java (AI assistant)
│   ├── Plat.java (Meal data model)
│   ├── AdapterMeals.java (RecyclerView adapter for meals)
│   ├── FavoritesAdapter.java (RecyclerView adapter for favorites)
│   └── database/
│       ├── AppDatabase.java (Database instance)
│       ├── MealEntity.java (Meals table)
│       ├── FavoriteEntity.java (Favorites table)
│       ├── MealDao.java (Meals database operations)
│       └── FavoriteDao.java (Favorites database operations)
├── res/
│   ├── layout/
│   │   ├── activity_main.xml (Main screen layout)
│   │   ├── activity_favorites.xml (Favorites screen layout)
│   │   ├── activity_instructions.xml (Details screen layout)
│   │   ├── activity_ai_chat.xml (AI chat layout)
│   │   ├── model_plat.xml (Meal item layout)
│   │   ├── dialog_add_meal.xml (Add favorite dialog)
│   │   └── dialog_edit_favorite.xml (Edit favorite dialog)
│   ├── values/
│   │   ├── colors.xml (App colors)
│   │   ├── strings.xml (Text strings)
│   │   └── themes.xml (App theme)
│   └── menu/
│       ├── main_menu.xml (Main screen menu)
│       └── favorites_menu.xml (Favorites screen menu)
└── AndroidManifest.xml (App configuration)
```

---

## Summary

Ensa Meal demonstrates:
- **API Integration**: Fetching data from TheMealDB
- **Database Management**: Room for local storage
- **CRUD Operations**: Create, Read, Update, Delete favorites
- **Modern UI**: RecyclerView, Material Design
- **AI Integration**: Groq API for cooking assistance
- **User Experience**: Search, ratings, comments, time tracking
- **Android Concepts**: Activities, Intents, Adapters, Fragments
- **Network Programming**: Volley and OkHttp
- **Asynchronous Operations**: Callbacks and background threads
- **Design Patterns**: Singleton, ViewHolder, Observer

This project showcases practical application of Android development concepts taught in university courses, integrated with modern AI technology.
