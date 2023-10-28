package com.aican.aicanapp.fragments.ph;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.MODE_PRIVATE;
import static com.aican.aicanapp.utils.Constants.SERVER_PATH;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.DialogMain;
import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.adapters.LogAdapter;
import com.aican.aicanapp.adapters.PrintLogAdapter;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.dataClasses.phData;
import com.aican.aicanapp.ph.PhView;
import com.aican.aicanapp.specificactivities.Export;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.utils.AlarmConstants;
import com.aican.aicanapp.utils.Constants;
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
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class phLogFragment extends Fragment {

    private static float LOG_INTERVAL = 0;
    Handler handler1;
    Runnable runnable1;

    WebSocket webSocket1;
    JSONObject jsonData;

    int autoLogggg = 0;

    PhView phView;
    TextView tvPhCurr, tvPhNext;
    String ph, temp, mv, date, time, batchnum, arnum, compound_name, ph_fetched, m_fetched,
            currentDate_fetched, currentTime_fetched, batchnum_fetched,
            arnum_fetched, compound_name_fetched;
    String ph1, mv1, ph2, mv2, ph3, mv3, ph4, mv4, ph5, mv5, dt1, dt2, dt3, dt4, dt5;
    //    LineChart lineChart;
    String mode, reportDate, reportTime;
    private static final int PERMISSION_REQUEST_CODE = 200;
    DatabaseReference deviceRef;
    ArrayList<phData> phDataModelList = new ArrayList<>();
    LogAdapter adapter;
    String offset, battery, slope, tempe, temperature, roleExport, nullEntry;
    DatabaseHelper databaseHelper;
    Button logBtn, exportBtn, printBtn, clearBtn, submitBtn;
    ImageButton enterBtn, batchBtn, arBtn;
    PrintLogAdapter plAdapter;
    EditText compound_name_txt, batch_number, ar_number, enterTime;
    String TABLE_NAME = "LogUserdetails";
    RecyclerView recyclerView;
    Handler handler;
    Runnable runnable;
    SwitchCompat switchHold, switchInterval, switchBtnClick;
    CardView autoLog;
    TextView autoLogWarn;
    Boolean isAlertShow = true;
    CardView timer_cloud_layout;
    ImageButton saveTimer;
    String deviceID = "";
    TextView log_interval_text;

    int timerInSec;
    Boolean isTimer;
    String companyName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ph_log, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        printLifecycle("onResume");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Constants.OFFLINE_MODE) {
            initiateSocketConnection();
        }
        printLifecycle("onStart");
    }

    @Override
    public void onStop() {

//        deviceRef.child("Data").child("AUTOLOG").setValue(0);
//        deviceRef.child("Data").child("LOG_INTERVAL").setValue(0);
        if (Constants.OFFLINE_MODE) {
            webSocket1.cancel();
        }
        super.onStop();
        printLifecycle("onStop");


    }

    void printLifecycle(String lyf) {
        Log.e("LifeCycleFragment", lyf);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        printLifecycle("onCreate");
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onDestroyView() {
        printLifecycle("onDestroyView");
        super.onDestroyView();
    }


    @Override
    public void onDetach() {
        printLifecycle("onDetach");
        super.onDetach();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        phView = view.findViewById(R.id.phView);
        tvPhCurr = view.findViewById(R.id.tvPhCurr);
        tvPhNext = view.findViewById(R.id.tvPhNext);
        autoLog = view.findViewById(R.id.autoLog);
        log_interval_text = view.findViewById(R.id.log_interval_text);

        logBtn = view.findViewById(R.id.logBtn);
        exportBtn = view.findViewById(R.id.export);
        enterBtn = view.findViewById(R.id.enter_text);
        printBtn = view.findViewById(R.id.print);
        compound_name_txt = view.findViewById(R.id.compound_name);
        batch_number = view.findViewById(R.id.batch_number);
        ar_number = view.findViewById(R.id.ar_number);
        batchBtn = view.findViewById(R.id.batch_text);
        arBtn = view.findViewById(R.id.ar_text);
        switchHold = view.findViewById(R.id.switchHold);
        switchInterval = view.findViewById(R.id.switchInterval);
        switchBtnClick = view.findViewById(R.id.switchBtnClick);
        clearBtn = view.findViewById(R.id.clear);
        submitBtn = view.findViewById(R.id.submit);
        enterTime = view.findViewById(R.id.EnterTime);
        timer_cloud_layout = view.findViewById(R.id.timer_cloud_layout);
        recyclerView = view.findViewById(R.id.recyclerViewLog);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        saveTimer = (ImageButton) view.findViewById(R.id.sumbit_timer);
        RecyclerView csvRecyclerView = view.findViewById(R.id.recyclerViewCSVLog);
        csvRecyclerView.setHasFixedSize(true);
        csvRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        databaseHelper = new DatabaseHelper(getContext());
        adapter = new LogAdapter(getContext(), getSQLList());
        adapter.notifyItemInserted(0);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        nullEntry = " ";
        jsonData = new JSONObject();


        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);

        deviceRef.child("UI").child("PH").child("PH_CAL").child("COMPANY_NAME").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @com.google.firebase.database.annotations.NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    companyName = snapshot.getValue(String.class);
                } else {
                    deviceRef.child("UI").child("PH").child("PH_CAL").child("COMPANY_NAME").setValue("NA");
                    companyName = "NA";
                }
            }

            @Override
            public void onCancelled(@NonNull @com.google.firebase.database.annotations.NotNull DatabaseError error) {
            }
        });


        if (!Constants.OFFLINE_MODE) {

            autoLogs();
            fetch_logs();
        }

        getFirebaseValue();
        if (checkPermission()) {
            Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }

        SharedPreferences shp = getContext().getSharedPreferences("CalibPrefs", MODE_PRIVATE);

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

        ph1 = shp.getString("PH1", "");
        ph2 = shp.getString("PH2", "");
        ph3 = shp.getString("PH3", "");
        ph4 = shp.getString("PH4", "");
        ph5 = shp.getString("PH5", "");

        DialogMain dialogMain = new DialogMain(getContext());
        dialogMain.setCancelable(false);
        Source.userTrack = "PhLogFrag logged : ";
        if (Source.subscription.equals("cfr")) {
            dialogMain.show(getActivity().getSupportFragmentManager(), "example dialog");
        }
        deviceRef.child("Data").child("LOG").setValue(0);
