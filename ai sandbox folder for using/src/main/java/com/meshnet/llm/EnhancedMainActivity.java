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
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Enhanced Main Activity for LocalLLM-Frontend.
 * Connects to local Ollama/LLM servers via HTTP with improved UX and bug fixes.
 * 
 * @author HerrSchlitz - Enhanced for Sandroid
 */
public class EnhancedMainActivity extends Activity {
    
    // UI Components
    private TextView chatDisplay;
    private EditText ipInput;
    private EditText modelInput;
    private EditText systemPromptInput;
    private EditText temperatureInput;
    private EditText promptInput;
    private Button sendBtn;
    private Button clearBtn;
    private Button connectBtn;
    private Button settingsBtn;
    private Button historyBtn;
    
    // State
    private boolean isSending = false;
    private boolean isServerOnline = false;
    private boolean isKeyboardShown = false;
    private LLMClient llmClient;
    private Handler mainHandler;
    
    // Reconnect logic
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final int RECONNECT_DELAY_MS = 5000;
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 30000;
    
    // Connection status
    private boolean isConnected = false;
    
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
        int promptInputId = getResources().getIdentifier("promptInput", "id", getPackageName());
        int sendBtnId = getResources().getIdentifier("sendBtn", "id", getPackageName());
        int clearBtnId = getResources().getIdentifier("clearBtn", "id", getPackageName());
        int connectBtnId = getResources().getIdentifier("connectBtn", "id", getPackageName());
        int settingsBtnId = getResources().getIdentifier("settingsBtn", "id", getPackageName());
        int historyBtnId = getResources().getIdentifier("historyBtn", "id", getPackageName());
        
        chatDisplay = findViewById(chatDisplayId);
        ipInput = findViewById(ipInputId);
        modelInput = findViewById(modelInputId);
        systemPromptInput = findViewById(systemPromptId);
        temperatureInput = findViewById(temperatureId);
        promptInput = findViewById(promptInputId);
        sendBtn = findViewById(sendBtnId);
        clearBtn = findViewById(clearBtnId);
        connectBtn = findViewById(connectBtnId);
        settingsBtn = findViewById(settingsBtnId);
        historyBtn = findViewById(historyBtnId);
        
        // Initialize UI state
        updateConnectionStatus(false);
        setupSendButton();
        setupClearButton();
        setupConnectButton();
        setupSettingsButton();
        setupHistoryButton();
        setupChatDisplay();
        
        // Show welcome message
        showWelcomeMessage();
        
