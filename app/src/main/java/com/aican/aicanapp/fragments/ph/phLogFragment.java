package com.aican.aicanapp.fragments.ph;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.Handler;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aican.aicanapp.Source;
import com.aican.aicanapp.adapters.FileAdapter;
import com.aican.aicanapp.adapters.PrintLogAdapter;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.DialogMain;
import com.aican.aicanapp.R;

import com.aican.aicanapp.adapters.LogAdapter;
import com.aican.aicanapp.dataClasses.phData;

import com.aican.aicanapp.ph.PhView;
import com.aican.aicanapp.specificactivities.Export;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.utils.AlarmConstants;
import com.aican.aicanapp.utils.Constants;
import com.aican.aicanapp.utils.MyXAxisValueFormatter;
import com.aspose.cells.FileFormatType;
import com.aspose.cells.LoadOptions;
import com.aspose.cells.PdfCompliance;
import com.aspose.cells.PdfSaveOptions;
import com.aspose.cells.Range;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.StyleFlag;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.api.LogDescriptor;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class phLogFragment extends Fragment {

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
    String offset, battery, slope, temperature, roleExport, nullEntry;
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
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {

//        deviceRef.child("Data").child("AUTOLOG").setValue(0);
//        deviceRef.child("Data").child("LOG_INTERVAL").setValue(0);
        super.onStop();


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        phView = view.findViewById(R.id.phView);
        tvPhCurr = view.findViewById(R.id.tvPhCurr);
        tvPhNext = view.findViewById(R.id.tvPhNext);
        autoLog = view.findViewById(R.id.autoLog);

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
        saveTimer = view.findViewById(R.id.sumbit_timer);
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

        fetch_logs();

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

        DialogMain dialogMain = new DialogMain();
        dialogMain.setCancelable(false);
        Source.userTrack = "PhLogFrag logged : ";
        if (Source.subscription.equals("cfr")) {
            dialogMain.show(getActivity().getSupportFragmentManager(), "example dialog");
        }
        deviceRef.child("Data").child("LOG").setValue(0);
//        deviceRef.child("Data").child("AUTOLOG").setValue(0);

        enterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compound_name = compound_name_txt.getText().toString();
                if (compound_name.matches("")) {
                    Toast.makeText(getContext(), "Enter Compound Name", Toast.LENGTH_SHORT).show();
                } else {
                    deviceRef.child("Data").child("COMPOUND_NAME").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            snapshot.getRef().setValue(compound_name);
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        }
                    });
                }
            }
        });

