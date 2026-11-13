package com.meshnet.llm;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Slider;
import android.widget.TextView;
import android.widget.Toast;

public class TemperatureSliderActivity extends Activity {
    private Slider temperatureSlider;
    private TextView temperatureValue;
    
    private String model;
    private double temperature;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_slider);
        
        temperatureSlider = findViewById(R.id.temperature_slider);
        temperatureValue = findViewById(R.id.temperature_value);
        
        // Get model from intent or default
        model = getIntent().getStringExtra("MODEL") 
                ?: SettingsManager.getInstance().getLastModel();
        
        if (model == null || model.isEmpty()) {
            model = "mistral"; // default
        }
        
        // Set default temperature
        temperature = SettingsManager.getInstance().getTemperature();
        if (temperature == 0) {
            temperature = 0.7; // default
        }
        
        temperatureSlider.setMin(0.0f);
        temperatureSlider.setMax(2.0f);
        temperatureSlider.setStep(0.1f);
        temperatureSlider.setValue((float) temperature);
        
        updateTemperatureDisplay();
        
        // Listen to slider changes
        temperatureSlider.addOnChangeListener((slider, value, fromUserChange) -> {
            if (fromUserChange) {
                temperature = value;
                updateTemperatureDisplay();
                SettingsManager.getInstance().setTemperature(temperature);
                
                // Show toast with new value
                Toast.makeText(
                    this, 
                    "Temperature: " + String.format("%.1f", temperature),
                    Toast.LENGTH_SHORT
                ).show();
            }
        });
        
        // Apply temperature to current chat if exists
        ChatSession currentSession = ChatManager.getInstance().getCurrentChat();
        if (currentSession != null) {
            currentSession.setTemperature(temperature);
        }
    }
    
    private void updateTemperatureDisplay() {
        temperatureValue.setText("Temperature: " + String.format("%.1f", temperature));
    }
}
