package com.meshnet.llm;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Main activity for LocalLLM-Frontend.
 * Connects to local Ollama/LLM servers via HTTP.
 * 
 * @author HerrSchlitz
 */
public class MainActivity extends Activity {
    
    // UI Components
    private TextView chatDisplay;
    private EditText ipInput;
    private EditText modelInput;
    private EditText systemPromptInput;
    private EditText temperatureInput;
    private Button sendBtn;
    private Button clearBtn;
    private Button connectBtn;
    
    // State
    private boolean isSending = false;
    private boolean isServerOnline = false;
    private LLMClient llmClient;
    private Handler mainHandler;
    
    // Reconnect logic
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final int RECONNECT_DELAY_MS = 3000;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize LLM Client
        llmClient = new LLMClient();
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize UI components
        int chatDisplayId = getResources().getIdentifier("chatDisplay", "id", getPackageName());
        int ipInputId = getResources().getIdentifier("ipInput", "id", getPackageName());
        int modelInputId = getResources().getIdentifier("modelInput", "id", getPackageName());
        int systemPromptId = getResources().getIdentifier("systemPrompt", "id", getPackageName());
        int temperatureId = getResources().getIdentifier("temperature", "id", getPackageName());
        int sendBtnId = getResources().getIdentifier("sendBtn", "id", getPackageName());
        int clearBtnId = getResources().getIdentifier("clearBtn", "id", getPackageName());
        int connectBtnId = getResources().getIdentifier("connectBtn", "id", getPackageName());
        
        chatDisplay = findViewById(chatDisplayId);
        ipInput = findViewById(ipInputId);
        modelInput = findViewById(modelInputId);
        systemPromptInput = findViewById(systemPromptId);
        temperatureInput = findViewById(temperatureId);
        sendBtn = findViewById(sendBtnId);
        clearBtn = findViewById(clearBtnId);
        connectBtn = findViewById(connectBtnId);
        
        // Set default values
        modelInput.setText("llama2");
        temperatureInput.setText("0.7");
        systemPromptInput.setText("You are a helpful AI assistant.");
        
        // Initialize LLM Client with default model
        llmClient.setModel("llama2");
        llmClient.setTemperature(0.7);
        
        // Setup Connect Button
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testConnection();
            }
        });
        
        // Setup Send Button
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToLLM();
            }
        });
        
        // Setup Clear Button
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearChat();
            }
        });
        
        // Initialize chat display
        chatDisplay.setText("Welcome to LocalLLM-Frontend!\n\n" +
                           "Enter your backend URL (e.g., http://192.168.1.100:11434)\n" +
                           "and start chatting with your local LLM.\n\n" +
                           "💡 Tip: Make sure your backend is listening on 0.0.0.0\n" +
                           "and you're on the same network or using Meshnet.");
        
        // Auto-detect connection on start if URL is set
        if (ipInput.getText() != null && !ipInput.getText().toString().isEmpty()) {
            String url = ipInput.getText().toString().trim();
            if (!url.isEmpty() && (url.startsWith("http://") || url.startsWith("https://"))) {
                testConnection();
            }
        }
    }
    
    /**
     * Test backend server connection
     */
    private void testConnection() {
        String url = ipInput.getText().toString().trim();
        
        if (url.isEmpty()) {
            showAlertDialog("Connect to Server", "Please enter a backend URL first.");
            return;
        }
        
        // Ensure protocol prefix
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
            ipInput.setText(url);
        }
        
        // Test connection with a simple health check
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean connected = llmClient.testConnection(url);
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connected) {
                            isServerOnline = true;
                            chatDisplay.append("\n\n🟢 Server Connected Successfully!\n");
                            chatDisplay.append("You can now start chatting.\n");
                            Toast.makeText(MainActivity.this, "Connected!", Toast.LENGTH_SHORT).show();
                        } else {
                            isServerOnline = false;
                            chatDisplay.append("\n\n🔴 Connection Failed!\n");
                            showAlertDialog("Connection Error", "Could not connect to backend. Check:\n" +
                                        "- Backend is running\n" +
                                        "- URL is correct (e.g., http://192.168.1.100:11434)\n" +
                                        "- Firewall settings\n" +
                                        "- Network connection");
                        }
                    }
                });
            }
        }).start();
    }
    
    /**
     * Send prompt to LLM
     */
    private void sendToLLM() {
        String url = ipInput.getText().toString().trim();
        String prompt = systemPromptInput.getText().toString().trim();
        
        // Combine system prompt and user input
        String fullPrompt = prompt;
        
        if (fullPrompt.isEmpty()) {
            showAlertDialog("Send Message", "Please enter a prompt to send.");
            return;
        }
        
        if (isSending) {
            showAlertDialog("Already Sending", "Please wait for the current response to finish.");
            return;
        }
        
        if (!isServerOnline) {
            showAlertDialog("Send Message", "Server not connected. Test connection first.");
            return;
        }
        
        // Update UI
        isSending = true;
        chatDisplay.append("\n---\nYou: " + fullPrompt);
        
        // Send to backend
        llmClient.sendChat(fullPrompt, new LLMClient.OnResponseListener() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatDisplay.append("\n\nAI: " + response);
                        isSending = false;
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatDisplay.append("\n\n⚠️ " + error);
                        isSending = false;
                        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                            reconnectAttempts++;
                            showAlertDialog("Connection Lost", "Server may be busy. Retrying in " + (reconnectAttempts * 3) + " seconds...");
                            // Auto-reconnect
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    testConnection();
                                }
                            }, reconnectAttempts * 3000);
                        } else {
                            showAlertDialog("Max Retries", "Server seems down. Try refreshing or restarting the backend.");
                        }
                    }
                });
            }
        });
        
        // Scroll to bottom
        chatDisplay.post(new Runnable() {
            @Override
            public void run() {
                chatDisplay.scrollToBottom();
            }
        });
    }
    
    /**
     * Clear chat history
     */
    private void clearChat() {
        chatDisplay.setText("Welcome to LocalLLM-Frontend!\n\n" +
                           "Enter your backend URL (e.g., http://192.168.1.100:11434)\n" +
                           "and start chatting with your local LLM.\n\n" +
                           "💡 Tip: Make sure your backend is listening on 0.0.0.0\n" +
                           "and you're on the same network or using Meshnet.");
    }
    
    /**
     * Show alert dialog
     */
    private void showAlertDialog(String title, String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        llmClient = null;
    }
}
