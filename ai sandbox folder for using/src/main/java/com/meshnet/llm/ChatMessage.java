package com.meshnet.llm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {
    private String id;
    private String role;  // "user" or "assistant"
    private String content;
    private long timestamp;

    public ChatMessage() {
        this.id = generateId();
        this.timestamp = System.currentTimeMillis();
    }

    private String generateId() {
        return "msg_" + System.currentTimeMillis() + "_" + (Math.random() * 1000);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
    }

    @Override
    public String toString() {
        return role + ": " + content;
    }
}
