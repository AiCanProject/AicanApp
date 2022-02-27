package com.aican.aicanapp.specificactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.adapters.LogAdapter;
import com.aican.aicanapp.dataClasses.phData;
import com.aican.aicanapp.dialogs.IndustryCalibrationPointDialog;
import com.aican.aicanapp.dialogs.SelectCalibrationPointsDialog;
import com.aican.aicanapp.fragments.ph.PhFragment;
import com.aican.aicanapp.ph.PhView;
import com.github.mikephil.charting.charts.LineChart;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class IndusPhActivity extends AppCompatActivity {

    private static final String TAG = "IndusPhActivity";
    PhView phView;
    Button calibrateBtn, logBtn;
    TextView tvPhCurr, tvPhNext, tvTempCurr, tvTempNext, tvEcCurr, batter, slopeVal, offsetVal;
    LineChart lineChart;
    RecyclerView recyclerView;

    TableLayout stk;
    DatabaseReference deviceRef;
    LinearLayout llStart, llStop, llClear, llExport;
    CardView cv1Min, cv5Min, cv10Min, cv15Min, cvClock;

    float ph = 0;
    int skipPoints = 0;
    int skipCount = 0;


    public static String DEVICE_ID = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indus_ph);

        phView = findViewById(R.id.phView);
        tvEcCurr = findViewById(R.id.tvEcCurr);
        tvTempCurr = findViewById(R.id.tvTempCurr);
        tvTempNext = findViewById(R.id.tvTempNext);
        logBtn = findViewById(R.id.logBtn);
        recyclerView = findViewById(R.id.tableRecycler);
        batter = findViewById(R.id.batteryPer);
        slopeVal = findViewById(R.id.slopeVal);
        offsetVal = findViewById(R.id.offsetVal);
        phView = findViewById(R.id.phView);
        calibrateBtn = findViewById(R.id.calibrateBtn);
        tvPhCurr = findViewById(R.id.tvPhCurr);
        tvPhNext = findViewById(R.id.tvPhNext);


        phData phData = new phData();
        phView.setCurrentPh(7);
        ArrayList<phData> list = new ArrayList<>();

        logBtn.setOnClickListener(v -> {
            String currentTime = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault()).format(new Date());
            phData.setDate(currentTime);

            deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Float p = snapshot.getValue(Float.class);
                    String ph = String.format(Locale.UK, "%.2f", p );

                    phData.setpH(ph);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

            deviceRef.child("Data").child("MV_VAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Float mv = snapshot.getValue(Float.class);
                    String m = String.format(Locale.UK, "%.2f", mv );
                    phData.setmV(m);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

            LogAdapter adapter = new LogAdapter(list);
            list.add(phData);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

        });


        calibrateBtn.setOnClickListener(v -> {
            IndustryCalibrationPointDialog dialog = new IndustryCalibrationPointDialog();
            dialog.show( getSupportFragmentManager(),null);
        });

        DEVICE_ID = getIntent().getStringExtra(Dashboard.KEY_DEVICE_ID);
        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(DEVICE_ID)).getReference().child("IPHMETER").child(DEVICE_ID);
        setupListeners();

    }

    private void setupListeners() {
        deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ph = snapshot.getValue(Float.class);
                if(ph==null) return;
                phView.moveTo(ph);
                String phCur = String.format(Locale.UK, "%.2f" , ph);
                tvPhCurr.setText(phCur);
                //updatePh(ph);
                //PhFragment.this.ph = ph;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        deviceRef.child("Data").child("TEMP_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float temp = snapshot.getValue(Float.class);
                if (temp == null) return;
                String formatTemp = String.format(Locale.UK, "%.1f", temp);


                tvTempCurr.setText(formatTemp + "°C");
                //updatePh(temp);
                //PhFragment.this.ph = temp;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        deviceRef.child("Data").child("MV_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String ec = snapshot.getValue(Integer.class).toString();
                if (ec== null) return;
                tvEcCurr.setText(ec);
                //updatePh(temp);
                //PhFragment.this.ph = temp;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        deviceRef.child("Data").child("HOLD").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float hold = snapshot.getValue(Float.class);
                if(hold == 1) {
                    tvPhNext.setTextColor(Color.GREEN);
                    tvPhCurr.setTextColor(Color.GREEN);
                }
                else{
                    tvPhCurr.setTextColor(Color.parseColor("#433A7F"));
                    tvPhCurr.setTextColor(Color.parseColor("#433A7F"));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        deviceRef.child("Data").child("SLOPE").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String slope = snapshot.getValue(Integer.class).toString();
                slopeVal.setText(slope + "%");


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        deviceRef.child("Data").child("OFFSET").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float offset = snapshot.getValue(Float.class);
                String off = String.format(Locale.UK, "%.2f", offset);
                offsetVal.setText(off);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });



    }

}