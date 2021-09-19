package com.aican.aicanapp.specificactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.fragments.temp.TempFragment;
import com.aican.aicanapp.graph.ForegroundService;
import com.aican.aicanapp.utils.PlotGraphNotifier;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;

import com.aican.aicanapp.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.jetbrains.annotations.NotNull;


public class SetTempGraphActivity extends AppCompatActivity {
    DatabaseReference deviceRef;
    LinearLayout llStart, llStop, llClear, llExport;
    CardView cv1Min, cv5Min, cv10Min, cv15Min, cvClock;
    int skipPoints = 0;
    int skipCount = 0;
    ArrayList<Entry> entriesOriginal;
    boolean isTimeOptionsVisible = false;
    ArrayList<Entry> logs = new ArrayList<>();
    long start = 0;
    ForegroundService myService;
    PlotGraphNotifier plotGraphNotifier;
    LineChart lineChart;
    int val=0;
    private boolean isLogging = false;
    TextView tvTempCurr,tvTempChange;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_temp_graph);
        tvTempCurr=findViewById(R.id.tvTempCurr);
        tvTempChange=findViewById(R.id.tvTempChange);

        lineChart = findViewById(R.id.line_chart);
        llStart = findViewById(R.id.llStart);
        llStop = findViewById(R.id.llStop);
        llClear = findViewById(R.id.llClear);
        llExport = findViewById(R.id.llExport);
        cv5Min = findViewById(R.id.cv5min);
        cv1Min = findViewById(R.id.cv1min);
        cv10Min = findViewById(R.id.cv10min);
        cv15Min = findViewById(R.id.cv15min);
        cvClock = findViewById(R.id.cvClock);

        entriesOriginal = new ArrayList<>();

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(TemperatureActivity.DEVICE_ID)).getReference()
                .child(TemperatureActivity.deviceType).child(TemperatureActivity.DEVICE_ID);

        cvClock.setOnClickListener(v -> {
            isTimeOptionsVisible = !isTimeOptionsVisible;
            if (isTimeOptionsVisible) {
                showTimeOptions();
            } else {
                hideTimeOptions();
            }
        });

        setupListeners();
        setupGraph();
    }

    private void setupListeners() {

        deviceRef.child("Data").child("TEMP1_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Integer temp = snapshot.getValue(Integer.class);
                if (temp == null) return;

                val = temp;
                tvTempCurr.setText(" "+temp+"°C ");
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        deviceRef.child("UI").child("TEMP").child("SET_TEMP").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer temp = snapshot.getValue(Integer.class);
                if (temp == null) return;
                tvTempChange.setText(" "+temp+"°C ");
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

        private void setupGraph() {
        LineDataSet lineDataSet = new LineDataSet(new ArrayList<>(), "Temp");

        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleRadius(4);
        lineDataSet.setValueTextSize(10);


        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);

        LineData data = new LineData(dataSets);
        lineChart.setData(data);
        lineChart.invalidate();
        lineChart.setDrawGridBackground(true);
        lineChart.setDrawBorders(true);

        Description d = new Description();
        d.setText("Temp Graph");
        lineChart.setDescription(d);

        llStart.setOnClickListener(v -> {
            if (ForegroundService.isRunning()) {
                Toast.makeText(this, "Another graph is logging", Toast.LENGTH_SHORT).show();
                return;
            }
            llStart.setVisibility(View.INVISIBLE);
            llStop.setVisibility(View.VISIBLE);
            llClear.setVisibility(View.INVISIBLE);
            llExport.setVisibility(View.INVISIBLE);
            startLogging();
        });
        llStop.setOnClickListener(v -> {
            llStart.setVisibility(View.VISIBLE);
            llStop.setVisibility(View.INVISIBLE);
            llClear.setVisibility(View.VISIBLE);
            llExport.setVisibility(View.VISIBLE);
            stopLogging();
        });
        llClear.setOnClickListener(v -> {
            llClear.setVisibility(View.INVISIBLE);
            llExport.setVisibility(View.INVISIBLE);
            clearLogs();
        });
        llExport.setOnClickListener(v -> {
            exportLogs();
        });
        cv1Min.setOnClickListener(v -> {
            skipPoints = (60 * 1000) / Dashboard.GRAPH_PLOT_DELAY;
            rescaleGraph();
        });
        cv5Min.setOnClickListener(v -> {
            skipPoints = (5 * 60 * 1000) / Dashboard.GRAPH_PLOT_DELAY;
            rescaleGraph();
        });
        cv10Min.setOnClickListener(v -> {
            skipPoints = (10 * 60 * 1000) / Dashboard.GRAPH_PLOT_DELAY;
            rescaleGraph();
        });
        cv15Min.setOnClickListener(v -> {
            skipPoints = (15 * 60 * 1000) / Dashboard.GRAPH_PLOT_DELAY;
            rescaleGraph();
        });
    }

    private void exportLogs() {
        if (!checkStoragePermission()) {
            return;
        }
        String csv = (getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + "/" + System.currentTimeMillis() + ".csv");
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(csv));

            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[]{"X", "Y"});
            for (int i = 0; i < logs.size(); i++) {
                String[] s = {String.valueOf(logs.get(i).getX()), String.valueOf(logs.get(i).getY())};
                data.add(s);
            }
            writer.writeAll(data); // data is adding to csv
            Toast.makeText(this, "Exported", Toast.LENGTH_LONG).show();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
                return false;
            }
        }
        return true;
    }

    private void clearLogs() {
        logs.clear();
        if (myService != null) {
            myService.clearEntries();
        }
    }

    private void stopLogging() {
        isLogging = false;
        if (myService != null) {
            myService.stopLogging(SetTempGraphActivity.class);
        }
    }

    private void startLogging() {
        logs.clear();
        isLogging = true;

        Context context = getApplicationContext();
        Intent intent = new Intent(context, ForegroundService.class);
        DatabaseReference ref = deviceRef.child("Data").child("TEMP1_VAL");
        ForegroundService.setInitials(TemperatureActivity.DEVICE_ID, ref, SetTempGraphActivity.class, start,
                TemperatureActivity.deviceType + "set");
        context.startService(intent);
        context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (service instanceof ForegroundService.MyBinder) {
                    myService = ((ForegroundService.MyBinder) service).getService();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        plotGraphNotifier.stop();
    }

    private void rescaleGraph() {
        ArrayList<Entry> entries = new ArrayList<>();
        int count = 0;
        for (Entry entry : entriesOriginal) {
            if (count == 0) {
                entries.add(entry);
            }
            ++count;
            if (count >= skipPoints) {
                count = 0;
            }
        }

        lineChart.getLineData().clearValues();

        LineDataSet lds = new LineDataSet(entries, "Temp");

        lds.setLineWidth(2);
        lds.setCircleRadius(4);
        lds.setValueTextSize(10);


        ArrayList<ILineDataSet> ds = new ArrayList<>();
        ds.add(lds);

        LineData ld = new LineData(ds);
        lineChart.setData(ld);
        lineChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();

        start = System.currentTimeMillis();

        if (ForegroundService.isMyTypeRunning(TemperatureActivity.DEVICE_ID, SetTempGraphActivity.class,
                TemperatureActivity.deviceType + "set")) {
            llStart.setVisibility(View.INVISIBLE);
            llStop.setVisibility(View.VISIBLE);
            llClear.setVisibility(View.INVISIBLE);
            llExport.setVisibility(View.INVISIBLE);

            start = ForegroundService.start;

            Intent intent = new Intent(this, ForegroundService.class);
            getApplicationContext().bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if (service instanceof ForegroundService.MyBinder) {
                        myService = ((ForegroundService.MyBinder) service).getService();
                        ArrayList<Entry> entries = myService.getEntries();
                        logs.clear();
                        logs.addAll(entries);

                        lineChart.getLineData().clearValues();

                        LineDataSet lineDataSet = new LineDataSet(logs, "Temp");

                        lineDataSet.setLineWidth(2);
                        lineDataSet.setCircleRadius(4);
                        lineDataSet.setValueTextSize(10);


                        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                        dataSets.add(lineDataSet);

                        LineData data = new LineData(dataSets);
                        lineChart.setData(data);
                        lineChart.invalidate();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            }, 0);
        }

        plotGraphNotifier = new PlotGraphNotifier(Dashboard.GRAPH_PLOT_DELAY, () -> {
            if (skipCount < skipPoints) {
                skipCount++;
                return;
            }
            skipCount = 0;
            long seconds = (System.currentTimeMillis() - start) / 1000;
            LineData data = lineChart.getData();
            Entry entry = new Entry(seconds, val);
            entriesOriginal.add(entry);
            data.addEntry(entry, 0);
            lineChart.notifyDataSetChanged();
            data.notifyDataChanged();
            lineChart.invalidate();
            if (isLogging) {
                logs.add(entry);
            }
        });
    }

    private void showTimeOptions() {
        cv1Min.setVisibility(View.VISIBLE);
        cv5Min.setVisibility(View.VISIBLE);
        cv10Min.setVisibility(View.VISIBLE);
        cv15Min.setVisibility(View.VISIBLE);

        Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        cv1Min.startAnimation(zoomIn);
        cv5Min.startAnimation(zoomIn);
        cv10Min.startAnimation(zoomIn);
        cv15Min.startAnimation(zoomIn);
    }

    private void hideTimeOptions() {
        Animation zoomOut = AnimationUtils.loadAnimation(this, R.anim.zoom_out);
        zoomOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                cv1Min.setVisibility(View.INVISIBLE);
                cv5Min.setVisibility(View.INVISIBLE);
                cv10Min.setVisibility(View.INVISIBLE);
                cv15Min.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        cv1Min.startAnimation(zoomOut);
        cv5Min.startAnimation(zoomOut);
        cv10Min.startAnimation(zoomOut);
        cv15Min.startAnimation(zoomOut);
    }
}