package com.aican.aicanapp.fragments.pump;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.graph.ForegroundService;
import com.aican.aicanapp.pumpController.VerticalSlider;
import com.aican.aicanapp.specificactivities.PumpActivity;
import com.aican.aicanapp.specificactivities.PumpCalibrateActivity;
import com.aican.aicanapp.utils.PlotGraphNotifier;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVWriter;

import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PumpFragment extends Fragment {

    VerticalSlider speedController;
    LineChart lineChart;
    SwitchCompat switchDir;
    Button calibrateBtn;
    ShapeableImageView startBtn;
    TextView tvStart;
    boolean isStarted = false;

    DatabaseReference deviceRef = null;


    LinearLayout llStart, llStop, llClear, llExport;
    CardView cv1Min, cv5Min, cv10Min, cv15Min, cvClock;
    float speed = -1;
    int skipPoints = 0;
    int skipCount = 0;

    ArrayList<Entry> entriesOriginal;
    boolean isTimeOptionsVisible = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pump, container, false);
    }

    ArrayList<Entry> logs = new ArrayList<>();
    long start = 0;
    ForegroundService myService;
    PlotGraphNotifier plotGraphNotifier;
    private boolean isLogging = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        speedController = view.findViewById(R.id.speedController);
        lineChart = view.findViewById(R.id.line_chart);
        calibrateBtn = view.findViewById(R.id.calibrateBtn);
        startBtn = view.findViewById(R.id.ivStartBtn);
        tvStart = view.findViewById(R.id.tvStart);
        switchDir = view.findViewById(R.id.switchDir);

        llStart = view.findViewById(R.id.llStart);
        llStop = view.findViewById(R.id.llStop);
        llClear = view.findViewById(R.id.llClear);
        llExport = view.findViewById(R.id.llExport);
        cv5Min = view.findViewById(R.id.cv5min);
        cv1Min = view.findViewById(R.id.cv1min);
        cv10Min = view.findViewById(R.id.cv10min);
        cv15Min = view.findViewById(R.id.cv15min);
        cvClock = view.findViewById(R.id.cvClock);

        speedController.setProgress(0);

        entriesOriginal = new ArrayList<>();

        calibrateBtn.setOnClickListener(v -> {
            startActivity(
                    new Intent(requireContext(), PumpCalibrateActivity.class)
            );
        });
        cvClock.setOnClickListener(v -> {
            isTimeOptionsVisible = !isTimeOptionsVisible;
            if (isTimeOptionsVisible) {
                showTimeOptions();
            } else {
                hideTimeOptions();
            }
        });

        startBtn.setOnClickListener(v -> {
            isStarted = !isStarted;
            refreshStartBtnUI();
        });
        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PumpActivity.DEVICE_ID)).getReference()
                .child("P_PUMP").child(PumpActivity.DEVICE_ID);

        checkModeAndSetListeners();
        setupGraph();


    }

    private void setupGraph() {
        LineDataSet lineDataSet = new LineDataSet(new ArrayList<>(), "Speed");

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
        d.setText("Pump Graph");
        lineChart.setDescription(d);

        llStart.setOnClickListener(v -> {
            if (ForegroundService.isRunning()) {
                Toast.makeText(requireContext(), "Another graph is logging", Toast.LENGTH_SHORT).show();
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

        LineDataSet lds = new LineDataSet(entries, "Speed");

        lds.setLineWidth(2);
        lds.setCircleRadius(4);
        lds.setValueTextSize(10);


        ArrayList<ILineDataSet> ds = new ArrayList<>();
        ds.add(lds);

        LineData ld = new LineData(ds);
        lineChart.setData(ld);
        lineChart.invalidate();
    }

    private void exportLogs() {
        if (!checkStoragePermission()) {
            return;
        }
        String csv = (requireContext().getExternalFilesDir(null).getAbsolutePath() + "/" + System.currentTimeMillis() + ".csv");
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(csv));

            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[]{"X", "Y"});
            for (int i = 0; i < logs.size(); i++) {
                String[] s = {String.valueOf(logs.get(i).getX()), String.valueOf(logs.get(i).getY())};
                data.add(s);
            }
            writer.writeAll(data); // data is adding to csv
            Toast.makeText(requireContext(), "Exported", Toast.LENGTH_LONG).show();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requireActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
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
            myService.stopLogging(PumpFragment.class);
        }
    }

    private void startLogging() {
        logs.clear();
        isLogging = true;

        Context context = requireContext();
        Intent intent = new Intent(context, ForegroundService.class);
        DatabaseReference ref = deviceRef.child("UI").child("MODE").child("PUMP").child("SPEED");
        ForegroundService.setInitials(PumpActivity.DEVICE_ID, ref, PumpFragment.class, start, "pump");
        requireActivity().startService(intent);
        requireActivity().bindService(intent, new ServiceConnection() {
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
    public void onResume() {
        super.onResume();
        start = System.currentTimeMillis();
        if (ForegroundService.isMyTypeRunning(PumpActivity.DEVICE_ID, PumpFragment.class, "pump")) {
            llStart.setVisibility(View.INVISIBLE);
            llStop.setVisibility(View.VISIBLE);
            llClear.setVisibility(View.INVISIBLE);
            llExport.setVisibility(View.INVISIBLE);

            start = ForegroundService.start;
            Intent intent = new Intent(requireContext(), ForegroundService.class);
            requireActivity().bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if (service instanceof ForegroundService.MyBinder) {
                        myService = ((ForegroundService.MyBinder) service).getService();
                        ArrayList<Entry> entries = myService.getEntries();
                        logs.clear();
                        logs.addAll(entries);
                        entriesOriginal.clear();
                        entriesOriginal.addAll(entries);

                        lineChart.getLineData().clearValues();

                        LineDataSet lineDataSet = new LineDataSet(logs, "Speed");

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
            if (speed == -1) return;
            if (skipCount < skipPoints) {
                skipCount++;
                return;
            }
            skipCount = 0;
            long seconds = (System.currentTimeMillis() - start) / 1000;
            LineData data = lineChart.getData();
            Entry entry = new Entry(seconds, speed);
            data.addEntry(entry, 0);
            entriesOriginal.add(entry);
            lineChart.notifyDataSetChanged();
            data.notifyDataChanged();
            lineChart.invalidate();
            if (isLogging) {
                logs.add(entry);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        plotGraphNotifier.stop();
    }


    private void checkModeAndSetListeners() {
        deviceRef.child("UI").child("MODE").child("MODE_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Integer mode = snapshot.getValue(Integer.class);
                if (mode == null) {
                    return;
                }
                if (mode == 1) {
                    setupListeners();
                } else {
                    speed = -1;
                    isStarted = false;
                    refreshStartBtnUI();
                    speedController.setProgress(0);
                    switchDir.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void setupListeners() {

        deviceRef.child("UI").child("MODE").child("PUMP").child("DIR").get().addOnSuccessListener(snapshot -> {
            Integer dir = snapshot.getValue(Integer.class);
            if (dir == null) return;

            switchDir.setChecked(dir == 0);
        });

        deviceRef.child("UI").child("MODE").child("PUMP").child("SPEED").get().addOnSuccessListener(snapshot -> {
            Integer speed = snapshot.getValue(Integer.class);
            if (speed == null) return;

            speedController.setProgress(speed);
        });

        deviceRef.child("UI").child("MODE").child("PUMP").child("SPEED").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Integer speed = snapshot.getValue(Integer.class);
                if (speed == null) return;
                PumpFragment.this.speed = speed;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        speedController.setOnProgressChangeListener(progress -> {
            deviceRef.child("UI").child("MODE").child("PUMP").child("SPEED").setValue(progress);
        });

        switchDir.setOnCheckedChangeListener((v, isChecked) -> {
            deviceRef.child("UI").child("MODE").child("PUMP").child("DIR").setValue(isChecked ? 0 : 1);
        });
    }

    private void refreshStartBtnUI() {
        if (isStarted) {
            tvStart.setText("STOP");
            startBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red));
        } else {
            tvStart.setText("START");
            startBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        }
    }

    private void showTimeOptions() {
        cv1Min.setVisibility(View.VISIBLE);
        cv5Min.setVisibility(View.VISIBLE);
        cv10Min.setVisibility(View.VISIBLE);
        cv15Min.setVisibility(View.VISIBLE);

        Animation zoomIn = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_in);
        cv1Min.startAnimation(zoomIn);
        cv5Min.startAnimation(zoomIn);
        cv10Min.startAnimation(zoomIn);
        cv15Min.startAnimation(zoomIn);
    }

    private void hideTimeOptions() {
        Animation zoomOut = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_out);
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
