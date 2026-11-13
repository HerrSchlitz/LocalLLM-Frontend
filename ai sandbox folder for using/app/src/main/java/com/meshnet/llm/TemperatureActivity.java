package com.meshnet.llm;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Slider;
import android.widget.TextView;

public class TemperatureActivity extends Activity {

    private static final String PREFS_NAME = "LLMSettings";
    private static final String KEY_TEMPERATURE = "temperature";
    private static final String KEY_TOP_P = "top_p";
    private static final String KEY_TOP_K = "top_k";
    private static final String KEY_REPEAT_PENALTY = "repeat_penalty";
    private static final String KEY_N_PRESENCE_PENALTY = "n_presence_penalty";
    private static final String KEY_N_FREQUENCY_PENALTY = "n_freq_penalty";

    private Slider temperatureSlider;
    private TextView tempValue;
    private TextView topPValue;
    private TextView topKValue;
    private TextView repeatPenaltyValue;
    private TextView presencePenaltyValue;
    private TextView freqPenaltyValue;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        temperatureSlider = findViewById(R.id.temperature_slider);
        tempValue = findViewById(R.id.temp_value);
        topPValue = findViewById(R.id.top_p_value);
        topKValue = findViewById(R.id.top_k_value);
        repeatPenaltyValue = findViewById(R.id.repeat_penalty_value);
        presencePenaltyValue = findViewById(R.id.presence_penalty_value);
        freqPenaltyValue = findViewById(R.id.freq_penalty_value);

        // Load saved values
        loadSettings();

        // Setup slider
        temperatureSlider.setMin(0.0f);
        temperatureSlider.setMax(2.0f);
        temperatureSlider.setStepSize(0.1f);
        temperatureSlider.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onSliderChangeListener(Slider slider, Slider.ValueFrom valueFrom, float value) {
                tempValue.setText(String.format("%.2f", value));
                saveSettings();
            }
        });

        // Load initial temp value
        tempValue.setText(String.format("%.2f", prefs.getFloat(KEY_TEMPERATURE, 0.7f)));
    }

    private void loadSettings() {
        temperatureSlider.setValue(prefs.getFloat(KEY_TEMPERATURE, 0.7f));
        topPValue.setText(String.format("%.2f", prefs.getFloat(KEY_TOP_P, 0.9f)));
        topKValue.setText(String.valueOf(prefs.getInt(KEY_TOP_K, 40)));
        repeatPenaltyValue.setText(String.format("%.2f", prefs.getFloat(KEY_REPEAT_PENALTY, 1.1f)));
        presencePenaltyValue.setText(String.format("%.2f", prefs.getFloat(KEY_N_PRESENCE_PENALTY, 0.0f)));
        freqPenaltyValue.setText(String.format("%.2f", prefs.getFloat(KEY_N_FREQUENCY_PENALTY, 0.0f)));
    }

    private void saveSettings() {
        prefs.edit()
            .putFloat(KEY_TEMPERATURE, (float) temperatureSlider.getValue())
            .putFloat(KEY_TOP_P, prefs.getFloat(KEY_TOP_P, 0.9f))
            .putInt(KEY_TOP_K, prefs.getInt(KEY_TOP_K, 40))
            .putFloat(KEY_REPEAT_PENALTY, prefs.getFloat(KEY_REPEAT_PENALTY, 1.1f))
            .putFloat(KEY_N_PRESENCE_PENALTY, prefs.getFloat(KEY_N_PRESENCE_PENALTY, 0.0f))
            .putFloat(KEY_N_FREQUENCY_PENALTY, prefs.getFloat(KEY_N_FREQUENCY_PENALTY, 0.0f))
            .apply();
    }

    @Override
    public void onBackPressed() {
        saveSettings();
        super.onBackPressed();
    }
}