//        deviceRef.child("Data").child("AUTOLOG").setValue(0);

//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                showChart();
//            }
//        }, 5000);


        submitBtn.setOnClickListener(view1 -> {
            saveDetails();
        });

        exportBtn.setEnabled(true);
        printBtn.setEnabled(true);

        if (!Constants.OFFLINE_MODE) {
            LOG_HOLD_AUTOLOG_FUN();
        }


        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Source.status_export = true;
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "Exported by " + Source.logUserName, ph, temp, mv, "", PhActivity.DEVICE_ID);

                SharedPreferences sh = getContext().getSharedPreferences("RolePref", MODE_PRIVATE);
                SharedPreferences.Editor roleE = sh.edit();
                String roleSuper = Source.logUserName;
                roleE.putString("roleSuper", roleSuper);
                roleE.commit();

                deleteAll();

                databaseHelper.insertCalibData(ph1, mv1, dt1);
                databaseHelper.insertCalibData(ph2, mv2, dt2);
                databaseHelper.insertCalibData(ph3, mv3, dt3);
                databaseHelper.insertCalibData(ph4, mv4, dt4);
                databaseHelper.insertCalibData(ph5, mv5, dt5);

                if (Source.subscription.equals("cfr")) {
//                    DialogMain dialogMain = new DialogMain();
//                    dialogMain.setCancelable(false);
//                    Source.userTrack = "PhLogFragment logged in by ";
                    dialogMain.show(getActivity().getSupportFragmentManager(), "example dialog");
                } else {
                    Intent intent = new Intent(getContext(), Export.class);
                    startActivity(intent);
                }
            }
        });

        /**
         * Getting a log of pH, temp, the time and date of that respective moment, and the name of the compound
         */
        logBtn.setOnClickListener(v -> {
            if (Constants.OFFLINE_MODE) {
                date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                if (ph == null || temp == null || mv == null) {
                    Toast.makeText(getContext(), "Fetching Data", Toast.LENGTH_SHORT).show();
                }

                databaseHelper.print_insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                databaseHelper.insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                databaseHelper.insert_action_data(time, date, "Log pressed : " + Source.logUserName, ph, temp, mv, compound_name, PhActivity.DEVICE_ID);

                adapter = new LogAdapter(getContext(), getList());
                recyclerView.setAdapter(adapter);

            } else {
                date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                fetch_logs();

                if (ph == null || temp == null || mv == null) {
                    Toast.makeText(getContext(), "Fetching Data", Toast.LENGTH_SHORT).show();
                } else {
                    databaseHelper.print_insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                    databaseHelper.insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                    databaseHelper.insert_action_data(time, date, "Log pressed : " + Source.logUserName, ph, temp, mv, compound_name, PhActivity.DEVICE_ID);
                }
                adapter = new LogAdapter(getContext(), getList());
                recyclerView.setAdapter(adapter);
            }
        });

        printBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                printBtn.setB

                try {
                    generatePDF();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
//                exportSensorCsv();

                String startsWith = "CurrentData";
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog";
                File root = new File(path);
                File[] filesAndFolders = root.listFiles();


                if (filesAndFolders == null || filesAndFolders.length == 0) {
                    Toast.makeText(requireContext(), "No Files Found", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    for (int i = 0; i < filesAndFolders.length; i++) {
                        filesAndFolders[i].getName().startsWith(startsWith);
                    }
                }


//                try {
//                    Workbook workbook = new Workbook(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog/CurrentData.xlsx");
//                    PdfSaveOptions options = new PdfSaveOptions();
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());
//                    String currentDateandTime = sdf.format(new Date());
//                    options.setCompliance(PdfCompliance.PDF_A_1_B);
//
////                    File Pdfdir = new File(Environment.getExternalStorageDirectory()+"/LabApp/Currentlog/LogPdf");
////                    if (!Pdfdir.exists()) {
////                        if (!Pdfdir.mkdirs()) {
////                            Log.d("App", "failed to create directory");
////                        }
////                    }
//                    String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog";
//                    File tempRoot = new File(tempPath);
//                    fileNotWrite(tempRoot);
//                    File[] tempFilesAndFolders = tempRoot.listFiles();
//                    workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog/CL_" + currentDateandTime + "_" + (tempFilesAndFolders.length - 1) + ".pdf", options);
//
//                    String path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog";
//                    File root1 = new File(path1);
//                    fileNotWrite(root1);
//                    File[] filesAndFolders1 = root1.listFiles();
//
//                    if (filesAndFolders1 == null || filesAndFolders1.length == 0) {
//
//                        return;
//                    } else {
//                        for (int i = 0; i < filesAndFolders1.length; i++) {
//                            if (filesAndFolders1[i].getName().endsWith(".csv") || filesAndFolders1[i].getName().endsWith(".xlsx")) {
//                                filesAndFolders1[i].delete();
//                            }
//                        }
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }


                String pathPDF = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog/";
                File rootPDF = new File(pathPDF);
                fileNotWrite(root);
                File[] filesAndFoldersPDF = rootPDF.listFiles();
                File[] filesAndFoldersNewPDF = new File[1];


                if (filesAndFoldersPDF == null || filesAndFoldersPDF.length == 0) {
                    return;
                } else {
                    for (int i = 0; i < filesAndFoldersPDF.length; i++) {
                        if (filesAndFoldersPDF[i].getName().endsWith(".pdf")) {
                            filesAndFoldersNewPDF[0] = filesAndFoldersPDF[i];

                        }
                    }

                }

                plAdapter = new PrintLogAdapter(getContext().getApplicationContext(), reverseFileArray(filesAndFoldersPDF));
                csvRecyclerView.setAdapter(plAdapter);
                plAdapter.notifyDataSetChanged();
                csvRecyclerView.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));


            }
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

