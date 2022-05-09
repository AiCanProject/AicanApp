package com.aican.aicanappnoncfr.specificactivities;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.aican.aicanappnoncfr.Dashboard.Dashboard;
import com.aican.aicanappnoncfr.R;
import com.aican.aicanappnoncfr.dialogs.ExitConfirmDialog;
import com.aican.aicanappnoncfr.fragments.ph.PhFragment;
import com.aican.aicanappnoncfr.graph.ForegroundService;
import com.aican.aicanappnoncfr.utils.PlotGraphNotifier;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.textfield.TextInputEditText;
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
import java.util.Locale;

import static android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;

public class EcTdsCalibrateActivity extends AppCompatActivity {

    TextInputEditText etBuffer, etTds;
    RelativeLayout startLayout, rlCoefficient;
    ImageView ivStartBtn;
    TextView tvStart, tvCoefficient;

    DatabaseReference deviceRef;
    String deviceId;
    LineChart lineChart;
    LinearLayout llStart, llStop, llClear, llExport;

    CardView cv1Min, cv5Min, cv10Min, cv15Min, cvClock;

    int skipPoints = 0;
    int skipCount = 0;

    ArrayList<Entry> entriesOriginal;

    float ec = 0;
    boolean isTimeOptionsVisible = false;
    ArrayList<Entry> logs = new ArrayList<>();
    long start = 0;
    ForegroundService myService;
    PlotGraphNotifier plotGraphNotifier;
    boolean isCalibrating = false;
    private boolean isLogging = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_Light);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ec_tds_calibrate);

        Window window = getWindow();
        window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.orange));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        deviceId = getIntent().getStringExtra(Dashboard.KEY_DEVICE_ID);
        if (deviceId == null) {
            throw new RuntimeException();
        }

        etBuffer = findViewById(R.id.etBuffer);
        etTds = findViewById(R.id.etTds);
        startLayout = findViewById(R.id.startLayout);
        rlCoefficient = findViewById(R.id.rlCoefficient);
        ivStartBtn = findViewById(R.id.ivStartBtn);
        tvCoefficient = findViewById(R.id.tvCoefficient);
        tvStart = findViewById(R.id.tvStart);
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

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(deviceId)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        entriesOriginal = new ArrayList<>();

        cvClock.setOnClickListener(v -> {
            isTimeOptionsVisible = !isTimeOptionsVisible;
            if (isTimeOptionsVisible) {
                showTimeOptions();
            } else {
                hideTimeOptions();
            }
        });

        ivStartBtn.setOnClickListener(v -> {
            etBuffer.clearFocus();
            ivStartBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryAlpha));
            ivStartBtn.setEnabled(false);
            tvStart.setVisibility(View.VISIBLE);
            tvStart.setText("Calibrating");

