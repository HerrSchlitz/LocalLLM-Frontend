package com.meshnet.llm;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;
import org.json.JSONArray;

public class MainActivity extends Activity {
    private TextView chatDisplay;
    private EditText ipInput, promptInput;
    private Button sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        int layoutId = getResources().getIdentifier("activity_main", "layout", getPackageName());
        setContentView(layoutId);

        int chatDisplayId = getResources().getIdentifier("chatDisplay", "id", getPackageName());
        int ipInputId = getResources().getIdentifier("ipInput", "id", getPackageName());
        int promptInputId = getResources().getIdentifier("promptInput", "id", getPackageName());
        int sendBtnId = getResources().getIdentifier("sendBtn", "id", getPackageName());

        chatDisplay = findViewById(chatDisplayId);
        ipInput = findViewById(ipInputId);
        promptInput = findViewById(promptInputId);
        sendBtn = findViewById(sendBtnId);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setPressed(!v.isPressed());
                
                final String prompt = promptInput.getText().toString().trim();
                String hostUrl = ipInput.getText().toString().trim();
                
                if (!prompt.isEmpty() && !hostUrl.isEmpty()) {
                    // Ensure the URL contains http/https protocol prefix
                    if (!hostUrl.startsWith("http://") && !hostUrl.startsWith("https://")) {
                        hostUrl = "http://" + hostUrl;
                    }
                    
                    chatDisplay.append("\nYou: " + prompt + "\n");
                    promptInput.setText("");
                    
                    final String finalHostUrl = hostUrl;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendToLLM(finalHostUrl, prompt);
                        }
                    }).start();
                }
            }
        });
    }

    private void sendToLLM(String host, String prompt) {
        try {
            URL url = new URL(host.replaceAll("/+$", "") + "/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000); // 15-second timeout protection
            conn.setReadTimeout(30000);

            // Constructing a safe JSON payload using Android's native JSONObject class
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("model", "local-model");
            jsonPayload.put("stream", false);

            JSONArray messagesArray = new JSONArray();
            JSONObject messageObj = new JSONObject();
            messageObj.put("role", "user");
            messageObj.put("content", prompt);
            messagesArray.put(messageObj);

            jsonPayload.put("messages", messagesArray);
            
            OutputStream os = conn.getOutputStream();
            os.write(jsonPayload.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                
                // Parse standard OpenAI chat completion response format
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                final String llmReply = message.getString("content");
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatDisplay.append("\nAI: " + llmReply + "\n");
                    }
                });
            } else {
                final int code = responseCode;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatDisplay.append("\n[Server Error Code: " + code + "]\n");
                    }
                });
            }
        } catch (final Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatDisplay.append("\n[Connection Failed: " + e.getMessage() + "]\n");
                }
            });
        }
    }
}
