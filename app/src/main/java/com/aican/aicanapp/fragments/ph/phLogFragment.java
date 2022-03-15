package com.aican.aicanapp.fragments.ph;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aican.aicanapp.DialogMain;
import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.adapters.LogAdapter;
import com.aican.aicanapp.dataClasses.phData;
import com.aican.aicanapp.graph.ForegroundService;
import com.aican.aicanapp.specificactivities.Export;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.utils.DecimalValueFormatter;
import com.aican.aicanapp.utils.MyXAxisValueFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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
import com.itextpdf.text.pdf.parser.Line;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class phLogFragment extends Fragment {

    private static final String FILE_NAME = "user_info.txt";

    LineChart lineChart;
    int pageHeight = 900;
    int pagewidth = 1280;
    String[] lines;
    private static final int PERMISSION_REQUEST_CODE = 200;
    DatabaseReference deviceRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ph_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lineChart = view.findViewById(R.id.graph);
        //GraphView graphView = view.findViewById(R.id.graph);
        Button export = view.findViewById(R.id.export);
        Button logBtn = view.findViewById(R.id.logBtn);
        Button clear = view.findViewById(R.id.clear);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        phData phData = new phData();
        ArrayList<phData> list = new ArrayList<>();
        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);

        setupGraph();

        if (checkPermission()) {
            Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }

        DialogMain dialogMain = new DialogMain();
        dialogMain.show(getActivity().getSupportFragmentManager(), "example dialog");

        export.setOnClickListener(v -> {
            FileInputStream fis = null;
            try {
                fis = getActivity().openFileInput(FILE_NAME);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String text;

                clear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        list.clear();
                    }
                });

                setupGraph();

                export.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent i = new Intent(getContext(), Export.class);
                        startActivity(i);

                    }
                    //generatePDF();
                });

                while ((text = br.readLine()) != null) {
                    sb.append(text).append("\n");
                }
                lines = sb.toString().split("\\n");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (Source.status) {
                generatePDF();
            } else {
                Toast.makeText(getContext(), "Access Not Granted", Toast.LENGTH_SHORT).show();
            }
        });

        logBtn.setOnClickListener(v -> {
            String currentTime = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault()).format(new Date());
            phData.setDate(currentTime);

            deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Float p = snapshot.getValue(Float.class);
                    String ph = String.format(Locale.UK, "%.2f", p);
                    phData.setpH(ph);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

            deviceRef.child("Data").child("EC_VAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Float mv = snapshot.getValue(Float.class);
                    String m = String.format(Locale.UK, "%.2f", mv);
                    phData.setmV(m);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
            LogAdapter adapter = new LogAdapter(list);
            list.add(phData);
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void generatePDF() {
        Source.status = false;
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();

        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pagewidth, pageHeight, 1).create();
        PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);
        Canvas canvas = myPage.getCanvas();

        paint.setTextSize(60);
        canvas.drawText("AICAN AUTOMATE", 30, 80, paint);

        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("12/02/2022 6:30", canvas.getWidth() - 40, 80, paint);

        paint.setColor(Color.rgb(150, 150, 150));
        canvas.drawRect(30, 150, canvas.getWidth() - 30, 160, paint);

        paint.setTextSize(20);
        canvas.drawText("Device Id: EPT2001", 200, 190, paint);

        paint.setTextSize(20);
        canvas.drawText("Last Calibration Date & Time: 16/02/2022 4:45", 380, 220, paint);

        paint.setTextSize(30);
        canvas.drawText("Slope: 60%", canvas.getWidth() - 40, 190, paint);

        paint.setTextSize(30);
        canvas.drawText("Temperature: 30", canvas.getWidth() - 40, 230, paint);

        paint.setTextSize(30);
        canvas.drawText("Offset: 40", canvas.getWidth() - 40, 270, paint);

        paint.setColor(Color.rgb(150, 150, 150));
        canvas.drawRect(30, 180, canvas.getWidth() - 30, canvas.getHeight() - 30, paint);

        pdfDocument.finishPage(myPage);

//        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/PdfTest/";
//        File dir = new File(path);
//        if (!dir.exists())
//            dir.mkdirs();
//
//        File filePath = new File(dir, "Test.pdf");
//
//        try {
//            pdfDocument.writeTo(new FileOutputStream(filePath));
//            Toast.makeText(requireContext(), "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
//            //btn_generate.setText("Check PDF");
//            //boolean_save=true;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(requireContext(), "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
//        }
//
//        pdfDocument.close();

        String stringFilePath = Environment.getExternalStorageDirectory().getPath() + "/Download/ProgrammerWorld.pdf";
        File file = new File(stringFilePath);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
//            file = new File(getActivity().getExternalFilesDir(String.valueOf(Environment.getExternalStorageDirectory())), "gfg.pdf");
//        }
//        else
//        {
//            file = new File(Environment.getExternalStorageDirectory(), "GFG.pdf");
//        }

//        File file = new File(Environment.getExternalStorageDirectory(), "GFG.pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(requireContext(), "PDF file generated successfully.", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
        pdfDocument.close();
    }

    private boolean checkPermission() {
        // checking of permissions.
        int permission1 = ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions((Activity) requireContext(), new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {

                // after requesting permissions we are showing
                // users a toast message of permission granted.
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

    ArrayList<Entry> entriesOriginal;
    int skipPoints = 0;

    Float ph;

    private void setupListeners() {
        deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                ph = snapshot.getValue(Float.class);
                if (ph == null) return;

                //phView.moveTo(ph);
                //updatePh(ph);
                //PhFragment.this.ph = ph;
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
    }

    private void setupGraph() {

        /*
        lineChart = new LineChart(getContext());
        lineChart.setPinchZoom(true);
        lineChart.setTouchEnabled(true);

        LineData lineData = new LineData();
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAvoidFirstLastClipping(true);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMaximum(14f);
        yAxis.setDrawGridLines(true);

        YAxis yAxis1 = lineChart.getAxisRight();
        yAxis1.setEnabled(false);
         */
        /*
        deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Entry> dataVal = new ArrayList<Entry>();
                if (snapshot.hasChildren()){
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        DataPoint dataPoint = dataSnapshot.getValue(DataPoint.class);
                        dataVal.add(new Entry((float)dataPoint.getX(),(float)dataPoint.getY()));
                    }
                    showChart(dataVal);
                }else{
                    lineChart.clear();
                    lineChart.invalidate();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
         */

        ArrayList<Entry> yValues = new ArrayList<>();
        yValues.add(new Entry(0, 40f));
        yValues.add(new Entry(1, 50f));
        yValues.add(new Entry(2, 70f));
        yValues.add(new Entry(3, 80f));
        yValues.add(new Entry(4, 30f));
        yValues.add(new Entry(5, 90f));
        yValues.add(new Entry(6, 20f));
        yValues.add(new Entry(7, 100f));


        LineDataSet set = new LineDataSet(yValues, "pH");
        set.setFillAlpha(110);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        lineChart.getXAxis().setValueFormatter(new MyXAxisValueFormatter());
        LineData data = new LineData(dataSets);
        lineChart.setData(data);

        lineChart.setPinchZoom(true);
        lineChart.setTouchEnabled(true);

    }
/*
    private void  addEntry() {
        LineData data = lineChart.getData();
        if (data != null){
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);

            if (set == null){
                set = createSet();
                data.addDataSet(set);
            }

            //data.addXValue("");
            data.addEntry(new Entry(((float)Math.random() * 7) + 6f , set.getEntryCount()),0);
        }
        lineChart.notifyDataSetChanged();
        lineChart.moveViewToX(data.getXMax() - 7);
    }

    private LineDataSet createSet(){
        LineDataSet set = new LineDataSet(null, "pH");
        set.setFillAlpha(65);
        set.setValueTextSize(10);
        set.setLineWidth(2f);

        return set;
    }

    @Override
    public void onResume() {
        super.onResume();

        Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i< 100; i++){
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addEntry();
                        }
                    }, 1000);
                    try {
                        Thread.sleep(600);
                    }catch (InterruptedException e){

                    }

                }



            }
        }).start();

 */
}

//    private void rescaleGraph() {
//        ArrayList<Entry> entries = new ArrayList<>();
//        int count = 0;
//        for (Entry entry : entriesOriginal) {
//            if (count == 0) {
//                entries.add(entry);
//            }
//            ++count;
//            if (count >= skipPoints) {
//                count = 0;
//            }
//        }
//    }
//
//    lineChart.getLineData().clearValues();
//
//    LineDataSet lds = new LineDataSet(entries, "pH");
//
//        lds.setLineWidth(2);
//        lds.setCircleRadius(4);
//        lds.setValueTextSize(10);
//
//
//        ArrayList<ILineDataSet> ds = new ArrayList<>();
//      ds.add(lds);
//
//    LineData ld = new LineData(ds);
//    lineChart.setData(ld);
//    lineChart.invalidate();
