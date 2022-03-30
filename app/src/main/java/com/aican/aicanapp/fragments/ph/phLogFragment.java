package com.aican.aicanapp.fragments.ph;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Activity;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
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
import android.widget.Toast;

import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.DialogMain;
import com.aican.aicanapp.R;

import com.aican.aicanapp.adapters.LogAdapter;
import com.aican.aicanapp.dataClasses.phData;

import com.aican.aicanapp.specificactivities.Export;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.utils.MyXAxisValueFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class phLogFragment extends Fragment {

    String ph, temp, time, compound_name, ph_fetched, m_fetched, currentTime_fetched, compound_name_fetched;
    LineChart lineChart;
    private static final int PERMISSION_REQUEST_CODE = 200;
    DatabaseReference deviceRef;
    ArrayList<phData> phDataModelList = new ArrayList<>();
    LogAdapter adapter;
    DatabaseHelper databaseHelper;
    Button logBtn, exportBtn, clearBtn;
    ImageButton enterBtn;
    EditText compound_name_txt;
    String TABLE_NAME = "LogUserdetails";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ph_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lineChart = view.findViewById(R.id.graph);
        logBtn = view.findViewById(R.id.logBtn);
        exportBtn = view.findViewById(R.id.export);
        clearBtn = view.findViewById(R.id.clear);
        enterBtn = view.findViewById(R.id.enter_text);
        compound_name_txt = view.findViewById(R.id.compound_name);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        databaseHelper = new DatabaseHelper(getContext());
        adapter = new LogAdapter(getContext(), getSQLList());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        fetch_logs();

        if (checkPermission()) {
            Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }

        DialogMain dialogMain = new DialogMain();
        dialogMain.setCancelable(false);
        dialogMain.show(getActivity().getSupportFragmentManager(), "example dialog");

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

        //showChart();
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
                Intent i = new Intent(getContext(), Export.class);
                startActivity(i);

            }
        });


        /**
         * Getting a log of pH, temp, the time and date of that respective moment, and the name of the compound
         */

        logBtn.setOnClickListener(v -> {

            time = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault()).format(new Date());
            fetch_logs();

            if (ph == null || temp == null) {
                Toast.makeText(getContext(), "Fetching Data", Toast.LENGTH_SHORT).show();
            } else {
                databaseHelper.insert_log_data(time, ph, temp, compound_name);
            }
            adapter = new LogAdapter(getContext(), getList());
            recyclerView.setAdapter(adapter);
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

    /*    Handler handlerr = new Handler();
        handlerr.postDelayed(new Runnable() {
            @Override
            public void run() {
                showChart();
            }
        }, 5000);

     */
    }

    /**
     * Passing on the data to LogAdapter
     *
     * @return
     */
    private List<phData> getList() {
        phDataModelList.add(new phData(ph, temp, time, compound_name));
        return phDataModelList;
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
        //ArrayList<Entry> xValues = new ArrayList<>();
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

    private void fetch_logs() {
        deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float p = snapshot.getValue(Float.class);
                ph = String.format(Locale.UK, "%.2f", p);
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
            currentTime_fetched = res.getString(0);
            ph_fetched = res.getString(1);
            m_fetched = res.getString(2);
            compound_name_fetched = res.getString(3);
            phDataModelList.add(new phData(ph_fetched, m_fetched, currentTime_fetched, compound_name_fetched));
        }
        return phDataModelList;
    }

    /*
     * Convert sqlite database to csv file
     *
     */
//
//    public void exportDatabaseCsv() {
//        String state = Environment.getExternalStorageState();
//
//        //We use the Download directory for saving our .csv file.
//        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        if (!exportDir.exists()) {
//            exportDir.mkdirs();
//        }
//
//        File file;
//        PrintWriter printWriter = null;
//
//        try{
//
//            file = new File(exportDir, "LogCSV.csv");
//            file.createNewFile();
//            printWriter = new PrintWriter(new FileWriter(file), true);
//
//            SQLiteDatabase db = databaseHelper.getWritableDatabase();
//
//            Cursor curCSV = db.rawQuery("SELECT * FROM LogUserdetails", null);
//            printWriter.println("TIME,pH,TEMP,NAME");
//
//            while(curCSV.moveToNext()){
//                String time = curCSV.getString(curCSV.getColumnIndex("time"));
//                String pH = curCSV.getString(curCSV.getColumnIndex("ph"));
//                String temp = curCSV.getString(curCSV.getColumnIndex("temperature"));
//                String comp = curCSV.getString(curCSV.getColumnIndex("compound"));
//
//                String record = time + "," + pH + "," + temp + "," + comp;
//                printWriter.println(record);
//            }
//            curCSV.close();
//            db.close();
//
//        } catch(Exception e) {
//            Log.d("csvexception", String.valueOf(e));
//        }
//    }


    /**
     * To generate PDFs
     */
//    private void generatePDF() {
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
//            Toast.makeText(requireContext(), "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        pdfDocument.close();
//    }

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
