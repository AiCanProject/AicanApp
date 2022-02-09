package com.aican.aicanapp.specificactivities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.adapters.ViewPagerAdapter;
import com.aican.aicanapp.fragments.ph.EcFragment;
import com.aican.aicanapp.fragments.ph.PhFragment;
import com.aican.aicanapp.fragments.ph.TdsFragment;
import com.aican.aicanapp.fragments.ph.TempFragment;
import com.aican.aicanapp.tempController.ProgressLabelView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class PhActivity extends AppCompatActivity {

    TextView timerTextView;
    Button calibrateBtn;
    ProgressLabelView currentPh, phChange;
    AppCompatSeekBar phSeekBar;

    TabLayout tabLayout;
    ViewPager2 viewPager;

    ArrayList<Fragment> fragments;
    ViewPagerAdapter phViewPagerAdapter;

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

        PhFragment fragment = new PhFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView,fragment).commit();

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
}