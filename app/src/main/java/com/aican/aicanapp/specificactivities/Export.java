package com.aican.aicanapp.specificactivities;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.adapters.FileAdapter;
import com.aican.aicanapp.data.DatabaseHelper;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Export extends AppCompatActivity {


    String ph1, mv1, ph2, mv2, ph3, mv3, ph4, mv4, ph5, mv5, dt1, dt2, dt3, dt4, dt5;
    Button startDat, exportPdf;
    TextView startDate;
    TextView deviceId;
    String user;
    String companyName;
    String nullEntry;
    EditText companyNameEditText;
    DatabaseHelper databaseHelper;
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        TextView noFilesText = findViewById(R.id.nofiles_textview);
        startDate = findViewById(R.id.date);
        deviceId = findViewById(R.id.DeviceId);
        exportPdf = findViewById(R.id.authenticateRole);
        companyNameEditText = findViewById(R.id.companyName);
        databaseHelper = new DatabaseHelper(this);
        nullEntry = " ";
        setFirebaseListeners();

        SharedPreferences shp = getSharedPreferences("CalibPrefs", MODE_PRIVATE);

        mv1 = shp.getString("MV1", "");
        mv2 = shp.getString("MV2", "");
        mv3 = shp.getString("MV3", "");
        mv4 = shp.getString("MV4", "");
        mv5 = shp.getString("MV5", "");

        dt1 = shp.getString("DT1", "");
        dt2 = shp.getString("DT2", "");
        dt3 = shp.getString("DT3", "");
        dt4 = shp.getString("DT4", "");
        dt5 = shp.getString("DT5", "");

        ph1 = "1.2";
        ph2 = "4.0";
        ph3 = "7.0";
        ph4 = "9.2";
        ph5 = "12.0";



        exportPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                exportDatabaseCsv();

                deleteAll();

                databaseHelper.insertCalibData(ph1, mv1, dt1);
                databaseHelper.insertCalibData(ph2, mv2, dt2);
                databaseHelper.insertCalibData(ph3, mv3, dt3);
                databaseHelper.insertCalibData(ph4, mv4, dt4);
                databaseHelper.insertCalibData(ph5, mv5, dt5);


            }
        });
//        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
//        builder.setTitleText("SELECT A RANGE OF DATE");
//        final MaterialDatePicker materialDatePicker = builder.build();

        //Range OF DATE
//        startDat.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
//            }
//        });

