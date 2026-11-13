package com.meshnet.llm;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced LLM Client with streaming, offline detection, memory management, and notifications
 */
public class EnhancedLLMClient {
    private static final String TAG = "EnhancedLLMClient";
    private static final int DEFAULT_TIMEOUT = 30000; // 30 seconds
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long RECONNECT_DELAY_MS = 2000;
    
    private Context context;
    private String serverUrl;
    private String model;
    private String systemPrompt;
    private float temperature;
    private float topP;
    private int topK;
    private int repeatPenalty;
    private boolean useStream;
    private boolean isRunning;
    private boolean isOnline;
    
    // Memory management
    private long lastMemoryMB = 0;
    private long totalMemoryMB = 0;
    private ScheduledExecutorService memoryMonitor;
    
    // Handler for UI updates
    private Handler uiHandler;
    
    // Notification manager
    private NotificationManager notificationManager;
    
    private Runnable streamingCallback;
    private Runnable errorCallback;
    private Runnable completionCallback;
    
    private final ConcurrentHashMap<String, String> chatHistory = new ConcurrentHashMap<>();
    
    public EnhancedLLMClient(Context context) {
        this.context = context.getApplicationContext();
        this.uiHandler = new Handler(Looper.getMainLooper());
        this.notificationManager = new NotificationManager(context);
        
        // Start memory monitoring
        startMemoryMonitoring();
    }
    
    private void startMemoryMonitoring() {
        memoryMonitor = Executors.newSingleThreadScheduledExecutor();
        memoryMonitor.scheduleAtFixedRate(this::checkMemoryUsage, 0, 30, TimeUnit.SECONDS);
    }
    
    private void checkMemoryUsage() {
        try {
            // Get memory info
            Runtime runtime = Runtime.getRuntime();
            long total = runtime.totalMemory();
            long used = runtime.totalMemory() - runtime.freeMemory();
            long max = runtime.maxMemory();
            
            long currentMB = used / (1024 * 1024);
            
            // Log if memory usage is high (>80% of max)
            if (max > 0 && currentMB > (max * 0.8)) {
                Log.w(TAG, "Memory usage high: " + currentMB + "MB");
            }
            
            // Update last memory reading
            lastMemoryMB = currentMB;
            totalMemoryMB = currentMB;
            
        } catch (Exception e) {
            Log.e(TAG, "Memory check failed", e);
        }
    }
    
    public void setServerUrl(String url) {
        this.serverUrl = url;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
    
    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }
    
    public void setTopP(float topP) {
        this.topP = topP;
    }
    
    public void setTopK(int topK) {
        this.topK = topK;
    }
    
    public void setRepeatPenalty(int repeatPenalty) {
        this.repeatPenalty = repeatPenalty;
    }
    
    public void setUseStream(boolean useStream) {
        this.useStream = useStream;
    }
    
    public void setStreamingCallback(Runnable callback) {
        this.streamingCallback = callback;
    }
    
    public void setErrorCallback(Runnable callback) {
        this.errorCallback = callback;
    }
    
    public void setCompletionCallback(Runnable callback) {
        this.completionCallback = callback;
    }
    
