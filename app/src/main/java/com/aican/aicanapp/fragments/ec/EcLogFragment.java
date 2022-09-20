package com.aican.aicanapp.fragments.ec;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.adapters.ECLogAdapter;
import com.aican.aicanapp.adapters.LogAdapter;
import com.aican.aicanapp.adapters.PrintLogAdapter;
import com.aican.aicanapp.adapters.printECLogAdapter;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.dataClasses.ecLogModel;
import com.aican.aicanapp.dataClasses.phData;
import com.aican.aicanapp.fragments.ph.phLogFragment;
import com.aican.aicanapp.specificactivities.EcActivity;
import com.aican.aicanapp.specificactivities.EcExport;
import com.aican.aicanapp.specificactivities.Export;
import com.aican.aicanapp.specificactivities.PhActivity;
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

public class EcLogFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int PERMISSION_REQUEST_CODE = 200;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    DatabaseReference deviceRef;

    Button logBtn, print, export, clear;
    String conductivity, TDS, temp, productName, batchNo, date, time;

    DatabaseHelper databaseHelper;
    RecyclerView recyclerViewLog, recyclerViewCSVLog;
    ArrayList<ecLogModel> ecLogList;
    ECLogAdapter logAdapter;

    String offset, battery, slope, nullEntry, temp2;
    String reportDate, reportTime;
    printECLogAdapter plAdapter;

    public EcLogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EcLogFragment.
     */
//     TODO: Rename and change types and number of parameters
    public static EcLogFragment newInstance(String param1, String param2) {
        EcLogFragment fragment = new EcLogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ec_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //initialise variables here
        logBtn = view.findViewById(R.id.logBtn);
        export = view.findViewById(R.id.export);
        clear = view.findViewById(R.id.clear);
        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(EcActivity.DEVICE_ID)).getReference().child("ECMETER").child(EcActivity.DEVICE_ID);
        databaseHelper = new DatabaseHelper(getContext());
        recyclerViewLog = view.findViewById(R.id.recyclerViewLog);
        ecLogList = new ArrayList<>();
        nullEntry = " ";
        recyclerViewCSVLog = view.findViewById(R.id.recyclerViewCSVLog);
        print = view.findViewById(R.id.print);
        if (checkPermission()) {
            Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }
        fetch_logs();

        logBtn.setOnClickListener(view1 -> {
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            fetch_logs();

            if (conductivity == null || temp == null || batchNo == null) {
                Toast.makeText(getContext(), "Fetching Data", Toast.LENGTH_SHORT).show();
            } else {
                databaseHelper.insertLogECDetails(date, time, conductivity, TDS, temp, productName, batchNo, EcActivity.DEVICE_ID);
                databaseHelper.printLogECDetails(date, time, conductivity, TDS, temp, productName, batchNo);
            }
            logAdapter = new ECLogAdapter(getContext(), getList());
//            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
//            recyclerViewLog.setLayoutManager(linearLayoutManager);
            recyclerViewLog.setAdapter(logAdapter);

        });



//        File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECLogDetails");
//        if (!exportDir.exists()) {
//            exportDir.mkdirs();
//        }

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportSensorCsv();


                String startsWith = "CurrentData";
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECLogDetails";
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
                    Workbook workbook = new Workbook(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECLogDetails/CurrentData.xlsx");
                    PdfSaveOptions options = new PdfSaveOptions();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());
                    options.setCompliance(PdfCompliance.PDF_A_1_B);

                    String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECLogDetails";
                    File tempRoot = new File(tempPath);
                    fileNotWrite(tempRoot);
                    File[] tempFilesAndFolders = tempRoot.listFiles();
                    workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECLogDetails/ECLogDetails" + currentDateandTime + "_" + (tempFilesAndFolders.length - 1) + ".pdf", options);

                    String path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECLogDetails";
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


                String pathPDF = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECLogDetails/";
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

                plAdapter = new printECLogAdapter(getContext().getApplicationContext(), filesAndFoldersPDF);
                recyclerViewCSVLog.setAdapter(plAdapter);
                plAdapter.notifyDataSetChanged();
                recyclerViewCSVLog.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));

                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                Cursor curCSV = db.rawQuery("SELECT * FROM PrintLogECdetails", null);
                if (curCSV != null && curCSV.getCount() > 0) {
                    deleteAllLogs();
                } else {
                    Toast.makeText(requireContext(), "Database is empty, please insert values", Toast.LENGTH_SHORT).show();
                }
//                XLSXtoPDF();
            }
        });

        recyclerViewLog.setHasFixedSize(true);
        recyclerViewLog.setLayoutManager(new LinearLayoutManager(requireContext()));

//        String startsWith = "CurrentData";
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECLogDetails";
        File root = new File(path);
        File[] filesAndFolders = root.listFiles();
        File[] filesAndFoldersPDF = new File[1];

        if (filesAndFolders == null || filesAndFolders.length == 0) {
            Toast.makeText(requireContext(), "No Files Found", Toast.LENGTH_SHORT).show();
            return;
        } else {
            for (int i = 0; i < filesAndFolders.length; i++) {
                if (filesAndFolders[i].getName().endsWith(".pdf")) {
                    filesAndFoldersPDF[0] = filesAndFolders[i];
                }
            }
        }


        if (checkPermission()) {
            Toast.makeText(getContext().getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }

        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Source.status_export = true;
                time = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault()).format(new Date());
