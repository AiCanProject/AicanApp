package com.aican.aicanapp.specificactivities;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.dialogs.EditPhBufferDialog;
import com.aican.aicanapp.fragments.ph.PhFragment;
import com.aican.aicanapp.graph.ForegroundService;
import com.aican.aicanapp.ph.PhView;
import com.aican.aicanapp.tempController.ProgressLabelView;
import com.aican.aicanapp.utils.PlotGraphNotifier;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
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
import com.opencsv.CSVWriter;

import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;

public class PhCalibrateActivity extends AppCompatActivity {

    PhView phView;
//    ProgressLabelView phTextView, plvCoefficient;
    Button btnStart,btnNext;
    TextView tvTimer, tvCoefficient, tvBufferCurr, tvBufferNext, tvPh, tvCoefficientLabel, tvEdit;
    Toolbar toolbar;
    LineChart lineChart;
    LinearLayout llStart, llStop, llClear, llExport;


    float[] buffers = new float[]{2.0F,4.0F,7.0F,9.0F,11.0F};
    String[] bufferLabels = new String[]{"B_1","B_2","B_3","B_4","B_5"};
    String[] coeffLabels = new String[]{"VAL_1","VAL_2","VAL_3","VAL_4","VAL_5"};
    int[] calValues = new int[]{11,21,31,41,51};
    int currentBuf = 0;

    DatabaseReference deviceRef;
    String deviceId;

    float ph=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_Light);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ph_calibrate);

        Window window = getWindow();
        window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.orange));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        deviceId = getIntent().getStringExtra(Dashboard.KEY_DEVICE_ID);
        if(deviceId==null){
            throw new RuntimeException();
        }

        phView = findViewById(R.id.phView);
//        phTextView = findViewById(R.id.phTextView);
        btnStart = findViewById(R.id.startBtn);
        btnNext = findViewById(R.id.next_btn);
        tvTimer = findViewById(R.id.tvTimer);
//        plvCoefficient = findViewById(R.id.plvCoefficient);
        tvCoefficient = findViewById(R.id.tvCoefficient);
        toolbar = findViewById(R.id.main_toolbar);
        tvBufferCurr = findViewById(R.id.tvBufferCurr);
        tvBufferNext = findViewById(R.id.tvBufferNext);
        tvPh = findViewById(R.id.tvPh);
        tvCoefficientLabel = findViewById(R.id.tvCoefficientLabel);
        lineChart = findViewById(R.id.line_chart);
        tvEdit = findViewById(R.id.tvEdit);
        llStart = findViewById(R.id.llStart);
        llStop = findViewById(R.id.llStop);
        llClear = findViewById(R.id.llClear);
        llExport = findViewById(R.id.llExport);

//        setSupportActionBar(toolbar);
//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        tvBufferCurr.setText(String.valueOf(buffers[0]));

        phView.setCurrentPh(buffers[0]);

