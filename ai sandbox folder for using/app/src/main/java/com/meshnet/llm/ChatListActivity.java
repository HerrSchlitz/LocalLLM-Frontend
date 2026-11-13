package com.meshnet.llm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import com.meshnet.llm.databinding.ItemChatBinding;
import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends Activity implements LifecycleOwner {
    private Toolbar toolbar;
    private ListView listView;
    private List<ChatSession> chatSessions;
    private ArrayAdapter<ChatSession> adapter;
    private LifecycleRegistry lifecycleRegistry;
    private ChatManager chatManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        lifecycleRegistry = new LifecycleRegistry(this);
        setContentView(R.layout.activity_chat_list);
        
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        chatManager = ChatManager.getInstance();
        
        // Load chat sessions
        chatSessions = chatManager.getAllSessions();
        
        // Setup adapter
        adapter = new ArrayAdapter<>(this, R.layout.item_chat, chatSessions);
        listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
        
        // Setup click listener
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ChatSession session = chatSessions.get(position);
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("chatId", session.getId());
            startActivity(intent);
        });
        
        // Long press for more options
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            showChatMenu(position);
            return true;
        });
    }

    private void showChatMenu(int position) {
        ChatSession session = chatSessions.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setTitle("Chat Options")
               .setItems(new String[]{"Rename", "Share", "Export", "Delete"}, (dialog, which) -> {
                   if (which == 0) {
                       // Rename
                       renameChat(session);
                   } else if (which == 1) {
                       // Share
                       shareChat(session);
                   } else if (which == 2) {
                       // Export
                       exportChat(session);
                   } else if (which == 3) {
                       // Delete
                       deleteChat(session);
                   }
               });
        
        builder.show();
    }

    private void renameChat(ChatSession session) {
        new AlertDialog.Builder(this)
            .setTitle("Rename Chat")
            .setView(R.layout.edit_text_input)
            .setPositiveButton("Save", (dialog, which) -> {
                EditText editText = findViewById(R.id.editText);
                String newName = editText.getText().toString();
                if (!newName.isEmpty()) {
                    session.setName(newName);
                    chatManager.saveSession(session);
                    adapter.notifyDataSetChanged();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void shareChat(ChatSession session) {
        // Share chat URL or export
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this chat: " + session.getName());
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void exportChat(ChatSession session) {
        // Export to text file
        Intent exportIntent = new Intent(this, ExportChatActivity.class);
        exportIntent.putExtra("chatId", session.getId());
        startActivity(exportIntent);
    }

    private void deleteChat(ChatSession session) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Chat")
            .setMessage("Are you sure you want to delete this chat?")
            .setPositiveButton("Delete", (dialog, which) -> {
                chatManager.deleteSession(session.getId());
                chatSessions.remove(session);
                adapter.notifyDataSetChanged();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onLifecycleStateChanged(@NonNull Lifecycle.Event event) {
        lifecycleRegistry.onLifecycleStateChanged(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lifecycleRegistry.removeLifecycleObserver(this);
    }
}