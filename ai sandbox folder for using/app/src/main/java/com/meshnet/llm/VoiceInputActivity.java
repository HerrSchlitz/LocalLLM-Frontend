package com.meshnet.llm;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class VoiceInputActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO = 1;
    private static final int REQUEST_PICK_FILE = 2;
    private MediaRecorder mediaRecorder;
    private Vibrator vibrator;
    private EditText editText;
    private Button recordButton, pickFileButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_input);

        editText = findViewById(R.id.editText);
        recordButton = findViewById(R.id.recordButton);
        pickFileButton = findViewById(R.id.pickFileButton);
        cancelButton = findViewById(R.id.cancelButton);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        recordButton.setOnClickListener(v -> startRecording());
        pickFileButton.setOnClickListener(v -> pickAudioFile());
        cancelButton.setOnClickListener(v -> finish());

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                recordButton.setEnabled(s.length() > 0);
            }
        });
    }

    private void startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        } else {
            try {
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                String filePath = getExternalFilesDir(null) + "/voice_recording.3gp";
                mediaRecorder.setOutputFile(filePath);
                mediaRecorder.prepare();
                mediaRecorder.start();
                recordButton.setEnabled(false);
                pickFileButton.setEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
                showAlertDialog("Recording Error", "Failed to start recording: " + e.getMessage());
            }
        }
    }

    private void pickAudioFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, REQUEST_PICK_FILE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_FILE && resultCode == RESULT_OK && data != null) {
            String filePath = data.getData() != null ?
                    data.getData().getPath() :
                    getExternalFilesDir(null) + "/picked_audio.3gp";
            editText.setText("Using audio file: " + filePath);
        }
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
