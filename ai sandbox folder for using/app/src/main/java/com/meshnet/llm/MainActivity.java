package com.meshnet.llm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<Message> messages;
    private EditText input;
    private Button sendButton;
    private ImageButton voiceBtn, fileBtn, settingsBtn;
    private SwipeRefreshLayout swipeRefresh;
    private ConnectionStatusView connectionStatusView;
    private SharedPreferences prefs;
    private ChatManager chatManager;
    private LLMClient client;
    private ThemeManager themeManager;
    private MarkdownRenderer markdownRenderer;
    private boolean isLoading = false;
    private int currentChatId = 0;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        input = findViewById(R.id.input);
        sendButton = findViewById(R.id.sendButton);
        voiceBtn = findViewById(R.id.voiceBtn);
        fileBtn = findViewById(R.id.fileBtn);
        settingsBtn = findViewById(R.id.settingsBtn);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        connectionStatusView = findViewById(R.id.connectionStatusView);

        // Initialize managers
        prefs = getSharedPreferences("sandroid_prefs", Context.MODE_PRIVATE);
        chatManager = new ChatManager(this);
        client = new LLMClient(this);
        themeManager = new ThemeManager(this);
        markdownRenderer = new MarkdownRenderer(this);

        // Apply theme
        applyTheme();

        // Load saved settings
        loadSettings();

        // Load current chat messages
        loadChatMessages();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup listeners
        setupListeners();

        // Setup swipe refresh
        setupSwipeRefresh();

        // Auto-scroll to bottom
        scrollToBottom();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new ChatAdapter(new ArrayList<>(), markdownRenderer);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
        voiceBtn.setOnClickListener(v -> startVoiceInput());
        fileBtn.setOnClickListener(v -> startFileUpload());
        settingsBtn.setOnClickListener(v -> openSettings());

        // Auto-hide keyboard when sending
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && !isLoading) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            loadChatMessages();
            swipeRefresh.setRefreshing(false);
        });
    }

    private void loadChatMessages() {
        messages = chatManager.getMessages(currentChatId);
        adapter.updateMessages(messages);
    }

    private void sendMessage() {
        String text = input.getText().toString().trim();
        if (text.isEmpty() || isLoading) return;

        // Add user message
        Message userMessage = new Message(MessageType.USER, text);
        messages.add(userMessage);
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();

        // Clear input
        input.setText("");
        input.requestFocus();

        // Show loading
        isLoading = true;
        Message loadingMessage = new Message(MessageType.LOADING, "Thinking...");
        messages.add(loadingMessage);
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();

        // Send to LLM
        client.sendMessage(text, new LLMClient.Callback() {
            @Override
            public void onStreamLine(String line) {
                mainHandler.post(() -> {
                    loadingMessage.setText(loadingMessage.getText() + "\n" + line);
                    adapter.notifyItemChanged(messages.size() - 1);
                });
            }

            @Override
            public void onComplete(String response) {
                mainHandler.post(() -> {
                    // Replace loading message with final response
                    messages.remove(messages.size() - 1);
                    Message aiMessage = new Message(MessageType.AI, response);
                    messages.add(aiMessage);
                    adapter.notifyItemRemoved(messages.size() - 1);
                    adapter.notifyItemInserted(messages.size() - 1);
                    scrollToBottom();
                    isLoading = false;
                    chatManager.saveMessages(currentChatId, messages);
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    loadingMessage.setText(error);
                    adapter.notifyItemChanged(messages.size() - 1);
                    isLoading = false;
                    chatManager.saveMessages(currentChatId, messages);
                });
            }
        });
    }

    private void scrollToBottom() {
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    private void applyTheme() {
        if (themeManager.isDarkMode()) {
            recyclerView.setBackgroundColor(Color.parseColor("#1e1e1e"));
            connectionStatusView.setTextColor(Color.parseColor("#ffffff"));
        } else {
            recyclerView.setBackgroundColor(Color.parseColor("#ffffff"));
            connectionStatusView.setTextColor(Color.parseColor("#000000"));
        }
    }

    private void loadSettings() {
        String ip = prefs.getString("ip", "127.0.0.1");
        String model = prefs.getString("model", "mistral");
        int temperature = prefs.getInt("temperature", 0.7);
        boolean stream = prefs.getBoolean("stream", true);
        boolean darkMode = prefs.getBoolean("darkMode", true);

        input.setHint("Ask anything...");
    }

    private void startVoiceInput() {
        Intent intent = new Intent(this, VoiceInputActivity.class);
        startActivityForResult(intent, 1);
    }

    private void startFileUpload() {
        Intent intent = new Intent(this, FileUploadActivity.class);
        startActivityForResult(intent, 2);
    }

    private void openSettings() {
        // Settings will be implemented
        Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String text = data.getStringExtra("voice_text");
            if (text != null) {
                input.setText(text);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
    }
}
