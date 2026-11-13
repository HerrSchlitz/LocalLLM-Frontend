package com.meshnet.llm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a chat session with messages
 */
public class ChatSession {
    private String id;
    private String name;
    private String model;
    private String systemPrompt;
    private double temperature;
    private boolean streamEnabled;
    private List<ChatMessage> messages;
    private boolean isLoading;
    private String isLoadingMessage;
    private Date lastActiveTime;
    private int unreadCount;

    public ChatSession() {
        this.messages = new ArrayList<>();
        this.lastActiveTime = new Date();
        this.id = generateId();
        this.name = "Chat " + new Date().getTime();
        this.model = "mistral";
        this.temperature = 0.7;
        this.streamEnabled = true;
    }

    public String generateId() {
        return java.util.UUID.randomUUID().toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { 
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
    }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public boolean isStreamEnabled() { return streamEnabled; }
    public void setStreamEnabled(boolean streamEnabled) { this.streamEnabled = streamEnabled; }

    public List<ChatMessage> getMessages() { return messages; }
    public void addMessage(ChatMessage message) { 
        this.messages.add(message);
        this.lastActiveTime = new Date();
    }
    
    public ChatMessage getLastMessage() {
        if (messages != null && !messages.isEmpty()) {
            return messages.get(messages.size() - 1);
        }
        return null;
    }

    public Date getLastActiveTime() { return lastActiveTime; }
    public void setLastActiveTime(Date time) { this.lastActiveTime = time; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public boolean isLoading() { return isLoading; }
    public void setLoading(boolean isLoading) { this.isLoading = isLoading; }

    public String getLoadMessage() { return isLoadingMessage; }
    public void setLoadMessage(String message) { this.isLoadingMessage = message; }

    // Check if chat is empty (no messages yet)
    public boolean isEmpty() {
        return messages == null || messages.isEmpty();
    }

    // Clear all messages
    public void clearMessages() {
        messages.clear();
        lastActiveTime = new Date();
    }

    @Override
    public String toString() {
        return "ChatSession{" +
                "name='" + name + '\'' +
                ", model='" + model + '\'' +
                ", messages=" + messages.size() +
                ", lastActive=" + (lastActiveTime != null ? lastActiveTime.getTime() : 0) +
                '}';
    }
}
