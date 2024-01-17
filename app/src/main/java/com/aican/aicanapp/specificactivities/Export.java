package com.aican.aicanapp.specificactivities;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import static androidx.camera.core.CameraX.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.adapters.CalibFileAdapter;
import com.aican.aicanapp.adapters.FileAdapter;
import com.aican.aicanapp.adapters.UserDataAdapter;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.utils.Constants;
import com.aican.aicanapp.utils.SharedPref;
import com.aspose.cells.FileFormatType;
import com.aspose.cells.LoadOptions;
import com.aspose.cells.PdfCompliance;
import com.aspose.cells.PdfSaveOptions;
import com.aspose.cells.Protection;
import com.aspose.cells.ProtectionType;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.cells.WorksheetCollection;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.common.base.Splitter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Export extends AppCompatActivity {

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST = 1888;
    private static final String[] permissions = new String[]{
            Manifest.permission.CAMERA
    };
    String calib_stat = "incomplete";

    Button selectCompanyLogo;
    ImageView companyLogo;
    //    String ph1, mv1, ph2, mv2, ph3, mv3, ph4, mv4, ph5, mv5, dt1, dt2, dt3, dt4, dt5;
    Button mDateBtn, exportUserData, exportCSV, convertToXls;
    ImageButton arNumBtn, batchNumBtn, compoundBtn;
    TextView tvStartDate, tvEndDate, tvUserLog;
    TextView deviceId, dateA;
    String deviceID;
    String user, roleExport, reportDate, reportTime;
    String startDateString, endDateString, startTimeString, endTimeString, arNumString, batchNumString, compoundName;
    String offset, battery, slope, temp;
    Integer startHour, startMinute, endHour, endMinute;
    String companyName;
    String nullEntry;
    Button printAllCalibData;
    FileAdapter fAdapter;
    UserDataAdapter uAdapter;
    EditText companyNameEditText, arNumEditText, batchNumEditText, compoundNameEditText;
    DatabaseHelper databaseHelper;
    private DatabaseReference deviceRef;
    private static final int PERMISSION_REQUEST_CODE = 200;
    String month, year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewCSV);
        RecyclerView userRecyclerView = findViewById(R.id.recyclerViewUserData);
