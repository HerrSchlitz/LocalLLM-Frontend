package com.meshnet.llm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Message model for chat bubbles with timestamps
 */
public class Message {
    private String id;
    private String role; // "user" or "assistant"
    private String content;
    private long timestamp;
    private String timestampFormatted;
    
    public Message(String id, String role, String content) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.timestampFormatted = formatTimestamp();
    }
    
    public static String formatTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date());
    }
    
    public String getId() { return id; }
    public String getRole() { return role; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
    public String getTimestampFormatted() { return timestampFormatted; }
    
    public void setContent(String content) { this.content = content; }
}
