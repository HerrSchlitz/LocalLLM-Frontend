package com.meshnet.llm;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SystemPromptEditorActivity extends AppCompatActivity {

    private EditText systemPromptEditText;
    private EditText modelParamsEditText;
    private String currentSystemPrompt;
    private String currentModelParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_prompt_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("System Prompt Editor");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        systemPromptEditText = findViewById(R.id.systemPromptEditText);
        modelParamsEditText = findViewById(R.id.modelParamsEditText);

        // Load current system prompt
        SystemPromptConfig config = SettingsManager.getSystemPromptConfig();
        if (config != null) {
            systemPromptEditText.setText(config.systemPrompt);
            modelParamsEditText.setText(config.modelParams);
        } else {
            systemPromptEditText.setText("You are a helpful assistant.");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public static class SystemPromptConfig {
        public String systemPrompt;
        public String modelParams;

        public static SystemPromptConfig fromJson(String json) {
            if (json == null || json.isEmpty()) {
                return new SystemPromptConfig();
            }
            Type listType = new TypeToken<ArrayList<ChatMessage>>() {
            }.getType();
            Gson gson = new Gson();
            return gson.fromJson(json, SystemPromptConfig.class);
        }

        public String toJson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }
}
