package com.aican.aicanapp.specificactivities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.fragments.temp.AlarmTempFragment;
import com.aican.aicanapp.fragments.temp.LogTempFragment;
import com.aican.aicanapp.fragments.temp.SetTempFragment;
import com.aican.aicanapp.fragments.temp.TempFragment;
import com.aican.aicanapp.fragments.temp.TempJobFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TemperatureActivity extends AppCompatActivity implements View.OnClickListener {

    TextView ph, calibrate, log, alarm, tabItemPh, tabItemCalib,job;

    public static final String DEVICE_TYPE_KEY = "device_type";
    public static String DEVICE_ID = null;
    public static String deviceType = null;


    DatabaseReference deviceRef;

    TempFragment tempFragment = new TempFragment();
    SetTempFragment setTempFragment = new SetTempFragment();
    LogTempFragment logTempFragment = new LogTempFragment();
    AlarmTempFragment alarmTempFragment = new AlarmTempFragment();
    TempJobFragment tempJobFragment = new TempJobFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setTheme(R.style.AppTheme_Light);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temp_activity);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.btnColor));

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        DEVICE_ID = getIntent().getStringExtra(Dashboard.KEY_DEVICE_ID);
        deviceType = getIntent().getStringExtra(DEVICE_TYPE_KEY);
        if (DEVICE_ID == null || deviceType == null) {
            throw new RuntimeException();
        }

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(TemperatureActivity.DEVICE_ID)).getReference()
                .child(TemperatureActivity.deviceType).child(TemperatureActivity.DEVICE_ID);

        Source.deviceID = DEVICE_ID;


        loadFragments(setTempFragment);
        ph = findViewById(R.id.item1);
        calibrate = findViewById(R.id.item2);
        log = findViewById(R.id.item3);
        alarm = findViewById(R.id.item4);
        job = findViewById(R.id.item5);
        tabItemPh = findViewById(R.id.tabItemP);
        tabItemCalib = findViewById(R.id.select2);

        ph.setOnClickListener(this);
        calibrate.setOnClickListener(this);
        log.setOnClickListener(this);
        alarm.setOnClickListener(this);
        job.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.item1) {
            deviceRef.child("UI").child("TEMP").child("MODE").setValue(0);
            tabItemPh.animate().x(0).setDuration(100);
            loadFragments(setTempFragment);
            ph.setTextColor(Color.WHITE);
            calibrate.setTextColor(Color.parseColor("#FF24003A"));
            log.setTextColor(Color.parseColor("#FF24003A"));
            alarm.setTextColor(Color.parseColor("#FF24003A"));
            job.setTextColor(Color.parseColor("#FF24003A"));
        } else if (view.getId() == R.id.item2) {
            deviceRef.child("UI").child("TEMP").child("MODE").setValue(1);
            loadFragments(tempFragment);
            calibrate.setTextColor(Color.WHITE);
            ph.setTextColor(Color.parseColor("#FF24003A"));
            log.setTextColor(Color.parseColor("#FF24003A"));
            alarm.setTextColor(Color.parseColor("#FF24003A"));
            job.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibrate.getWidth();
            tabItemPh.animate().x(size).setDuration(100);

        } else if (view.getId() == R.id.item3) {
            deviceRef.child("UI").child("TEMP").child("MODE").setValue(2);
            loadFragments(logTempFragment);
            log.setTextColor(Color.WHITE);
            ph.setTextColor(Color.parseColor("#FF24003A"));
            calibrate.setTextColor(Color.parseColor("#FF24003A"));
            alarm.setTextColor(Color.parseColor("#FF24003A"));
            job.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibrate.getWidth() * 2;
            tabItemPh.animate().x(size).setDuration(100);

        } else if (view.getId() == R.id.item4) {
            deviceRef.child("UI").child("TEMP").child("MODE").setValue(3);
            loadFragments(alarmTempFragment);

            alarm.setTextColor(Color.WHITE);
            ph.setTextColor(Color.parseColor("#FF24003A"));
            calibrate.setTextColor(Color.parseColor("#FF24003A"));
            log.setTextColor(Color.parseColor("#FF24003A"));
            job.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibrate.getWidth() * 3;
            tabItemPh.animate().x(size).setDuration(100);
        }else if (view.getId() == R.id.item5){

            deviceRef.child("UI").child("TEMP").child("MODE").setValue(4);
            loadFragments(tempJobFragment);

            job.setTextColor(Color.WHITE);
            ph.setTextColor(Color.parseColor("#FF24003A"));
            calibrate.setTextColor(Color.parseColor("#FF24003A"));
            log.setTextColor(Color.parseColor("#FF24003A"));
            alarm.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibrate.getWidth() * 4;
            tabItemPh.animate().x(size).setDuration(100);

        }
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        Intent intent = new Intent(TemperatureActivity.this, Dashboard.class);
        startActivity(intent);
    }

    private boolean loadFragments(Fragment fragment) {
        if (fragment != null) {
            Log.d("navigation", "loadFragments: Frag is loaded");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, fragment)
                    .addToBackStack(null)
                    .commit();

           // deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);

            return true;
        }
        return false;
    }
}