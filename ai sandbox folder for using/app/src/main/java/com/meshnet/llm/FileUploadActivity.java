package com.meshnet.llm;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * File Upload Activity for sending images, PDFs, and other files to the LLM
 * Supports vision models that can process uploaded files
 */
public class FileUploadActivity extends AppCompatActivity {

    private EditText filePathInput;
    private Button selectFileButton, uploadButton;
    private TextView statusText;
    private Uri selectedFileUri;
    private static final int FILE_CHOOSER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_upload);

        filePathInput = findViewById(R.id.filePathInput);
        selectFileButton = findViewById(R.id.selectFileButton);
        uploadButton = findViewById(R.id.uploadButton);
        statusText = findViewById(R.id.statusText);

        // Request permissions
        requestPermissions();

        selectFileButton.setOnClickListener(v -> openFilePicker());
        uploadButton.setOnClickListener(v -> uploadFile());
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    FILE_CHOOSER_REQUEST);
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        startActivityForResult(intent, FILE_CHOOSER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && resultCode == RESULT_OK) {
            if (data.getData() != null) {
                selectedFileUri = data.getData();
                try {
                    String path = getPath(selectedFileUri);
                    filePathInput.setText(path);
                    statusText.setText("File selected: " + getPath(selectedFileUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getPath(Uri uri) throws IOException {
        String path = uri.getPath();
        if (path == null) {
            // Fallback for some Android versions
            return uri.toString();
        }
        return path;
    }

    private void uploadFile() {
        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show();
            return;
        }

        statusText.setText("Uploading file...");
        uploadButton.setEnabled(false);

        new Thread(() -> {
            try {
                File file = getFileFromUri(selectedFileUri);
                if (file != null) {
                    // Upload file to backend (implement your backend API here)
                    String filePath = file.getAbsolutePath();
                    String fileExtension = getFileExtension(filePath);
                    String fileType = getFileType(fileExtension);

                    // Create intent to return file info to main activity
                    Intent intent = new Intent();
                    intent.putExtra("file_path", filePath);
                    intent.putExtra("file_type", fileType);
                    intent.putExtra("file_size", file.length());
                    intent.putExtra("file_name", file.getName());
                    
                    // Send back to MainActivity
                    Intent mainIntent = new Intent(FileUploadActivity.this, MainActivity.class);
                    mainIntent.setAction("FILE_UPLOAD");
                    mainIntent.putExtras(intent);
                    startActivity(mainIntent);
                    finish();
                } else {
                    statusText.setText("File not found");
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusText.setText("Upload error: " + e.getMessage());
            } finally {
                uploadButton.setEnabled(true);
            }
        }).start();
    }

    private File getFileFromUri(Uri uri) throws IOException {
        File file = new File(uri.getPath());
        if (file.exists()) {
            return file;
        }
        // For content:// URIs, use ContentResolver to get the file
        return file; // Simplified - implement proper ContentResolver logic if needed
    }

    private String getFileExtension(String path) {
        int lastDot = path.lastIndexOf('.');
        return lastDot > 0 ? path.substring(lastDot + 1).toLowerCase() : "";
    }

    private String getFileType(String extension) {
        switch (extension.toLowerCase()) {
            case "jpg", "jpeg", "png", "gif", "webp":
                return "image";
            case "pdf":
                return "pdf";
            case "txt":
                return "text";
            case "md":
                return "markdown";
            default:
                return "unknown";
        }
    }
}
