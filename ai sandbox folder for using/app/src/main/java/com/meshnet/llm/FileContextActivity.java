package com.meshnet.llm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for uploading local files to use as context for the LLM.
 * Supports images, PDFs, and text files.
 */
public class FileContextActivity extends Activity {
    
    private static final int PICK_FILE = 1;
    
    private List<File> uploadedFiles = new ArrayList<>();
    private FilePickerDialog filePickerDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_context);
        
        initFilePicker();
        setupUI();
    }
    
    private void initFilePicker() {
        filePickerDialog = new FilePickerDialog(this);
        
        findViewById(R.id.btnPickFiles).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, PICK_FILE);
        });
    }
    
    private void setupUI() {
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnClear).setOnClickListener(v -> {
            uploadedFiles.clear();
            Toast.makeText(this, "All files cleared", Toast.LENGTH_SHORT).show();
        });
        
        findViewById(R.id.btnUpload).setOnClickListener(v -> {
            if (uploadedFiles.isEmpty()) {
                Toast.makeText(this, "Please select files first", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Simulate upload
            Intent result = new Intent();
            result.putExtra("uploaded_files", uploadedFiles);
            setResult(RESULT_OK, result);
            finish();
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            
            try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                if (inputStream != null) {
                    File tempFile = saveFileToCache(inputStream, uri);
                    uploadedFiles.add(tempFile);
                    
                    Toast.makeText(this, "File added: " + tempFile.getName(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading file", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private File saveFileToCache(InputStream inputStream, Uri uri) throws IOException {
        // Get file name from URI
        String displayName = getContentResolver().getDisplay_name(uri);
        if (displayName == null) {
            displayName = "untitled";
        }
        
        // Generate unique cache path
        String cachePath = getCacheDir().getAbsolutePath() + "/uploaded_files/";
        File cacheDir = new File(cachePath);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        
        String fileName = System.currentTimeMillis() + "_" + displayName;
        File outputFile = new File(cacheDir, fileName);
        
        // Copy input stream to output file
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        byte[] buffer = new byte[4096];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        outputStream.close();
        inputStream.close();
        
        return outputFile;
    }
    
    public List<File> getUploadedFiles() {
        return uploadedFiles;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up cache
        File cacheDir = new File(getCacheDir().getAbsolutePath() + "/uploaded_files/");
        if (cacheDir.exists()) {
            deleteRecursive(cacheDir);
        }
    }
    
    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    deleteRecursive(subFile);
                }
            }
        } else {
            file.delete();
        }
    }
}
