package com.example.ensa_meal;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private ScrollView scrollView;
    private OkHttpClient client;
    private StringBuilder chatHistory;
    private List<JSONObject> conversationHistory;
    private FavoriteDao favoriteDao;
    private List<FavoriteEntity> userFavorites;
    private StringBuilder currentStreamingResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupListeners();

        // Configure OkHttp with longer timeout for streaming
        client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        chatHistory = new StringBuilder();
        conversationHistory = new ArrayList<>();
        currentStreamingResponse = new StringBuilder();

        AppDatabase database = AppDatabase.getInstance(this);
        favoriteDao = database.favoriteDao();
        loadFavorites();
    }

    private void initializeViews() {
        questionInput = findViewById(R.id.question_input);
        sendButton = findViewById(R.id.send_button);
        chatTextView = findViewById(R.id.chat_text_view);
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
            appendToChat("Error: API key not configured. Add GROQ_API_KEY to local.properties<br><br>");
            return;
        }

        sendButton.setEnabled(false);

        appendToChat("<b>You:</b> " + question + "<br><br>");
        storeUserMessage(question);
        questionInput.setText("");

        // Start Chef response on new line
        appendToChat("<b>Chef:</b><br>");
        currentStreamingResponse = new StringBuilder();

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
                        sendButton.setEnabled(true);
                        appendToChat("Could not connect. Check your internet.<br><br>");
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> {
                            sendButton.setEnabled(true);
                            if (response.code() == 401) {
                                appendToChat("Invalid API key. Check local.properties<br><br>");
                            } else {
                                appendToChat("Something went wrong. Try again.<br><br>");
                            }
                        });
                        return;
                    }

                    // Handle streaming response
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body().byteStream()))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();

                                if (data.equals("[DONE]")) {
                                    break;
                                }

                                try {
                                    JSONObject chunk = new JSONObject(data);
                                    JSONArray choices = chunk.getJSONArray("choices");

                                    if (choices.length() > 0) {
                                        JSONObject delta = choices.getJSONObject(0).optJSONObject("delta");

                                        if (delta != null && delta.has("content")) {
                                            String content = delta.getString("content");
                                            currentStreamingResponse.append(content);

                                            // Update UI with streamed content
                                            runOnUiThread(() -> updateStreamingText(content));
                                        }
                                    }
                                } catch (JSONException e) {
                                    // Skip malformed chunks
                                }
                            }
                        }

                        // Finalize the response
                        runOnUiThread(() -> {
                            sendButton.setEnabled(true);
                            appendToChat("<br><br>");
                            storeAssistantResponse(currentStreamingResponse.toString());
                        });

                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            sendButton.setEnabled(true);
                            appendToChat("Error reading response<br><br>");
                        });
                    }
                }
            });
        } catch (JSONException e) {
            sendButton.setEnabled(true);
            appendToChat("Error creating request<br><br>");
        }
    }

    private void updateStreamingText(String newContent) {
        // Convert \n to <br> for proper HTML rendering
        String htmlContent = newContent.replace("\n", "<br>");
        chatHistory.append(htmlContent);
        chatTextView.setText(Html.fromHtml(chatHistory.toString(), Html.FROM_HTML_MODE_COMPACT));
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private JSONObject buildRequestBody(String question) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("stream", true); // Enable streaming

        JSONArray messages = new JSONArray();

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", buildSimplePrompt());
        messages.put(systemMessage);

        for (JSONObject msg : conversationHistory) {
            messages.put(msg);
        }

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.4);
        requestBody.put("max_tokens", 600);

        return requestBody;
    }

    private String buildSimplePrompt() {
        String favorites = buildFavoritesContext();

        // COSTAR Framework Prompt (Optimized)
        return "# CONTEXT\n" +
                "You are Chef, a cooking assistant in Ensa_Meal app. You help users decide meals and provide recipes.\n" +
                "USER'S FAVORITES: " + favorites + "\n\n" +

                "# OBJECTIVE\n" +
                "Give decisive meal recommendations and clear recipes. Max 1-2 questions before deciding. Use favorites when relevant. Match user's language.\n\n" +

                "# STYLE & TONE\n" +
                "Friendly, confident friend. Say 'Make this' not 'you could try'. No jargon. Be decisive.\n\n" +

                "# AUDIENCE\n" +
                "Home cooks (any skill). Languages: English/French/Arabic/Darija (use Latin letters for Darija). Assume basic ingredients available.\n\n" +

                "# RESPONSE FORMAT\n\n" +

                "FORMATTING:\n" +
                "- Line breaks between ideas (never one paragraph)\n" +
                "- Numbered lists for steps\n" +
                "- Bullets for ingredients\n" +
                "- Keep lines short\n\n" +

                "PATTERNS:\n\n" +

                "For 'what to cook?':\n" +
                "- Have favorites? Suggest ONE\n" +
                "- No favorites: ask 'Something in mind, or should I suggest?'\n" +
                "- If suggest: 'What sounds good: light (salad), hearty (tajine), quick (pasta), comforting (soup)?'\n" +
                "- Max 1-2 questions, then DECIDE\n\n" +

                "For suggestions: meal name + why it fits + time + 'Want recipe?'\n\n" +

                "For recipes:\n" +
                "Ingredients: (bullets)\n" +
                "Steps: (numbered)\n" +
                "Total time:\n\n" +

                "# EXAMPLE\n\n" +
                "User: What should I cook?\n" +
                "Chef: Something in mind, or want me to suggest?\n\n" +
                "User: Suggest\n" +
                "Chef: What sounds good:\n" +
                "- Light (salad)\n" +
                "- Hearty (tajine)\n" +
                "- Quick (pasta)\n" +
                "- Comforting (soup)\n\n" +
                "User: Quick\n" +
                "Chef: Make Garlic Shrimp Pasta.\n\n" +
                "15 minutes, tastes restaurant-quality.\n\n" +
                "Want the recipe?";
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
        chatTextView.setText(Html.fromHtml(chatHistory.toString(), Html.FROM_HTML_MODE_COMPACT));
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
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
            return "None saved yet";
        }

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < userFavorites.size(); i++) {
            FavoriteEntity fav = userFavorites.get(i);
            context.append("- ").append(fav.getMealName());
            if (fav.getUserComment() != null && !fav.getUserComment().isEmpty()) {
                context.append(" (").append(fav.getUserComment()).append(")");
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
