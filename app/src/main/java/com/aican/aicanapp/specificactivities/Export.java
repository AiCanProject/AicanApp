package com.aican.aicanapp.specificactivities;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.adapters.FileAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Export extends AppCompatActivity {

    TextView startDate, endDate;
    TextView deviceId;
    Button button;
    int pageHeight = 900;
    int pagewidth = 1280;
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        button = findViewById(R.id.authenticateRole);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        TextView noFilesText = findViewById(R.id.nofiles_textview);
        startDate = findViewById(R.id.date);
        endDate = findViewById(R.id.endDate);
        deviceId = findViewById(R.id.DeviceId);
        setFirebaseListeners();


        if (checkPermission()) {
            Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }

        String pdfPattern = ".pdf";
        String path = Environment.getExternalStorageDirectory().getPath() + "/Download/";
        File root = new File(path);
        File[] filesAndFolders = root.listFiles();

        if(filesAndFolders==null || filesAndFolders.length ==0){
            noFilesText.setVisibility(View.VISIBLE);
            return;
        }else {
           for (int i = 0; i<filesAndFolders.length; i++) {
               if (filesAndFolders[i].getName().endsWith(pdfPattern)) {
                   return;
               }
           }
        }

        noFilesText.setVisibility(View.INVISIBLE);
        recyclerView.setAdapter(new FileAdapter(this,filesAndFolders));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePDF();
            }
        });

        startDate.setOnClickListener(view -> getCurrentDate(startDate));

        endDate.setOnClickListener(view -> getCurrentDate(endDate));
    }

    private boolean checkPermission() {
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(getApplicationContext(), "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denined.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void getCurrentDate(TextView tvDate) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getApplicationContext(), (view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            tvDate.setText(simpleDateFormat.format(calendar.getTime()));
        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void setFirebaseListeners() {
        DatabaseReference dataRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child(Dashboard.DEVICE_TYPE_PH).child(PhActivity.DEVICE_ID);
        dataRef.child("ID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @com.google.firebase.database.annotations.NotNull DataSnapshot snapshot) {
                String p = snapshot.getValue(String.class);
                deviceId.setText(p);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }

    private void generatePDF() {
        //Source.status = false;
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();

        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pagewidth, pageHeight, 1).create();
        PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);
        Canvas canvas = myPage.getCanvas();

        paint.setTextSize(60);
        canvas.drawText("AICAN AUTOMATE", 30, 80, paint);

        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("12/02/2022 6:30", canvas.getWidth() - 40, 80, paint);

        paint.setColor(Color.rgb(150, 150, 150));
        canvas.drawRect(30, 150, canvas.getWidth() - 30, 160, paint);

        paint.setTextSize(20);
        canvas.drawText("Device Id: EPT2001", 200, 190, paint);

        paint.setTextSize(20);
        canvas.drawText("Last Calibration Date & Time: 16/02/2022 4:45", 380, 220, paint);

        paint.setTextSize(30);
        canvas.drawText("Slope: 60%", canvas.getWidth() - 40, 190, paint);

        paint.setTextSize(30);
        canvas.drawText("Temperature: 30", canvas.getWidth() - 40, 230, paint);

        paint.setTextSize(30);
        canvas.drawText("Offset: 40", canvas.getWidth() - 40, 270, paint);

        paint.setColor(Color.rgb(150, 150, 150));
        canvas.drawRect(30, 180, canvas.getWidth() - 30, canvas.getHeight() - 30, paint);

        pdfDocument.finishPage(myPage);

//        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/PdfTest/";
//        File dir = new File(path);
//        if (!dir.exists())
//            dir.mkdirs();
//
//        File filePath = new File(dir, "Test.pdf");
//
//        try {
//            pdfDocument.writeTo(new FileOutputStream(filePath));
//            Toast.makeText(requireContext(), "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
//            //btn_generate.setText("Check PDF");
//            //boolean_save=true;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(requireContext(), "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
//        }
//
//        pdfDocument.close();

        String stringFilePath = Environment.getExternalStorageDirectory().getPath() + "/Download/ProgrammerWorld.pdf";
        File file = new File(stringFilePath);

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF file generated successfully.", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
        pdfDocument.close();
    }

}