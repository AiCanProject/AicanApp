package com.aican.aicanapp.specificactivities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.userdatabase.UserDatabase;
import com.aican.aicanapp.fragments.ph.PhCalibFragment;
import com.aican.aicanapp.fragments.ph.PhFragment;
import com.aican.aicanapp.fragments.ph.phAlarmFragment;
import com.aican.aicanapp.fragments.ph.phLogFragment;

public class PhActivity extends AppCompatActivity implements View.OnClickListener {

    TextView ph, calibrate, log, alarm, tabItemPh, tabItemCalib;

    ImageView setting, user_database;

    PhFragment phFragment = new PhFragment();
    PhCalibFragment phCalibFragment = new PhCalibFragment();
    phLogFragment phLogFragment = new phLogFragment();
    phAlarmFragment phAlarmFragment = new phAlarmFragment();

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

        loadFragments(phFragment);
        ph = findViewById(R.id.item1);
        calibrate = findViewById(R.id.item2);
        log = findViewById(R.id.item3);
        alarm = findViewById(R.id.item4);
        tabItemPh = findViewById(R.id.tabItemP);
        tabItemCalib = findViewById(R.id.select2);

        setting = findViewById(R.id.settings);
        user_database = findViewById(R.id.user_database);
        ph.setOnClickListener(this);
        calibrate.setOnClickListener(this);
        log.setOnClickListener(this);
        alarm.setOnClickListener(this);

        user_database.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PhActivity.this, UserDatabase.class));
            }
        });
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.item1) {
            tabItemPh.animate().x(0).setDuration(100);
            loadFragments(phFragment);
            ph.setTextColor(Color.WHITE);
            calibrate.setTextColor(Color.parseColor("#FF24003A"));
            log.setTextColor(Color.parseColor("#FF24003A"));
            alarm.setTextColor(Color.parseColor("#FF24003A"));
        } else if (view.getId() == R.id.item2) {

            loadFragments(phCalibFragment);
            calibrate.setTextColor(Color.WHITE);
            ph.setTextColor(Color.parseColor("#FF24003A"));
            log.setTextColor(Color.parseColor("#FF24003A"));
            alarm.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibrate.getWidth();
            tabItemPh.animate().x(size).setDuration(100);

        } else if (view.getId() == R.id.item3) {
            loadFragments(phLogFragment);
            log.setTextColor(Color.WHITE);
            ph.setTextColor(Color.parseColor("#FF24003A"));
            calibrate.setTextColor(Color.parseColor("#FF24003A"));
            alarm.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibrate.getWidth() * 2;
            tabItemPh.animate().x(size).setDuration(100);

        } else if (view.getId() == R.id.item4) {
            loadFragments(phAlarmFragment);

            alarm.setTextColor(Color.WHITE);
            ph.setTextColor(Color.parseColor("#FF24003A"));
            calibrate.setTextColor(Color.parseColor("#FF24003A"));
            log.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibrate.getWidth() * 3;
            tabItemPh.animate().x(size).setDuration(100);
        }
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        Intent intent = new Intent(PhActivity.this, Dashboard.class);
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

            return true;
        }
        return false;
    }
}