### Module 1: Initial Meal Display and Search

*Goal: We'll show how the app displays an initial list of meals when it starts and how users can search for different meals.*

---

#### **Part 1: What We're About to See (The Simple Idea)**

🗣️ **Talking Point:** When you open the app, it immediately shows you a list of popular meals. You can then easily search for any meal you like using the search bar.

#### **Part 2: Live Demonstration (In the App)**

➡️ **Action:** **Launch the app.**

🗣️ **Talking Point:** Notice how a list of chicken meals appears right away. This is the app's starting point. Now, let's try searching for something else.

➡️ **Action:** **Tap on the search bar and type "pasta".**

🗣️ **Talking Point:** As I type, the list of meals updates instantly, showing all the pasta dishes available. This makes finding meals very quick and easy.

#### **Part 3: How It Works (In the Code)**

💻 **Navigate to:** `app/src/main/java/com/example/ensa_meal/MainActivity.java`

🧠 **Code Walkthrough:**

*   In the `onCreate()` method, after setting up the screen, the app immediately calls `searchMeals("chicken")`. // *This is how the initial list of chicken meals is shown.*
*   The `searchMeals()` function is where the app talks to the internet. It uses a library called **Volley** to send a request to a meal database (TheMealDB API). // *This happens in the background so the app stays smooth.*
*   When you type in the search bar, the `setOnQueryTextListener` in `setupListeners()` notices your input.
*   For every letter you type, the `onQueryTextChange()` method calls `searchMeals()` again with your new search term. // *This is why the list updates instantly as you type.*
*   When the app gets the meal data back from the internet, the `handleApiResponse()` method takes that raw data and turns it into a list of `Plat` objects.
*   Finally, `adapterMeals.notifyDataSetChanged()` tells the list on the screen to update itself with the new meals.

💡 **Why We Did It This Way (The Simple Reason):** We fetch data directly from the internet on launch and search to always provide the most up-to-date meal information. The instant search updates make the app feel very responsive and user-friendly.

---

### Module 2: Viewing Meal Details (Instructions)

*Goal: We'll demonstrate how to view detailed instructions for any meal, including its ingredients and preparation steps.*

---

#### **Part 1: What We're About to See (The Simple Idea)**

🗣️ **Talking Point:** When you see a meal you like, you can tap on it to get all the details: a bigger picture, a full description, and step-by-step instructions on how to make it.

#### **Part 2: Live Demonstration (In the App)**

➡️ **Action:** **Tap on any meal in the list.**

🗣️ **Talking Point:** You'll see a new screen pop up with a large image of the meal, its name, and a detailed description. This is where you find everything you need to prepare the dish.

#### **Part 3: How It Works (In the Code)**

💻 **Navigate to:** `app/src/main/java/com/example/ensa_meal/MainActivity.java`

🧠 **Code Walkthrough:**

*   In `MainActivity.java`, when you tap on a meal, the `onItemClick()` method is triggered.
*   This method creates an `Intent` to start a new screen, which is our `Instructions.java` activity.
*   It then puts all the meal's information (like its ID, name, image, and instructions) into a `Bundle` and attaches it to the `Intent`. // *Think of a Bundle as a small package for sending data between screens.*

💻 **Navigate to:** `app/src/main/java/com/example/ensa_meal/Instructions.java`

🧠 **Code Walkthrough:**

*   In the `onCreate()` method of `Instructions.java`, the app receives the `Bundle` we sent from `MainActivity`.
*   It then takes the meal information out of the `Bundle` and uses it to fill in all the text and image views on the screen. // *This is how the meal's picture, name, and instructions get displayed.*
*   The `Glide` library is used here to efficiently load the meal image from the internet and display it.

💡 **Why We Did It This Way (The Simple Reason):** By passing all the meal details directly to the `Instructions` screen, we avoid making another trip to the internet, making the experience fast and smooth for the user.

---

### Module 3: Managing Favorites - Adding and Indicating Favorites

*Goal: We'll demonstrate how to add meals to your favorites list and how the app visually tells you which meals you've already saved.*

---

#### **Part 1: What We're About to See (The Simple Idea)**

🗣️ **Talking Point:** You can easily mark any meal as a favorite. The app will then show a special star icon next to it, so you always know which ones you've saved. If you try to add it again, it won't create a duplicate or erase your notes.

#### **Part 2: Live Demonstration (In the App)**

➡️ **Action:** **Scroll through the meal list and tap the "star" icon next to a meal that is currently "star off".**

🗣️ **Talking Point:** See how the star icon immediately changes to "star on"? This tells you that the meal has been added to your favorites. A small message also confirms this.

➡️ **Action:** **Tap the "star" icon next to the same meal again.**

🗣️ **Talking Point:** Now the star icon changes back to "star off", indicating the meal has been removed from your favorites. The app confirms this with another message. This way, you can easily toggle meals in and out of your favorites.

#### **Part 3: How It Works (In the Code)**

💻 **Navigate to:** `app/src/main/java/com/example/ensa_meal/MainActivity.java`

🧠 **Code Walkthrough:**

*   In `MainActivity.java`, when the app starts or resumes, the `loadFavoriteMealIds()` method is called. // *This fetches all the IDs of meals you've marked as favorite from the phone's local database.*
*   These favorite IDs are then passed to our `AdapterMeals` which is responsible for showing each meal item on the screen.
*   When you tap the star icon, the `onToggleFavoriteClick()` method is activated.
*   Inside `onToggleFavoriteClick()`, the app first checks if the meal is already a favorite using `favoriteDao.isFavorite(mealId)`.
*   If it *is* a favorite, it calls `favoriteDao.removeFromFavoritesById(mealId)` to delete it from the database and removes its ID from our `favoriteMealIds` list.
*   If it *is not* a favorite, it creates a `FavoriteEntity` (which is like a blueprint for a favorite meal) and calls `favoriteDao.addToFavorites(favorite)` to save it to the database. It also adds its ID to our `favoriteMealIds` list.
*   Finally, `adapterMeals.setFavoriteMealIds(favoriteMealIds)` updates the list on the screen, making sure the star icons are correct.

