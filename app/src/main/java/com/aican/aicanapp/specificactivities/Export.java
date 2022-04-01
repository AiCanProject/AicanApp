package com.aican.aicanapp.specificactivities;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.aican.aicanapp.adapters.FileAdapter;
import com.aican.aicanapp.data.DatabaseHelper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Export extends AppCompatActivity {


//    String ph1, mv1, ph2, mv2, ph3, mv3, ph4, mv4, ph5, mv5, dt1, dt2, dt3, dt4, dt5;
    Button startDat, exportPdf;
    TextView startDate;
    TextView deviceId;
    String user;
    String companyName;
    String nullEntry;
    FileAdapter fAdapter;
    EditText companyNameEditText;
    DatabaseHelper databaseHelper;
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewCSV);
        TextView noFilesText = findViewById(R.id.nofiles_textview);
        startDate = findViewById(R.id.date);
        deviceId = findViewById(R.id.DeviceId);
        exportPdf = findViewById(R.id.authenticateRole);
        companyNameEditText = findViewById(R.id.companyName);
        databaseHelper = new DatabaseHelper(this);
        nullEntry = " ";
        setFirebaseListeners();

        exportPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                exportDatabaseCsv();

                String pdfPattern = ".csv";
                String path = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).toString();
                File root = new File(path);
                File[] filesAndFolders = root.listFiles();

                if (filesAndFolders == null || filesAndFolders.length == 0) {
                    noFilesText.setVisibility(View.VISIBLE);
                    return;
                } else {
                    for (int i = 0; i < filesAndFolders.length; i++) {
                        filesAndFolders[i].getName().endsWith(pdfPattern);
                    }
                }

                noFilesText.setVisibility(View.INVISIBLE);
                fAdapter = new FileAdapter(getApplicationContext(), filesAndFolders);
                recyclerView.setAdapter(fAdapter);
                fAdapter.notifyDataSetChanged();
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            }
        });


        String path = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).toString();
        File root = new File(path);
        File[] filesAndFolders = root.listFiles();

        noFilesText.setVisibility(View.INVISIBLE);
        fAdapter = new FileAdapter(this, filesAndFolders);
        recyclerView.setAdapter(fAdapter);
        fAdapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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



    }

    public void exportDatabaseCsv() {

        companyName = "Company: " + companyNameEditText.getText().toString();

        //We use the Download directory for saving our .csv file.
        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        //File exportDir = new File(Environment.getExternalStorageDirectory()+ File.separator+ "AICAN");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file;
        PrintWriter printWriter = null;

        try {
            String fileName = new SimpleDateFormat("yyyyMMddHHmmss'.csv'").format(new Date());

            file = new File(exportDir, fileName);
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