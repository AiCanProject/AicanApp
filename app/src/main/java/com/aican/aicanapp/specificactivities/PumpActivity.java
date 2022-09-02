package com.aican.aicanapp.specificactivities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.adapters.ViewPagerAdapter;
import com.aican.aicanapp.fragments.pump.DoseFragment;
import com.aican.aicanapp.fragments.pump.JobFragment;
import com.aican.aicanapp.fragments.pump.PCalibFragment;
import com.aican.aicanapp.fragments.pump.PumpCalibFragment;
import com.aican.aicanapp.fragments.pump.PumpFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PumpActivity extends AppCompatActivity implements View.OnClickListener{

    TextView calibration, pump, job, alarm, tabItemTemp, tabItemTune;

    DatabaseReference deviceRef;


    public static final int STATUS_OFF = 0;
    public static final int STATUS_DOSE = 10;
    public static final int STATUS_DOSE_COMPLETED = 11;
    public static final int STATUS_PUMP = 20;
    public static final int STATUS_CAL_START = 30;
    public static final int STATUS_CAL_FINISH = 31;

    PumpFragment pumpFragment = new PumpFragment();
    DoseFragment doseFragment = new DoseFragment();
    PumpCalibFragment calibFragment = new PumpCalibFragment();
    PCalibFragment pCalibFragment = new PCalibFragment();
    JobFragment jobFragment = new JobFragment();

    ViewPager2 viewPager;
    TabLayout tabLayout;

    ArrayList<Fragment> fragments;
    ViewPagerAdapter adapter;

    public static String DEVICE_ID = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_Light);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pump);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        DEVICE_ID = getIntent().getStringExtra(Dashboard.KEY_DEVICE_ID);
        if (DEVICE_ID == null) {
            throw new RuntimeException();
        }

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(DEVICE_ID)).getReference().child("P_PUMP").child(PumpActivity.DEVICE_ID);

        loadFragments(doseFragment);
        pump = findViewById(R.id.item1);
        calibration = findViewById(R.id.item2);
        job = findViewById(R.id.item3);
        alarm = findViewById(R.id.item4);
        tabItemTemp = findViewById(R.id.tabItemP);
        tabItemTune = findViewById(R.id.select2);

        pump.setOnClickListener(this);
        calibration.setOnClickListener(this);
        job.setOnClickListener(this);
        alarm.setOnClickListener(this);

//        tabLayout = findViewById(R.id.tabLayout);
//        viewPager = findViewById(R.id.viewPager);
//
//        fragments = new ArrayList<>();
//        fragments.add(new DoseFragment());
//        fragments.add(new PumpFragment());
//        adapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), fragments);
//        viewPager.setAdapter(adapter);
//
//        new TabLayoutMediator(tabLayout, viewPager, (tab, position)->{
//            if(position==0){
//                tab.setText("Dose");
//            }else{
//                tab.setText("Pump");
//            }
//        }).attach();

    }
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.item1) {
            deviceRef.child("UI").child("Mode").setValue(0);
            tabItemTemp.animate().x(0).setDuration(100);
            loadFragments(doseFragment);
            pump.setTextColor(Color.WHITE);
            calibration.setTextColor(Color.parseColor("#FF24003A"));
            job.setTextColor(Color.parseColor("#FF24003A"));
            alarm.setTextColor(Color.parseColor("#FF24003A"));
        } else if (view.getId() == R.id.item2) {
            deviceRef.child("UI").child("Mode").setValue(1);

            // calib fragment is pump fragment here
            loadFragments(calibFragment);
            calibration.setTextColor(Color.WHITE);
            pump.setTextColor(Color.parseColor("#FF24003A"));
            job.setTextColor(Color.parseColor("#FF24003A"));
            alarm.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibration.getWidth();
            tabItemTemp.animate().x(size).setDuration(100);

        } else if (view.getId() == R.id.item3) {
            deviceRef.child("UI").child("Mode").setValue(2);
            loadFragments(pCalibFragment);
            job.setTextColor(Color.WHITE);
            pump.setTextColor(Color.parseColor("#FF24003A"));
            calibration.setTextColor(Color.parseColor("#FF24003A"));
            alarm.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibration.getWidth() * 2;
            tabItemTemp.animate().x(size).setDuration(100);

        } else if (view.getId() == R.id.item4) {
            loadFragments(jobFragment);

            alarm.setTextColor(Color.WHITE);
            pump.setTextColor(Color.parseColor("#FF24003A"));
            calibration.setTextColor(Color.parseColor("#FF24003A"));
            job.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibration.getWidth() * 3;
            tabItemTemp.animate().x(size).setDuration(100);
        }
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        Intent intent = new Intent(PumpActivity.this, Dashboard.class);
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


            //deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);

            return true;
        }
        return false;
    }


}
