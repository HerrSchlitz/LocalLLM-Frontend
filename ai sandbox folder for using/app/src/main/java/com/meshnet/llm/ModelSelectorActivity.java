package com.meshnet.llm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import com.meshnet.llm.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Model Selector Activity - Browse and select available LLM models
 * Displays all models available on the Ollama server
 */
public class ModelSelectorActivity extends AppCompatActivity {

    private MainActivity mainActivity;
    private ListView modelList;
    private SearchView searchView;
    private List<String> availableModels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_selector);

        mainActivity = (MainActivity) getIntent().getExtras().get("MainActivity");
        if (mainActivity == null) {
            finish();
            return;
        }

        modelList = findViewById(R.id.modelList);
        searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterModels(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterModels(newText);
                return true;
            }
        });

        // Load available models from LLMClient
        loadAvailableModels();
    }

    private void loadAvailableModels() {
        if (mainActivity.llmClient != null) {
            // Simulate getting models from server
            availableModels.add("mistral");
            availableModels.add("llama3");
            availableModels.add("codellama");
            availableModels.add("gemma");
            availableModels.add("phi");
            availableModels.add("qwen2");
            availableModels.add("command");
            availableModels.add("falcon");
            availableModels.add("stablelm");
            
            // Add current model to top
            if (mainActivity.settingsManager != null && 
                mainActivity.settingsManager.getModelName() != null) {
                String currentModel = mainActivity.settingsManager.getModelName();
                availableModels.remove(currentModel);
                availableModels.add(0, currentModel);
            }

            ListAdapter adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_list_item_1, availableModels);
            modelList.setAdapter(adapter);
        }
    }

    private void filterModels(String query) {
        List<String> filtered = new ArrayList<>();
        if (query.isEmpty()) {
            filtered = availableModels;
        } else {
            for (String model : availableModels) {
                if (model.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
                    filtered.add(model);
                }
            }
        }

        ListAdapter adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, filtered);
        modelList.setAdapter(adapter);
    }

    private void selectModel(String modelName) {
        mainActivity.settingsManager.setModelName(modelName);
        mainActivity.llmClient.setModelName(modelName);
        
        Toast.makeText(this, "Model changed to: " + modelName, Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("MainActivity", mainActivity);
        intent.putExtra("switchToMain", true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (modelList.getCount() > 0) {
            selectModel(modelList.getSelectedItem() != null ? 
                modelList.getSelectedItem().toString() : null);
        } else {
            super.onBackPressed();
        }
    }
}
