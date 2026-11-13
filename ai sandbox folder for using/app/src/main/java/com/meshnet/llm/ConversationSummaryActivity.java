package com.meshnet.llm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ConversationSummaryActivity extends Activity {
    private EditText summaryInput;
    private TextView summaryPreview;
    private Button generateSummaryBtn;
    private String conversationHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_summary);

        summaryInput = findViewById(R.id.summaryInput);
        summaryPreview = findViewById(R.id.summaryPreview);
        generateSummaryBtn = findViewById(R.id.generateSummaryBtn);

        // Get conversation history from intent
        if (getIntent().hasExtra("conversationHistory")) {
            conversationHistory = getIntent().getStringExtra("conversationHistory");
        }

        generateSummaryBtn.setOnClickListener(v -> {
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                // In production, call LLM API to generate summary
                // For now, show preview
                summaryPreview.setText("AI Summary:\n\n" + conversationHistory.substring(0, Math.min(500, conversationHistory.length())) + "...");
                Toast.makeText(this, "Summary generated (demo mode)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No conversation to summarize", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
