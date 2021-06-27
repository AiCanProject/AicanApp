package com.aican.aicanapp.specificactivities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.adapters.ViewPagerAdapter;
import com.aican.aicanapp.fragments.temp.SetTempFragment;
import com.aican.aicanapp.fragments.temp.TempFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class TemperatureActivity extends AppCompatActivity {

    public static final String DEVICE_TYPE_KEY = "device_type";
    public static String DEVICE_ID = null;
    public static String deviceType = null;


    TabLayout tabLayout;
    ViewPager2 viewPager;
    ViewPagerAdapter viewPagerAdapter;
    ArrayList<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setTheme(R.style.AppTheme_Light);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temp_activity);

        DEVICE_ID = getIntent().getStringExtra(Dashboard.KEY_DEVICE_ID);
        deviceType = getIntent().getStringExtra(DEVICE_TYPE_KEY);
        if (DEVICE_ID == null || deviceType == null) {
            throw new RuntimeException();
        }

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        fragments = new ArrayList<>();
        fragments.add(new SetTempFragment());
        fragments.add(new TempFragment());
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), fragments);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("set temp");
            } else if (position == 1) {
                tab.setText("temp");
            }
        }).attach();

    }

}