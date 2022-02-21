package com.aican.aicanapp.specificactivities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.adapters.ViewPagerAdapter;
import com.aican.aicanapp.fragments.ph.EcFragment;
import com.aican.aicanapp.fragments.ph.PhCalibFragment;
import com.aican.aicanapp.fragments.ph.PhFragment;
import com.aican.aicanapp.fragments.ph.TdsFragment;
import com.aican.aicanapp.fragments.ph.TempFragment;
import com.aican.aicanapp.fragments.ph.phAlarmFragment;
import com.aican.aicanapp.fragments.ph.phLogFragment;
import com.aican.aicanapp.tempController.ProgressLabelView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class PhActivity extends AppCompatActivity implements View.OnClickListener{

    TextView ph, calibrate,log, alarm, tabItemPh, tabItemCalib;
    TextView timerTextView;
    Button calibrateBtn;
    ProgressLabelView currentPh, phChange;
    AppCompatSeekBar phSeekBar;

    ImageView setting;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    Toolbar toolbar;

    ArrayList<Fragment> fragments;
    ViewPagerAdapter phViewPagerAdapter;

    PhFragment phFragment = new PhFragment();
    PhCalibFragment phCalibFragment = new PhCalibFragment();
    phLogFragment phLogFragment = new phLogFragment();
    phAlarmFragment phAlarmFragment = new phAlarmFragment();

    public static String DEVICE_ID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        setTheme(R.style.AppTheme_Light);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ph);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        DEVICE_ID = getIntent().getStringExtra(Dashboard.KEY_DEVICE_ID);
        if(DEVICE_ID==null){
            throw new RuntimeException();
        }

        loadFragments(phFragment);
        ph = findViewById(R.id.item1);
        calibrate = findViewById(R.id.item2);
        log = findViewById(R.id.item3);
        alarm= findViewById(R.id.item4);
        tabItemPh = findViewById(R.id.tabItemP);
        tabItemCalib = findViewById(R.id.select2);

        setting = findViewById(R.id.settings);
        ph.setOnClickListener(this);
        calibrate.setOnClickListener(this);
        log.setOnClickListener(this);
        alarm.setOnClickListener(this);

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PhActivity.this, Dashboard.class);
                startActivity(intent);
            }
        });


        //tabLayout = findViewById(R.id.tabLayout);
        //viewPager = findViewById(R.id.viewPager);

        //fragments = new ArrayList<>();
        //fragments.add(new PhFragment());
        //fragments.add(new TempFragment());
        //fragments.add(new EcFragment());
        //fragments.add(new TdsFragment());
        //phViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), fragments);
        //viewPager.setAdapter(phViewPagerAdapter);

        /*new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("ph");
                    break;
               // case 1:
                 //   tab.setText("temp");
                   // break;
               // case 2:
                 //   tab.setText("ec");
                   // break;
                //case 3:
                  //  tab.setText("tds");
                   // break;
            }
        }).attach();
*/

        //PhFragment fragment = new PhFragment();
        //getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView,fragment).commit();

//        timerTextView = findViewById(R.id.countDownTv);
//        calibrateBtn = findViewById(R.id.calibrateBtn);
//        calibrateBtn.setClickable(false);
//        currentPh = findViewById(R.id.currentPhTextView);
//        phChange = findViewById(R.id.phTextView);
//        phSeekBar = findViewById(R.id.curveSeekView);
//
//        calibrateBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                currentPh.setProgress(phSeekBar.getProgress());
//            }
//        });
//
//        phSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                phChange.setProgress(seekBar.getProgress());
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                calibrateBtn.setClickable(false);
//                long duration = TimeUnit.MINUTES.toMillis(2);
//                new CountDownTimer(duration,1000){
//
//                    @Override
//                    public void onTick(long l) {
//                        String prefix = "Change the Ph in ";
//                        String sDuration = prefix + String.format(Locale.ENGLISH,"%02d : %02d",TimeUnit.MILLISECONDS.toMinutes(l),TimeUnit.MILLISECONDS.toSeconds(l)-TimeUnit.MINUTES.toSeconds((TimeUnit.MILLISECONDS.toMinutes(l))));
//
//                        timerTextView.setText(sDuration);
//                    }
//
//                    @Override
//                    public void onFinish() {
//                        timerTextView.setVisibility(View.GONE);
//                        calibrateBtn.setClickable(true);
//                        Toast.makeText(PhActivity.this, "Timer Finished", Toast.LENGTH_SHORT).show();
//                    }
//                }.start();
//
//            }
//        });
//
////        timerTextView.setTextColor(getAttr(R.attr.primaryTextColor));
//        long duration = TimeUnit.MINUTES.toMillis(2);
//
//        new CountDownTimer(duration,1000){
//
//            @Override
//            public void onTick(long l) {
//                String prefix = "Change the Ph in ";
//                String sDuration = prefix + String.format(Locale.ENGLISH,"%02d : %02d",TimeUnit.MILLISECONDS.toMinutes(l),TimeUnit.MILLISECONDS.toSeconds(l)-TimeUnit.MINUTES.toSeconds((TimeUnit.MILLISECONDS.toMinutes(l))));
//
//                timerTextView.setText(sDuration);
//            }
//
//            @Override
//            public void onFinish() {
//                timerTextView.setVisibility(View.GONE);
//                calibrateBtn.setClickable(true);
//                Toast.makeText(PhActivity.this, "Timer Finished", Toast.LENGTH_SHORT).show();
//            }
//        }.start();
    }
//    private int getAttr(@AttrRes int attrRes){
//        TypedValue typedValue = new TypedValue();
//        getTheme().resolveAttribute(attrRes,typedValue,true);
//
//        return typedValue.data;
//    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.item1){
            tabItemPh.animate().x(0).setDuration(100);
            loadFragments(phFragment);
            ph.setTextColor(Color.WHITE);
            calibrate.setTextColor(Color.parseColor("#FF24003A"));
            log.setTextColor(Color.parseColor("#FF24003A"));
            alarm.setTextColor(Color.parseColor("#FF24003A"));
        } else if (view.getId() == R.id.item2){

            loadFragments(phCalibFragment);
            calibrate.setTextColor(Color.WHITE);
            ph.setTextColor(Color.parseColor("#FF24003A"));
            log.setTextColor(Color.parseColor("#FF24003A"));
            alarm.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibrate.getWidth();
            tabItemPh.animate().x(size).setDuration(100);

        } else if (view.getId() == R.id.item3){
            loadFragments(phLogFragment);
            log.setTextColor(Color.WHITE);
            ph.setTextColor(Color.parseColor("#FF24003A"));
            calibrate.setTextColor(Color.parseColor("#FF24003A"));
            alarm.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibrate.getWidth()*2;
            tabItemPh.animate().x(size).setDuration(100);



        } else if(view.getId() == R.id.item4) {
            loadFragments(phAlarmFragment);

            alarm.setTextColor(Color.WHITE);
            ph.setTextColor(Color.parseColor("#FF24003A"));
            calibrate.setTextColor(Color.parseColor("#FF24003A"));
            log.setTextColor(Color.parseColor("#FF24003A"));
            int size = calibrate.getWidth()*3;
            tabItemPh.animate().x(size).setDuration(100);


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

        return true;
    }
    return false;
}


}