package com.aican.aicanapp.fragments.ph;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.aican.aicanapp.utils.MyXAxisValueFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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
    String ph, temp, mv, time, compound_name, ph_fetched, m_fetched, currentTime_fetched, compound_name_fetched;
    String ph1, mv1, ph2, mv2, ph3, mv3, ph4, mv4, ph5, mv5, dt1, dt2, dt3, dt4, dt5;
    LineChart lineChart;
    String mode;
    private static final int PERMISSION_REQUEST_CODE = 200;
    DatabaseReference deviceRef;
    ArrayList<phData> phDataModelList = new ArrayList<>();
    LogAdapter adapter;
    String offset, battery, slope, temperature, roleExport, nullEntry;
    DatabaseHelper databaseHelper;
    Button logBtn, exportBtn, printBtn;
    ImageButton enterBtn;
    PrintLogAdapter plAdapter;
    EditText compound_name_txt;
    String TABLE_NAME = "LogUserdetails";

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
        super.onStop();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        phView = view.findViewById(R.id.phView);
        tvPhCurr = view.findViewById(R.id.tvPhCurr);
        tvPhNext = view.findViewById(R.id.tvPhNext);

        lineChart = view.findViewById(R.id.graph);
        logBtn = view.findViewById(R.id.logBtn);
        exportBtn = view.findViewById(R.id.export);
        enterBtn = view.findViewById(R.id.enter_text);
        printBtn = view.findViewById(R.id.print);
        compound_name_txt = view.findViewById(R.id.compound_name);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewLog);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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

        ph1 = "1.2";
        ph2 = "4.0";
        ph3 = "7.0";
        ph4 = "9.2";
        ph5 = "12.0";

//        DialogMain dialogMain = new DialogMain();
//        dialogMain.setCancelable(false);
//        Source.userTrack = "PhLogFragment logged in by ";
//        dialogMain.show(getActivity().getSupportFragmentManager(), "example dialog");

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

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showChart();
            }
        }, 5000);

        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Source.status_export = true;
                time = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault()).format(new Date());
                databaseHelper.insert_action_data(time, "Exported by " + Source.userName, ph, temp, mv, "");

                SharedPreferences sh = getContext().getSharedPreferences("RolePref", MODE_PRIVATE);
                SharedPreferences.Editor roleE = sh.edit();
                String roleSuper = Source.userName;
                roleE.putString("roleSuper", roleSuper);
                roleE.commit();

                deleteAll();

                databaseHelper.insertCalibData(ph1, mv1, dt1);
                databaseHelper.insertCalibData(ph2, mv2, dt2);
                databaseHelper.insertCalibData(ph3, mv3, dt3);
                databaseHelper.insertCalibData(ph4, mv4, dt4);
                databaseHelper.insertCalibData(ph5, mv5, dt5);

                Intent intent = new Intent(getContext(), Export.class);
                startActivity(intent);

    //                dialogMain.show(getActivity().getSupportFragmentManager(), "example dialog");
            }
        });

        /**
         * Getting a log of pH, temp, the time and date of that respective moment, and the name of the compound
         */
        logBtn.setOnClickListener(v -> {

            time = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            fetch_logs();

            if (ph == null || temp == null || mv == null) {
                Toast.makeText(getContext(), "Fetching Data", Toast.LENGTH_SHORT).show();
            } else {
                databaseHelper.print_insert_log_data(time, ph, temp, compound_name);
                databaseHelper.insert_log_data(time, ph, temp, compound_name);
                databaseHelper.insert_action_data(time, "Log button pressed by " + Source.userName, ph, temp, mv, compound_name);
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

                plAdapter = new PrintLogAdapter(getContext().getApplicationContext(), filesAndFolders);
                csvRecyclerView.setAdapter(plAdapter);
                plAdapter.notifyDataSetChanged();
                csvRecyclerView.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));

                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                Cursor curCSV = db.rawQuery("SELECT * FROM PrintLogUserdetails", null);
                if (curCSV != null && curCSV.getCount() > 0){
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

        if (filesAndFolders == null || filesAndFolders.length == 0) {
            Toast.makeText(requireContext(), "No Files Found", Toast.LENGTH_SHORT).show();
            return;
        } else {
            for (int i = 0; i < filesAndFolders.length; i++) {
                filesAndFolders[i].getName().startsWith("CurrentData");
            }
        }

        plAdapter = new PrintLogAdapter(getContext().getApplicationContext(), filesAndFolders);
        csvRecyclerView.setAdapter(plAdapter);
        plAdapter.notifyDataSetChanged();
        csvRecyclerView.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));

        if (checkPermission()) {
            Toast.makeText(getContext().getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
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

            file = new File(exportDir, "CurrentData.csv");
            file.createNewFile();
            printWriter = new PrintWriter(new FileWriter(file), true);

            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            Cursor calibCSV = db.rawQuery("SELECT * FROM Calibdetails", null);
            Cursor curCSV = db.rawQuery("SELECT * FROM PrintLogUserdetails", null);

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
            calibCSV.close();
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

    private List<phData> getList() {
        phDataModelList.add(0, new phData(ph, temp, time, compound_name));
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

    public void showChart() {
        int countColumns = columns();

        ArrayList<Entry> yValues = new ArrayList<>();
        for (int i = 0; i < countColumns; i++) {
            yValues.add(new Entry(Float.parseFloat(String.valueOf(i)), Float.parseFloat(ph)));
            LineDataSet set = new LineDataSet(yValues, "pH");
            set.setFillAlpha(110);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set);
            LineData data = new LineData(dataSets);
            lineChart.setData(data);
            lineChart.setPinchZoom(true);
            lineChart.setTouchEnabled(true);
        }

        lineChart.getDescription().setText("Tap on graph to Plot!");
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(250);
        xAxis.setLabelCount(3);
        xAxis.setValueFormatter(new MyXAxisValueFormatter());

        LineDataSet set = new LineDataSet(yValues, "pH");
        set.setFillAlpha(110);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        lineChart.setPinchZoom(true);
        lineChart.setTouchEnabled(true);
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
                Float temp = snapshot.getValue(Float.class);
                phLogFragment.this.temp = String.format(Locale.UK, "%.2f", temp);
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
            time = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault()).format(new Date());
            currentTime_fetched = res.getString(0);
            ph_fetched = res.getString(1);
            m_fetched = res.getString(2);
            compound_name_fetched = res.getString(3);
            if (time.equals(currentTime_fetched)) {
                phDataModelList.add(0, new phData(ph_fetched, m_fetched, currentTime_fetched, compound_name_fetched));
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
}