//        phTextView.setTextColor(getAttr(R.attr.primaryTextColor));
//        plvCoefficient.setTextColor(getAttr(R.attr.primaryTextColor));

        btnStart.setOnClickListener(v->{
            btnStart.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryAlpha));
            btnStart.setEnabled(false);
            tvTimer.setVisibility(View.VISIBLE);
            CountDownTimer timer = new CountDownTimer(120000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    millisUntilFinished/=1000;
                    int min = (int)millisUntilFinished/60;
                    int sec = (int)millisUntilFinished%60;
                    String time = String.format(Locale.UK, "%02d:%02d", min, sec);
                    tvTimer.setText(time);
                }

                @Override
                public void onFinish() {
                    runOnUiThread(()->{
                        tvTimer.setVisibility(View.INVISIBLE);
                        deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);
                        deviceRef.child("UI").child("PH").child("PH_CAL").child(coeffLabels[currentBuf]).get().addOnSuccessListener(dataSnapshot -> {
                            Float coeff = dataSnapshot.getValue(Float.class);
                            if(coeff==null) return;
                            displayCoeffAndPrepareNext(coeff);
                        });
                    });
                }
            };
            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(calValues[currentBuf]).addOnSuccessListener(t->{
                timer.start();
            });
        });

        btnNext.setOnClickListener(v->{
            if(currentBuf==buffers.length-1){
                Intent intent = new Intent(this, PhActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return;
            }
            btnNext.setVisibility(View.INVISIBLE);
            currentBuf+=1;

            btnStart.setBackgroundColor(ContextCompat.getColor(PhCalibrateActivity.this, R.color.colorPrimary));
            btnStart.setEnabled(true);

            phView.moveTo(buffers[currentBuf]);
            updateBufferValue(buffers[currentBuf]);

            tvCoefficient.setVisibility(View.INVISIBLE);
            tvCoefficientLabel.setVisibility(View.INVISIBLE);

        });

        tvEdit.setOnClickListener(v->{
            EditPhBufferDialog dialog = new EditPhBufferDialog(ph->{
                updateBufferValue(ph);
                deviceRef.child("UI").child("PH").child("PH_CAL").child(bufferLabels[currentBuf]).setValue(ph);
            });
            dialog.show(getSupportFragmentManager(), null);
        });

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(deviceId)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);

        setupGraph();
        loadBuffers();
        setupListeners();
    }

    private void setupGraph() {
        LineDataSet lineDataSet = new LineDataSet(new ArrayList<>(),"pH");

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
        d.setText("pH Graph");
        lineChart.setDescription(d);

        llStart.setOnClickListener(v->{
            if(ForegroundService.isRunning()){
                Toast.makeText(this, "Another graph is logging", Toast.LENGTH_SHORT).show();
                return;
            }
            llStart.setVisibility(View.INVISIBLE);
            llStop.setVisibility(View.VISIBLE);
            llClear.setVisibility(View.INVISIBLE);
            llExport.setVisibility(View.INVISIBLE);
            startLogging();
        });
        llStop.setOnClickListener(v->{
            llStart.setVisibility(View.VISIBLE);
            llStop.setVisibility(View.INVISIBLE);
            llClear.setVisibility(View.VISIBLE);
            llExport.setVisibility(View.VISIBLE);
            stopLogging();
        });
        llClear.setOnClickListener(v->{
            llClear.setVisibility(View.INVISIBLE);
            llExport.setVisibility(View.INVISIBLE);
            clearLogs();
        });
        llExport.setOnClickListener(v->{
            exportLogs();
        });
    }

    private void exportLogs() {
        if(!checkStoragePermission()){
            return;
        }
        String csv = (getExternalFilesDir(null).getAbsolutePath() + "/"+System.currentTimeMillis()+".csv");
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(csv));

            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[]{"X", "Y"});
            for(int i=0; i<logs.size(); i++)
            {
                String[] s = {String.valueOf(logs.get(i).getX()), String.valueOf(logs.get(i).getY())};
                data.add(s);
            }
            writer.writeAll(data); // data is adding to csv
            Toast.makeText(this,"Exported",Toast.LENGTH_LONG).show();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
                return false;
            }
        }
        return true;
    }

    private void clearLogs() {
        logs.clear();
        if(myService!=null){
            myService.clearEntries();
        }
    }

    private void stopLogging() {
        isLogging = false;
        if(myService!=null){
            myService.stopLogging(PhFragment.class);
        }
    }

    ArrayList<Entry> logs = new ArrayList<>();
    private boolean isLogging = false;
    private void startLogging() {
        logs.clear();
        isLogging = true;

        Context context = this;
        Intent intent = new Intent(context, ForegroundService.class);
        DatabaseReference ref = deviceRef.child("Data").child("PH_VAL");
        ForegroundService.setInitials(PhActivity.DEVICE_ID,ref, PhFragment.class, null);
        startService(intent);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if(service instanceof ForegroundService.MyBinder){
                    myService = ((ForegroundService.MyBinder) service).getService();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, 0);
    }


    ForegroundService myService;
    PlotGraphNotifier plotGraphNotifier;
    @Override
    public void onResume() {
        super.onResume();

        if(ForegroundService.isMyClassRunning(PhActivity.DEVICE_ID, PhFragment.class)){
            llStart.setVisibility(View.INVISIBLE);
            llStop.setVisibility(View.VISIBLE);
            llClear.setVisibility(View.INVISIBLE);
            llExport.setVisibility(View.INVISIBLE);

            Intent intent = new Intent(this, ForegroundService.class);
            bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if(service instanceof ForegroundService.MyBinder){
                        myService = ((ForegroundService.MyBinder) service).getService();
                        ArrayList<Entry> entries=myService.getEntries();
                        logs.clear();
                        logs.addAll(entries);

                        lineChart.getLineData().clearValues();

                        LineDataSet lineDataSet = new LineDataSet(logs,"pH");

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
            },0);
        }

        long start = System.currentTimeMillis();
        plotGraphNotifier = new PlotGraphNotifier(2000, ()->{
            long seconds = (System.currentTimeMillis()-start)/1000;
            LineData data = lineChart.getData();
            Entry entry = new Entry(seconds, ph);
            data.addEntry(entry, 0);
            lineChart.notifyDataSetChanged();
            data.notifyDataChanged();
            lineChart.invalidate();
            if(isLogging){
                logs.add(entry);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        plotGraphNotifier.stop();
    }

    private void displayCoeffAndPrepareNext(float coeff) {
        if(currentBuf==buffers.length-1){
            btnNext.setText("Done");
        }
        btnNext.setVisibility(View.VISIBLE);

        tvCoefficientLabel.setVisibility(View.VISIBLE);
        tvCoefficient.setText(String.format(Locale.UK, "%.2f", coeff));
        tvCoefficient.setVisibility(View.VISIBLE);
    }

    private void loadBuffers() {
        deviceRef.child("UI").child("PH").child("PH_CAL").get().addOnSuccessListener(snapshot -> {
            buffers[0] = snapshot.child("B_1").getValue(Float.class);
            buffers[1] = snapshot.child("B_2").getValue(Float.class);
            buffers[2] = snapshot.child("B_3").getValue(Float.class);
            buffers[3] = snapshot.child("B_4").getValue(Float.class);
            buffers[4] = snapshot.child("B_5").getValue(Float.class);

            tvBufferCurr.setText(String.valueOf(buffers[0]));
            phView.setCurrentPh(buffers[0]);
        });
    }

    private void setupListeners() {
        deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ph = snapshot.getValue(Float.class);
                if(ph==null) return;
                String phString = String.format(Locale.UK, "%.2f", ph);
                tvPh.setText(phString);
                PhCalibrateActivity.this.ph = ph;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private int getAttr(@AttrRes int attrRes){
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attrRes,typedValue,true);

        return typedValue.data;
    }
    
    private void updateBufferValue(Float value){
        String newValue = String.valueOf(value);
        tvBufferNext.setText(newValue);

        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        Animation slideInBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tvBufferCurr.setVisibility(View.INVISIBLE);
                TextView t = tvBufferCurr;
                tvBufferCurr = tvBufferNext;
                tvBufferNext = t;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        tvBufferCurr.startAnimation(fadeOut);
        tvBufferNext.setVisibility(View.VISIBLE);
        tvBufferNext.startAnimation(slideInBottom);
    }

}