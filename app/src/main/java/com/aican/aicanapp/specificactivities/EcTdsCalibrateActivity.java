package com.aican.aicanapp.specificactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.github.mikephil.charting.charts.LineChart;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;

import static android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;

public class EcTdsCalibrateActivity extends AppCompatActivity {

    TextInputEditText etBuffer,etTds;
    RelativeLayout startLayout, rlCoefficient;
    ImageView ivStartBtn;
    TextView tvStart, tvCoefficient;

    DatabaseReference deviceRef;
    String deviceId;
    LineChart lineChart;
    FillGraphDataTask fillGraphDataTask;

    int tds=0;

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
        if(deviceId==null){
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

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(deviceId)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);


        ivStartBtn.setOnClickListener(v->{
            ivStartBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryAlpha));
            ivStartBtn.setEnabled(false);
            tvStart.setVisibility(View.VISIBLE);

            CountDownTimer timer = new CountDownTimer(120000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    millisUntilFinished/=1000;
                    int min = (int)millisUntilFinished/60;
                    int sec = (int)millisUntilFinished%60;
                    String time = String.format(Locale.UK,"%02d:%02d", min, sec);
                    tvStart.setText(time);
                }

                @Override
                public void onFinish() {
                    deviceRef.child("UI").child("EC").child("EC_CAL").child("VAL_1").get().addOnSuccessListener(snapshot -> {
                       Float coeff = snapshot.getValue(Float.class);
                       if(coeff == null) return;
                       tvCoefficient.setText(String.valueOf(coeff));
                       startLayout.setVisibility(View.INVISIBLE);
                       rlCoefficient.setVisibility(View.VISIBLE);
                    });
                }
            };

            deviceRef.child("UI").child("EC").child("EC_CAL").child("B_1").setValue(Integer.valueOf(etBuffer.getText().toString())).addOnSuccessListener(t->{
                deviceRef.child("UI").child("EC").child("EC_CAL").child("CAL").setValue(1).addOnSuccessListener(t2-> timer.start());
            });
        });

        setupGraph();
        setupListeners();
    }

    private void setupGraph() {
        LineDataSet lineDataSet = new LineDataSet(new ArrayList<>(),"TDS");

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
    }

    @Override
    public void onResume() {
        super.onResume();
        fillGraphDataTask= new FillGraphDataTask();
        fillGraphDataTask.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        fillGraphDataTask.stopRunning();
        fillGraphDataTask.cancel(true);
    }

    private void setupListeners() {
        deviceRef.child("UI").child("EC").child("EC_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Integer val = snapshot.getValue(Integer.class);
                if(val==null) return;
                etTds.setText(String.valueOf(val));
                tds = val;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    class FillGraphDataTask extends AsyncTask<Void, Void, Void> {

        Long start;
        boolean running=true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            start = System.currentTimeMillis();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            while (running){
                publishProgress();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            long seconds = (System.currentTimeMillis()-start)/1000;
            LineData data = lineChart.getData();
            data.addEntry(new Entry(seconds, tds), 0);

            if(data.getXMax()-data.getXMin()>60){
                lineChart.getXAxis().setAxisMinimum(data.getXMax()-60);
            }
            lineChart.notifyDataSetChanged();
            data.notifyDataChanged();
            lineChart.invalidate();
        }

        void stopRunning(){
            running=false;
        }
    }

}