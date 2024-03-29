package com.aican.aicanapp.ProbeScan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.data.DatabaseHelper;
import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProbeScanner extends AppCompatActivity implements OnQrResultListener {

    private static final int RC_CAMERA_PERMISSION = 100;
    private static final String[] permissions = new String[]{
            Manifest.permission.CAMERA
    };
    PreviewView previewView;
    ImageAnalysis analyzer;
    ProcessCameraProvider cameraProvider;
    ImageView iv;
    private ExecutorService cameraExecutor;
    EditText etId;
    DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        float ratio = (float) (dm.heightPixels) / dm.widthPixels;
        if (ratio > 1.8) {
            setContentView(R.layout.activity_scan_qr);
        } else {
            setContentView(R.layout.activity_scan_qr_small);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        iv = findViewById(R.id.iv);
        previewView = findViewById(R.id.previewView);
        etId = findViewById(R.id.etId);

        if (checkPermissions()) {
            removeCameraRequiredLayout();
            startCamera();
        } else {
            setCameraRequiredLayout();
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        etId.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onQrResult(etId.getText().toString());
                return true;
            }
            return false;
        });

        databaseHelper = new DatabaseHelper(this);

    }

    private void removeCameraRequiredLayout() {
        Glide.with(this).load("file:///android_asset/scan_qr.gif").into(iv);
    }

    private void setCameraRequiredLayout() {
    }

    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                analyzer = new ImageAnalysis.Builder()
                        .build();
                ImageScanner scanner = new ImageScanner(this);
                analyzer.setAnalyzer(cameraExecutor, scanner);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, analyzer);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        boolean allGranted = true;
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (!allGranted)
            requestPermissions(permissions, RC_CAMERA_PERMISSION);
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == RC_CAMERA_PERMISSION) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                }
            }
            if (!allGranted) {
                Toast.makeText(this, "Permission needed for app to work.", Toast.LENGTH_SHORT).show();
            } else {
                removeCameraRequiredLayout();
                startCamera();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onQrResult(String result) {
        if (cameraProvider != null && analyzer != null) {
            cameraProvider.unbind(analyzer);
        }
        probeDetail(result);
    }

    private void probeDetail(String result) {
//        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

        String activity = getIntent().getStringExtra("activity");
//        Toast.makeText(this, activity, Toast.LENGTH_SHORT).show();
        Source.scannerData = result;

        if (activity.equals("PhFragment")) {

            boolean inserted = databaseHelper.insert_probe(result);
            Source.scannerData = "-";
            if (inserted) {
                Toast.makeText(this, "inserted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error while inserting", Toast.LENGTH_SHORT).show();
            }
        }

        if (activity.equals("EcFragment")) {

            boolean inserted = databaseHelper.insert_ec_probe(result);
            Source.scannerData = "-";
            if (inserted) {
                Toast.makeText(this, "inserted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error while inserting", Toast.LENGTH_SHORT).show();
            }
        }

        if (activity.equals("PhCalibFragment")) {
            String view = getIntent().getStringExtra("view");
//            Source.scannerData = result;
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            switch (view) {
                case "qr1":
                    myEdit.putString("BFD1", result);
                    myEdit.commit();
                    break;
                case "qr2":
                    myEdit.putString("BFD2", result);
                    myEdit.commit();
                    break;
                case "qr3":
                    myEdit.putString("BFD3", result);
                    myEdit.commit();
                    break;
                case "qr4":
                    myEdit.putString("BFD4", result);
                    myEdit.commit();
                    break;
                case "qr5":
                    myEdit.putString("BFD5", result);
                    myEdit.commit();
                    break;
                default:
                    break;

            }
        }

        finish();

    }

    private static class ImageScanner implements ImageAnalysis.Analyzer {

        OnQrResultListener onQrResultListener;
        BarcodeScanner scanner;

        public ImageScanner(OnQrResultListener onQrResultListener) {
            this.onQrResultListener = onQrResultListener;
            BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build();
            scanner = BarcodeScanning.getClient(options);
        }

        @Override
        public void analyze(@NonNull ImageProxy image) {
            scanImageForQr(image);
        }

        private void scanImageForQr(ImageProxy imageProxy) {
            @SuppressLint("UnsafeOptInUsageError") Image mediaImage = imageProxy.getImage();
            if (mediaImage == null) {
                return;
            }

            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            scanner.process(image).addOnSuccessListener(barcodes -> {
                for (int i = 0; i < barcodes.size(); ++i) {
                    String result = barcodes.get(i).getRawValue();
                    if (validateQrResult(result)) {
                        onQrResultListener.onQrResult(result);
                        break;
                    }
                }
                imageProxy.close();
            });

        }

        private boolean validateQrResult(String rawValue) {
            //TODO
            return true;
        }

    }

}