package com.aican.aicanappnoncfr.specificactivities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.aican.aicanappnoncfr.Dashboard.Dashboard;
import com.aican.aicanappnoncfr.R;
import com.aican.aicanappnoncfr.fragments.ec.EcAlarmFragment;
import com.aican.aicanappnoncfr.fragments.ec.EcCalibFragment;
import com.aican.aicanappnoncfr.fragments.ec.EcFragment;
import com.aican.aicanappnoncfr.fragments.ec.EcGraphFragment;
import com.aican.aicanappnoncfr.fragments.ec.EcLogFragment;
import com.aican.aicanappnoncfr.fragments.ph.PhCalibFragment;
import com.aican.aicanappnoncfr.fragments.ph.PhFragment;
import com.aican.aicanappnoncfr.fragments.ph.phAlarmFragment;
import com.aican.aicanappnoncfr.fragments.ph.phGraphFragment;
import com.aican.aicanappnoncfr.fragments.ph.phLogFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EcActivity extends AppCompatActivity implements View.OnClickListener {

    TextView ph, calibrate, log, graph, alarm, tabItemPh, tabItemCalib;

    DatabaseReference deviceRef;

    EcFragment ecFragment = new EcFragment();
    EcCalibFragment ecCalibFragment = new EcCalibFragment();
    EcAlarmFragment ecAlarmFragment = new EcAlarmFragment();
    EcLogFragment ecLogFragment = new EcLogFragment();
    EcGraphFragment ecGraphFragment = new EcGraphFragment();

    public static String DEVICE_ID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setTheme(R.style.AppTheme_Light);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ec);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.btnColor));

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        DEVICE_ID = getIntent().getStringExtra(Dashboard.KEY_DEVICE_ID);
        if (DEVICE_ID == null) {
            throw new RuntimeException();
        }
        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(DEVICE_ID)).getReference().child("ECMETER").child(EcActivity.DEVICE_ID);

        loadFragments(ecFragment);
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
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.item1) {
            tabItemPh.animate().x(0).setDuration(100);
            loadFragments(ecFragment);
            ph.setTextColor(Color.WHITE);
            calibrate.setTextColor(Color.parseColor("#FF24003A"));
            log.setTextColor(Color.parseColor("#FF24003A"));
            graph.setTextColor(Color.parseColor("#FF24003A"));
            alarm.setTextColor(Color.parseColor("#FF24003A"));
        } else if (view.getId() == R.id.item2) {

            loadFragments(ecCalibFragment);
            calibrate.setTextColor(Color.WHITE);
            ph.setTextColor(Color.parseColor("#FF24003A"));
            log.setTextColor(Color.parseColor("#FF24003A"));
            graph.setTextColor(Color.parseColor("#FF24003A"));
            alarm.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibrate.getWidth();
            tabItemPh.animate().x(size).setDuration(100);

        } else if (view.getId() == R.id.item3) {
            loadFragments(ecLogFragment);
            log.setTextColor(Color.WHITE);
            ph.setTextColor(Color.parseColor("#FF24003A"));
            calibrate.setTextColor(Color.parseColor("#FF24003A"));
            graph.setTextColor(Color.parseColor("#FF24003A"));
            alarm.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibrate.getWidth() * 2;
            tabItemPh.animate().x(size).setDuration(100);

        } else if (view.getId() == R.id.item4) {
            loadFragments(ecGraphFragment);

            graph.setTextColor(Color.WHITE);
            ph.setTextColor(Color.parseColor("#FF24003A"));
            calibrate.setTextColor(Color.parseColor("#FF24003A"));
            log.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibrate.getWidth() * 3;
            tabItemPh.animate().x(size).setDuration(100);

        } else if (view.getId() == R.id.item5) {
            loadFragments(ecAlarmFragment);

            alarm.setTextColor(Color.WHITE);
            ph.setTextColor(Color.parseColor("#FF24003A"));
            calibrate.setTextColor(Color.parseColor("#FF24003A"));
            graph.setTextColor(Color.parseColor("#FF24003A"));
            log.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibrate.getWidth() * 4;
            tabItemPh.animate().x(size).setDuration(100);
        }
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        Intent intent = new Intent(EcActivity.this, Dashboard.class);
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


//            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);

            return true;
        }
        return false;
    }
}