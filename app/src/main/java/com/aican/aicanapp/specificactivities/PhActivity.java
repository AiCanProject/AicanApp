package com.aican.aicanapp.specificactivities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.ProbeScan.ProbeScanner;
import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.fragments.ph.PhCalibFragment;
import com.aican.aicanapp.fragments.ph.PhCalibFragmentNew;
import com.aican.aicanapp.fragments.ph.PhFragment;
import com.aican.aicanapp.fragments.ph.phAlarmFragment;
import com.aican.aicanapp.fragments.ph.phGraphFragment;
import com.aican.aicanapp.fragments.ph.phLogFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PhActivity extends AppCompatActivity implements View.OnClickListener {

    TextView ph, calibrate, log, graph, alarm, tabItemPh, tabItemCalib;
    DatabaseReference deviceRef;

    PhFragment phFragment = new PhFragment();
    PhCalibFragment phCalibFragment = new PhCalibFragment();
    phLogFragment phLogFragment = new phLogFragment();
    phGraphFragment phGraphFragment = new phGraphFragment();
    phAlarmFragment phAlarmFragment = new phAlarmFragment();
    PhCalibFragmentNew phCalibFragmentNew = new PhCalibFragmentNew();
    DatabaseHelper databaseHelper;

    public static String DEVICE_ID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setTheme(R.style.AppTheme_Light);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ph);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.btnColor));

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        DEVICE_ID = getIntent().getStringExtra(Dashboard.KEY_DEVICE_ID);
        if (DEVICE_ID == null) {
            throw new RuntimeException();
        }

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);

        deviceRef.child("Data").child("AUTOLOG").setValue(0);

        deviceRef.child("Data").child("AUTOLOG").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Source.auto_log = snapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
        databaseHelper = new DatabaseHelper(PhActivity.this);

        ph = findViewById(R.id.item1);
        calibrate = findViewById(R.id.item2);
        log = findViewById(R.id.item3);
        graph = findViewById(R.id.item4);
        alarm = findViewById(R.id.item5);
        tabItemPh = findViewById(R.id.tabItemP);
        tabItemCalib = findViewById(R.id.select2);

        ph.setOnClickListener(this);
        calibrate.setOnClickListener(this);
        log.setOnClickListener(this);
        graph.setOnClickListener(this);
        alarm.setOnClickListener(this);


        String i = getIntent().getStringExtra("refreshCalib");
        if (i != null) {
            if (i.equals("y")) {
                tabItemPh.setBackground(getResources().getDrawable(R.drawable.backselect1));
                tabItemPh.setVisibility(View.INVISIBLE);
                TextView select2 = findViewById(R.id.select2);
                select2.setBackground(getResources().getDrawable(R.drawable.back_select2));

                loadFragments(phCalibFragmentNew);
                calibrate.setTextColor(Color.WHITE);
                ph.setTextColor(Color.parseColor("#FF24003A"));
                log.setTextColor(Color.parseColor("#FF24003A"));
                graph.setTextColor(Color.parseColor("#FF24003A"));
                alarm.setTextColor(Color.parseColor("#FF24003A"));
//                Toast.makeText(this, "" + calibrate.getWidth(), Toast.LENGTH_SHORT).show();
//                int size = calibrate.getWidth();
//                tabItemPh.animate().x(size).setDuration(100);

            }
        } else {
            loadFragments(phFragment);
        }


        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        deviceRef.child("PH_MODE").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @com.google.firebase.database.annotations.NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    PhCalibFragment.PH_MODE = snapshot.getValue(String.class);
                } else {
                    deviceRef.child("PH_MODE").setValue("both");
                    PhCalibFragment.PH_MODE = "both";
                }
            }

            @Override
            public void onCancelled(@NonNull @com.google.firebase.database.annotations.NotNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (Source.auto_log == 0 && !Source.calibratingNow) {
            TextView select2 = findViewById(R.id.select2);
            select2.setBackground(getResources().getDrawable(R.drawable.backselect1));
            select2.setVisibility(View.INVISIBLE);
            tabItemPh.setVisibility(View.VISIBLE);
            tabItemPh.setBackground(getResources().getDrawable(R.drawable.back_select2));

            if (view.getId() == R.id.item1) {
                tabItemPh.animate().x(0).setDuration(100);
                loadFragments(phFragment);
                ph.setTextColor(Color.WHITE);
                calibrate.setTextColor(Color.parseColor("#FF24003A"));
                log.setTextColor(Color.parseColor("#FF24003A"));
                graph.setTextColor(Color.parseColor("#FF24003A"));
                alarm.setTextColor(Color.parseColor("#FF24003A"));
            } else if (view.getId() == R.id.item2) {

//                loadFragments(phCalibFragment);
                loadFragments(phCalibFragmentNew);

                calibrate.setTextColor(Color.WHITE);
                ph.setTextColor(Color.parseColor("#FF24003A"));
                log.setTextColor(Color.parseColor("#FF24003A"));
                graph.setTextColor(Color.parseColor("#FF24003A"));
                alarm.setTextColor(Color.parseColor("#FF24003A"));
                int size = calibrate.getWidth();
                tabItemPh.animate().x(size).setDuration(100);

            } else if (view.getId() == R.id.item3) {
                loadFragments(phLogFragment);
                log.setTextColor(Color.WHITE);
                ph.setTextColor(Color.parseColor("#FF24003A"));
                calibrate.setTextColor(Color.parseColor("#FF24003A"));
                graph.setTextColor(Color.parseColor("#FF24003A"));
                alarm.setTextColor(Color.parseColor("#FF24003A"));
                int size = calibrate.getWidth() * 2;
                tabItemPh.animate().x(size).setDuration(100);

            } else if (view.getId() == R.id.item4) {
                loadFragments(phGraphFragment);

                graph.setTextColor(Color.WHITE);
                ph.setTextColor(Color.parseColor("#FF24003A"));
                calibrate.setTextColor(Color.parseColor("#FF24003A"));
                log.setTextColor(Color.parseColor("#FF24003A"));
                alarm.setTextColor(Color.parseColor("#FF24003A"));
                int size = calibrate.getWidth() * 3;
                tabItemPh.animate().x(size).setDuration(100);

            } else if (view.getId() == R.id.item5) {
                loadFragments(phAlarmFragment);

                alarm.setTextColor(Color.WHITE);
                ph.setTextColor(Color.parseColor("#FF24003A"));
                calibrate.setTextColor(Color.parseColor("#FF24003A"));
                graph.setTextColor(Color.parseColor("#FF24003A"));
                log.setTextColor(Color.parseColor("#FF24003A"));
                int size = calibrate.getWidth() * 4;
                tabItemPh.animate().x(size).setDuration(100);
            } else if (view.getId() == R.id.cLProbes) {
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "Probe Scanner : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

                Intent intent = new Intent(PhActivity.this, ProbeScanner.class);
                intent.putExtra("activity", "PhFragment");
//            intent.addFlags()
                startActivity(intent);
            }
        } else {
            Toast.makeText(this, "You cannot change fragment while calibrating / logging", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (Source.auto_log == 0 && !Source.calibratingNow) {
            Intent intent = new Intent(PhActivity.this, Dashboard.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "You cannot change fragment while logging / calibrating", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean loadFragments(Fragment fragment) {
        if (fragment != null) {
            Log.d("navigation", "loadFragments: Frag is loaded");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, fragment)
                    .addToBackStack(null)
                    .commit();

            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);

            return true;
        }
        return false;
    }

}