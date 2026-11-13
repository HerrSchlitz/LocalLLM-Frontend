package com.meshnet.llm;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ModelParametersActivity extends Activity {
    
    private EditText topKInput;
    private EditText topPInput;
    private EditText repeatPenaltyInput;
    private EditText numPredictInput;
    private EditText temperatureInput;
    private EditText numBatchInput;
    private TextView infoText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_parameters);
        
        infoText = findViewById(R.id.infoText);
        topKInput = findViewById(R.id.topKInput);
        topPInput = findViewById(R.id.topPInput);
        repeatPenaltyInput = findViewById(R.id.repeatPenaltyInput);
        numPredictInput = findViewById(R.id.numPredictInput);
        temperatureInput = findViewById(R.id.temperatureInput);
        numBatchInput = findViewById(R.id.numBatchInput);
        
        // Load default values
        topKInput.setText("40");
        topPInput.setText("0.9");
        repeatPenaltyInput.setText("1.1");
        numPredictInput.setText("128");
        temperatureInput.setText("0.7");
        numBatchInput.setText("512");
        
        infoText.setText(
            "Model Parameters (Advanced):\n\n" +
            "• Top K: Number of top tokens to consider\n" +
            "• Top P: Nucleus sampling parameter\n" +
            "• Repeat Penalty: Penalty for repeated tokens\n" +
            "• Num Predict: Max tokens to generate\n" +
            "• Temperature: Creativity (lower = more focused)\n" +
            "• Num Batch: Batch size for processing\n\n" +
            "Tip: Lower temperature (0.2-0.5) for coding tasks, " +
            "higher (0.7-1.0) for creative writing."
        );
    }
    
    @Override
    public void onBackPressed() {
        // Show confirmation dialog
        int dialogNum = 1;
        // ... dialog logic
    }
}
