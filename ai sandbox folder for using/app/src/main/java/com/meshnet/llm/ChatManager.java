package com.meshnet.llm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatManager {
    private static final String TAG = "ChatManager";
    private final Context context;
    private final SharedPreferences prefs;
    private final CopyOnWriteArrayList<ChatSession> chats = new CopyOnWriteArrayList<>();
    private int nextChatId = 1;
    
    public ChatManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences("llm_app", Context.MODE_PRIVATE);
        loadChats();
    }
    
    public String createNewChat(String name) {
        ChatSession chat = new ChatSession(nextChatId++, name);
        chats.add(chat);
        saveChat(chat);
        return chat.getId();
    }
    
    public ChatSession getChat(String chatId) {
        for (ChatSession chat : chats) {
            if (chat.getId().equals(chatId)) {
                return chat;
            }
        }
        return null;
    }
    
    public List<ChatSession> getChats() {
        return new ArrayList<>(chats);
    }
    
    public ChatSession getCurrentChat() {
        if (chats.isEmpty()) {
            return createNewChat("Chat 1");
        }
        return chats.get(0);
    }
    
    public void setCurrentChat(String chatId) {
        ChatSession chat = getChat(chatId);
        if (chat != null) {
            chats.remove(chat);
            chats.add(0, chat);
            saveChats();
        }
    }
    
    public void addMessage(String chatId, String role, String content) {
        ChatSession chat = getChat(chatId);
        if (chat != null) {
            chat.addMessage(role, content);
            saveChat(chat);
        }
    }
    
    public void deleteChat(String chatId) {
        ChatSession chat = getChat(chatId);
        if (chat != null) {
            chats.remove(chat);
            if (chats.isEmpty()) {
                createNewChat("Chat 1");
            }
            deleteChatFile(chatId);
            saveChats();
        }
    }
    
    public void renameChat(String chatId, String newName) {
        ChatSession chat = getChat(chatId);
        if (chat != null) {
            chat.setName(newName);
            saveChat(chat);
        }
    }
    
    public void exportChat(String chatId, String outputPath) {
        ChatSession chat = getChat(chatId);
        if (chat != null) {
            try (FileWriter writer = new FileWriter(outputPath)) {
                writer.write("=== Chat Export: " + chat.getName() + " ===\n");
                writer.write("Created: " + chat.getCreatedAt() + "\n");
                writer.write("Messages:\n\n");
                
                for (ChatMessage message : chat.getMessages()) {
                    writer.write("[" + message.getRole() + "] " + message.getContent() + "\n");
                    writer.write("---\n");
                }
            } catch (IOException e) {
                Log.e(TAG, "Export failed: " + e.getMessage());
            }
        }
    }
    
    public void generateSummary(String chatId) {
        ChatSession chat = getChat(chatId);
        if (chat != null) {
            StringBuilder summary = new StringBuilder();
            summary.append("=== Summary of: ").append(chat.getName()).append(" ===\n\n");
            
            for (ChatMessage message : chat.getMessages()) {
                summary.append("[").append(message.getRole()).append("]: ")
                      .append(message.getContent().substring(0, Math.min(200, message.getContent().length())))
                      .append("...\n\n");
            }
            
            chat.setSummary(summary.toString());
            saveChat(chat);
        }
    }
    
    private void saveChat(ChatSession chat) {
        try {
            String json = new JSONObject()
                .put("id", chat.getId())
                .put("name", chat.getName())
                .put("messages", new JSONArray(chat.getMessages()))
                .put("createdAt", chat.getCreatedAt())
                .put("summary", chat.getSummary())
                .toString();
            
            prefs.edit()
                .putString("chat_" + chat.getId(), json)
                .apply();
        } catch (Exception e) {
            Log.e(TAG, "Save failed: " + e.getMessage());
        }
    }
    
    private void saveChats() {
        try {
            JSONArray allChats = new JSONArray();
            for (ChatSession chat : chats) {
                String json = new JSONObject()
                    .put("id", chat.getId())
                    .put("name", chat.getName())
                    .put("messages", new JSONArray(chat.getMessages()))
                    .put("createdAt", chat.getCreatedAt())
                    .put("summary", chat.getSummary())
                    .toString();
                allChats.put(json);
            }
            prefs.edit().putString("all_chats", allChats.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Batch save failed: " + e.getMessage());
        }
    }
    
    private void loadChats() {
        String allChats = prefs.getString("all_chats", null);
        if (allChats != null) {
            try {
                JSONArray all = new JSONArray(allChats);
                for (int i = 0; i < all.length(); i++) {
                    JSONObject obj = all.getJSONObject(i);
                    String id = obj.getString("id");
                    String name = obj.getString("name");
                    JSONArray msgs = obj.getJSONArray("messages");
                    
                    ChatSession chat = new ChatSession();
                    chat.setId(id);
                    chat.setName(name);
                    chat.setCreatedAt(obj.optString("createdAt", ""));
                    chat.setSummary(obj.optString("summary", ""));
                    
                    for (int j = 0; j < msgs.length(); j++) {
                        JSONObject msg = msgs.getJSONObject(j);
                        chat.addMessage(msg.getString("role"), msg.getString("content"));
                    }
                    
                    chats.add(chat);
                }
                sortChatsByDate();
            } catch (Exception e) {
                Log.e(TAG, "Load failed: " + e.getMessage());
            }
        }
    }
    
    private void sortChatsByDate() {
        chats.sort((a, b) -> {
            Date aDate = a.getCreatedAtTime();
            Date bDate = b.getCreatedAtTime();
            return bDate.compareTo(aDate);
        });
    }
    
    private void deleteChatFile(String chatId) {
        File file = new File(context.getFilesDir(), "chat_" + chatId + ".json");
        if (file.exists()) {
            file.delete();
        }
    }
}