//        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
//            @Override
//            public void onPositiveButtonClick(Object selection) {
//            startDate.setText(materialDatePicker.getHeaderText());
//            }
//        });


        if (checkPermission()) {
            Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }

        String pdfPattern = ".csv";
        String path = Environment.getExternalStorageDirectory().getPath() + "/Download/";
        File root = new File(path);
        File[] filesAndFolders = root.listFiles();

        if (filesAndFolders == null || filesAndFolders.length == 0) {
            noFilesText.setVisibility(View.VISIBLE);
            return;
        } else {
            for (int i = 0; i < filesAndFolders.length; i++) {
                if (filesAndFolders[i].getName().endsWith(pdfPattern)) {
                    return;
                }
            }
        }

        noFilesText.setVisibility(View.INVISIBLE);
        recyclerView.setAdapter(new FileAdapter(this, filesAndFolders));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }

    public void exportDatabaseCsv() {

        companyName = "Company: " + companyNameEditText.getText().toString();

        //We use the Download directory for saving our .csv file.
        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file;
        PrintWriter printWriter = null;

        try {

            file = new File(exportDir, "LogCSV.csv");
            file.createNewFile();
            printWriter = new PrintWriter(new FileWriter(file), true);

            SharedPreferences shp = getSharedPreferences("MySharedPrefs", MODE_PRIVATE);
            user = "User: " + shp.getString("userid", "");

            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            Cursor calibCSV = db.rawQuery("SELECT * FROM Calibdetails", null);
            Cursor curCSV = db.rawQuery("SELECT * FROM LogUserdetails", null);

            printWriter.println(companyName + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(user + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("Callibration Table" + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("pH,mV,DATE");
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);


            while (calibCSV.moveToNext()) {
                String ph = calibCSV.getString(calibCSV.getColumnIndex("pH"));
                String mv = calibCSV.getString(calibCSV.getColumnIndex("mV"));
                String date = calibCSV.getString(calibCSV.getColumnIndex("date"));

                String record1 = ph + "," + mv + "," + date;

                printWriter.println(record1);
            }

            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("Log Table" + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("TIME,pH,TEMP,NAME");

            while (curCSV.moveToNext()) {

                String time = curCSV.getString(curCSV.getColumnIndex("time"));
                String pH = curCSV.getString(curCSV.getColumnIndex("ph"));
                String temp = curCSV.getString(curCSV.getColumnIndex("temperature"));
                String comp = curCSV.getString(curCSV.getColumnIndex("compound"));

                String record = time + "," + pH + "," + temp + "," + comp;

                printWriter.println(record);
            }
            curCSV.close();
            db.close();

        } catch (Exception e) {
            Log.d("csvexception", String.valueOf(e));
        }
    }

    public void deleteAll()
    {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.execSQL("DELETE FROM Calibdetails");
        db.close();
    }

//    private void generatePDFOLD() {
//        PdfDocument pdfDocument = new PdfDocument();
//        Paint paint = new Paint();
//
//        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pagewidth, pageHeight, 1).create();
//        PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);
//        Canvas canvas = myPage.getCanvas();
//
//        paint.setTextSize(60);
//        canvas.drawText("AICAN AUTOMATE", 30, 80, paint);
//
//        paint.setTextSize(40);
//        paint.setTextAlign(Paint.Align.RIGHT);
//        canvas.drawText("12/02/2022 6:30", canvas.getWidth() - 40, 80, paint);
//
//        paint.setColor(Color.rgb(150, 150, 150));
//        canvas.drawRect(30, 150, canvas.getWidth() - 30, 160, paint);
//
//        paint.setTextSize(20);
//        canvas.drawText("Device Id: EPT2001", 200, 190, paint);
//
//        paint.setTextSize(20);
//        canvas.drawText("Last Calibration Date & Time: 16/02/2022 4:45", 380, 220, paint);
//
//        paint.setTextSize(30);
//        canvas.drawText("Slope: 60%", canvas.getWidth() - 40, 190, paint);
//
//        paint.setTextSize(30);
//        canvas.drawText("Temperature: 30", canvas.getWidth() - 40, 230, paint);
//
//        paint.setTextSize(30);
//        canvas.drawText("Offset: 40", canvas.getWidth() - 40, 270, paint);
//
//        paint.setColor(Color.rgb(150, 150, 150));
//        canvas.drawRect(30, 180, canvas.getWidth() - 30, canvas.getHeight() - 30, paint);
//
//        pdfDocument.finishPage(myPage);
//
//        String stringFilePath = Environment.getExternalStorageDirectory().getPath() + "/Download/World.pdf";
//        File file = new File(stringFilePath);
//
//        try {
//            pdfDocument.writeTo(new FileOutputStream(file));
//            Toast.makeText(this, "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        pdfDocument.close();
//    }
//
//    private void generatePDF() {
//        //Source.status = false;
//        PdfDocument pdfDocument = new PdfDocument();
//        Paint paint = new Paint();
//
//        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pagewidth, pageHeight, 1).create();
//        PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);
//        Canvas canvas = myPage.getCanvas();
//
//        paint.setTextSize(60);
//        canvas.drawText("AICAN AUTOMATE", 30, 80, paint);
//
//        paint.setTextSize(40);
//        paint.setTextAlign(Paint.Align.RIGHT);
//        canvas.drawText("12/02/2022 6:30", canvas.getWidth() - 40, 80, paint);
//
//        paint.setColor(Color.rgb(150, 150, 150));
//        canvas.drawRect(30, 150, canvas.getWidth() - 30, 160, paint);
//
//        paint.setTextSize(20);
//        canvas.drawText("Device Id: EPT2001", 200, 190, paint);
//
//        paint.setTextSize(20);
//        canvas.drawText("Last Calibration Date & Time: 16/02/2022 4:45", 380, 220, paint);
//
//        paint.setTextSize(30);
//        canvas.drawText("Slope: 60%", canvas.getWidth() - 40, 190, paint);
//
//        paint.setTextSize(30);
//        canvas.drawText("Temperature: 30", canvas.getWidth() - 40, 230, paint);
//
//        paint.setTextSize(30);
//        canvas.drawText("Offset: 40", canvas.getWidth() - 40, 270, paint);
//
//        paint.setColor(Color.rgb(150, 150, 150));
//        canvas.drawRect(30, 180, canvas.getWidth() - 30, canvas.getHeight() - 30, paint);
//
//        pdfDocument.finishPage(myPage);
//
//        String path = Environment.getExternalStorageDirectory().getPath() + "/Download/Test.pdf";
//        File dir = new File(path);
////        if (!dir.exists())
////            dir.mkdirs();
////
////        File filePath = new File(dir, "Test.pdf");
//
//        try {
//            pdfDocument.writeTo(new FileOutputStream(dir));
//            Toast.makeText(this, "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
//            //btn_generate.setText("Check PDF");
//            //boolean_save=true;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
//        }
//
//        pdfDocument.close();
//
////        String stringFilePath = Environment.getExternalStorageDirectory().getPath() + "/Download/ProgrammerWorld.pdf";
////        File file = new File(stringFilePath);
////
////        try {
////            pdfDocument.writeTo(new FileOutputStream(file));
////            Toast.makeText(this, "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
////
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////        pdfDocument.close();
//    }

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

}