//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                showChart();
//            }
//        }, 5000);

        batchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                batchnum = batch_number.getText().toString();
                if (batchnum.matches("")) {
                    Toast.makeText(getContext(), "Enter Batch Name", Toast.LENGTH_SHORT).show();
                } else {
                    deviceRef.child("Data").child("BATCH_NUMBER").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            snapshot.getRef().setValue(batchnum);
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        }
                    });
                }
            }
        });

        arBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arnum = ar_number.getText().toString();
                if (arnum.matches("")) {
                    Toast.makeText(getContext(), "Enter AR Name", Toast.LENGTH_SHORT).show();
                } else {
                    deviceRef.child("Data").child("AR_NUMBER").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            snapshot.getRef().setValue(arnum);
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        }
                    });
                }
            }
        });

        submitBtn.setOnClickListener(view1 -> {
            saveDetails();
        });

        if (deviceRef.child("Data").child("LOG") != null)
            deviceRef.child("Data").child("LOG").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int log = snapshot.getValue(Integer.class);

                    if (log == 1) {
                        deviceRef.child("Data").child("LOG").setValue(0);
                        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                        fetch_logs();
                        if (ph == null || temp == null || mv == null) {
                            Toast.makeText(getContext(), "Fetching Data", Toast.LENGTH_SHORT).show();
                        } else {
                            databaseHelper.insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                            databaseHelper.insert_action_data(date + " " + time, "Log pressed : " + Source.logUserName, ph, temp, mv, compound_name, PhActivity.DEVICE_ID);
                        }
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
                        fetch_logs();
                        deviceRef.child("Data").child("HOLD").setValue(0);

                        if (ph == null || temp == null || mv == null) {
                            Toast.makeText(getContext(), "Fetching Data", Toast.LENGTH_SHORT).show();
                        } else {
                            databaseHelper.print_insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                            databaseHelper.insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                            databaseHelper.insert_action_data(date + " " + time, "Log pressed : " + Source.userName, ph, temp, mv, compound_name, PhActivity.DEVICE_ID);
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

                if (AutoLog == 1) {
                    switchHold.setChecked(true);
                    switchInterval.setChecked(false);
                    switchBtnClick.setChecked(false);
                } else if (AutoLog == 2) {
                    isAlertShow = false;
                    switchHold.setChecked(false);
                    switchInterval.setChecked(true);
                    switchBtnClick.setChecked(false);
                } else if (AutoLog == 3) {
                    switchHold.setChecked(false);
                    switchInterval.setChecked(false);
                    switchBtnClick.setChecked(true);
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

        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Source.status_export = true;
                time = new SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale.getDefault()).format(new Date());
//                databaseHelper.insert_action_data(time, "Exported by " + Source.logUserName, ph, temp, mv, "", PhActivity.DEVICE_ID);

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

            date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            fetch_logs();

            if (ph == null || temp == null || mv == null) {
                Toast.makeText(getContext(), "Fetching Data", Toast.LENGTH_SHORT).show();
            } else {
                databaseHelper.print_insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                databaseHelper.insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
                databaseHelper.insert_action_data(date + " " + time, "Log pressed : " + Source.logUserName, ph, temp, mv, compound_name, PhActivity.DEVICE_ID);
            }
            adapter = new LogAdapter(getContext(), getList());
            recyclerView.setAdapter(adapter);
        });

        printBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportSensorCsv();

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


                try {
                    Workbook workbook = new Workbook(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog/CurrentData.xlsx");
                    PdfSaveOptions options = new PdfSaveOptions();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());
                    options.setCompliance(PdfCompliance.PDF_A_1_B);

//                    File Pdfdir = new File(Environment.getExternalStorageDirectory()+"/LabApp/Currentlog/LogPdf");
//                    if (!Pdfdir.exists()) {
//                        if (!Pdfdir.mkdirs()) {
//                            Log.d("App", "failed to create directory");
//                        }
//                    }
                    String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog";
                    File tempRoot = new File(tempPath);
                    fileNotWrite(tempRoot);
                    File[] tempFilesAndFolders = tempRoot.listFiles();
                    workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog/CL_" + currentDateandTime + "_" + (tempFilesAndFolders.length - 1) + ".pdf", options);

                    String path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog";
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

                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                Cursor curCSV = db.rawQuery("SELECT * FROM PrintLogUserdetails", null);
                if (curCSV != null && curCSV.getCount() > 0) {
                    deleteAllLogs();
                } else {
                    Toast.makeText(requireContext(), "Database is empty, please insert values", Toast.LENGTH_SHORT).show();
                }
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
                    switchBtnClick.setChecked(false);
                    switchHold.setChecked(false);
                    deviceRef.child("Data").child("AUTOLOG").setValue(2);
                    timer_cloud_layout.setVisibility(View.VISIBLE);

                    if (Constants.timeInSec == 0) {

                    }
                    else {
                        float f = (float)Constants.timeInSec/60000;
                        enterTime.setText(""+(f));
                        if (handler != null)
                            handler.removeCallbacks(runnable);
                        handler();
                    }

                } else {
                    isTimer = false;
                    Constants.timeInSec = 0;
                    isAlertShow = true;
                    if (handler != null)
                        handler.removeCallbacks(runnable);
                    timer_cloud_layout.setVisibility(View.GONE);

                }
            }
        });

        saveTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!enterTime.getText().toString().isEmpty()) {
                    double d = Double.parseDouble(enterTime.getText().toString()) * 60000;
                    Double db = new Double(d);
                    Constants.timeInSec = db.intValue();
                    deviceRef.child("Data").child("LOG_INTERVAL").setValue(Constants.timeInSec);
                    handler();
                }
            }
        });

        switchBtnClick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (switchBtnClick.isChecked()) {
                    deviceRef.child("Data").child("AUTOLOG").setValue(3);

                    if (switchInterval.isChecked()) {
                        isTimer = false;
                        handler.removeCallbacks(runnable);
                        switchInterval.setChecked(false);
                    }
                    switchHold.setChecked(false);
                }
            }
        });
        if (deviceRef.child("Data").child("LOG_INTERVAL") != null) {
            deviceRef.child("Data").child("LOG_INTERVAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Constants.timeInSec = snapshot.getValue(Integer.class);
                    if (snapshot.getValue(Integer.class) != null) {
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
                    deviceRef.child("Data").child("HOLD").setValue(0);
                    deviceRef.child("Data").child("AUTOLOG").setValue(1);
                    if (switchInterval.isChecked()) {
                        isTimer = false;
                        handler.removeCallbacks(runnable);
                        switchInterval.setChecked(false);
                    }

                    switchBtnClick.setChecked(false);
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

            }
        });
    }

    private void saveDetails() {
        compound_name = compound_name_txt.getText().toString();
        if (!compound_name.isEmpty()) {
            deviceRef.child("Data").child("COMPOUND_NAME").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    snapshot.getRef().setValue(compound_name);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });
        }

        //saving batch number
        batchnum = batch_number.getText().toString();
        if (!batchnum.isEmpty()) {
            deviceRef.child("Data").child("BATCH_NUMBER").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    snapshot.getRef().setValue(batchnum);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });
        }

        arnum = ar_number.getText().toString();
        if (!arnum.isEmpty()) {
            deviceRef.child("Data").child("AR_NUMBER").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
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
        deviceRef.child("Data").child("AUTOLOG").setValue(0);
        super.onDestroy();
    }

    @Override
    public void onPause() {
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

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Log.d("Timer", "doInBackground: in handler");
                takeLog();
                handler();
            }
        };
        handler.postDelayed(runnable, Constants.timeInSec);
        Log.d("Timer", "doInBackground: out handler");


    }


    void takeLog() {

        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        fetch_logs();

        if (ph == null || temp == null || mv == null) {
            Toast.makeText(getContext(), "Fetching Data", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("TakeLog", "takeLog: " + date + " " + time + " " + ph + " " + temp + " " + batchnum + " " + arnum + " " + compound_name + " " + PhActivity.DEVICE_ID);
            databaseHelper.print_insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
            databaseHelper.insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name, PhActivity.DEVICE_ID);
            databaseHelper.insert_action_data(date + " " + time, "Log pressed : " + Source.logUserName, ph, temp, mv, compound_name, PhActivity.DEVICE_ID);
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

    public void exportSensorCsv() {
        //We use the Download directory for saving our .csv file.
        File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file;
        PrintWriter printWriter = null;

        try {
            reportDate = "Date: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            reportTime = "Time: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            file = new File(exportDir, "CurrentData.csv");
            file.createNewFile();
            printWriter = new PrintWriter(new FileWriter(file), true);

            SharedPreferences shp = requireContext().getSharedPreferences("Extras", MODE_PRIVATE);
            offset = "Offset: " + shp.getString("offset", "");
            battery = "Battery: " + shp.getString("battery", "");
            slope = "Slope: " + shp.getString("slope", "");
            temp = "Temperature: " + shp.getString("temp", "");

            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            Cursor calibCSV = db.rawQuery("SELECT * FROM CalibData", null);
            Cursor curCSV = db.rawQuery("SELECT * FROM PrintLogUserdetails", null);
            printWriter.println("Company: " + companyName);
            printWriter.println("Username: " + Source.logUserName);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);

//            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
//            printWriter.println(reportDate + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(reportDate);
            printWriter.println(reportTime);
//            printWriter.println(reportTime + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(offset + "," + battery);
            printWriter.println(temp);
            printWriter.println(slope);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("Calibration Table");
            printWriter.println("_____pH___,___mV____,__DATE____TIME__");
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);


            while (calibCSV.moveToNext()) {

                String ph = calibCSV.getString(calibCSV.getColumnIndex("PH"));
                String mv = calibCSV.getString(calibCSV.getColumnIndex("MV"));
                String date = calibCSV.getString(calibCSV.getColumnIndex("DT"));


                String record1 = ph + "," + mv + "," + date;

                printWriter.println(record1);
            }
            calibCSV.close();
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("Log Table" + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
//            printWriter.println("Date,Time,pH,Temp,Batch No,AR No,Compound");
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);

            int i = 0;
            while (curCSV.moveToNext()) {
                if (i == 0) {
//                    printWriter.println("Date,Time,pH,Temp,Batch No,AR No,Compound");
                    String record = "__Date____" + "," + "_____Time__" + "," + "_______________pH______" + "," + "___Temp" + "," + "_Batch No" + "," + "__AR No_" + "," + "Compound";
                    printWriter.println(record);
                    i++;
                }
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
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("Operator Sign");
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + "," + nullEntry + "," + "Supervisor Sign");
            curCSV.close();
            db.close();

            LoadOptions loadOptions = new LoadOptions(FileFormatType.CSV);

            String inputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog/";
            Workbook workbook = new Workbook(inputFile + "CurrentData.csv", loadOptions);
            Worksheet worksheet = workbook.getWorksheets().get(0);
            worksheet.getCells().setColumnWidth(0, 12.5);
            worksheet.getCells().setColumnWidth(1, 10.5);
            worksheet.getCells().setColumnWidth(2, 18.5);

            Range rng = worksheet.getCells().createRange("B2:D7");
            Style st = worksheet.getWorkbook().createStyle();
            st.setVerticalAlignment(TextAlignmentType.LEFT);
            st.setHorizontalAlignment(TextAlignmentType.LEFT);

            StyleFlag flag = new StyleFlag();

            flag.setAlignments(true);

            rng.applyStyle(st, flag);

            workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/Currentlog/CurrentData.xlsx", SaveFormat.XLSX);

        } catch (Exception e) {
            Log.d("csvexception", String.valueOf(e));
        }
    }

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

    public int columns() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.databaseHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

