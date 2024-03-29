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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.adapters.FileAdapter;
import com.aican.aicanapp.adapters.UserDataAdapter;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aspose.cells.FileFormatType;
import com.aspose.cells.LoadOptions;
import com.aspose.cells.PdfCompliance;
import com.aspose.cells.PdfSaveOptions;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EcExport extends AppCompatActivity {


    //    String ph1, mv1, ph2, mv2, ph3, mv3, ph4, mv4, ph5, mv5, dt1, dt2, dt3, dt4, dt5;
    Button mDateBtn, exportUserData, exportCSV, convertToXls;
    ImageButton arNumBtn, batchNumBtn, compoundBtn;
    TextView tvStartDate, tvEndDate, tvUserLog;
    TextView deviceId;
    String deviceID;
    String user, roleExport, reportDate, reportTime;
    String startDateString, endDateString, startTimeString, endTimeString, arNumString, batchNumString, compoundName;
    String offset, battery, slope, temp;
    Integer startHour, startMinute, endHour, endMinute;
    String companyName;
    String nullEntry;
    FileAdapter fAdapter;
    UserDataAdapter uAdapter;
    EditText companyNameEditText, arNumEditText, batchNumEditText, compoundNameEditText;
    DatabaseHelper databaseHelper;
    private DatabaseReference deviceRef;
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ec_export);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewCSV);
        RecyclerView userRecyclerView = findViewById(R.id.recyclerViewUserData);
