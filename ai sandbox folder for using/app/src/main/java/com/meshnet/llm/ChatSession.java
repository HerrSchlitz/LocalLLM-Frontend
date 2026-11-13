package com.meshnet.llm;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a chat session with messages.
 * Used for persistent chat history management.
 */
public class ChatSession {
    private String id;
    private String name;
    private String systemPrompt;
    private List<ChatMessage> messages;
    private long lastModified;
    private long lastActive;

    public ChatSession() {
        messages = new ArrayList<>();
        id = java.util.UUID.randomUUID().toString();
        lastModified = System.currentTimeMillis();
        lastActive = System.currentTimeMillis();
    }

    public static ChatSession create() {
        ChatSession session = new ChatSession();
        session.id = java.util.UUID.randomUUID().toString();
        return session;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getLastActive() {
        return lastActive;
    }

    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        lastModified = System.currentTimeMillis();
        lastActive = System.currentTimeMillis();
    }

    public void clear() {
        messages.clear();
        lastModified = System.currentTimeMillis();
        lastActive = System.currentTimeMillis();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }
}