//    public void showChart() {
//        int countColumns = columns();
//
//        ArrayList<Entry> yValues = new ArrayList<>();
//        for (int i = 0; i < countColumns; i++) {
//            yValues.add(new Entry(Float.parseFloat(String.valueOf(i)), Float.parseFloat(ph)));
//            LineDataSet set = new LineDataSet(yValues, "pH");
//            set.setFillAlpha(110);
//            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
//            dataSets.add(set);
//            LineData data = new LineData(dataSets);
//            lineChart.setData(data);
//            lineChart.setPinchZoom(true);
//            lineChart.setTouchEnabled(true);
//        }
//
//        lineChart.getDescription().setText("Tap on graph to Plot!");
//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setAxisMinimum(0);
//        xAxis.setAxisMaximum(250);
//        xAxis.setLabelCount(3);
//        xAxis.setValueFormatter(new MyXAxisValueFormatter());
//
//        LineDataSet set = new LineDataSet(yValues, "pH");
//        set.setFillAlpha(110);
//        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
//        dataSets.add(set);
//
//        lineChart.setPinchZoom(true);
//        lineChart.setTouchEnabled(true);
//    }

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
                String tempe = temp1 < -127.0 ? "N/A" : String.format(Locale.UK, "%.2f", temp1);

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
}