        // Scroll to bottom on load
        scrollToBottom();
    }
    
    /**
     * Setup send button click listener
     */
    private void setupSendButton() {
        if (sendBtn != null) {
            sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage();
                }
            });
            
            // Visual feedback on press
            sendBtn.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundColor(getColor(android.R.color.holo_blue_dark));
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundColor(getColor(android.R.color.holo_blue_light));
                    return true;
                }
                return false;
            });
        }
    }
    
    /**
     * Setup clear button click listener
     */
    private void setupClearButton() {
        if (clearBtn != null) {
            clearBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearChat();
                }
            });
        }
    }
    
    /**
     * Setup connect button click listener
     */
    private void setupConnectButton() {
        if (connectBtn != null) {
            connectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connectToServer();
                }
            });
        }
    }
    
    /**
     * Setup settings button click listener
     */
    private void setupSettingsButton() {
        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSettingsDialog();
                }
            });
        }
    }
    
    /**
     * Setup history button click listener
     */
    private void setupHistoryButton() {
        if (historyBtn != null) {
            historyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Implement chat history feature
                    showAlertDialog("Chat History", "History feature coming soon! Sessions will be saved locally.");
                }
            });
        }
    }
    
    /**
     * Setup chat display with auto-scroll
     */
    private void setupChatDisplay() {
        if (chatDisplay != null) {
            chatDisplay.setMovementState(TextView.MOVER_STATE_IDLE);
            chatDisplay.setMovementHint(TextView.MOVEMENT_HINT_SCROLL);
        }
    }
    
    /**
     * Show welcome message
     */
    private void showWelcomeMessage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatDisplay.setText("🤖 Welcome to Sandroid LocalLLM-Frontend!\n\n" +
                                   "Connect to your local Ollama or LLM server and start chatting!\n\n" +
                                   "💡 Tips:\n" +
                                   "• Make sure your backend is listening on 0.0.0.0\n" +
                                   "• Use Meshnet for secure local connections\n" +
                                   "• Add protocol (http://) if not included\n" +
                                   "• Default port for Ollama: :11434\n\n" +
                                   "Example: http://192.168.1.100:11434\n\n" +
                                   "─────────────────────────────\n\n");
            }
        });
    }
    
    /**
     * Send message to LLM
     */
    private void sendMessage() {
        // Prevent double clicks
        if (isSending) {
            return;
        }
        
        final String prompt = promptInput.getText().toString().trim();
        String hostUrl = ipInput.getText().toString().trim();
        String model = modelInput.getText().toString().trim();
        String systemPrompt = systemPromptInput.getText().toString().trim();
        double temperature = 0.7;
        
        // Validate inputs
        if (prompt.isEmpty()) {
            showToast("Please enter a message");
            return;
        }
        
        if (hostUrl.isEmpty()) {
            showToast("Please enter server IP/URL");
            showToast("💡 Tip: Add http:// if not included");
            return;
        }
        
        // Ensure URL has protocol
        if (!hostUrl.startsWith("http://") && !hostUrl.startsWith("https://")) {
            hostUrl = "http://" + hostUrl;
            ipInput.setText(hostUrl);
        }
        
        // Get model or use default
        if (model.isEmpty()) {
            model = "llama2"; // Default model
        }
        
        // Show sending indicator
        isSending = true;
        updateConnectionStatus(true);
        promptInput.setEnabled(false);
        sendBtn.setEnabled(false);
        
        final String finalHostUrl = hostUrl;
        final String finalModel = model;
        final String finalSystemPrompt = systemPrompt;
        final String finalTemperature = String.valueOf(temperature);
        
        // Append user message
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatDisplay.append("\n🧑 You: " + prompt + "\n");
                scrollToBottom();
            }
        });
        
        // Send in background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendToLLM(finalHostUrl, finalModel, finalSystemPrompt, finalTemperature);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    /**
     * Send to LLM server
     */
    private void sendToLLM(String host, String model, String systemPrompt, double temperature) {
        try {
            // Clean URL and add API endpoint
            String apiUrl = host.replaceAll("/+$", "") + "/v1/chat/completions";
            URL url = new URL(apiUrl);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            
            // Construct JSON payload
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("model", model);
            jsonPayload.put("messages", new JSONArray());
            
            // Add system prompt if provided
            if (!systemPrompt.isEmpty()) {
                JSONObject systemMsg = new JSONObject();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                jsonPayload.getJSONArray("messages").put(systemMsg);
            }
            
            // Add user message
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", promptInput.getText().toString().trim());
            jsonPayload.getJSONArray("messages").put(userMsg);
            
            // Set temperature
            jsonPayload.put("temperature", temperature);
            jsonPayload.put("stream", false);
            
            // Send request
            OutputStream os = conn.getOutputStream();
            os.write(jsonPayload.toString().getBytes("UTF-8"));
            os.flush();
            os.close();
            
            // Get response
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line;
                StringBuilder response = new StringBuilder();
                
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                
                // Parse response
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                String llmReply = message.getString("content");
                
                // Display response
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatDisplay.append("\n🤖 AI: " + llmReply + "\n");
                        scrollToBottom();
                        updateConnectionStatus(true);
                    }
                });
                
            } else {
                // Handle error response
                try {
                    JSONObject errorJson = new JSONObject(conn.getErrorStream());
                    String errorBody = errorJson.getString("message");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatDisplay.append("\n❌ Error: " + errorBody + "\n");
                            updateConnectionStatus(false);
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatDisplay.append("\n❌ Error Code: " + responseCode + "\n");
                            updateConnectionStatus(false);
                        }
                    });
                }
            }
            
        } catch (UnknownHostException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatDisplay.append("\n❌ Connection Failed: Could not resolve host\n");
                    showToast("Please check your server IP/URL");
                    updateConnectionStatus(false);
                }
            });
        } catch (IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatDisplay.append("\n❌ Connection Failed: " + e.getMessage() + "\n");
                    showToast("Network issue or server offline");
                    updateConnectionStatus(false);
                    attemptReconnect();
                }
            });
        } catch (Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatDisplay.append("\n❌ Error: " + e.getMessage() + "\n");
                    e.printStackTrace();
                    updateConnectionStatus(false);
                }
            });
        } finally {
            // Reset state
            isSending = false;
            promptInput.setEnabled(true);
            sendBtn.setEnabled(true);
            scrollToBottom();
        }
    }
    
    /**
     * Connect to server test
     */
    private void connectToServer() {
        String hostUrl = ipInput.getText().toString().trim();
        
        if (hostUrl.isEmpty()) {
            showToast("Please enter server URL");
            return;
        }
        
        // Ensure protocol
        if (!hostUrl.startsWith("http://") && !hostUrl.startsWith("https://")) {
            hostUrl = "http://" + hostUrl;
            ipInput.setText(hostUrl);
        }
        
        // Test connection
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(hostUrl + "/v1/models");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    
                    int responseCode = conn.getResponseCode();
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (responseCode == 200) {
                                isServerOnline = true;
                                isConnected = true;
                                updateConnectionStatus(true);
                                showToast("✅ Connected to server!");
                                chatDisplay.append("\n✅ Server: " + hostUrl + "\n");
                                chatDisplay.append("   Status: Online\n");
                            } else {
                                isServerOnline = false;
                                isConnected = false;
                                updateConnectionStatus(false);
                                showToast("⚠️ Server responded with code: " + responseCode);
                            }
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            isServerOnline = false;
                            isConnected = false;
                            updateConnectionStatus(false);
                            showToast("⚠️ Could not reach server: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }
    
    /**
     * Attempt to reconnect after disconnect
     */
    private void attemptReconnect() {
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts++;
            showToast("Reconnecting... (" + reconnectAttempts + "/" + MAX_RECONNECT_ATTEMPTS + ")");
            
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    connectToServer();
                }
            }, RECONNECT_DELAY_MS);
        }
    }
    
    /**
     * Clear chat history
     */
    private void clearChat() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatDisplay.setText("🤖 Welcome to Sandroid LocalLLM-Frontend!\n\n" +
                                   "Connect to your local Ollama or LLM server and start chatting!\n\n" +
                                   "💡 Tips:\n" +
                                   "• Make sure your backend is listening on 0.0.0.0\n" +
                                   "• Use Meshnet for secure local connections\n" +
                                   "• Add protocol (http://) if not included\n" +
                                   "• Default port for Ollama: :11434\n\n" +
                                   "─────────────────────────────\n\n");
            }
        });
    }
    
    /**
     * Update connection status UI
     */
    private void updateConnectionStatus(boolean isConnected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    isConnected = true;
                    isServerOnline = true;
                    chatDisplay.append("\n[Status: 🟢 Online]\n");
                } else {
                    isConnected = false;
                    isServerOnline = false;
                    chatDisplay.append("\n[Status: 🔴 Offline]\n");
                }
            }
        });
    }
    
    /**
     * Show toast message
     */
    private void showToast(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EnhancedMainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Show settings dialog
     */
    private void showSettingsDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(EnhancedMainActivity.this)
                    .setTitle("Settings")
                    .setMessage("Settings menu coming soon!\n\n" +
                               "• Dark/Light theme toggle\n" +
                               "• Default model selection\n" +
                               "• Temperature slider\n" +
                               "• System prompt template\n" +
                               "• Chat history backup")
                    .setPositiveButton("OK", null)
                    .show();
            }
        });
    }
    
    /**
     * Scroll to bottom of chat display
     */
    private void scrollToBottom() {
        if (chatDisplay != null) {
            chatDisplay.post(new Runnable() {
                @Override
                public void run() {
                    chatDisplay.scrollTo(0, chatDisplay.getLineCount() - 1);
                }
            });
        }
    }
    
    /**
     * Handle keyboard visibility
     */
    private void handleKeyboardVisibility() {
        if (promptInput != null) {
            promptInput.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Auto-scroll as user types
                    if (count > 0) {
                        scrollToBottom();
                    }
                }
                
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void afterTextChanged(CharSequence s) {}
            });
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        llmClient = null;
    }
}
