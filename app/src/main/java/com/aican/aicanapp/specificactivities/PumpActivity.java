package com.aican.aicanapp.specificactivities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.adapters.ViewPagerAdapter;
import com.aican.aicanapp.fragments.pump.DoseFragment;
import com.aican.aicanapp.fragments.pump.PumpFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PumpActivity extends AppCompatActivity {

    public static final int STATUS_OFF = 0;
    public static final int STATUS_DOSE = 10;
    public static final int STATUS_DOSE_COMPLETED = 11;
    public static final int STATUS_PUMP = 20;
    public static final int STATUS_CAL_START = 30;
    public static final int STATUS_CAL_FINISH = 31;

    DatabaseReference deviceRef = null;
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

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PumpActivity.DEVICE_ID)).getReference()
                .child("P_PUMP").child(PumpActivity.DEVICE_ID);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        fragments = new ArrayList<>();
        fragments.add(new DoseFragment());
        fragments.add(new PumpFragment());
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), fragments);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    deviceRef.child("UI").child("Mode").setValue(0);
                }else {
                    deviceRef.child("UI").child("Mode").setValue(1);
                }

            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position)->{
            if(position==0){
                tab.setText("Dose");
//                deviceRef.child("UI").child("Mode").setValue(0);
            }else{
                tab.setText("Pump");
  //              deviceRef.child("UI").child("Mode").setValue(1);
            }
        }).attach();

    }

}