//        TextView noFilesText = findViewById(R.id.nofiles_textview);
        deviceId = findViewById(R.id.DeviceId);
        exportCSV = findViewById(R.id.exportCSV);
        mDateBtn = findViewById(R.id.materialDateBtn);
        arNumEditText = findViewById(R.id.ar_num_sort);
        exportUserData = findViewById(R.id.exportUserData);
        batchNumEditText = findViewById(R.id.batch_num_sort);
        arNumBtn = findViewById(R.id.ar_text_button);
        batchNumBtn = findViewById(R.id.batch_text_button);
        compoundBtn = findViewById(R.id.compound_text_button);
        compoundNameEditText = findViewById(R.id.compound_num_sort);
        convertToXls = findViewById(R.id.convertToXls);
        tvUserLog = findViewById(R.id.tvUserLog);
        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(EcActivity.DEVICE_ID)).getReference().child("ECMETER").child(EcActivity.DEVICE_ID);

        companyNameEditText = findViewById(R.id.companyName);
        databaseHelper = new DatabaseHelper(this);
        nullEntry = " ";
        setFirebaseListeners();

        convertToXls.setVisibility(View.INVISIBLE);

        if (Source.subscription.equals("nonCfr")) {
            exportUserData.setVisibility(View.GONE);
            userRecyclerView.setVisibility(View.GONE);
            tvUserLog.setVisibility(View.GONE);
        }


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

                    calendar.set(0, 0, 0, startHour, startMinute);

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

                        calendar2.set(0, 0, 0, endHour, endMinute);

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

                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcExcelFiles/";
                File root = new File(path);
                fileNotWrite(root);
                File[] filesAndFolders = root.listFiles();

                if (filesAndFolders == null || filesAndFolders.length == 0) {

                    return;
                } else {
                    for (int i = 0; i < filesAndFolders.length; i++) {
                        filesAndFolders[i].getName().endsWith(".xlsx");
                    }
                }
                try {
                    Bitmap sign = getSign();
                    if (sign != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();

                        sign.compress(Bitmap.CompressFormat.PNG, 100, stream);

                        byte[] bitmapData = stream.toByteArray();
                    }

                    Workbook workbook = new Workbook(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcExcelFiles/DataSensorLog.xlsx");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());
                    PdfSaveOptions options = new PdfSaveOptions();
                    options.setCompliance(PdfCompliance.PDF_A_1_B);
                    String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcSensordata";
                    File tempRoot = new File(tempPath);
                    fileNotWrite(tempRoot);
                    File[] tempFilesAndFolders = tempRoot.listFiles();
                    workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcSensordata/DSL" + currentDateandTime + "_" + (tempFilesAndFolders.length - 1) + ".pdf", options);

                    String path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcSensordata";
                    File root1 = new File(path1);
                    fileNotWrite(root1);
                    File[] filesAndFolders1 = root1.listFiles();

                    if (filesAndFolders1 == null || filesAndFolders1.length == 0) {

                        return;
                    } else {
                        for (int i = 0; i < filesAndFolders1.length; i++) {
                            if (filesAndFolders1[i].getName().endsWith(".csv") || filesAndFolders1[i].getName().endsWith(".xlsx")) {
                                filesAndFolders1[i].delete();
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


                String pathPDF = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcSensordata/";
                File rootPDF = new File(pathPDF);
                fileNotWrite(root);
                File[] filesAndFoldersPDF = rootPDF.listFiles();


                fAdapter = new FileAdapter(getApplicationContext(), filesAndFoldersPDF, "EcExport");
                recyclerView.setAdapter(fAdapter);
                fAdapter.notifyDataSetChanged();
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                convertToXls.setVisibility(View.INVISIBLE);
            }
        });

        convertToXls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Workbook workbook = new Workbook(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcExcelFiles/DataSensorLog.xlsx");

                    PdfSaveOptions options = new PdfSaveOptions();
                    options.setCompliance(PdfCompliance.PDF_A_1_B);
                    workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcSensordata/DataSensorLog.pdf", options);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        exportUserData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Boolean[] isSuccessful = {false};

                companyName = "";
                if (companyName.isEmpty()) {
                    deviceRef.child("UI").child("EC").child("EC_CAL").child("COMPANY_NAME").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                companyName = String.valueOf(task.getResult().getValue());
                                isSuccessful[0] = true;
                                Log.d("TAG", "onComplete: on success " + companyName);
                                databaseHelper.insert_action_data("","" , "Userdata exported", "", "", "", "", EcActivity.DEVICE_ID);
                            } else {
                                companyNameEditText.setError("Enter Company Name");
                            }
                        }
                    });
                } else {
                    deviceRef.child("UI").child("EC").child("EC_CAL").child("COMPANY_NAME").setValue(companyName);
                    isSuccessful[0] = true;
                }

                if (true) {

                    exportUserData();


                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcUseractivity";
                    File root = new File(path);
                    fileNotWrite(root);
                    File[] filesAndFolders = root.listFiles();

                    if (filesAndFolders == null || filesAndFolders.length == 0) {

                        return;
                    } else {
                        for (int i = 0; i < filesAndFolders.length; i++) {
                            filesAndFolders[i].getName().endsWith(".csv");
                        }
                    }


                    try {
                        String path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcUseractivity/";
                        File root1 = new File(path1);
                        fileNotWrite(root1);
                        File[] filesAndFolders1 = root1.listFiles();

                        if (filesAndFolders1 == null || filesAndFolders1.length == 0) {

                            return;
                        } else {
                            for (int i = 0; i < filesAndFolders1.length; i++) {
                                filesAndFolders1[i].getName().endsWith(".xlsx");
                            }
                        }

                        Workbook workbook = null;

                        workbook = new Workbook(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcUseractivity/DataUserActivity.xlsx");

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault());
                        String currentDateandTime = sdf.format(new Date());
                        PdfSaveOptions options = new PdfSaveOptions();
                        options.setCompliance(PdfCompliance.PDF_A_1_B);
                        String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcUseractivity";
                        File tempRoot = new File(tempPath);
                        fileNotWrite(tempRoot);
                        File[] tempFilesAndFolders = tempRoot.listFiles();
                        workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcUseractivity/DataUserActivity" + currentDateandTime + "_" + (tempFilesAndFolders.length - 1) + ".pdf", options);

                        String path_1 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcUseractivity";
                        File root_1 = new File(path_1);
                        fileNotWrite(root_1);
                        File[] filesAndFolders_1 = root_1.listFiles();

                        if (filesAndFolders_1 == null || filesAndFolders_1.length == 0) {

                            return;
                        } else {
                            for (int i = 0; i < filesAndFolders1.length; i++) {
                                if (filesAndFolders_1[i].getName().endsWith(".csv") || filesAndFolders_1[i].getName().endsWith(".xlsx")) {
                                    filesAndFolders_1[i].delete();
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String pathPDF = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcUseractivity/";
                    File rootPDF = new File(pathPDF);
                    fileNotWrite(root);
                    File[] filesAndFoldersPDF = rootPDF.listFiles();


                    uAdapter = new UserDataAdapter(EcExport.this, filesAndFoldersPDF);
                    userRecyclerView.setAdapter(uAdapter);
                    uAdapter.notifyDataSetChanged();
                    userRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                }

            }
        });

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcExcelFiles/";
        String path2 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcUseractivity";
        File root = new File(path);
        File root2 = new File(path2);
        fileNotWrite(root);
        fileNotWrite(root2);
        File[] filesAndFolders = root.listFiles();
        File[] filesAndFolders2 = root2.listFiles();

        if (filesAndFolders == null || filesAndFolders.length == 0) {
            Toast.makeText(this, "No Files Found", Toast.LENGTH_SHORT).show();
            return;
        } else {
            for (int i = 0; i < filesAndFolders.length; i++) {
                filesAndFolders[i].getName().endsWith(".pdf");
            }
        }

        if (filesAndFolders2 == null || filesAndFolders2.length == 0) {
            Toast.makeText(this, "No Files Found", Toast.LENGTH_SHORT).show();
            return;
        } else {
            for (int j = 0; j < filesAndFolders2.length; j++) {
                filesAndFolders2[j].getName().endsWith(".pdf");
            }
        }

        fAdapter = new FileAdapter(this, filesAndFolders, "EcExport");
        uAdapter = new UserDataAdapter(this, filesAndFolders2);
        recyclerView.setAdapter(fAdapter);
        userRecyclerView.setAdapter(uAdapter);
        fAdapter.notifyDataSetChanged();
        uAdapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        if (checkPermission()) {
            Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }


    }

    private Bitmap getSign() {
        SharedPreferences sh = getSharedPreferences("signature", Context.MODE_PRIVATE);
        String photo = sh.getString("signature_data", "");
        Bitmap bitmap = null;

        if (!photo.equalsIgnoreCase("")) {
            byte[] b = Base64.decode(photo, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return bitmap;
    }

    public void exportDatabaseCsv() {

        companyName = "Company: " + companyNameEditText.getText().toString();
        reportDate = "Date: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        reportTime = "Time: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        //We use the Download directory for saving our .csv file.
        File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcSensordata");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File outputDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcExcelFiles");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
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

            setFirebaseListeners();


            SharedPreferences shp2 = getSharedPreferences("RolePref", MODE_PRIVATE);
            roleExport = "Made By: " + shp2.getString("roleSuper", "");

            Log.d("debzdate", startDateString + "," + endDateString);

            SQLiteDatabase db = databaseHelper.getWritableDatabase();

//            Cursor calibCSV = db.rawQuery("SELECT * FROM CalibData", null);
            Cursor cursor = db.rawQuery("SELECT * FROM LogECdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND ('" + compoundName + "') AND (batch = '" + batchNumString + "') AND (productName = '" + arNumString + "')", null);


//            Cursor curCSV = db.rawQuery("Select * from LogECdetails ",null);
//            Cursor cursor = db.rawQuery("Select * from LogECdetails", null);

            if (arNumEditText.getText().toString().isEmpty()) {
                arNumString = null;
            }

            if (compoundNameEditText.getText().toString().isEmpty()) {
                compoundName = null;
            }

            if (batchNumEditText.getText().toString().isEmpty()) {
                batchNumString = null;
            }

            //Setting sql query according to filer

            if (startDateString != null && compoundName != null && batchNumString != null && arNumString != null) {

                cursor = db.rawQuery("SELECT * FROM LogECdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);

            } else if (startDateString != null && compoundName != null && batchNumString != null) {

                cursor = db.rawQuery("SELECT * FROM LogECdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "')", null);

            } else if (startDateString != null && compoundName != null && arNumString != null) {

                cursor = db.rawQuery("SELECT * FROM LogECdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);

            } else if (startDateString != null && batchNumString != null && arNumString != null) {

                cursor = db.rawQuery("SELECT * FROM LogECdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);

            } else if (compoundName != null && batchNumString != null && arNumString != null) {

                cursor = db.rawQuery("SELECT * FROM LogECdetails WHERE  (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);

            } else if (startDateString != null && compoundName != null) {

                cursor = db.rawQuery("SELECT * FROM LogECdetails WHERE  (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (arnum = '" + compoundName + "')", null);

            } else if (startDateString != null && batchNumString != null) {

                cursor = db.rawQuery("SELECT * FROM LogECdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (batchnum = '" + batchNumString + "')", null);

            } else if (startDateString != null && arNumString != null) {

                cursor = db.rawQuery("SELECT * FROM LogECdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (compound = '" + arNumString + "')", null);

            } else if (compoundName != null && batchNumString != null) {

                cursor = db.rawQuery("SELECT * FROM LogECdetails WHERE  (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "')", null);

            } else if (compoundName != null && arNumString != null) {

                cursor = db.rawQuery("SELECT * FROM LogECdetails WHERE  (arnum = '" + compoundName + "') AND (compound = '" + arNumString + "')", null);

            } else if (batchNumString != null && arNumString != null) {

                cursor = db.rawQuery("SELECT * FROM LogECdetails WHERE  (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);

            } else if (startDateString != null) {

                cursor = db.rawQuery("SELECT * FROM LogECdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "')", null);

            } else if (compoundName != null) {

                cursor = db.rawQuery("SELECT * FROM LogECdetails WHERE  (arnum = '" + compoundName + "')", null);

            } else if (batchNumString != null) {

                cursor = db.rawQuery("SELECT * FROM LogECdetails WHERE  (batchnum = '" + batchNumString + "') ", null);

            } else if (arNumString != null) {

                cursor = db.rawQuery("SELECT * FROM LogECdetails WHERE  (compound = '" + arNumString + "')", null);

            } else {
                cursor = db.rawQuery("SELECT * FROM LogECdetails", null);
            }


            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + companyName);
            printWriter.println(reportDate);
            printWriter.println(reportTime);
            printWriter.println(roleExport);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
//            printWriter.println(offset + "," + battery + "," + temp + "," + slope+ "," + nullEntry+ "," + nullEntry+ "," + nullEntry);
            printWriter.println(offset + "," + battery);
            printWriter.println(temp);
            printWriter.println(slope);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println();
            printWriter.println("Calibration Table");
//            printWriter.println("pH,mV,DATE");
            printWriter.println("____pH________mV____DATE_____TIME___");

            printWriter.println();


//            while (calibCSV.moveToNext()) {
//                String ph = calibCSV.getString(calibCSV.getColumnIndex("PH"));
//                String mv = calibCSV.getString(calibCSV.getColumnIndex("MV"));
//                String date = calibCSV.getString(calibCSV.getColumnIndex("DT"));
//
//
//                String record1 = ph + "," + mv + "," + date.substring(0, 10) + "," + date.substring(10, 16);
//
////                String record1 = ph + "," + mv + "," + date;
//
//                printWriter.println(record1);
//            }
//            calibCSV.close();
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("_____Date______Time_____Conduct______TDS___Temp___productName___batch___Device___");
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);


            while (cursor.moveToNext()) {

                String date = cursor.getString(cursor.getColumnIndex("date"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String conductivity = cursor.getString(cursor.getColumnIndex("conductivity"));
                String tds = cursor.getString(cursor.getColumnIndex("TDS"));
                String temperature = cursor.getString(cursor.getColumnIndex("temperature"));
                String productName = cursor.getString(cursor.getColumnIndex("productName"));
                String batch = cursor.getString(cursor.getColumnIndex("batch"));
                String device = cursor.getString(cursor.getColumnIndex("deviceID"));

                String record = date + "," + time + "," + conductivity + "," + tds + "," + temperature + "," + productName + "," + batch + "," + device;

                printWriter.println(record);
            }
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("Operator Sign");
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + "," + nullEntry + "," + "Supervisor Sign");
            fileNotWrite(file);
            cursor.close();
            db.close();

            LoadOptions loadOptions = new LoadOptions(FileFormatType.CSV);
            String inputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcSensordata/";

            Workbook workbook = new Workbook(inputFile + "DataSensorLog.csv", loadOptions);
            Worksheet worksheet = workbook.getWorksheets().get(0);
            worksheet.getCells().setColumnWidth(0, 10.0);
            worksheet.getCells().setColumnWidth(1, 8.0);
            worksheet.getCells().setColumnWidth(2, 8.0);
            worksheet.getCells().setColumnWidth(3, 10.0);
            worksheet.getCells().setColumnWidth(4, 10.0);
            worksheet.getCells().setColumnWidth(5, 10.0);
            worksheet.getCells().setColumnWidth(6, 10.0);
            worksheet.getCells().setColumnWidth(7, 10.0);
            workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcExcelFiles/DataSensorLog.xlsx", SaveFormat.XLSX);

        } catch (Exception e) {
            Log.d("csvexception", String.valueOf(e));
        }
    }

    public void fileNotWrite(File file) {
        file.setWritable(false);
        if (file.canWrite()) {
            Log.d("csv", "Nhi kaam kar rha");
        } else {
            Log.d("csvnw", "Party Bhaiiiii");
        }
    }

    public void exportUserData() {
        File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcUseractivity");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file;
        PrintWriter printWriter = null;

        try {

            file = new File(exportDir, "DataUserActivity.csv");
            file.createNewFile();


            printWriter = new PrintWriter(new FileWriter(file), true);


            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            SharedPreferences shp2 = getSharedPreferences("RolePref", MODE_PRIVATE);
            roleExport = "Supervisor: " + shp2.getString("roleSuper", "");

//            Cursor userCSV = db.rawQuery("SELECT * FROM UserActiondetails", null);


            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(companyName + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(reportDate + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(reportTime + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(roleExport + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(offset + "," + battery + "," + temp + "," + slope + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("User Activity Table" + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("DATE,TIME,ACTIVITY,pH,TEMP,mV");
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);

//            printWriter.println(companyName + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
//            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
//            printWriter.println(roleExport + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
//            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
//            printWriter.println(offset + "," + battery + "," + slope + "," + temp);
//            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
//            printWriter.println("User Activity Table" + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
//            printWriter.println("TIME,ACTIVITY,pH,TEMPERATURE,mV");
//            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);


//            while (userCSV.moveToNext()) {
//                String Time = userCSV.getString(userCSV.getColumnIndex("time"));
//                String Activity = userCSV.getString(userCSV.getColumnIndex("useraction"));
//                String Ph = userCSV.getString(userCSV.getColumnIndex("ph"));
//                String Temp = userCSV.getString(userCSV.getColumnIndex("temperature"));
//                String Mv = userCSV.getString(userCSV.getColumnIndex("mv"));
//
//                String record2 = Time.substring(0, 10) + "," + Time.substring(10, 16) + "," + Activity + "," + Ph + "," + Temp + "," + Mv;
//
//                printWriter.println(record2);
//            }

            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("Operator Sign" + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + "Supervisor Sign" + "," + nullEntry + "," + nullEntry);
//            userCSV.close();
            db.close();

            LoadOptions loadOptions = new LoadOptions(FileFormatType.CSV);
            String inputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcUseractivity/";

            Workbook workbook = new Workbook(inputFile + "DataUserActivity.csv", loadOptions);
            Worksheet worksheet = workbook.getWorksheets().get(0);
            worksheet.getCells().setColumnWidth(0, 18.5);
            worksheet.getCells().setColumnWidth(2, 18.5);
            workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/EcUseractivity/DataUserActivity.xlsx", SaveFormat.XLSX);

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
        DatabaseReference dataRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(EcActivity.DEVICE_ID)).getReference().child(Dashboard.DEVICE_TYPE_EC).child(EcActivity.DEVICE_ID);
        dataRef.child("ID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @com.google.firebase.database.annotations.NotNull DataSnapshot snapshot) {
                String p = snapshot.getValue(String.class);
                deviceID = p;
                deviceId.setText(p);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }

}