//            CountDownTimer timer = new CountDownTimer(120000, 1000) {
//                @Override
//                public void onTick(long millisUntilFinished) {
//                    millisUntilFinished /= 1000;
//                    int min = (int)millisUntilFinished/60;
//                    int sec = (int)millisUntilFinished%60;
//                    String time = String.format(Locale.UK,"%02d:%02d", min, sec);
//                    tvStart.setText(time);
//                }
//
//                @Override
//                public void onFinish() {
//                    deviceRef.child("UI").child("EC").child("EC_CAL").child("CAL").setValue(0);
//                    deviceRef.child("UI").child("EC").child("EC_CAL").child("VAL_1").get().addOnSuccessListener(snapshot -> {
//                       Float coeff = snapshot.getValue(Float.class);
//                       if(coeff == null) return;
//                       tvCoefficient.setText(String.valueOf(coeff));
//                       startLayout.setVisibility(View.INVISIBLE);
//                       rlCoefficient.setVisibility(View.VISIBLE);
//                    });
//                }
//            };
            isCalibrating = true;

            deviceRef.child("UI").child("EC").child("EC_CAL").child("B_1").setValue(Integer.valueOf(etBuffer.getText().toString())).addOnSuccessListener(t -> {
                deviceRef.child("UI").child("EC").child("EC_CAL").child("CAL").setValue(10);
            });

            deviceRef.child("UI").child("EC").child("EC_CAL").child("CAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Integer cal = snapshot.getValue(Integer.class);
                    if (cal == null) return;

                    if (cal == 11) {
                        deviceRef.child("UI").child("EC").child("EC_CAL").child("VAL_1").get().addOnSuccessListener(snapshot1 -> {
                            Float coeff = snapshot1.getValue(Float.class);
                            if (coeff == null) return;
                            tvCoefficient.setText(String.format(Locale.UK, "%.2f", coeff));
                            startLayout.setVisibility(View.INVISIBLE);
                            rlCoefficient.setVisibility(View.VISIBLE);

                            isCalibrating = false;
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        });

        setupGraph();
        setupListeners();
    }

    private void setupGraph() {
        LineDataSet lineDataSet = new LineDataSet(new ArrayList<>(), "TDS");

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
        d.setText("EC Graph");
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
        String csv = (getExternalFilesDir(null).getAbsolutePath() + "/" + System.currentTimeMillis() + ".csv");
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
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
                return false;
            }
        }
        return true;
    }

    private void stopLogging() {
        isLogging = false;
        if (myService != null) {
            myService.stopLogging(PhFragment.class);
        }
    }

    private void clearLogs() {
        logs.clear();
        if (myService != null) {
            myService.clearEntries();
        }
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

        LineDataSet lds = new LineDataSet(entries, "pH");

        lds.setLineWidth(2);
        lds.setCircleRadius(4);
        lds.setValueTextSize(10);


        ArrayList<ILineDataSet> ds = new ArrayList<>();
        ds.add(lds);

        LineData ld = new LineData(ds);
        lineChart.setData(ld);
        lineChart.invalidate();
    }


    private void setupListeners() {
        deviceRef.child("Data").child("EC_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float val = snapshot.getValue(Float.class);
                if (val == null) return;
                etTds.setText(String.valueOf(val));
                ec = val;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void startLogging() {
        logs.clear();
        isLogging = true;

        Context context = this;
        Intent intent = new Intent(context, ForegroundService.class);
        DatabaseReference ref = deviceRef.child("Data").child("EC_VAL");
        ForegroundService.setInitials(PhActivity.DEVICE_ID, ref, PhFragment.class, start, null);
        startService(intent);
        bindService(intent, new ServiceConnection() {
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

    @Override
    public void onResume() {
        super.onResume();

        start = System.currentTimeMillis();

        if (ForegroundService.isMyClassRunning(PhActivity.DEVICE_ID, PhFragment.class)) {
            llStart.setVisibility(View.INVISIBLE);
            llStop.setVisibility(View.VISIBLE);
            llClear.setVisibility(View.INVISIBLE);
            llExport.setVisibility(View.INVISIBLE);

            start = ForegroundService.start;

            Intent intent = new Intent(this, ForegroundService.class);
            bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if (service instanceof ForegroundService.MyBinder) {
                        myService = ((ForegroundService.MyBinder) service).getService();
                        ArrayList<Entry> entries = myService.getEntries();
                        logs.clear();
                        logs.addAll(entries);

                        lineChart.getLineData().clearValues();

                        LineDataSet lineDataSet = new LineDataSet(logs, "EC");

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

        long start = System.currentTimeMillis();
        plotGraphNotifier = new PlotGraphNotifier(Dashboard.GRAPH_PLOT_DELAY, () -> {
            if (skipCount < skipPoints) {
                skipCount++;
                return;
            }
            skipCount = 0;
            long seconds = (System.currentTimeMillis() - start) / 1000;
            LineData data = lineChart.getData();
            Entry entry = new Entry(seconds, ec);
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
    public void onBackPressed() {
        if (isCalibrating) {
            ExitConfirmDialog dialog = new ExitConfirmDialog((new ExitConfirmDialog.DialogCallbacks() {
                @Override
                public void onYesClicked(Dialog dialog1) {
                    deviceRef.child("UI").child("EC").child("EC_CAL").child("CAL").setValue(0);
                    dialog1.dismiss();
                    isCalibrating = false;
                    onBackPressed();
                }

                @Override
                public void onNoClicked(Dialog dialog1) {
                    dialog1.dismiss();
                }
            }));

            dialog.show(getSupportFragmentManager(), null);
        } else {
            deviceRef.child("UI").child("EC").child("EC_CAL").child("CAL").setValue(0);
            super.onBackPressed();
        }
    }
}