💻 **Navigate to:** `app/src/main/java/com/example/ensa_meal/AdapterMeals.java`

🧠 **Code Walkthrough:**

*   In the `onBindViewHolder()` method, for each meal shown on the screen, the adapter checks if its ID is in the `favoriteMealIds` list it received.
*   Based on this check, it sets the `favoriteIcon` to either a "star on" (`android.R.drawable.btn_star_big_on`) or "star off" (`android.R.drawable.btn_star_big_off`) image. // *This is how you see which meals are your favorites at a glance.*
*   The `favoriteIcon.setOnClickListener()` is set up to call `onToggleFavoriteClick()` back in `MainActivity` when you tap the star.

💻 **Navigate to:** `app/src/main/java/com/example/ensa_meal/database/FavoriteDao.java`

🧠 **Code Walkthrough:**

*   The `FavoriteDao` acts as the bridge between our app and the local database.
*   `isFavorite(String mealId)`: This function quickly checks if a meal's ID exists in our favorites table.
*   `addToFavorites(FavoriteEntity favorite)`: This saves a new favorite meal. If you try to add the same meal twice, it just updates the existing one, so your comments and ratings are safe!
*   `removeFromFavoritesById(String mealId)`: This removes a favorite meal using its ID.
*   `getFavoriteMealIds()`: This simply gives us a list of all the IDs of your saved favorite meals.

💡 **Why We Did It This Way (The Simple Reason):** By showing a clear star icon and allowing easy toggling, we make it super intuitive for users to manage their favorites. The database handles duplicates automatically, so user data like comments and ratings are never lost.

---

### Module 4: Managing Favorites - Editing and Deleting Favorites

*Goal: We'll demonstrate how to view your saved favorite meals, edit their comments and ratings, and remove them from your favorites list.*

---

#### **Part 1: What We're About to See (The Simple Idea)**

🗣️ **Talking Point:** All your favorite meals are kept in a special list. From there, you can change your personal notes and ratings for each meal, or remove them entirely if you no longer want them in your favorites.

#### **Part 2: Live Demonstration (In the App)**

➡️ **Action:** **Tap the "My Favorites" button on the main screen.**

🗣️ **Talking Point:** This takes us to your personal list of favorite meals. Each meal shows its picture, name, your comment, and your rating.

➡️ **Action:** **Tap the "edit" icon (pencil) next to any favorite meal.**

🗣️ **Talking Point:** A small window appears, letting you change your comment and adjust the rating. After saving, you'll see your updates immediately on the list.

➡️ **Action:** **Tap the "delete" icon (trash can) next to any favorite meal.**

🗣️ **Talking Point:** The app asks for confirmation to make sure you really want to remove it. If you confirm, the meal disappears from your favorites list. You can also swipe left or right on a favorite meal to delete it quickly.

#### **Part 3: How It Works (In the Code)**

💻 **Navigate to:** `app/src/main/java/com/example/ensa_meal/FavoritesActivity.java`

🧠 **Code Walkthrough:**

*   When `FavoritesActivity` starts, the `loadFavorites()` method is called. // *This fetches all your saved favorite meals from the database.*
*   The `setupRecyclerView()` method then prepares the list to display these favorites using the `FavoritesAdapter`.
*   When you tap the "edit" icon, the `onEditFavorite()` method is called.
*   This method shows an `AlertDialog` (the small window) where you can type a new comment and set a new rating.
*   When you tap "Save", it calls `favoriteDao.updateComment()` and `favoriteDao.updateRating()` to save your changes to the database.
*   When you tap the "delete" icon, the `onDeleteFavorite()` method is called.
*   This method first shows a confirmation dialog. If you confirm, it calls the private `deleteFavorite()` method.
*   The `deleteFavorite()` method then uses `favoriteDao.removeFromFavorites()` to remove the meal from the database and updates the list on the screen.
*   The `setupSwipeToDelete()` method sets up the ability to swipe left or right on a meal to delete it, which also calls `deleteFavorite()`.

💻 **Navigate to:** `app/src/main/java/com/example/ensa_meal/FavoritesAdapter.java`

🧠 **Code Walkthrough:**

*   The `FavoritesAdapter` is responsible for taking each `FavoriteEntity` (your saved meal) and displaying it correctly in the list.
*   It sets the meal's name, your comment, rating, and image.
*   It also sets up the click listeners for the "edit" and "delete" buttons, which then tell the `FavoritesActivity` what action to perform.

💻 **Navigate to:** `app/src/main/java/com/example/ensa_meal/database/FavoriteDao.java`

🧠 **Code Walkthrough:**

*   `getAllFavorites()`: This function retrieves all your favorite meals from the database, ordered by when you added them.
*   `updateComment(String mealId, String comment)`: This updates the comment for a specific favorite meal.
*   `updateRating(String mealId, float rating)`: This updates the rating for a specific favorite meal.
*   `removeFromFavorites(FavoriteEntity favorite)`: This removes a favorite meal from the database.

💡 **Why We Did It This Way (The Simple Reason):** Providing a dedicated favorites screen with clear edit and delete options, along with the convenient swipe-to-delete gesture, gives users full control over their saved meals and personal notes.