//        String startsWith = "CurrentData";
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog";
        File root = new File(path);
        File[] filesAndFolders = root.listFiles();

        plAdapter = new PrintLogAdapter(getContext().getApplicationContext(), filesAndFolders);
        csvRecyclerView.setAdapter(plAdapter);
        plAdapter.notifyDataSetChanged();
        csvRecyclerView.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));
        if (checkPermission()) {
            Toast.makeText(getContext().getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }

        switchInterval.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (switchInterval.isChecked()) {
                    Constants.logIntervalActive = true;
                    autoLogggg = 2;
                    updateAutoLog();
                    if (Constants.OFFLINE_MODE) {
                        try {
                            switchBtnClick.setChecked(false);
                            switchHold.setChecked(false);
                            jsonData = new JSONObject();

                            jsonData.put("AUTOLOG", "2");
                            jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                            webSocket1.send(jsonData.toString());
                            if (Constants.timeInSec == 0) {

                            } else {
                                float f = (float) Constants.timeInSec / 60000;
                                enterTime.setText("" + f);
                                if (handler != null)
                                    handler.removeCallbacks(runnable);
                                takeLog();
                                handler();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        switchBtnClick.setChecked(false);
                        switchHold.setChecked(false);
                        deviceRef.child("Data").child("AUTOLOG").setValue(2);

                        if (Constants.timeInSec == 0) {

                        } else {
                            float f = (float) Constants.timeInSec / 60000;
                            enterTime.setText("" + f);
                            if (handler != null)
                                handler.removeCallbacks(runnable);
                            takeLog();
                            handler();
                        }
                    }

                } else {

                    Constants.logIntervalActive = false;
                    autoLogggg = 0;
                    updateAutoLog();
                    if (Constants.OFFLINE_MODE) {
                        try {
                            isTimer = false;
                            Constants.timeInSec = 0;
                            jsonData = new JSONObject();
                            jsonData.put("AUTOLOG", "0");
                            jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                            webSocket1.send(jsonData.toString());
                            isAlertShow = true;
                            if (handler != null)
                                handler.removeCallbacks(runnable);

                            if (!switchHold.isChecked() && !switchBtnClick.isChecked()) {
                                jsonData = new JSONObject();
                                jsonData.put("AUTOLOG", "0");
                                jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                webSocket1.send(jsonData.toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        isTimer = false;
                        Constants.timeInSec = 0;
                        deviceRef.child("Data").child("LOG_INTERVAL").setValue(0);

                        isAlertShow = true;
                        if (handler != null)
                            handler.removeCallbacks(runnable);

                        if (!switchHold.isChecked() && !switchBtnClick.isChecked()) {
                            deviceRef.child("Data").child("AUTOLOG").setValue(0);

                        }

                    }
                }
            }
        });

        saveTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!enterTime.getText().toString().isEmpty() && switchInterval.isChecked()) {
                    if (Constants.OFFLINE_MODE) {
                        try {
                            double d = Double.parseDouble(enterTime.getText().toString()) * 60000;
                            Double db = new Double(d);
                            Constants.timeInSec = db.intValue();
                            double a = (double) Constants.timeInSec / 60000;
                            Log.d("TimerVal", "" + a);


                            deviceRef.child("Data").child("LOG_INTERVAL").setValue(a);

                            jsonData = new JSONObject();

                            jsonData.put("LOG_INTERVAL", String.valueOf(a));
                            jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                            webSocket1.send(jsonData.toString());
                            LOG_INTERVAL = Float.parseFloat(enterTime.getText().toString()) * 60;
                            log_interval_text.setText(String.valueOf(LOG_INTERVAL));

//                    startTimer();

                            takeLog();
                            handler();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        double d = Double.parseDouble(enterTime.getText().toString()) * 60000;
                        Double db = new Double(d);
                        Constants.timeInSec = db.intValue();
                        double a = (double) Constants.timeInSec / 60000;
                        Log.d("TimerVal", "" + a);
                        deviceRef.child("Data").child("LOG_INTERVAL").setValue(a);
                        LOG_INTERVAL = Float.parseFloat(enterTime.getText().toString()) * 60;
                        log_interval_text.setText(String.valueOf(LOG_INTERVAL));

//                    startTimer();

                        takeLog();
                        handler();
                    }
                }
            }
        });

        switchBtnClick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (switchBtnClick.isChecked()) {
                    autoLogggg = 3;
                    updateAutoLog();
                    if (Constants.OFFLINE_MODE) {
                        try {
                            jsonData = new JSONObject();
                            jsonData.put("AUTOLOG", "3");
                            jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                            webSocket1.send(jsonData.toString());

                            if (switchInterval.isChecked()) {
                                isTimer = false;
                                handler.removeCallbacks(runnable);
                                switchInterval.setChecked(false);
                            }
                            switchHold.setChecked(false);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {

                        deviceRef.child("Data").child("AUTOLOG").setValue(3);

                        if (switchInterval.isChecked()) {
                            isTimer = false;
                            handler.removeCallbacks(runnable);
                            switchInterval.setChecked(false);
                        }
                        switchHold.setChecked(false);
                    }
                } else {
                    autoLogggg = 0;
                    updateAutoLog();
//                    if (!switchInterval.isChecked() && !switchHold.isChecked()) {
                    if (Constants.OFFLINE_MODE) {
                        try {
                            jsonData = new JSONObject();
                            jsonData.put("AUTOLOG", "0");
                            jsonData.put("LOG", "0");
                            jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                            webSocket1.send(jsonData.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        deviceRef.child("Data").child("AUTOLOG").setValue(0);
                        deviceRef.child("Data").child("LOG").setValue(0);
                    }
                }
            }

        });
        if (deviceRef.child("Data").child("LOG_INTERVAL") != null) {
            deviceRef.child("Data").child("LOG_INTERVAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double a = snapshot.getValue(Double.class) * 60000;
                    Constants.timeInSec = (int) a;
                    if (snapshot.getValue(Double.class) != null) {
                        if (switchInterval.isChecked() && !isAlertShow) {
                            Log.d("Timer", "onDataChange: " + Constants.timeInSec);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        switchHold.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (switchHold.isChecked()) {
                    autoLogggg = 1;
                    updateAutoLog();
                    if (Constants.OFFLINE_MODE) {
                        try {
                            jsonData = new JSONObject();
                            jsonData.put("HOLD", "0");
                            jsonData.put("AUTOLOG", "1");
                            jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                            webSocket1.send(jsonData.toString());

                            if (switchInterval.isChecked()) {
                                isTimer = false;
                                handler.removeCallbacks(runnable);
                                switchInterval.setChecked(false);
                            }
                            switchBtnClick.setChecked(false);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        deviceRef.child("Data").child("HOLD").setValue(0);
                        deviceRef.child("Data").child("AUTOLOG").setValue(1);
                        if (switchInterval.isChecked()) {
                            isTimer = false;
                            handler.removeCallbacks(runnable);
                            switchInterval.setChecked(false);
                        }
                        switchBtnClick.setChecked(false);
                    }
                } else {
                    if (!switchInterval.isChecked() && !switchBtnClick.isChecked()) {
                        autoLogggg = 0;
                        updateAutoLog();
                        if (Constants.OFFLINE_MODE) {
                            try {
                                jsonData = new JSONObject();
                                jsonData.put("AUTOLOG", "0");
                                jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                webSocket1.send(jsonData.toString());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            deviceRef.child("Data").child("AUTOLOG").setValue(0);
                        }
                    }
                }
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phDataModelList.clear();
                ArrayList<phData> ar = new ArrayList<>();
                adapter = new LogAdapter(getContext(), ar);
                recyclerView.setAdapter(adapter);
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                Cursor curCSV = db.rawQuery("SELECT * FROM PrintLogUserdetails", null);

                if (curCSV != null && curCSV.getCount() > 0) {
                    deleteAllLogs();
                } else {
                    Toast.makeText(requireContext(), "Database is empty, please insert values", Toast.LENGTH_SHORT).show();
                }


            }
        });

        if (enterTime.getText().toString().equals("")) {
            LOG_INTERVAL = 0 * 60;

        } else {
            LOG_INTERVAL = Float.parseFloat(enterTime.getText().toString()) * 60;
        }
        log_interval_text.setText(String.valueOf(LOG_INTERVAL));

        updateAutoLog();

    }

    private void updateAutoLog() {
        int AutoLog = autoLogggg;
        Source.auto_log = AutoLog;
        if (AutoLog == 0) {
            exportBtn.setEnabled(true);
            printBtn.setEnabled(true);

        } else if (AutoLog == 1) {
            exportBtn.setEnabled(false);
            printBtn.setEnabled(false);
            switchHold.setChecked(true);
            switchInterval.setChecked(false);
            switchBtnClick.setChecked(false);
        } else if (AutoLog == 2) {
            exportBtn.setEnabled(false);
            printBtn.setEnabled(false);
            isAlertShow = false;
            switchHold.setChecked(false);
            switchInterval.setChecked(true);
            switchBtnClick.setChecked(false);
        } else if (AutoLog == 3) {
            exportBtn.setEnabled(false);
            printBtn.setEnabled(false);
            switchHold.setChecked(false);
            switchInterval.setChecked(false);
            switchBtnClick.setChecked(true);
        } else {
            exportBtn.setEnabled(true);
            printBtn.setEnabled(true);
        }
    }

    private void LOG_HOLD_AUTOLOG_FUN() {
        deviceRef.child("Data").child("LOG").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int log = snapshot.getValue(Integer.class);

                if (log == 1) {
                    date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                    fetch_logs();
                    if (ph == null || temp == null || mv == null) {
                        Toast.makeText(getContext(), "Fetching Data", Toast.LENGTH_SHORT).show();
                    } else {
                        databaseHelper.insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                        databaseHelper.print_insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                        databaseHelper.insert_action_data(time, date, "Log pressed : " + Source.logUserName, ph, temp, mv, compound_name, PhActivity.DEVICE_ID);
                    }
                    if (switchBtnClick.isChecked()) {
                        takeLog();
                    }
                    deviceRef.child("Data").child("LOG").setValue(0);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        deviceRef.child("Data").child("HOLD").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                int hold = snapshot.getValue(Integer.class);

                if (switchHold.isChecked())
                    if (hold == 1) {
                        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                        deviceRef.child("Data").child("HOLD").setValue(0);
                        fetch_logs();

                        if (ph == null || temp == null || mv == null) {
                            Toast.makeText(getContext(), "Fetching Data", Toast.LENGTH_SHORT).show();
                        } else {
                            databaseHelper.print_insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                            databaseHelper.insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                            databaseHelper.insert_action_data(time, date, "Log pressed : " + Source.userName, ph, temp, mv, compound_name, PhActivity.DEVICE_ID);
                        }
                        adapter = new LogAdapter(getContext(), getList());
                        recyclerView.setAdapter(adapter);
                    }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });


        deviceRef.child("Data").child("AUTOLOG").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                int AutoLog = snapshot.getValue(Integer.class);
                Source.auto_log = AutoLog;
                if (AutoLog == 0) {
                    exportBtn.setEnabled(true);
                    printBtn.setEnabled(true);

                } else if (AutoLog == 1) {
                    exportBtn.setEnabled(false);
                    printBtn.setEnabled(false);
                    switchHold.setChecked(true);
                    switchInterval.setChecked(false);
                    switchBtnClick.setChecked(false);
                } else if (AutoLog == 2) {
                    exportBtn.setEnabled(false);
                    printBtn.setEnabled(false);
                    isAlertShow = false;
                    switchHold.setChecked(false);
                    switchInterval.setChecked(true);
                    switchBtnClick.setChecked(false);
                } else if (AutoLog == 3) {
                    exportBtn.setEnabled(false);
                    printBtn.setEnabled(false);
                    switchHold.setChecked(false);
                    switchInterval.setChecked(false);
                    switchBtnClick.setChecked(true);
                } else {
                    exportBtn.setEnabled(true);
                    printBtn.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

//
//        deviceRef.child("Data").child("AUTOLOG").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                int clicked = snapshot.getValue(Integer.class);
//                if(clicked == 1){
//                   takeLog();
//                    deviceRef.child("Data").child("AUTOLOG").setValue(0);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//            }
//        });

    }

    private void autoLogs() {
        autoLog.setVisibility(View.GONE);
        deviceRef.child("AUTO_LOG").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String autoLogVar = snapshot.getValue(String.class);
                    if (autoLogVar.equals("on")) {
                        autoLog.setVisibility(View.VISIBLE);
                    } else if (autoLogVar.equals("off")) {
                        autoLog.setVisibility(View.GONE);
                    } else {
                        autoLog.setVisibility(View.GONE);
                    }
                } else {
                    deviceRef.child("AUTO_LOG").setValue("off");

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }

    private void startTimer() {

        LOG_INTERVAL = Float.parseFloat(enterTime.getText().toString()) * 60;
        log_interval_text.setText(String.valueOf(LOG_INTERVAL));
        handler1 = new Handler();
        runnable1 = new Runnable() {
            public void run() {
                if (!Constants.logIntervalActive) {
                    LOG_INTERVAL = 0;
                    log_interval_text.setText(String.valueOf(LOG_INTERVAL));
                    handler1.removeCallbacks(this);
                }
                Log.d("Runnable", "Handler is working");
                if (LOG_INTERVAL == 0) { // just remove call backs

                    log_interval_text.setText(String.valueOf(LOG_INTERVAL));
                    handler1.removeCallbacks(this);
                    Log.d("Runnable", "ok");
                } else { // post again
                    --LOG_INTERVAL;
                    log_interval_text.setText(String.valueOf(LOG_INTERVAL));
                    handler1.postDelayed(this, 1000);
                }
            }
        };

        runnable1.run();
    }

    private void generatePDF() throws FileNotFoundException {

        String company_name = "Company: " + companyName;
        String user_name = "Username: " + Source.logUserName;
        String device_id = "DeviceID: " + deviceID;

        reportDate = "Date: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        reportTime = "Time: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());


        SharedPreferences shp = requireContext().getSharedPreferences("Extras", MODE_PRIVATE);
        offset = "Offset: " + shp.getString("offset", "");
        battery = "Battery: " + shp.getString("battery", "");
        slope = "Slope: " + shp.getString("slope", "");
        tempe = "Temperature: " + shp.getString("temp", "");

        File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog";
        File tempRoot = new File(tempPath);
        fileNotWrite(tempRoot);
        File[] tempFilesAndFolders = tempRoot.listFiles();


        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog/CL_" + currentDateandTime + "_" + ((tempFilesAndFolders != null ? tempFilesAndFolders.length : 0) - 1) + ".pdf");
        OutputStream outputStream = new FileOutputStream(file);
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        if (Constants.OFFLINE_MODE) {
            document.add(new Paragraph("Offline Mode"));
        }
        document.add(new Paragraph(company_name + "\n" + user_name + "\n" + device_id));
        document.add(new Paragraph(""));
        document.add(new Paragraph(reportDate
                + "  |  " + reportTime + "\n" +
                offset + "  |  " + battery + "\n" + slope + "  |  " + tempe
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

            table.addCell(ph != null ? ph : "--");
            table.addCell(pHAC != null ? pHAC : "--");
            table.addCell(slope != null ? slope : "--");
            table.addCell(mv != null ? mv : "--");
            table.addCell(date != null ? date : "--");
            table.addCell(temperature1 != null ? temperature1 : "--");

        }
        document.add(table);

        document.add(new Paragraph(""));
        document.add(new Paragraph("Log Table"));

        float[] columnWidth1 = {240f, 120f, 150f, 150f, 270f, 270f, 270f};
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
            curCSV = db.rawQuery("SELECT * FROM PrintLogUserdetails", null);
        } else {
            curCSV = db.rawQuery("SELECT * FROM PrintLogUserdetails", null);
        }


        while (curCSV.moveToNext()) {

            String date = curCSV.getString(curCSV.getColumnIndex("date"));
            String time = curCSV.getString(curCSV.getColumnIndex("time"));
            String pH = curCSV.getString(curCSV.getColumnIndex("ph"));
            String temp = curCSV.getString(curCSV.getColumnIndex("temperature"));
            String batchnum = curCSV.getString(curCSV.getColumnIndex("batchnum"));
            String arnum = curCSV.getString(curCSV.getColumnIndex("arnum"));
            String comp = curCSV.getString(curCSV.getColumnIndex("compound"));

            String newBatchNum = "--";
            if (batchnum != null && batchnum.length() >= 8) {
                newBatchNum = stringSplitter(batchnum);
            } else {
                newBatchNum = batchnum;
            }
            String newArum = "--";
            if (arnum != null && arnum.length() >= 8) {
                newArum = stringSplitter(arnum);
            } else {
                newArum = arnum;
            }
            String newComp = "--";
            if (comp != null && comp.length() >= 8) {
                newComp = stringSplitter(comp);
            } else {
                newComp = comp;
            }

            table1.addCell(date != null ? date : "--");
            table1.addCell(time != null ? time : "--");
            table1.addCell(pH != null ? pH : "--");
            table1.addCell(temp != null ? temp : "--");
            table1.addCell(newBatchNum != null ? newBatchNum : "--");
            table1.addCell(newArum != null ? newArum : "--");
            table1.addCell(newComp != null ? newComp : "--");

        }

        document.add(table1);

        document.add(new Paragraph("Operator Sign                                                                                      Supervisor Sign"));

        Bitmap imgBit1 = getSignImage();
        if (imgBit1 != null) {
            Uri uri1 = getImageUri(getContext(), imgBit1);

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

        Toast.makeText(getContext(), "Pdf generated", Toast.LENGTH_SHORT).show();
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireActivity().managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private Bitmap getSignImage() {
        SharedPreferences sh = getContext().getSharedPreferences("signature", Context.MODE_PRIVATE);
        String photo = sh.getString("signature_data", "");
        Bitmap bitmap = null;

        if (!photo.equalsIgnoreCase("")) {
            byte[] b = Base64.decode(photo, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return bitmap;
    }


    private String stringSplitter(String str) {
        String newText = "";
        Iterable<String> strings = Splitter.fixedLength(8).split(str);
        for (String temp : strings) {
            newText = newText + " " + temp;
        }
        return newText.trim();
    }

    private void saveDetails() {

        if (Constants.OFFLINE_MODE) {
            if (!compound_name_txt.getText().toString().isEmpty()) {
                compound_name = compound_name_txt.getText().toString();
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "Compound name changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

            } else {
                compound_name = "NA";
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "Compound name changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

            }

            if (!batch_number.getText().toString().isEmpty()) {
                batchnum = batch_number.getText().toString();

                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "Batchnum changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);


            } else {
                batchnum = "NA";
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "Batchnum changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

            }

            if (!ar_number.getText().toString().isEmpty()) {
                arnum = ar_number.getText().toString();
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "AR_NUMBER changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

            } else {
                arnum = "NA";
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "AR_NUMBER changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

            }

        } else {


            if (!compound_name_txt.getText().toString().isEmpty()) {
                compound_name = compound_name_txt.getText().toString();
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "Compound name changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

            } else {
                compound_name = "NA";
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "Compound name changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

            }


            deviceRef.child("Data").child("COMPOUND_NAME").setValue(compound_name);


            //saving batch number
            if (!batch_number.getText().toString().isEmpty()) {
                batchnum = batch_number.getText().toString();

                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "Batchnum changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);


            } else {
                batchnum = "NA";
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "Batchnum changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

            }

            deviceRef.child("Data").child("BATCH_NUMBER").setValue(batchnum);


            if (!ar_number.getText().toString().isEmpty()) {
                arnum = ar_number.getText().toString();
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "AR_NUMBER changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

            } else {
                arnum = "NA";
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "AR_NUMBER changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

            }

            deviceRef.child("Data").child("AR_NUMBER").setValue(arnum);

        }
    }

    private void saveDetails2() {

        if (Constants.OFFLINE_MODE) {
            if (!compound_name_txt.getText().toString().isEmpty()) {
                compound_name = compound_name_txt.getText().toString();
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "Compound name changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

            } else {
                compound_name = "NA";
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "Compound name changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

            }

            if (!batch_number.getText().toString().isEmpty()) {
                batchnum = batch_number.getText().toString();

                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "Batchnum changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);


            } else {
                batchnum = "NA";
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "Batchnum changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

            }

            if (!ar_number.getText().toString().isEmpty()) {
                arnum = ar_number.getText().toString();
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "AR_NUMBER changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

            } else {
                arnum = "NA";
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "AR_NUMBER changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

            }

        } else {


            deviceRef.child("Data").child("COMPOUND_NAME").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (!compound_name_txt.getText().toString().isEmpty()) {
                        compound_name = compound_name_txt.getText().toString();
                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                        databaseHelper.insert_action_data(time, date, "Compound name changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

                    } else {
                        compound_name = "NA";
                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                        databaseHelper.insert_action_data(time, date, "Compound name changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

                    }
                    snapshot.getRef().setValue(compound_name);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });

            //saving batch number
            deviceRef.child("Data").child("BATCH_NUMBER").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (!batch_number.getText().toString().isEmpty()) {
                        batchnum = batch_number.getText().toString();

                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                        databaseHelper.insert_action_data(time, date, "Batchnum changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);


                    } else {
                        batchnum = "NA";
                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                        databaseHelper.insert_action_data(time, date, "Batchnum changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

                    }
                    snapshot.getRef().setValue(batchnum);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });

            deviceRef.child("Data").child("AR_NUMBER").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (!ar_number.getText().toString().isEmpty()) {
                        arnum = ar_number.getText().toString();
                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                        databaseHelper.insert_action_data(time, date, "AR_NUMBER changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

                    } else {
                        arnum = "NA";
                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                        databaseHelper.insert_action_data(time, date, "AR_NUMBER changed : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

                    }
                    snapshot.getRef().setValue(arnum);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });
        }
    }

    @Override
    public void onDestroy() {

        printLifecycle("onDestroy");
        deviceRef.child("Data").child("AUTOLOG").setValue(0);
        deleteAllLogs();
        deleteAllLogsOffline();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        printLifecycle("onPause");
        Log.d("Timer", "onPause: ");
        if (handler != null)
            handler.removeCallbacks(runnable);
        super.onPause();
    }

    private void openTimerDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.set_timer_dialog_layout);
        dialog.show();

        Button cancel, set_timer;
        cancel = dialog.findViewById(R.id.cancelBtn);
        set_timer = dialog.findViewById(R.id.set_timer);


        EditText timer = dialog.findViewById(R.id.timerEditText);

        set_timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!timer.getText().toString().isEmpty()) {
                    double d = Double.parseDouble(timer.getText().toString()) * 60000;
                    Double db = new Double(d);
                    Constants.timeInSec = db.intValue();
                    deviceRef.child("Data").child("LOG_INTERVAL").setValue(Constants.timeInSec);
                    LOG_INTERVAL = Float.parseFloat(enterTime.getText().toString()) * 60;
                    log_interval_text.setText(String.valueOf(LOG_INTERVAL));
                    takeLog();
                    handler();
                    dialog.dismiss();
                }
            }
        });

        cancel.setOnClickListener(view -> {
            dialog.dismiss();
            switchInterval.setChecked(false);
        });
    }

    void handler() {
        Log.d("Timer", "doInBackground: in while " + Constants.timeInSec);


        Toast.makeText(getContext(), "Background service running ", Toast.LENGTH_SHORT).show();
        Toast.makeText(getContext(), Constants.timeInSec + "", Toast.LENGTH_SHORT).show();

        if (handler1 != null) {
            handler1.removeCallbacks(runnable1);
        }
        startTimer();


        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                if (!Constants.logIntervalActive) {
                    handler.removeCallbacks(this);
                }

                Log.d("Timer", "doInBackground: in handler");

                takeLog();
                handler();


            }
        };
        handler.postDelayed(runnable, Constants.timeInSec);
        Log.d("Timer", "doInBackground: out handler");


    }

    public void getFirebaseValue() {
        DatabaseReference dataRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child(Dashboard.DEVICE_TYPE_PH).child(PhActivity.DEVICE_ID);
        dataRef.child("ID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @com.google.firebase.database.annotations.NotNull DataSnapshot snapshot) {
                String p = snapshot.getValue(String.class);
                deviceID = p;
            }

            @Override
            public void onCancelled(@NonNull @com.google.firebase.database.annotations.NotNull DatabaseError error) {
            }
        });
    }

    void takeLog() {

        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        if (Constants.OFFLINE_MODE) {
            databaseHelper.print_insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
            databaseHelper.insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
            databaseHelper.insert_action_data(time, date, "Log pressed : " + Source.logUserName, ph, temp, mv, compound_name, PhActivity.DEVICE_ID);
        } else {
            fetch_logs();

            if (ph == null || temp == null || mv == null) {

                Toast.makeText(getContext(), "Fetching Data", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("TakeLog", "takeLog: " + date + " " + time + " " + ph + " " + temp + " " + batchnum + " " + arnum + " " + compound_name + " " + PhActivity.DEVICE_ID);

                databaseHelper.print_insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                databaseHelper.insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                databaseHelper.insert_action_data(time, date, "Log pressed : " + Source.logUserName, ph, temp, mv, compound_name, PhActivity.DEVICE_ID);
            }
        }
        adapter = new LogAdapter(getContext(), getList());
        recyclerView.setAdapter(adapter);

    }

    public void fileNotWrite(File file) {
        file.setWritable(false);
        if (file.canWrite()) {
            Log.d("csv", "Not Working");
        } else {
            Log.d("csvnw", "Working");
        }
    }

    /**
     * Passing on the data to LogAdapter
     *
     * @return
     */


    private List<phData> getList() {
        phDataModelList.add(0, new phData(ph, temp, date, time, batchnum, arnum, compound_name));
        return phDataModelList;
    }

    public void deleteAll() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.execSQL("DELETE FROM Calibdetails");
        db.close();
    }


    public void deleteAllLogs() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.execSQL("DELETE FROM PrintLogUserdetails");
        db.close();
    }

    public void deleteAllLogsOffline() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.execSQL("DELETE FROM PrintLogUserdetailsOffline");
        db.close();
    }

    public int columns() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.databaseHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }


    /**
     * Fetching the values from firebase
     */
    private void fetch_logs() {
        deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float p = snapshot.getValue(Float.class);
                ph = String.format(Locale.UK, "%.2f", p);

                tvPhCurr.setText(ph);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("Data").child("TEMP_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float temp1 = snapshot.getValue(Float.class);
                String tempe = temp1 <= -127.0 ? "NA" : String.format(Locale.UK, "%.2f", temp1);

                phLogFragment.this.temp = tempe;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("Data").child("COMPOUND_NAME").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                compound_name = (String) snapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("Data").child("BATCH_NUMBER").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                batchnum = (String) snapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("Data").child("AR_NUMBER").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                arnum = (String) snapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });


        deviceRef.child("Data").child("EC_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float p = snapshot.getValue(Float.class);
                mv = String.format(Locale.UK, "%.2f", p);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }

    /**
     * Fetching log entries from SQL Database
     *
     * @return
     */
    private ArrayList<phData> getSQLList() {
        Cursor res = databaseHelper.get_log();
        if (res.getCount() == 0) {
            Toast.makeText(getContext(), "No entry", Toast.LENGTH_SHORT).show();
        }
        while (res.moveToNext()) {
            date = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(new Date());
            time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            currentDate_fetched = res.getString(0);
            currentTime_fetched = res.getString(1);
            ph_fetched = res.getString(2);
            m_fetched = res.getString(3);
            batchnum_fetched = res.getString(4);
            arnum_fetched = res.getString(5);
            compound_name_fetched = res.getString(6);
            if (date.equals(currentDate_fetched) && time.equals(currentTime_fetched)) {
                phDataModelList.add(0, new phData(ph_fetched, m_fetched, currentDate_fetched,
                        currentTime_fetched, batchnum_fetched, arnum_fetched, compound_name_fetched));
            }
        }
        return phDataModelList;
    }

    /**
     * checking of permissions.
     *
     * @return
     */
    private boolean checkPermission() {
        int permission1 = ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * requesting permissions if not provided.
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions((Activity) requireContext(), new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    /**
     * after requesting permissions we are showing
     * users a toast message of permission granted.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(requireContext(), "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Permission Denined.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    File[] reverseFileArray(File[] fileArray) {
        for (int i = 0; i < fileArray.length / 2; i++) {
            File a = fileArray[i];
            fileArray[i] = fileArray[fileArray.length - i - 1];
            fileArray[fileArray.length - i - 1] = a;
        }

        return fileArray.length > 0 ? fileArray : null;
    }

    private void initiateSocketConnection() {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_PATH).build();
        webSocket1 = client.newWebSocket(request, new SocketListener());
    }

    private class SocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);

            if (webSocket1 == null) {
                webSocket.cancel();
            }

            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(),
                        "Socket Connection Successful!",
                        Toast.LENGTH_SHORT).show();

                try {
                    jsonData.put("SOCKET_INIT", "Successfully Initialized on phLogFragment");
                    jsonData.put("LOG", "0");
                    jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                    webSocket.send(jsonData.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                                "Socket Connection Unsuccessful!",
                                Toast.LENGTH_SHORT).show();

                    });
                }

            });

        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);

            if (webSocket1 == null) {
                webSocket.cancel();
            }

            getActivity().runOnUiThread(() -> {
                try {
                    jsonData = new JSONObject(text);
                    Log.d("JSONReceived:PHFragment", "onMessage: " + text);

                    if (jsonData.has("PH_VAL") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                        String val = jsonData.getString("PH_VAL");
                        tvPhCurr.setText(val);
                        phView.moveTo(Float.parseFloat(val));
                        ph = val;
                        AlarmConstants.PH = Float.parseFloat(val);
                    }

                    if (jsonData.has("TEMP_VAL") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {

                        float tempval = Float.parseFloat(jsonData.getString("TEMP_VAL"));



                        String temp1 = String.valueOf(Math.round(tempval));

                        if (Integer.parseInt(temp1) <= -127) {
                            temp = "NA";
                        } else {
                            temp = temp1;
                        }

                    }

                    if (jsonData.has("LOG") && jsonData.getString("LOG").equals("1") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                        if (switchBtnClick.isChecked()) {
                            date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                            time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
//                        fetch_logs();
                            if (ph == null || temp == null || mv == null) {
                                Toast.makeText(getContext(), "Fetching Data", Toast.LENGTH_SHORT).show();
                            }
//                        } else {
//                            databaseHelper.print_insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
//                            databaseHelper.insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
//                            databaseHelper.insert_action_data(time, date, "Log pressed : " + Source.logUserName, ph, temp, mv, compound_name, PhActivity.DEVICE_ID);
//                        }
//                        if (Constants.OFFLINE_MODE) {
//                            databaseHelper.print_insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
//                            databaseHelper.insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
//                        }
                            takeLog();
                        }
//                        deviceRef.child("Data").child("LOG").setValue(0);

                    }

                    if (jsonData.has("HOLD") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {

                        if (switchHold.isChecked()) {
                            if (jsonData.getString("HOLD").equals("1")) {
                                date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
//                            fetch_logs();
                                Toast.makeText(getContext(), "HOLD " + jsonData.getString("HOLD"), Toast.LENGTH_SHORT).show();

                                jsonData = new JSONObject();
                                jsonData.put("HOLD", String.valueOf(0));
                                jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                                webSocket1.send(jsonData.toString());

//                                deviceRef.child("Data").child("HOLD").setValue(0);


                                if (ph == null || temp == null || mv == null) {
//                                    Toast.makeText(getContext(), "Fetching Data", Toast.LENGTH_SHORT).show();
                                }
//                                } else {
//                                    databaseHelper.print_insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
//                                    databaseHelper.insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
//                                    databaseHelper.insert_action_data(time, date, "Log pressed : " + Source.userName, ph, temp, mv, compound_name, PhActivity.DEVICE_ID);
//                                }
                                adapter = new LogAdapter(getContext(), getList());
                                recyclerView.setAdapter(adapter);
                                if (Constants.OFFLINE_MODE) {
                                    databaseHelper.print_insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                                    databaseHelper.insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                                }
                            }

                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

        }

    }


}
