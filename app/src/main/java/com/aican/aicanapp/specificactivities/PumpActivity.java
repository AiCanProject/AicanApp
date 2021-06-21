package com.aican.aicanapp.specificactivities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.aican.aicanapp.R;
import com.aican.aicanapp.adapters.PumpViewPagerAdapter;
import com.aican.aicanapp.fragments.pump.DoseFragment;
import com.aican.aicanapp.fragments.pump.PumpFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class PumpActivity extends AppCompatActivity {

    ViewPager2 viewPager;
    TabLayout tabLayout;

    ArrayList<Fragment> fragments;
    PumpViewPagerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_Light);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pump);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        fragments = new ArrayList<>();
        fragments.add(new DoseFragment());
        fragments.add(new PumpFragment());
        adapter = new PumpViewPagerAdapter(getSupportFragmentManager(), getLifecycle(),fragments);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position)->{
            if(position==0){
                tab.setText("Dose");
            }else{
                tab.setText("Pump");
            }
        }).attach();

    }

}