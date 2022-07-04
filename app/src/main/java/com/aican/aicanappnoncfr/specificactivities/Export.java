package com.aican.aicanappnoncfr.specificactivities;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.aican.aicanappnoncfr.Dashboard.Dashboard;
import com.aican.aicanappnoncfr.R;
import com.aican.aicanappnoncfr.adapters.FileAdapter;
import com.aican.aicanappnoncfr.adapters.UserDataAdapter;
import com.aican.aicanappnoncfr.data.DatabaseHelper;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Export extends AppCompatActivity {


    //    String ph1, mv1, ph2, mv2, ph3, mv3, ph4, mv4, ph5, mv5, dt1, dt2, dt3, dt4, dt5;
    Button mDateBtn, exportUserData, exportCSV;
    ImageButton arNumBtn, batchNumBtn, compoundBtn;
    TextView tvStartDate, tvEndDate;
    TextView deviceId;
    String user, roleExport, reportDate, reportTime;
    String startDateString, endDateString, startTimeString, endTimeString, arNumString, batchNumString,compoundName;
    Integer startHour, startMinute, endHour, endMinute;
    String offset, battery, slope, temp;
    String companyName;
    String nullEntry;
    FileAdapter fAdapter;
    UserDataAdapter uAdapter;
    EditText companyNameEditText, arNumEditText, batchNumEditText, compoundNameEditText;
    DatabaseHelper databaseHelper;
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewCSV);

        deviceId = findViewById(R.id.DeviceId);
        exportCSV = findViewById(R.id.exportCSV);
        mDateBtn = findViewById(R.id.materialDateBtn);
        arNumEditText = findViewById(R.id.ar_num_sort);
        batchNumEditText = findViewById(R.id.batch_num_sort);
        arNumBtn = findViewById(R.id.ar_text_button);
        batchNumBtn = findViewById(R.id.batch_text_button);
        compoundBtn = findViewById(R.id.compound_text_button);
        compoundNameEditText = findViewById(R.id.compound_num_sort);

        companyNameEditText = findViewById(R.id.companyName);
        databaseHelper = new DatabaseHelper(this);
        nullEntry = " ";
        setFirebaseListeners();

        mDateBtn.setOnClickListener(v -> {
            MaterialDatePicker datePicker =
                    MaterialDatePicker.Builder.dateRangePicker()
                            .setSelection(new Pair(MaterialDatePicker.thisMonthInUtcMilliseconds(),
                                    MaterialDatePicker.todayInUtcMilliseconds()))
                            .setTitleText("Select dates")
                            .build();
            datePicker.show(getSupportFragmentManager(), "date");

            datePicker.addOnPositiveButtonClickListener((MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>) selection -> {
                Long startDate = selection.first;
                Long endDate = selection.second;
                startDateString = DateFormat.format("yyyy-MM-dd", new Date(startDate)).toString();
                endDateString = DateFormat.format("yyyy-MM-dd", new Date(endDate)).toString();
                String date = "Start: " + startDateString + " End: " + endDateString;
                Toast.makeText(this, date, Toast.LENGTH_SHORT).show();

                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setTitleText("Select Start Time")
                        .setHour(12)
                        .setMinute(10)
                        .build();
                timePicker.show(getSupportFragmentManager(), "time");

                timePicker.addOnPositiveButtonClickListener(dialog -> {

                    startHour = timePicker.getHour();
                    startMinute = timePicker.getMinute();

                    Calendar calendar = Calendar.getInstance();

                    calendar.set(0,0,0,startHour,startMinute);

                    startTimeString = DateFormat.format("HH:mm", calendar).toString();

                    MaterialTimePicker timePicker2 = new MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_24H)
                            .setTitleText("Select End Time")
                            .setHour(12)
                            .setMinute(10)
                            .build();
                    timePicker2.show(getSupportFragmentManager(), "time");

                    timePicker2.addOnPositiveButtonClickListener(dialog2 -> {

                        endHour = timePicker2.getHour();
                        endMinute = timePicker2.getMinute();

                        Calendar calendar2 = Calendar.getInstance();

                        calendar2.set(0,0,0,endHour, endMinute);

                        endTimeString = DateFormat.format("HH:mm", calendar2).toString();
                    });
                });
            });


        });

        arNumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arNumString = arNumEditText.getText().toString();
            }
        });

        batchNumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batchNumString = batchNumEditText.getText().toString();
            }
        });

        compoundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compoundName = compoundNameEditText.getText().toString();
            }
        });


        exportCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                exportDatabaseCsv();

                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Sensordata";
                File root = new File(path);
                root.setReadOnly();
                File[] filesAndFolders = root.listFiles();

                if (filesAndFolders == null || filesAndFolders.length == 0) {

                    return;
                } else {
                    for (int i = 0; i < filesAndFolders.length; i++) {
                        filesAndFolders[i].getName().endsWith(".csv");
                    }
                }


                fAdapter = new FileAdapter(getApplicationContext(), filesAndFolders);
                recyclerView.setAdapter(fAdapter);
                fAdapter.notifyDataSetChanged();
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            }
        });


        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Sensordata";
        String path2 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Useractivity";
        File root = new File(path);
        root.setReadOnly();
        File root2 = new File(path2);
        File[] filesAndFolders = root.listFiles();
        File[] filesAndFolders2 = root2.listFiles();

        if (filesAndFolders == null || filesAndFolders.length == 0) {
            Toast.makeText(this, "No Files Found", Toast.LENGTH_SHORT).show();
            return;
        } else {
            for (int i = 0; i < filesAndFolders.length; i++) {
                filesAndFolders[i].getName().endsWith(".csv");
            }
        }

        if (filesAndFolders2 == null || filesAndFolders2.length == 0) {
            Toast.makeText(this, "No Files Found", Toast.LENGTH_SHORT).show();
            return;
        } else {
            for (int j = 0; j < filesAndFolders2.length; j++) {
                filesAndFolders2[j].getName().endsWith(".csv");
            }
        }

        fAdapter = new FileAdapter(this, filesAndFolders);
        uAdapter = new UserDataAdapter(this, filesAndFolders2);
        recyclerView.setAdapter(fAdapter);
        fAdapter.notifyDataSetChanged();
        uAdapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (checkPermission()) {
            Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }


    }

    public void exportDatabaseCsv() {

        companyName = "Company Name: " + companyNameEditText.getText().toString();
        reportDate ="Report Date: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        reportTime ="Report Time: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        //We use the Download directory for saving our .csv file.
        File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Sensordata");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file;
        PrintWriter printWriter = null;

        try {

            file = new File(exportDir, "DataSensorLog.csv");
            file.createNewFile();
            printWriter = new PrintWriter(new FileWriter(file), true);

            SharedPreferences shp = getSharedPreferences("Extras", MODE_PRIVATE);
            offset = "Offset: " + shp.getString("offset", "");
            battery = "Battery: " + shp.getString("battery", "");
            slope = "Slope: " + shp.getString("slope", "");
            temp = "Temperature: " + shp.getString("temp", "");


            SharedPreferences shp2 = getSharedPreferences("RolePref", MODE_PRIVATE);
            roleExport = "Report Generated By: " + shp2.getString("roleSuper", "");

            Log.d("debzdate", startDateString + "," + endDateString);

            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            Cursor calibCSV = db.rawQuery("SELECT * FROM Calibdetails", null);
            Cursor  curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);


            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry+ "," + nullEntry+ "," + nullEntry+ "," + nullEntry);
            printWriter.println(companyName + "," + nullEntry + "," + nullEntry + "," + nullEntry+ "," + nullEntry+ "," + nullEntry+ "," + nullEntry);
            printWriter.println(reportDate + "," + nullEntry + "," + nullEntry + "," + nullEntry+ "," + nullEntry+ "," + nullEntry+ "," + nullEntry);
            printWriter.println(reportTime + "," + nullEntry + "," + nullEntry + "," + nullEntry+ "," + nullEntry+ "," + nullEntry+ "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry+ "," + nullEntry+ "," + nullEntry+ "," + nullEntry);
            printWriter.println(offset + "," + battery + "," + slope + "," + temp+ "," + nullEntry+ "," + nullEntry+ "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry+ "," + nullEntry+ "," + nullEntry+ "," + nullEntry);
            printWriter.println("Callibration Table" + "," + nullEntry + "," + nullEntry + "," + nullEntry+ "," + nullEntry+ "," + nullEntry+ "," + nullEntry);
            printWriter.println("pH,mV,DATE");
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry+ "," + nullEntry+ "," + nullEntry+ "," + nullEntry);


            while (calibCSV.moveToNext()) {
                String ph = calibCSV.getString(calibCSV.getColumnIndex("pH"));
                String mv = calibCSV.getString(calibCSV.getColumnIndex("mV"));
                String date = calibCSV.getString(calibCSV.getColumnIndex("date"));

                String record1 = ph + "," + mv + "," + date;

                printWriter.println(record1);
            }
            calibCSV.close();
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry+ "," + nullEntry+ "," + nullEntry+ "," + nullEntry);
            printWriter.println("Log Table" + "," + nullEntry + "," + nullEntry + "," + nullEntry+ "," + nullEntry+ "," + nullEntry+ "," + nullEntry);
            printWriter.println("Date,Time,pH,Temp,Batch,AR,Product");
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry+ "," + nullEntry+ "," + nullEntry+ "," + nullEntry);

            while (curCSV.moveToNext()) {

                String date = curCSV.getString(curCSV.getColumnIndex("date"));
                String time = curCSV.getString(curCSV.getColumnIndex("time"));
                String pH = curCSV.getString(curCSV.getColumnIndex("ph"));
                String temp = curCSV.getString(curCSV.getColumnIndex("temperature"));
                String batchnum = curCSV.getString(curCSV.getColumnIndex("batchnum"));
                String arnum = curCSV.getString(curCSV.getColumnIndex("arnum"));
                String comp = curCSV.getString(curCSV.getColumnIndex("compound"));

                String record = date + "," + time + "," + pH + "," + temp + "," + batchnum + "," + arnum + "," + comp;

                printWriter.println(record);
            }
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry+ "," + nullEntry+ "," + nullEntry+ "," + nullEntry);
            printWriter.println("Operator Sign" + "," + nullEntry + "," + nullEntry + "," + nullEntry+ "," + "Supervisor Sign"+ "," + nullEntry+ "," + nullEntry);
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