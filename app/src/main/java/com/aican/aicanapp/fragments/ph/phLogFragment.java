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
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class phLogFragment extends Fragment {

    String ph, temp, time, compound_name, ph_fetched, m_fetched, currentTime_fetched,compound_name_fetched;
    LineChart lineChart;
    int pageHeight = 900;
    int pagewidth = 1280;
    private static final int PERMISSION_REQUEST_CODE = 200;
    DatabaseReference deviceRef;
    ArrayList<phData> phDataModelList = new ArrayList<>();
    LogAdapter adapter;
    String[] lines;
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
        //GraphView graphView = view.findViewById(R.id.graph);
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

//        setupGraph();

        if (checkPermission()) {
            Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }

        DialogMain dialogMain = new DialogMain();
        dialogMain.setCancelable(false);
        dialogMain.show(getActivity().getSupportFragmentManager(), "example dialog");

       /* exportBtn.setOnClickListener(v -> {
            if(!Source.userRole.equals("Operator")){
                FileInputStream fis = null;
                try {
                    fis = getActivity().openFileInput(FILE_NAME);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String text;

                    clearBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            list.clear();
                        }
                    });

                    setupGraph();

                    exportBtn.setOnClickListener(new View.OnClickListener() {
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
                    Intent i = new Intent(getContext(), Export.class);
                    startActivity(i);

                    //                generatePDF();
                } else {
                    Toast.makeText(getContext(), "Access Not Granted", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(getContext(), "Access Not Granted", Toast.LENGTH_SHORT).show();
            }
        });

        */

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
                Intent i = new Intent(getContext(), Export.class);
                startActivity(i);
            }
            //generatePDF();
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

        Handler handlerr = new Handler();
        handlerr.postDelayed(new Runnable() {
            @Override
            public void run() {
                showChart();
            }
        }, 5000);
    }

    /**
     * Passing on the data to LogAdapter
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
        Log.d("ryu", String.valueOf(countColumns));


        ArrayList<Entry> yValues = new ArrayList<>();
        for(int i =0; i<countColumns; i++){
            yValues.add(new Entry(Float.parseFloat(String.valueOf(i)), Float.parseFloat(ph)));
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
        Log.d("debz", ph);

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

    /**
     * To generate PDFs
     */
    private void generatePDF() {
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

    private void setupGraph() {


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
    }
}