//                databaseHelper.insert_action_data(time, "Exported by " + Source.userName, ph, temp, mv, "");

                SharedPreferences sh = getContext().getSharedPreferences("RolePref", MODE_PRIVATE);
                SharedPreferences.Editor roleE = sh.edit();
                String roleSuper = Source.userName;
                roleE.putString("roleSuper", roleSuper);
                roleE.commit();

//                deleteAll();
//
//                databaseHelper.insertCalibData(ph1, mv1, dt1);
//                databaseHelper.insertCalibData(ph2, mv2, dt2);
//                databaseHelper.insertCalibData(ph3, mv3, dt3);
//                databaseHelper.insertCalibData(ph4, mv4, dt4);
//                databaseHelper.insertCalibData(ph5, mv5, dt5);

                if (Source.subscription.equals("cfr")) {
//                    DialogMain dialogMain = new DialogMain();
//                    dialogMain.setCancelable(false);
//                    Source.userTrack = "PhLogFragment logged in by ";
//                    dialogMain.show(getActivity().getSupportFragmentManager(), "example dialog");
                } else {
                    Intent intent = new Intent(getContext(), EcExport.class);
                    startActivity(intent);
                }
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ecLogList.clear();
                ArrayList<ecLogModel> ar = new ArrayList<>();
                logAdapter = new ECLogAdapter(getContext(), ar);
                recyclerViewLog.setAdapter(logAdapter);

            }
        });
    }

    private void fetch_logs() {
        deviceRef.child("Data").child("RESISTIVITY_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float p = snapshot.getValue(Float.class);
                conductivity = String.format(Locale.UK, "%.2f", p);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("Data").child("TEMP_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float temperature = snapshot.getValue(Float.class);
                temp = String.format(Locale.UK, "%.2f", temperature);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("Data").child("COMPOUND_NAME").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                productName = (String) snapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("Data").child("BATCH_NUMBER").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                batchNo = (String) snapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceRef.child("Data").child("TDS_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float tds = snapshot.getValue(Float.class);
                TDS = String.format(Locale.UK, "%.2f", tds);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }

    private List<ecLogModel> getList() {
        ecLogList.add(0, new ecLogModel(date, time, conductivity, TDS, temp, batchNo, productName));
        return ecLogList;
    }

    public void exportSensorCsv() {
        //We use the Download directory for saving our .csv file.
        File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECLogDetails");
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
            temp2 = "Temperature: " + shp.getString("temp", "");
            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            Cursor curCSV = db.rawQuery("SELECT * FROM PrintLogECdetails", null);


            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(reportDate);
//            printWriter.println(reportDate + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
//            printWriter.println(reportDate + "," + nullEntry + "," + nullEntry);
            printWriter.println(reportTime);
//            printWriter.println(reportTime + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
//            printWriter.println(offset + "," + battery + "," + temp + "," + slope + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(offset + "," + battery);
            printWriter.println(temp);
            printWriter.println(slope);
//            printWriter.println(offset + "," + battery);
//            printWriter.println(temp + "," + slope);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);


            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println("Log Table" + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry + "," + nullEntry);

            int i = 0;
            while (curCSV.moveToNext()) {
                if (i == 0) {
//                    printWriter.println("Date,Time,pH,Temp,Batch No,AR No,Compound");
                    String record = "__Date____" + "," + "_____Time__" + "," + "conductivity" + "," + "___tds" + "," + "temperature" + "," + "productName" + "," + "batch";
                    printWriter.println(record);
                    i++;
                }
                String date = curCSV.getString(curCSV.getColumnIndex("date"));
                String time = curCSV.getString(curCSV.getColumnIndex("time"));
                String conductivity = curCSV.getString(curCSV.getColumnIndex("conductivity"));
                String tds = curCSV.getString(curCSV.getColumnIndex("TDS"));
                String temperature = curCSV.getString(curCSV.getColumnIndex("temperature"));
                String productName = curCSV.getString(curCSV.getColumnIndex("productName"));
                String batch = curCSV.getString(curCSV.getColumnIndex("batch"));

                String record = date + "," + time + "," + conductivity + "," + tds + "," + temperature + "," + productName + "," + batch;

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

            String inputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECLogDetails/";
            Workbook workbook = new Workbook(inputFile + "CurrentData.csv", loadOptions);
            Worksheet worksheet = workbook.getWorksheets().get(0);
            worksheet.getCells().setColumnWidth(0, 10.0);
            worksheet.getCells().setColumnWidth(1, 10.0);

            Range rng = worksheet.getCells().createRange("B2:D7");
            Style st = worksheet.getWorkbook().createStyle();
            st.setVerticalAlignment(TextAlignmentType.LEFT);
            st.setHorizontalAlignment(TextAlignmentType.LEFT);

            StyleFlag flag = new StyleFlag();

            flag.setAlignments(true);

            rng.applyStyle(st, flag);

            workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/ECLogDetails/CurrentData.xlsx", SaveFormat.XLSX);


        } catch (Exception e) {
            Log.d("csvexception", String.valueOf(e));
        }
    }

    public void XLSXtoPDF() {

    }

    public void fileNotWrite(File file) {
        file.setWritable(false);
        if (file.canWrite()) {
            Log.d("csv", "Not Working");
        } else {
            Log.d("csvnw", "Working");
        }
    }

    public void deleteAllLogs() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.execSQL("DELETE FROM PrintLogECdetails");
        db.close();
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
}