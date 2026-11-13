package com.meshnet.llm;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * ThemeManager - Handles dark/light theme switching and persistence.
 * Stores theme preference in SharedPreferences.
 */
public class ThemeManager {
    private static final String PREFS_NAME = "LLMThemePrefs";
    private static final String KEY_THEME = "theme";
    private static final String DEFAULT_THEME = "light";

    private static ThemeManager instance;
    private SharedPreferences prefs;

    private ThemeManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Set current theme (light or dark)
     */
    public void setTheme(String theme) {
        prefs.edit().putString(KEY_THEME, theme).apply();
    }

    /**
     * Get current theme
     */
    public String getTheme() {
        return prefs.getString(KEY_THEME, DEFAULT_THEME);
    }

    /**
     * Set theme programmatically
     * @param context Application context
     * @param theme "light" or "dark"
     */
    public void applyTheme(Context context, String theme) {
        setTheme(theme);
        
        // Apply color scheme
        if ("dark".equals(theme)) {
            context.applyStyle(android.R.style.Theme_Dark, true);
        } else {
            context.applyStyle(android.R.style.Theme_Light, true);
        }
    }

    /**
     * Check if dark mode is enabled
     */
    public boolean isDarkMode() {
        return "dark".equals(getTheme());
    }
}