    /**
     * Check if server is online by making a simple request
     */
    public boolean checkConnection() {
        try {
            // Try to resolve the hostname
            InetAddress[] addresses = InetAddress.getAllByName(serverUrl);
            
            if (addresses.length == 0) {
                Log.e(TAG, "Could not resolve server address: " + serverUrl);
                return false;
            }
            
            // Try a simple HTTP request
            URL url = new URL("http://" + serverUrl + "/api/tags");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                Log.d(TAG, "Server online");
                isOnline = true;
                return true;
            } else {
                Log.w(TAG, "Server returned: " + responseCode);
                isOnline = false;
                return false;
            }
            
        } catch (UnknownHostException e) {
            Log.e(TAG, "Server offline - Host not found: " + serverUrl, e);
            isOnline = false;
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Server offline - Connection failed: " + serverUrl, e);
            isOnline = false;
            return false;
        }
    }
    
    /**
     * Get list of available models from server
     */
    public List<String> getAvailableModels() {
        List<String> models = new ArrayList<>();
        
        try {
            URL url = new URL("http://" + serverUrl + "/api/tags");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            
            InputStream inputStream = conn.getInputStream();
            String response = readFullResponse(inputStream);
            
            JSONObject json = new JSONObject(response);
            JSONArray modelsArray = json.getJSONArray("models");
            
            for (int i = 0; i < modelsArray.length(); i++) {
                JSONObject modelObj = modelsArray.getJSONObject(i);
                models.add(modelObj.getString("name"));
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to get models list", e);
        }
        
        return models;
    }
    
    /**
     * Start a chat session with streaming
     */
    public void startChat(String message, Runnable onTokenCallback, Runnable onDoneCallback) {
        if (!isOnline) {
            showError("Server is offline");
            return;
        }
        
        isRunning = true;
        
        // Build the request
        JSONObject requestBody = new JSONObject();
        
        try {
            requestBody.put("model", model);
            requestBody.put("messages", new JSONArray());
            
            // Add system prompt if exists
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                JSONObject systemMsg = new JSONObject();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                requestBody.getJSONArray("messages").put(systemMsg);
            }
            
            // Add user message
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", message);
            requestBody.getJSONArray("messages").put(userMsg);
            
            // Add parameters
            requestBody.put("stream", true);
            requestBody.put("stream_options", new JSONObject());
            JSONObject streamOptions = requestBody.getJSONObject("stream_options");
            streamOptions.put("include_usage", true);
            
            if (temperature != 0.7f) {
                requestBody.put("temperature", temperature);
            }
            
            if (topP != 0.9f) {
                requestBody.put("top_p", topP);
            }
            
            if (topK != 40) {
                requestBody.put("top_k", topK);
            }
            
            if (repeatPenalty != 1.1) {
                requestBody.put("repeat_penalty", repeatPenalty);
            }
            
        } catch (Exception e) {
            showError("Error building request: " + e.getMessage());
            isRunning = false;
            return;
        }
        
        // Make the request
        makeStreamingRequest(requestBody, onTokenCallback, onDoneCallback);
    }
    
    /**
     * Make streaming request with retry logic
     */
    private void makeStreamingRequest(JSONObject requestBody, 
                                       Runnable onTokenCallback, 
                                       Runnable onDoneCallback) {
        int attempt = 0;
        
        while (isRunning && attempt < MAX_RECONNECT_ATTEMPTS) {
            try {
                URL url = new URL("http://" + serverUrl + "/api/chat");
                
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(DEFAULT_TIMEOUT);
                conn.setReadTimeout(DEFAULT_TIMEOUT);
                
                // Send request body
                String jsonBody = requestBody.toString();
                byte[] jsonBytes = jsonBody.getBytes(StandardCharsets.UTF_8);
                conn.setDoOutput(true);
                try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), 
                         StandardCharsets.UTF_8)) {
                    writer.write(jsonBody);
                }
                
                int responseCode = conn.getResponseCode();
                
                if (responseCode == 200) {
                    // Read streaming response
                    readStreamingResponse(conn.getInputStream(), onTokenCallback, onDoneCallback);
                    return; // Success, exit retry loop
                } else {
                    Log.w(TAG, "Server returned: " + responseCode);
                    
                    // Read error response
                    StringBuilder error = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            error.append(line);
                        }
                    }
                    
                    showError("Server error: " + responseCode + " - " + error.toString());
                    
                    // Wait before retry
                    if (attempt < MAX_RECONNECT_ATTEMPTS - 1) {
                        attempt++;
                        Thread.sleep(RECONNECT_DELAY_MS * (attempt + 1));
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Streaming error attempt " + attempt, e);
                
                if (attempt < MAX_RECONNECT_ATTEMPTS - 1) {
                    attempt++;
                    Thread.sleep(RECONNECT_DELAY_MS * (attempt + 1));
                }
            }
        }
        
        // All retries exhausted
        if (isRunning) {
            showError("Connection failed after " + MAX_RECONNECT_ATTEMPTS + " attempts");
            isRunning = false;
        }
    }
    
    /**
     * Read streaming response token by token
     */
    private void readStreamingResponse(InputStream inputStream, 
                                       Runnable onTokenCallback, 
                                       Runnable onDoneCallback) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        
        int tokenCount = 0;
        
        while (isRunning && (line = reader.readLine()) != null) {
            // Parse SSE format: data: {...}
            if (!line.startsWith("data:")) {
                continue;
            }
            
            String data = line.substring(5).trim();
            
            if (data.equals("[DONE]")) {
                break;
            }
            
            try {
                JSONObject json = new JSONObject(data);
                
                // Get delta/content if streaming, or full response
                if (json.has("delta")) {
                    JSONObject delta = json.getJSONObject("delta");
                    if (delta.has("content")) {
                        String content = delta.getString("content");
                        
                        // Call token callback
                        if (onTokenCallback != null) {
                            uiHandler.post(onTokenCallback);
                        }
                        
                        // Store in history
                        chatHistory.put(tokenCount++, content);
                    }
                } else if (json.has("message")) {
                    JSONObject message = json.getJSONObject("message");
                    if (message.has("content")) {
                        String content = message.getString("content");
                        
                        if (onTokenCallback != null) {
                            uiHandler.post(onTokenCallback);
                        }
                        
                        chatHistory.put(tokenCount++, content);
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error parsing token: " + line, e);
            }
        }
        
        // Call done callback
        if (onDoneCallback != null) {
            uiHandler.post(onDoneCallback);
        }
        
        isRunning = false;
    }
    
    /**
     * Read full (non-streaming) response
     */
    public String getFullResponse(String message) {
        if (!isOnline) {
            showError("Server is offline");
            return null;
        }
        
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("messages", new JSONArray());
            
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                JSONObject systemMsg = new JSONObject();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                requestBody.getJSONArray("messages").put(systemMsg);
            }
            
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", message);
            requestBody.getJSONArray("messages").put(userMsg);
            
            requestBody.put("stream", false);
            
            if (temperature != 0.7f) {
                requestBody.put("temperature", temperature);
            }
            
            URL url = new URL("http://" + serverUrl + "/api/chat");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(DEFAULT_TIMEOUT);
            
            String jsonBody = requestBody.toString();
            byte[] jsonBytes = jsonBody.getBytes(StandardCharsets.UTF_8);
            conn.setDoOutput(true);
            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), 
                     StandardCharsets.UTF_8)) {
                writer.write(jsonBody);
            }
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                InputStream inputStream = conn.getInputStream();
                String response = readFullResponse(inputStream);
                
                // Parse response
                JSONObject json = new JSONObject(response);
                JSONObject message = json.getJSONObject("message");
                String content = message.getString("content");
                
                return content;
                
            } else {
                showError("Server returned: " + responseCode);
                return null;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Full response error", e);
            showError("Error getting response: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Read full response from stream (for non-streaming mode)
     */
    private String readFullResponse(InputStream inputStream) throws IOException {
        StringBuilder response = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        
        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
        }
        
        return response.toString();
    }
    
    /**
     * Show error message
     */
    public void showError(String message) {
        if (errorCallback != null) {
            uiHandler.post(errorCallback);
        }
        
        // Also show notification
        notificationManager.showNotification("Error", message);
    }
    
    /**
     * Get current memory usage
     */
    public long getCurrentMemoryMB() {
        return lastMemoryMB;
    }
    
    /**
     * Get chat history
     */
    public Map<String, String> getChatHistory() {
        return chatHistory;
    }
    
    /**
     * Clear chat history
     */
    public void clearChatHistory() {
        chatHistory.clear();
    }
    
    /**
     * Stop current chat session
     */
    public void stopChat() {
        isRunning = false;
    }
    
    /**
     * Check if currently running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Check if server is online
     */
    public boolean isOnline() {
        return isOnline;
    }
    
    public void setNotificationManager(NotificationManager manager) {
        this.notificationManager = manager;
    }
}