//        TextView noFilesText = findViewById(R.id.nofiles_textview);
        selectCompanyLogo = findViewById(R.id.selectCompanyLogo);
        selectCompanyLogo.setOnClickListener(v -> {
            showOptionDialog();
        });
        companyLogo = findViewById(R.id.companyLogo);
        deviceId = findViewById(R.id.DeviceId);
        dateA = findViewById(R.id.dateA);
        exportCSV = findViewById(R.id.exportCSV);
        printAllCalibData = findViewById(R.id.printAllCalibData);
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
        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);

        companyNameEditText = findViewById(R.id.companyName);

        companyNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                companyName = String.valueOf(charSequence);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        databaseHelper = new DatabaseHelper(this);
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        databaseHelper.insert_action_data(time, date, "Exported : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

        nullEntry = " ";
        setFirebaseListeners();

        Cursor res = databaseHelper.get_userActivity_data();
        if (res != null) {
            if (res.moveToFirst()) {

                year = res.getString(1).substring(0, 4);
                month = res.getString(1).substring(5, 7);

                dateA.setText("Data available from " + res.getString(1));

            }
        } else {
            dateA.setText("No data available");
            month = "01";
            year = String.valueOf(Calendar.YEAR);
        }

        convertToXls.setVisibility(View.INVISIBLE);

        if (Source.subscription.equals("nonCfr")) {
            exportUserData.setVisibility(View.GONE);
            userRecyclerView.setVisibility(View.GONE);
            tvUserLog.setVisibility(View.GONE);
        }

        mDateBtn.setOnClickListener(v -> {

            Calendar calendar1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

            // now set the starting bound from current month to
            // previous MARCH
            if (month.equals("01")) {
                calendar1.set(Calendar.MONTH, Calendar.JANUARY);

            } else if (month.equals("02")) {
                calendar1.set(Calendar.MONTH, Calendar.FEBRUARY);

            } else if (month.equals("03")) {
                calendar1.set(Calendar.MONTH, Calendar.MARCH);

            } else if (month.equals("04")) {
                calendar1.set(Calendar.MONTH, Calendar.APRIL);

            } else if (month.equals("05")) {
                calendar1.set(Calendar.MONTH, Calendar.MAY);

            } else if (month.equals("06")) {
                calendar1.set(Calendar.MONTH, Calendar.JUNE);

            } else if (month.equals("07")) {
                calendar1.set(Calendar.MONTH, Calendar.JULY);

            } else if (month.equals("08")) {
                calendar1.set(Calendar.MONTH, Calendar.AUGUST);

            } else if (month.equals("09")) {
                calendar1.set(Calendar.MONTH, Calendar.SEPTEMBER);

            } else if (month.equals("10")) {
                calendar1.set(Calendar.MONTH, Calendar.OCTOBER);

            } else if (month.equals("11")) {
                calendar1.set(Calendar.MONTH, Calendar.NOVEMBER);

            } else if (month.equals("12")) {
                calendar1.set(Calendar.MONTH, Calendar.DECEMBER);

            } else {
                calendar1.set(Calendar.MONTH, Calendar.JANUARY);

            }
//            calendar1.set(Calendar.DATE, 7);

            long start = calendar1.getTimeInMillis();

            // now set the ending bound from current month to
            // DECEMBER
            calendar1.set(Calendar.MONTH, Calendar.DECEMBER);
            long end = calendar1.getTimeInMillis();

            CalendarConstraints.Builder calendarConstraintBuilder = new CalendarConstraints.Builder();
            calendarConstraintBuilder.setStart(start);
            calendarConstraintBuilder.setEnd(end);


            MaterialDatePicker datePicker =
                    MaterialDatePicker.Builder.dateRangePicker()
                            .setSelection(new Pair(MaterialDatePicker.thisMonthInUtcMilliseconds(),
                                    MaterialDatePicker.todayInUtcMilliseconds()))
                            .setTitleText("Select dates")
                            .setCalendarConstraints(calendarConstraintBuilder.build())
                            .build();
            datePicker.show(getSupportFragmentManager(), "date");

            datePicker.addOnPositiveButtonClickListener((MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>) selection -> {
                Long startDate = selection.first;
                Long endDate = selection.second;
                startDateString = DateFormat.format("yyyy-MM-dd", new Date(startDate)).toString();
                endDateString = DateFormat.format("yyyy-MM-dd", new Date(endDate)).toString();
                String date1 = "Start: " + startDateString + " End: " + endDateString;
                Toast.makeText(this, date1, Toast.LENGTH_SHORT).show();

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
                companyName = companyNameEditText.getText().toString();
                if (!companyName.isEmpty()) {
                    if (Constants.OFFLINE_MODE) {
                        SharedPreferences company_name = getSharedPreferences("COMPANY_NAME", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editT = company_name.edit();
                        editT.putString("COMPANY_NAME", companyName);
                        editT.commit();
                    } else {
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("COMPANY_NAME").setValue(companyName);
                    }
                }

                try {
                    generatePDF1();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


                String pathPDF = new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Sensordata/";
                File rootPDF = new File(pathPDF);
                fileNotWrite(rootPDF);
                File[] filesAndFoldersPDF = rootPDF.listFiles();


                fAdapter = new FileAdapter(getApplicationContext(), reverseFileArray(filesAndFoldersPDF), "PhExport");
                recyclerView.setAdapter(fAdapter);
                fAdapter.notifyDataSetChanged();
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                convertToXls.setVisibility(View.INVISIBLE);
            }
        });

        String pathPDF = new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Sensordata/";
        File rootPDF = new File(pathPDF);
        fileNotWrite(rootPDF);
        File[] filesAndFoldersPDF = rootPDF.listFiles();


        fAdapter = new FileAdapter(getApplicationContext(), reverseFileArray(filesAndFoldersPDF != null ? filesAndFoldersPDF : new File[0]), "PhExport");
        recyclerView.setAdapter(fAdapter);
        fAdapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        String path11 = new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Useractivity";
        File root11 = new File(path11);

        String pathPDF11 = new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Useractivity/";
        File rootPDF11 = new File(pathPDF11);
        fileNotWrite(root11);
        File[] filesAndFoldersPDF11 = rootPDF11.listFiles();


        uAdapter = new UserDataAdapter(Export.this, reverseFileArray(filesAndFoldersPDF11));
        userRecyclerView.setAdapter(uAdapter);
        uAdapter.notifyDataSetChanged();
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        convertToXls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Workbook workbook = new Workbook(new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/ExcelFiles/DataSensorLog.xlsx");

                    PdfSaveOptions options = new PdfSaveOptions();
                    options.setCompliance(PdfCompliance.PDF_A_1_B);
                    workbook.save(new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Sensordata/DataSensorLog.pdf", options);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });



        exportUserData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                companyName = companyNameEditText.getText().toString();
                if (!companyName.isEmpty()) {
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("COMPANY_NAME").setValue(companyName);
                }

                try {
                    generatePDF2();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

//                exportUserData();


                String path = new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Useractivity";
                File root = new File(path);

                String pathPDF = new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Useractivity/";
                File rootPDF = new File(pathPDF);
                fileNotWrite(root);
                File[] filesAndFoldersPDF = rootPDF.listFiles();


                uAdapter = new UserDataAdapter(Export.this, reverseFileArray(filesAndFoldersPDF));
                userRecyclerView.setAdapter(uAdapter);
                uAdapter.notifyDataSetChanged();
                userRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            }


        });

        if (checkPermission()) {
            Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }

        if (Constants.OFFLINE_MODE) {
            SharedPreferences company_name = getSharedPreferences("COMPANY_NAME", Context.MODE_PRIVATE);
            companyNameEditText.setText(company_name.getString("COMPANY_NAME", "N/A"));
        }

        Bitmap comLo = getCompanyLogo();
        if (comLo != null) {
            companyLogo.setImageBitmap(comLo);
        }


        printAllCalibData.setOnClickListener(v -> {
            try {
                generateAllPDF();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
//                exportCalibData();

            String path = new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/CalibrationData";
            File root = new File(path);
            File[] filesAndFolders = root.listFiles();

            if (filesAndFolders == null || filesAndFolders.length == 0) {

                return;
            } else {
                for (int i = 0; i < filesAndFolders.length; i++) {
                    filesAndFolders[i].getName().endsWith(".pdf");
                }
            }

            String pathPDF1 = new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Sensordata/";
            File rootPDF1 = new File(pathPDF1);
            fileNotWrite(root);
            File[] filesAndFoldersPDF1 = rootPDF1.listFiles();


            fAdapter = new FileAdapter(getApplicationContext(), reverseFileArray(filesAndFoldersPDF1), "PhExport");
            recyclerView.setAdapter(fAdapter);
            fAdapter.notifyDataSetChanged();
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            convertToXls.setVisibility(View.INVISIBLE);

        });
    }

    private void generateAllPDF() throws FileNotFoundException {

        String company_name = "Company: " + companyName;
        String user_name = "Report generated by: " + Source.logUserName;
        String device_id = "DeviceID: " + deviceID;
        String calib_by = "Last calibrated by: " + Source.calib_completed_by;

        reportDate = "Date: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        reportTime = "Time: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        SharedPreferences shp = getSharedPreferences("Extras", MODE_PRIVATE);
        offset = "Offset: " + shp.getString("offset", "");

        if (Constants.OFFLINE_DATA){

            if (SharedPref.getSavedData(Export.this,"OFFSET_"+PhActivity.DEVICE_ID) != null && SharedPref.getSavedData(Export.this,"OFFSET_"+PhActivity.DEVICE_ID) != ""){
                String  data =  SharedPref.getSavedData(Export.this,"OFFSET_"+PhActivity.DEVICE_ID);
                offset = "Offset: " + data;
            }else{
                offset = "Offset: " + "null";

            }
        }else {
        }


        temp = "Temperature: " + shp.getString("temp", "");
        battery = "Battery: " + shp.getString("battery", "");
        if (Constants.OFFLINE_DATA){
            if (SharedPref.getSavedData(Export.this,"SLOPE_"+PhActivity.DEVICE_ID) != null && SharedPref.getSavedData(Export.this,"SLOPE_"+PhActivity.DEVICE_ID) != ""){
                String  data =  SharedPref.getSavedData(Export.this,"SLOPE_"+PhActivity.DEVICE_ID);
                slope = "Slope: " + data;
            }else{
                slope = "Slope: " + "null";

            }

            if (SharedPref.getSavedData(Export.this,"TEMP_VAL_"+PhActivity.DEVICE_ID) != null && SharedPref.getSavedData(Export.this,"TEMP_VAL_"+PhActivity.DEVICE_ID) != ""){
                String  data =  SharedPref.getSavedData(Export.this,"TEMP_VAL_"+PhActivity.DEVICE_ID);
                temp = "Temperature: " + data;
            }else{
                temp = "Temperature: " + "null";

            }

        }else {
            slope = "Slope: " + shp.getString("slope", "");
        }

        File exportDir = new File(new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Sensordata");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        String tempPath = new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Sensordata";
        File tempRoot = new File(tempPath);
        fileNotWrite(tempRoot);
        File[] tempFilesAndFolders = tempRoot.listFiles();


        File file = new File(new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Sensordata/AllCalibData_" + currentDateandTime + "_" + ((tempFilesAndFolders != null ? tempFilesAndFolders.length : 0) - 1) + ".pdf");
        OutputStream outputStream = new FileOutputStream(file);
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);


//        Text text = new Text(company_name);
//        Text text1 = new Text(user_name);
//        Text text2 = new Text(device_id);
//
//
//
//        document.add(new Paragraph(text).add(text1).add(text2));
        document.add(new Paragraph(company_name + "\n" + calib_by + "\n" + user_name + "\n" + device_id));
        document.add(new Paragraph(""));
        document.add(new Paragraph(reportDate
                + "  |  " + reportTime + "\n" +
                offset + "  |  " + battery + "\n" + slope + "  |  " + temp
        ));

        document.add(new Paragraph(""));
        document.add(new Paragraph("Calibration Table"));

        float columnWidth[] = {200f, 210f, 190f, 170f, 340f, 170f, 210f};
        Table table = new Table(columnWidth);
        table.addCell("pH");
        table.addCell("pH Aft Calib");
        table.addCell("Slope");
        table.addCell("mV");
        table.addCell("Date & Time");
        table.addCell("Temperature");
        table.addCell("Calibrated by");

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        Cursor calibCSV = db.rawQuery("SELECT * FROM CalibAllData", null);

        if (startDateString != null) {

            calibCSV = db.rawQuery("SELECT * FROM CalibAllData WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "')", null);
//            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "')", null);

        } else {
            calibCSV = db.rawQuery("SELECT * FROM CalibAllData", null);
        }


        while (calibCSV.moveToNext()) {
            String ph = calibCSV.getString(calibCSV.getColumnIndex("PH"));
            String mv = calibCSV.getString(calibCSV.getColumnIndex("MV"));
            String date = calibCSV.getString(calibCSV.getColumnIndex("DT"));
            String slope = calibCSV.getString(calibCSV.getColumnIndex("SLOPE"));
            String pHAC = calibCSV.getString(calibCSV.getColumnIndex("pHAC"));
            String temperature1 = calibCSV.getString(calibCSV.getColumnIndex("temperature"));

            table.addCell(ph);
            table.addCell(pHAC + "");
            table.addCell(slope + "");
            table.addCell(mv);
            table.addCell(date);
            table.addCell(temperature1);
            table.addCell(Source.calib_completed_by == null ? "Unknown" : Source.calib_completed_by);

        }
        document.add(table);


        document.add(new Paragraph("Calibration : " + calib_stat));


        document.add(new Paragraph("Operator Sign                                                                                      Supervisor Sign"));

        Bitmap imgBit1 = getSignImage();
        if (imgBit1 != null) {
            Uri uri1 = getImageUri(this, imgBit1);

            try {
                String add = getPath(uri1);
                ImageData imageData1 = ImageDataFactory.create(add);
                Image image1 = new Image(imageData1).setHeight(80f).setWidth(80f);
//                table12.addCell(new Cell(2, 1).add(image));
                // Adding image to the document
                document.add(image1);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        document.close();

        Toast.makeText(this, "Pdf generated", Toast.LENGTH_SHORT).show();

    }


    public static int PICK_IMAGE = 1;

    private void showOptionDialog() {
        Dialog dialog = new Dialog(Export.this);
        dialog.setContentView(R.layout.img_options_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermissions()) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    dialog.dismiss();
                } else {
                    Toast.makeText(Export.this, "Camera permission required", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            saveImage(photo);
            companyLogo.setImageBitmap(photo);
            companyLogo.setVisibility(View.VISIBLE);
//            selectCompanyLogo.setText("Ok!");
//            selectCompanyLogo.setEnabled(false);
        }
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri picUri = data.getData();//<- get Uri here from data intent
                if (picUri != null) {
//                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    Bitmap photo = null;

                    try {
                        photo = android.provider.MediaStore.Images.Media.getBitmap(
                                this.getContentResolver(),
                                picUri);
                        saveImage(photo);
                        companyLogo.setImageBitmap(photo);
                        companyLogo.setVisibility(View.VISIBLE);
//                        selectCompanyLogo.setText("Ok!");
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                }
            }
        }
    }

    private void saveImage(Bitmap realImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        realImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();

        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

        SharedPreferences shre = getSharedPreferences("logo", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = shre.edit();
        edit.putString("logo_data", encodedImage);
        edit.commit();
    }

    private Bitmap getCompanyLogo() {
        SharedPreferences sh = getSharedPreferences("logo", Context.MODE_PRIVATE);
        String photo = sh.getString("logo_data", "");
        Bitmap bitmap = null;

        if (!photo.equalsIgnoreCase("")) {
            byte[] b = Base64.decode(photo, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return bitmap;
    }

    private Bitmap getSignImage() {
        SharedPreferences sh = getSharedPreferences("signature", Context.MODE_PRIVATE);
        String photo = sh.getString("signature_data", "");
        Bitmap bitmap = null;

        if (!photo.equalsIgnoreCase("")) {
            byte[] b = Base64.decode(photo, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return bitmap;
    }

    File[] reverseFileArray(File[] fileArray) {
        if(fileArray != null) {
            for (int i = 0; i < fileArray.length / 2; i++) {
                File a = fileArray[i];
                fileArray[i] = fileArray[fileArray.length - i - 1];
                fileArray[fileArray.length - i - 1] = a;
            }
        }
        return fileArray;
    }

    private String stringSplitter(String str) {
        String newText = "";
        Iterable<String> strings = Splitter.fixedLength(8).split(str);
        for (String temp : strings) {
            newText = newText + " " + temp;
        }
        return newText.trim();
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void generatePDF1() throws FileNotFoundException {

        String device_id = "DeviceID: " + deviceID;

        companyName = "" + companyNameEditText.getText().toString();
        reportDate = "Date: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        reportTime = "Time: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        SharedPreferences shp = getSharedPreferences("Extras", MODE_PRIVATE);
        offset = "Offset: " + shp.getString("offset", "");

        if (Constants.OFFLINE_DATA){

            if (SharedPref.getSavedData(Export.this,"OFFSET_"+PhActivity.DEVICE_ID) != null && SharedPref.getSavedData(Export.this,"OFFSET_"+PhActivity.DEVICE_ID) != ""){
                String  data =  SharedPref.getSavedData(Export.this,"OFFSET_"+PhActivity.DEVICE_ID);
                offset = "Offset: " + data;
            }else{
                offset = "Offset: " + "null";

            }
        }else {
        }

        temp = "Temperature: " + shp.getString("temp", "");
        battery = "Battery: " + shp.getString("battery", "");
        if (Constants.OFFLINE_DATA){
            if (SharedPref.getSavedData(Export.this,"SLOPE_"+PhActivity.DEVICE_ID) != null && SharedPref.getSavedData(Export.this,"SLOPE_"+PhActivity.DEVICE_ID) != ""){
                String  data =  SharedPref.getSavedData(Export.this,"SLOPE_"+PhActivity.DEVICE_ID);
                slope = "Slope: " + data;
            }else{
                slope = "Slope: " + "null";

            }
            if (SharedPref.getSavedData(Export.this,"TEMP_VAL_"+PhActivity.DEVICE_ID) != null && SharedPref.getSavedData(Export.this,"TEMP_VAL_"+PhActivity.DEVICE_ID) != ""){
                String  data =  SharedPref.getSavedData(Export.this,"TEMP_VAL_"+PhActivity.DEVICE_ID);
                temp = "Temperature: " + data;
            }else{
                temp = "Temperature: " + "null";

            }
        }else {
            slope = "Slope: " + shp.getString("slope", "");
        }

        roleExport = "Made By: " + Source.logUserName;

        File exportDir = new File(new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Sensordata");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        String tempPath = new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Sensordata";
        File tempRoot = new File(tempPath);
        fileNotWrite(tempRoot);
        File[] tempFilesAndFolders = tempRoot.listFiles();


        File file = new File(new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Sensordata/DSL_" + currentDateandTime + "_" + ((tempFilesAndFolders != null ? tempFilesAndFolders.length : 0) - 1) + ".pdf");
        OutputStream outputStream = new FileOutputStream(file);
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

//        float[] columnWidth12 = {150, 400, 400};
//        Table table12 = new Table(columnWidth12);

        Bitmap imgBit = getCompanyLogo();
        if (imgBit != null) {
            Uri uri = getImageUri(Export.this, imgBit);

            try {
                String add = getPath(uri);
                ImageData imageData = ImageDataFactory.create(add);
                Image image = new Image(imageData).setHeight(80f).setWidth(80f);
//                table12.addCell(new Cell(2, 1).add(image));
                // Adding image to the document
                document.add(image);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
//
//        table12.addCell(new Paragraph(companyName));
//        table12.setBorder(Border.NO_BORDER);
//
//        document.add(table12);
        if (Constants.OFFLINE_MODE) {
            document.add(new Paragraph("Offline Mode"));
        }
        document.add(new Paragraph(companyName + "\n" + roleExport + "\n" + device_id));
        document.add(new Paragraph(""));
        document.add(new Paragraph(reportDate
                + "  |  " + reportTime + "\n" +
                offset + "  |  " + battery + "\n" + slope + "  |  " + temp
        ));
        document.add(new Paragraph(""));
        document.add(new Paragraph("Calibration Table"));

        float columnWidth[] = {200f, 210f, 190f, 170f, 340f, 170f};
        Table table = new Table(columnWidth);
        table.addCell("pH");
        table.addCell("pH Aft Calib");
        table.addCell("Slope");
        table.addCell("mV");
        table.addCell("Date & Time");
        table.addCell("Temperature");

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Cursor calibCSV = null;
        if (Constants.OFFLINE_MODE) {
//            calibCSV = db.rawQuery("SELECT * FROM CalibOfflineData", null);
            if (Source.calibMode == 0){
                calibCSV = db.rawQuery("SELECT * FROM CalibOfflineDataFive", null);

            }
            if (Source.calibMode == 1){
                calibCSV = db.rawQuery("SELECT * FROM CalibOfflineDataThree", null);

            }
        } else {
            calibCSV = db.rawQuery("SELECT * FROM CalibData", null);

        }

        while (calibCSV != null && calibCSV.moveToNext()) {
            String ph = calibCSV.getString(calibCSV.getColumnIndex("PH"));
            String mv = calibCSV.getString(calibCSV.getColumnIndex("MV"));
            String date = calibCSV.getString(calibCSV.getColumnIndex("DT"));
            String slope = calibCSV.getString(calibCSV.getColumnIndex("SLOPE"));
            String pHAC = calibCSV.getString(calibCSV.getColumnIndex("pHAC"));
            String temperature1 = calibCSV.getString(calibCSV.getColumnIndex("temperature"));

            table.addCell(ph);
            table.addCell(pHAC + "");
            table.addCell(slope + "");
            table.addCell(mv);
            table.addCell(date);
            table.addCell(temperature1);

        }
        document.add(table);

        document.add(new Paragraph(""));
        document.add(new Paragraph("Log Table"));

        float[] columnWidth1 = {210f, 120f, 170f, 150f, 350f, 350f, 250f};
        Table table1 = new Table(columnWidth1);
        table1.addCell("Date");
        table1.addCell("Time");
        table1.addCell("pH");
        table1.addCell("Temp");
        table1.addCell("Batch No");
        table1.addCell("AR No");
        table1.addCell("Compound");

        Cursor curCSV;
        if (Constants.OFFLINE_MODE) {
            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);

        } else {

            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);
//            Cursor curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "')')", null);
        }
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

            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);

        } else if (startDateString != null && compoundName != null && batchNumString != null) {

            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "')", null);

        } else if (startDateString != null && compoundName != null && arNumString != null) {

            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);

        } else if (startDateString != null && batchNumString != null && arNumString != null) {

            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);

        } else if (compoundName != null && batchNumString != null && arNumString != null) {

            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE  (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);

        } else if (startDateString != null && compoundName != null) {

            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE  (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (arnum = '" + compoundName + "')", null);

        } else if (startDateString != null && batchNumString != null) {

            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (batchnum = '" + batchNumString + "')", null);

        } else if (startDateString != null && arNumString != null) {

            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (compound = '" + arNumString + "')", null);

        } else if (compoundName != null && batchNumString != null) {

            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE  (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "')", null);

        } else if (compoundName != null && arNumString != null) {

            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE  (arnum = '" + compoundName + "') AND (compound = '" + arNumString + "')", null);

        } else if (batchNumString != null && arNumString != null) {

            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE  (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);

        } else if (startDateString != null) {

            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "')", null);
//            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "')", null);

        } else if (compoundName != null) {

            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE  (arnum = '" + compoundName + "')", null);

        } else if (batchNumString != null) {

            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE  (batchnum = '" + batchNumString + "') ", null);

        } else if (arNumString != null) {

            curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE  (compound = '" + arNumString + "')", null);

        } else {
            curCSV = db.rawQuery("SELECT * FROM LogUserdetails", null);
        }

        while (curCSV.moveToNext()) {

            String date = curCSV.getString(curCSV.getColumnIndex("date"));
            String time = curCSV.getString(curCSV.getColumnIndex("time"));
            String device = curCSV.getString(curCSV.getColumnIndex("deviceID"));
            String pH = curCSV.getString(curCSV.getColumnIndex("ph"));
            String temp = curCSV.getString(curCSV.getColumnIndex("temperature"));
            String batchnum = curCSV.getString(curCSV.getColumnIndex("batchnum"));
            String arnum = curCSV.getString(curCSV.getColumnIndex("arnum"));
            String comp = curCSV.getString(curCSV.getColumnIndex("compound"));


            table1.addCell(date);
            table1.addCell(time);
            table1.addCell(pH != null ? pH : "--");
            table1.addCell(temp != null ? temp : "--");
            if (batchnum == null){
                batchnum = "--";
            }
            table1.addCell(batchnum != null && batchnum.length() >= 8 ? stringSplitter(batchnum) : batchnum);
            if (arnum == null){
                arnum = "--";
            }
            table1.addCell(arnum != null && arnum.length() >= 8 ? stringSplitter(arnum) : arnum);
            if (comp == null){
                comp = "--";
            }
            table1.addCell(comp != null && comp.length() >= 8 ? stringSplitter(comp) : comp);

        }

        document.add(table1);

        document.add(new Paragraph(""));
        document.add(new Paragraph("Operator Sign                                                                                          Supervisor Sign"));

        Bitmap imgBit1 = getSignImage();
        if (imgBit1 != null) {
            Uri uri1 = getImageUri(Export.this, imgBit1);

            try {
                String add = getPath(uri1);
                ImageData imageData1 = ImageDataFactory.create(add);
                Image image1 = new Image(imageData1).setHeight(80f).setWidth(80f);
//                table12.addCell(new Cell(2, 1).add(image));
                // Adding image to the document
                document.add(image1);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        document.close();

        Toast.makeText(Export.this, "Pdf generated", Toast.LENGTH_SHORT).show();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
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
            requestPermissions(permissions, CAMERA_REQUEST);
        return allGranted;
    }


    public void exportDatabaseCsv() {

        companyName = "" + companyNameEditText.getText().toString();
        reportDate = "Date: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        reportTime = "Time: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        //We use the Download directory for saving our .csv file.
        File exportDir = new File(new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Sensordata");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File outputDir = new File(new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/ExcelFiles");
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

            if (Constants.OFFLINE_DATA){

                if (SharedPref.getSavedData(Export.this,"OFFSET_"+PhActivity.DEVICE_ID) != null && SharedPref.getSavedData(Export.this,"OFFSET_"+PhActivity.DEVICE_ID) != ""){
                    String  data =  SharedPref.getSavedData(Export.this,"OFFSET_"+PhActivity.DEVICE_ID);
                    offset = "Offset: " + data;
                }else{
                    offset = "Offset: " + "null";

                }
            }else {
            }


            battery = "Battery: " + shp.getString("battery", "");
            temp = "Temperature: " + shp.getString("temp", "");
            if (Constants.OFFLINE_DATA){
                if (SharedPref.getSavedData(Export.this,"SLOPE_"+PhActivity.DEVICE_ID) != null && SharedPref.getSavedData(Export.this,"SLOPE_"+PhActivity.DEVICE_ID) != ""){
                    String  data =  SharedPref.getSavedData(Export.this,"SLOPE_"+PhActivity.DEVICE_ID);
                    slope = "Slope: " + data;
                }else{
                    slope = "Slope: " + "null";

                }
                if (SharedPref.getSavedData(Export.this,"TEMP_VAL_"+PhActivity.DEVICE_ID) != null && SharedPref.getSavedData(Export.this,"TEMP_VAL_"+PhActivity.DEVICE_ID) != ""){
                    String  data =  SharedPref.getSavedData(Export.this,"TEMP_VAL_"+PhActivity.DEVICE_ID);
                    temp = "Temperature: " + data;
                }else{
                    temp = "Temperature: " + "null";

                }
            }else {
                slope = "Slope: " + shp.getString("slope", "");
            }


            setFirebaseListeners();

            SharedPreferences shp2 = getSharedPreferences("RolePref", MODE_PRIVATE);
//            roleExport = "Made By: " + shp2.getString("roleSuper", "");
            roleExport = "Made By: " + Source.logUserName;


            Log.d("debzdate", startDateString + "," + endDateString);

            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            Cursor calibCSV = db.rawQuery("SELECT * FROM CalibData", null);
            Cursor curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);
//            Cursor curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "')')", null);

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

                curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);

            } else if (startDateString != null && compoundName != null && batchNumString != null) {

                curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "')", null);

            } else if (startDateString != null && compoundName != null && arNumString != null) {

                curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);

            } else if (startDateString != null && batchNumString != null && arNumString != null) {

                curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);

            } else if (compoundName != null && batchNumString != null && arNumString != null) {

                curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE  (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);

            } else if (startDateString != null && compoundName != null) {

                curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE  (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (arnum = '" + compoundName + "')", null);

            } else if (startDateString != null && batchNumString != null) {

                curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (batchnum = '" + batchNumString + "')", null);

            } else if (startDateString != null && arNumString != null) {

                curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "') AND (compound = '" + arNumString + "')", null);

            } else if (compoundName != null && batchNumString != null) {

                curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE  (arnum = '" + compoundName + "') AND (batchnum = '" + batchNumString + "')", null);

            } else if (compoundName != null && arNumString != null) {

                curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE  (arnum = '" + compoundName + "') AND (compound = '" + arNumString + "')", null);

            } else if (batchNumString != null && arNumString != null) {

                curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE  (batchnum = '" + batchNumString + "') AND (compound = '" + arNumString + "')", null);

            } else if (startDateString != null) {

                curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "')", null);

            } else if (compoundName != null) {

                curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE  (arnum = '" + compoundName + "')", null);

            } else if (batchNumString != null) {

                curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE  (batchnum = '" + batchNumString + "') ", null);

            } else if (arNumString != null) {

                curCSV = db.rawQuery("SELECT * FROM LogUserdetails WHERE  (compound = '" + arNumString + "')", null);

            } else {
                curCSV = db.rawQuery("SELECT * FROM LogUserdetails", null);
            }


            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("Company: " + companyName);
            printWriter.println(reportDate);
            printWriter.println(reportTime);
            printWriter.println("DeviceID: " + deviceId.getText().toString());
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
            printWriter.println("_____DATE___,___TIME____,___pH___,pHAfterCalib,____________mV__,Temperature");

            printWriter.println();


            while (calibCSV.moveToNext()) {
                String ph = calibCSV.getString(calibCSV.getColumnIndex("PH"));
                String mv = calibCSV.getString(calibCSV.getColumnIndex("MV"));
                String date = calibCSV.getString(calibCSV.getColumnIndex("DT"));
                String pHAC = calibCSV.getString(calibCSV.getColumnIndex("pHAC"));
                String temperature1 = calibCSV.getString(calibCSV.getColumnIndex("temperature"));

                String record1 = date.substring(0, 10) + "," + date.substring(11, 19) + "," + ph + "," + pHAC + "," + mv + "," + temperature1;


//                String record1 = ph + "," + mv + "," + date;

                printWriter.println(record1);
            }
            calibCSV.close();
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("_____Date______Time____pH____Temp______Batch No___________AR No_______Compound______");
//            printWriter.println("_____Date______Time____pH____Temp______Batch No___________AR No____Compound__Device___");
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);


            while (curCSV.moveToNext()) {

                String date = curCSV.getString(curCSV.getColumnIndex("date"));
                String time = curCSV.getString(curCSV.getColumnIndex("time"));
                String device = curCSV.getString(curCSV.getColumnIndex("deviceID"));
                String pH = curCSV.getString(curCSV.getColumnIndex("ph"));
                String temp = curCSV.getString(curCSV.getColumnIndex("temperature"));
                String batchnum = curCSV.getString(curCSV.getColumnIndex("batchnum"));
                String arnum = curCSV.getString(curCSV.getColumnIndex("arnum"));
                String comp = curCSV.getString(curCSV.getColumnIndex("compound"));
//                String record = date + "   " + time +  "     " + pH + "       " + temp + "       " + batchnum + "       " + arnum + "       " + comp + "      " + device;

//                String record = date + "," + time + "," + pH + "," + temp + "," + batchnum + "," + arnum + "," + comp + "," + device;
                String record = date + "," + time + "," + pH + "," + temp + "," + batchnum + "," + arnum + "," + comp;

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
            curCSV.close();
            db.close();

            LoadOptions loadOptions = new LoadOptions(FileFormatType.CSV);
            String inputFile = new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Sensordata/";

            Workbook workbook = new Workbook(inputFile + "DataSensorLog.csv", loadOptions);
            Worksheet worksheet = workbook.getWorksheets().get(0);
            worksheet.getCells().setColumnWidth(0, 10);
            worksheet.getCells().setColumnWidth(1, 7.5);
            worksheet.getCells().setColumnWidth(2, 7.5);
            worksheet.getCells().setColumnWidth(3, 7);
            worksheet.getCells().setColumnWidth(4, 18);
            worksheet.getCells().setColumnWidth(5, 16);
            worksheet.getCells().setColumnWidth(6, 12.0);
//            worksheet.getCells().setColumnWidth(7, 9.0);
            workbook.save(new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/ExcelFiles/DataSensorLog.xlsx", SaveFormat.XLSX);

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

    public void generatePDF2() throws FileNotFoundException {
        String company_name = "Company: " + companyName;
        String user_name = "Supervisor: " + Source.logUserName;
        String device_id = "DeviceID: " + deviceID;

        reportDate = "Date: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        reportTime = "Time: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());


        SharedPreferences shp = getSharedPreferences("Extras", MODE_PRIVATE);
        offset = "Offset: " + shp.getString("offset", "");

        if (Constants.OFFLINE_DATA){

            if (SharedPref.getSavedData(Export.this,"OFFSET_"+PhActivity.DEVICE_ID) != null && SharedPref.getSavedData(Export.this,"OFFSET_"+PhActivity.DEVICE_ID) != ""){
                String  data =  SharedPref.getSavedData(Export.this,"OFFSET_"+PhActivity.DEVICE_ID);
                offset = "Offset: " + data;
            }else{
                offset = "Offset: " + "null";

            }
        }else {
        }


        String tempe = "Temperature: " + shp.getString("temp", "");


        battery = "Battery: " + shp.getString("battery", "");
        if (Constants.OFFLINE_DATA){
            if (SharedPref.getSavedData(Export.this,"SLOPE_"+PhActivity.DEVICE_ID) != null && SharedPref.getSavedData(Export.this,"SLOPE_"+PhActivity.DEVICE_ID) != ""){
                String  data =  SharedPref.getSavedData(Export.this,"SLOPE_"+PhActivity.DEVICE_ID);
                slope = "Slope: " + data;
            }else{
                slope = "Slope: " + "null";

            }
            if (SharedPref.getSavedData(Export.this,"TEMP_VAL_"+PhActivity.DEVICE_ID) != null && SharedPref.getSavedData(Export.this,"TEMP_VAL_"+PhActivity.DEVICE_ID) != ""){
                String  data =  SharedPref.getSavedData(Export.this,"TEMP_VAL_"+PhActivity.DEVICE_ID);
                tempe = "Temperature: " + data;
            }else{
                tempe = "Temperature: " + "null";

            }
        }else {
            slope = "Slope: " + shp.getString("slope", "");
        }


        File exportDir = new File(new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Useractivity");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        String tempPath = new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Useractivity";
        File tempRoot = new File(tempPath);
        fileNotWrite(tempRoot);
        File[] tempFilesAndFolders = tempRoot.listFiles();


        File file = new File(new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Useractivity/UA_" + currentDateandTime + "_" + (tempFilesAndFolders.length - 1) + ".pdf");
        OutputStream outputStream = new FileOutputStream(file);
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        Bitmap imgBit = getCompanyLogo();
        if (imgBit != null) {
            Uri uri = getImageUri(Export.this, imgBit);

            try {
                String add = getPath(uri);
                ImageData imageData = ImageDataFactory.create(add);
                Image image = new Image(imageData).setHeight(80f).setWidth(80f);
//                table12.addCell(new Cell(2, 1).add(image));
                // Adding image to the document
                document.add(image);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        document.add(new Paragraph(company_name + "\n" + user_name + "\n" + device_id));
        document.add(new Paragraph(""));
        document.add(new Paragraph(reportDate
                + "  |  " + reportTime + "\n" +
                offset + "  |  " + battery + "\n" + slope + "  |  " + tempe
        ));
        document.add(new Paragraph(""));

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        SharedPreferences shp2 = getSharedPreferences("RolePref", MODE_PRIVATE);
//            roleExport = "Supervisor: " + shp2.getString("roleSuper", "");
        roleExport = "Supervisor: " + Source.logUserName;

        Cursor userCSV = db.rawQuery("SELECT * FROM UserActiondetails", null);


        if (startDateString != null) {

            userCSV = db.rawQuery("SELECT * FROM UserActiondetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "')", null);
//            userCSV = db.rawQuery("SELECT * FROM UserActiondetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "')", null);

        }

        document.add(new Paragraph("User Activity Data Table"));

        float[] columnWidth1 = {300f, 350f, 150f, 150f, 170f, 200f};
        Table table1 = new Table(columnWidth1);
        table1.addCell("Date & Time");
        table1.addCell("Activity");
        table1.addCell("pH");
        table1.addCell("Temp");
        table1.addCell("mV");
        table1.addCell("Device ID");


        while (userCSV.moveToNext()) {
            String Time = userCSV.getString(userCSV.getColumnIndex("time"));
            String Date = userCSV.getString(userCSV.getColumnIndex("date"));
            String activity = userCSV.getString(userCSV.getColumnIndex("useraction"));
            String Ph = userCSV.getString(userCSV.getColumnIndex("ph"));
            String Temp = userCSV.getString(userCSV.getColumnIndex("temperature"));
            String Mv = userCSV.getString(userCSV.getColumnIndex("mv"));
            String device = userCSV.getString(userCSV.getColumnIndex("deviceID"));
            Date = Date + " " + Time;
//            String record2 = Date + "," + Activity + "," + Ph + "," + Temp + "," + Mv + "," + device;

            table1.addCell(Date + "");
            table1.addCell(activity + "");
            table1.addCell(Ph + "");
            table1.addCell(Temp + "");
            table1.addCell(Mv + "");
            table1.addCell(device + "");


        }

        document.add(table1);


        document.add(new Paragraph("Operator Sign                                                                                      Supervisor Sign"));

        Bitmap imgBit1 = getSignImage();
        if (imgBit1 != null) {
            Uri uri1 = getImageUri(Export.this, imgBit1);

            try {
                String add = getPath(uri1);
                ImageData imageData1 = ImageDataFactory.create(add);
                Image image1 = new Image(imageData1).setHeight(80f).setWidth(80f);
//                table12.addCell(new Cell(2, 1).add(image));
                // Adding image to the document
                document.add(image1);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        document.close();

        Toast.makeText(Export.this, "Pdf generated", Toast.LENGTH_SHORT).show();
    }

    public void exportUserData() {
        File exportDir = new File(new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Useractivity");
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
//            roleExport = "Supervisor: " + shp2.getString("roleSuper", "");
            roleExport = "Supervisor: " + Source.logUserName;

            Cursor userCSV = db.rawQuery("SELECT * FROM UserActiondetails", null);


            if (startDateString != null) {

                userCSV = db.rawQuery("SELECT * FROM UserActiondetails WHERE (DATE(date) BETWEEN '" + startDateString + "' AND '" + endDateString + "') AND (time BETWEEN '" + startTimeString + "' AND '" + endTimeString + "')", null);

            }


            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("Company: " + companyName);
            printWriter.println(reportDate);
            printWriter.println(reportTime);
            printWriter.println("DeviceID: " + deviceId.getText().toString());
            printWriter.println(roleExport);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(offset + "," + battery);
            printWriter.println(temp);
            printWriter.println(slope);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("User Activity Table" + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println();
            printWriter.println("__DATE & TIME__,__ACTIVITY__,________pH__,__TEMP__,___mV__,_Device ID_");
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);


            while (userCSV.moveToNext()) {
                String Time = userCSV.getString(userCSV.getColumnIndex("time"));
                String Date = userCSV.getString(userCSV.getColumnIndex("date"));
                String Activity = userCSV.getString(userCSV.getColumnIndex("useraction"));
                String Ph = userCSV.getString(userCSV.getColumnIndex("ph"));
                String Temp = userCSV.getString(userCSV.getColumnIndex("temperature"));
                String Mv = userCSV.getString(userCSV.getColumnIndex("mv"));
                String device = userCSV.getString(userCSV.getColumnIndex("deviceID"));
                Date = Date + " " + Time;
                String record2 = Date + "," + Activity + "," + Ph + "," + Temp + "," + Mv + "," + device;

                printWriter.println(record2);
            }


            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("Operator Sign");
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + "Supervisor Sign");
            userCSV.close();
            db.close();

            LoadOptions loadOptions = new LoadOptions(FileFormatType.CSV);
            String inputFile = new ContextWrapper(Export.this).getExternalMediaDirs()[0] + File.separator + "/LabApp/Useractivity/";

            Workbook workbook = new Workbook(inputFile + "DataUserActivity.csv", loadOptions);
            Worksheet worksheet = workbook.getWorksheets().get(0);
            worksheet.getCells().setColumnWidth(0, 18.5);
            worksheet.getCells().setColumnWidth(1, 20.5);
            worksheet.getCells().setColumnWidth(2, 12.5);
            workbook.save(new ContextWrapper(Export.this).getExternalMediaDirs()[0]+ File.separator + "/LabApp/Useractivity/DataUserActivity.xlsx", SaveFormat.XLSX);

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == CAMERA_REQUEST) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                }
            }
            if (!allGranted) {
                Toast.makeText(this, "Permission needed for app to work.", Toast.LENGTH_SHORT).show();
            } else {

            }
        }
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

        if (deviceRef.child("Data").child("CALIBRATION_STAT") != null)
            deviceRef.child("Data").child("CALIBRATION_STAT").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    calib_stat = snapshot.getValue(String.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        DatabaseReference dataRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child(Dashboard.DEVICE_TYPE_PH).child(PhActivity.DEVICE_ID);
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

        if (!Constants.OFFLINE_DATA) {
            deviceRef.child("UI").child("PH").child("PH_CAL").child("COMPANY_NAME").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    companyName = snapshot.getValue(String.class);
                    companyNameEditText.setText(companyName);
                    SharedPreferences company_name = getSharedPreferences("COMPANY_NAME", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editT = company_name.edit();
                    editT.putString("COMPANY_NAME", companyName);
                    editT.commit();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            if (SharedPref.getSavedData(Export.this,"COMPANY_NAME") != null && SharedPref.getSavedData(
                    Export.this,"COMPANY_NAME") != "N/A"){
                companyName = SharedPref.getSavedData(Export.this,"COMPANY_NAME");
                companyNameEditText.setText(companyName);
            }else{
                companyName ="N/A";
                companyNameEditText.setText(companyName);
            }
        }
    }


}