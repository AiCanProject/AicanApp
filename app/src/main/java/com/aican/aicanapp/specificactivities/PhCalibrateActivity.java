package com.aican.aicanapp.specificactivities;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.dialogs.EditPhBufferDialog;
import com.aican.aicanapp.ph.PhView;
import com.aican.aicanapp.tempController.ProgressLabelView;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

    float[] buffers = new float[]{2.0F,4.0F,7.0F,9.0F,11.0F};
    String[] bufferLabels = new String[]{"B_1","B_2","B_3","B_4","B_5"};
    String[] coeffLabels = new String[]{"VAL_1","VAL_2","VAL_3","VAL_4","VAL_5"};
    int[] calValues = new int[]{11,21,31,41,51};
    int currentBuf = 0;

    DatabaseReference deviceRef;
    String deviceId;

    float ph=0;
    FillGraphDataTask fillGraphDataTask;

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

//        setSupportActionBar(toolbar);
//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

//        phTextView.setAnimationDuration(0);
//        phTextView.setProgress(buffers[0]);
//        phTextView.setAnimationDuration(800);
//        plvCoefficient.setAnimationDuration(0);

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
            data.addEntry(new Entry(seconds, ph), 0);
            lineChart.notifyDataSetChanged();
            data.notifyDataChanged();
            lineChart.invalidate();
        }

        void stopRunning(){
            running=false;
        }
    }
}