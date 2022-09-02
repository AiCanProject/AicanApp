package com.aican.aicanapp.utils;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.aican.aicanapp.R;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class PDFViewer extends AppCompatActivity {
    private static final String TAG = "PDFViewerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfviewer);

        //getting path from previous activity
        Intent intent=getIntent();
        String path = intent.getStringExtra("Path");
        Log.d(TAG, "onCreate: path -> "+path);

        //Getting file from the path
        File file = new File(path);

        PDFView pdfView = findViewById(R.id.pdfViewer);

        pdfView.fromFile(file).load();
    }
}