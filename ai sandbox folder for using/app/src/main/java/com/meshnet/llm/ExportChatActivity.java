package com.meshnet.llm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ExportChatActivity extends Activity {
    private TextView chatContent;
    private Button btnExportText;
    private Button btnExportPDF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_chat);

        chatContent = findViewById(R.id.chatContent);
        btnExportText = findViewById(R.id.btnExportText);
        btnExportPDF = findViewById(R.id.btnExportPDF);

        // Get chat content from intent
        String chatText = getIntent().getStringExtra("chatContent");
        if (chatText != null) {
            chatContent.setText(chatText);
        }

        btnExportText.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, chatContent.getText().toString());
            startActivity(shareIntent);
        });

        btnExportPDF.setOnClickListener(v -> {
            // Simulate PDF export (implement real PDF library like iText if needed)
            Toast.makeText(this, "PDF export: Save chat as .pdf", Toast.LENGTH_LONG).show();
        });
    }
}
