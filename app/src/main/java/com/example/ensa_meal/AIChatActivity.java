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
            sendButton.setEnabled(true);
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
