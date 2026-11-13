package com.meshnet.llm;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Enhanced LLM Client with streaming support, better error handling, and connection management
 */
public class LLMClient {

    private static final String TAG = "LLMClient";
    private static final int DEFAULT_TIMEOUT_SECONDS = 60;
    private static final int CONNECT_TIMEOUT_SECONDS = 15;
    private static final int READ_TIMEOUT_SECONDS = 30;

    private static Retrofit retrofit;
    private static LLMClient client;

    private interface LLMService {
        Call<ResponseBody> chatStream(Map<String, Object> params, String model, String url);
        Call<LLMResponse> chatNonStream(Map<String, Object> params, String model, String url);
    }

    private static LLMService getLLMService(Context context) {
        String baseUrl = buildBaseUrl(context);
        Log.d(TAG, "Using base URL: " + baseUrl);
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(createOkHttpClient())
                .build();
        return retrofit.create(LLMService.class);
    }

    private static String buildBaseUrl(Context context) {
        String ip = context.getSharedPreferences("LLMSettings", Context.MODE_PRIVATE)
                .getString("server_ip", "");
        String model = context.getSharedPreferences("LLMSettings", Context.MODE_PRIVATE)
                .getString("model_name", "mistral");
        String systemPrompt = context.getSharedPreferences("LLMSettings", Context.MODE_PRIVATE)
                .getString("system_prompt", "");
        double temperature = context.getSharedPreferences("LLMSettings", Context.MODE_PRIVATE)
                .getFloat("temperature", 0.7f);

        if (ip == null || ip.isEmpty()) {
            ip = context.getString(R.string.default_server_ip);
        }
        if (!ip.endsWith(":")) {
            ip += ":";
        }
        if (ip.endsWith("/")) {
            ip = ip.substring(0, ip.length() - 1);
        }
        ip += "11434/";
        if (!ip.endsWith("/")) {
            ip += "/";
        }

        String system = systemPrompt.isEmpty() ? null : systemPrompt;
        String url = ip + "v1/chat/completions";

        Map<String, Object> params = new HashMap<>();
        params.put("model", model);
        params.put("messages", new ArrayList<>());
        params.put("temperature", temperature);
        params.put("stream", true);
        if (system != null && !system.isEmpty()) {
            params.put("system", system);
        }

        return ip;
    }

    private static OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    public static LLMClient getInstance(Context context) {
        if (client == null) {
            client = new LLMClient();
        }
        return client;
    }

    private LLMClient() {}

    public String getServerIP(Context context) {
        return context.getSharedPreferences("LLMSettings", Context.MODE_PRIVATE)
                .getString("server_ip", "");
    }

    public void setServerIP(Context context, String ip) {
        context.getSharedPreferences("LLMSettings", Context.MODE_PRIVATE)
                .edit()
                .putString("server_ip", ip)
                .apply();
    }

    public String getModelName(Context context) {
        return context.getSharedPreferences("LLMSettings", Context.MODE_PRIVATE)
                .getString("model_name", "mistral");
    }

    public void setModelName(Context context, String model) {
        context.getSharedPreferences("LLMSettings", Context.MODE_PRIVATE)
                .edit()
                .putString("model_name", model)
                .apply();
    }

    public void setSystemPrompt(Context context, String prompt) {
        context.getSharedPreferences("LLMSettings", Context.MODE_PRIVATE)
                .edit()
                .putString("system_prompt", prompt)
                .apply();
    }

    public String getSystemPrompt(Context context) {
        return context.getSharedPreferences("LLMSettings", Context.MODE_PRIVATE)
                .getString("system_prompt", "");
    }

    public double getTemperature(Context context) {
        return context.getSharedPreferences("LLMSettings", Context.MODE_PRIVATE)
                .getFloat("temperature", 0.7f);
    }

    public void setTemperature(Context context, double temp) {
        context.getSharedPreferences("LLMSettings", Context.MODE_PRIVATE)
                .edit()
                .putFloat("temperature", (float) temp)
                .apply();
    }

    public boolean isStreamingEnabled(Context context) {
        return context.getSharedPreferences("LLMSettings", Context.MODE_PRIVATE)
                .getBoolean("streaming_enabled", true);
    }

    public void setStreamingEnabled(Context context, boolean enabled) {
        context.getSharedPreferences("LLMSettings", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("streaming_enabled", enabled)
                .apply();
    }

    public void chatStream(Context context, ChatSession chatSession, ResponseCallback callback) {
        LLMService service = getLLMService(context);
        String model = getModelName(context);
        String systemPrompt = getSystemPrompt(context);
        double temperature = getTemperature(context);

        Map<String, Object> messages = new ArrayList<>();
        messages.add(new HashMap<String, Object>() {{
            put("role", "system");
            put("content", systemPrompt);
        }});
        messages.add(new HashMap<String, Object>() {{
            put("role", "user");
            put("content", chatSession.getLastMessage());
        }});

        Map<String, Object> params = new HashMap<>();
        params.put("model", model);
        params.put("messages", messages);
        params.put("temperature", temperature);
        params.put("stream", true);

        Call<ResponseBody> call = service.chatStream(params, model, null);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (callback != null) {
                    callback.onResponseReceived(response, chatSession, messages);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (callback != null) {
                    callback.onError(t.getMessage(), chatSession);
                }
            }
        });
    }

    public interface ResponseCallback {
        void onResponseReceived(Response<ResponseBody> response, ChatSession chatSession, List<Map<String, Object>> messages);
        void onError(String message, ChatSession chatSession);
    }

    public class LLMResponse {
        public String model;
        public int created;
        public List<Choice> choices;
        public Usage usage;

        public static class Choice {
            public int index;
            public Message message;

            public static class Message {
                public String role;
                public String content;
            }
        }

        public static class Usage {
            public int promptTokens;
            public int completionTokens;
            public int totalTokens;
        }
    }

    public void chatNonStream(Context context, ChatSession chatSession, ResponseCallback callback) {
        LLMService service = getLLMService(context);
        String model = getModelName(context);
        String systemPrompt = getSystemPrompt(context);
        double temperature = getTemperature(context);

        Map<String, Object> messages = new ArrayList<>();
        messages.add(new HashMap<String, Object>() {{
            put("role", "system");
            put("content", systemPrompt);
        }});
        messages.add(new HashMap<String, Object>() {{
            put("role", "user");
            put("content", chatSession.getLastMessage());
        }});

        Map<String, Object> params = new HashMap<>();
        params.put("model", model);
        params.put("messages", messages);
        params.put("temperature", temperature);
        params.put("stream", false);

        Call<LLMResponse> call = service.chatNonStream(params, model, null);
        call.enqueue(new Callback<LLMResponse>() {
            @Override
            public void onResponse(Call<LLMResponse> call, Response<LLMResponse> response) {
                if (callback != null) {
                    callback.onResponseReceived(response, chatSession, messages);
                }
            }

            @Override
            public void onFailure(Call<LLMResponse> call, Throwable t) {
                if (callback != null) {
                    callback.onError(t.getMessage(), chatSession);
                }
            }
        });
    }

    public String getAvailableModels(Context context) {
        return context.getSharedPreferences("LLMSettings", Context.MODE_PRIVATE)
                .getString("available_models", "");
    }

    public void setAvailableModels(Context context, String models) {
        context.getSharedPreferences("LLMSettings", Context.MODE_PRIVATE)
                .edit()
                .putString("available_models", models)
                .apply();
    }
}
