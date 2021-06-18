package com.aican.aicanapp.specificactivities;

import androidx.annotation.AttrRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.aican.aicanapp.R;
import com.aican.aicanapp.ph.PhView;
import com.aican.aicanapp.tempController.ProgressLabelView;

import java.util.Objects;

import static android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;

public class PhCalibrateActivity extends AppCompatActivity {

    PhView phView;
    ProgressLabelView phTextView, plvCoefficient;
    Button btnStart,btnNext;
    TextView tvTimer, tvCoefficient;
    Toolbar toolbar;

    int[] buffers = new int[]{2,4,7,9,11};
    int currentBuf = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_Light);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ph_calibrate);

        Window window = getWindow();
        window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.orange));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        phView = findViewById(R.id.phView);
        phTextView = findViewById(R.id.phTextView);
        btnStart = findViewById(R.id.startBtn);
        btnNext = findViewById(R.id.next_btn);
        tvTimer = findViewById(R.id.tvTimer);
        plvCoefficient = findViewById(R.id.plvCoefficient);
        tvCoefficient = findViewById(R.id.tvCoefficient);
        toolbar = findViewById(R.id.main_toolbar);

//        setSupportActionBar(toolbar);
//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        phTextView.setAnimationDuration(0);
        phTextView.setProgress(buffers[0]);
        phTextView.setAnimationDuration(800);
        plvCoefficient.setAnimationDuration(0);

        phView.setCurrentPh(buffers[0]);

        phTextView.setTextColor(getAttr(R.attr.primaryTextColor));
        plvCoefficient.setTextColor(getAttr(R.attr.primaryTextColor));

        btnStart.setOnClickListener(v->{
            btnStart.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryAlpha));
            btnStart.setEnabled(false);
            tvTimer.setVisibility(View.VISIBLE);
            CountDownTimer timer = new CountDownTimer(5000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    millisUntilFinished/=1000;
                    int min = (int)millisUntilFinished/60;
                    int sec = (int)millisUntilFinished%60;
                    String time = String.format("%02d:%02d", min, sec);
                    tvTimer.setText(time);
                }

                @Override
                public void onFinish() {
                    runOnUiThread(()->{
                        tvTimer.setVisibility(View.INVISIBLE);

                        if(currentBuf==buffers.length-1){
                            btnNext.setText("Done");
                        }
                        btnNext.setVisibility(View.VISIBLE);

                        tvCoefficient.setVisibility(View.VISIBLE);
                        plvCoefficient.setProgress(10);
                        plvCoefficient.setVisibility(View.VISIBLE);
                    });
                }
            };
            timer.start();
        });

        btnNext.setOnClickListener(v->{
            if(currentBuf==buffers.length-1){
                Intent intent = new Intent(this, PhActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return;
            }
            btnNext.setVisibility(View.INVISIBLE);
            currentBuf+=1;

            btnStart.setBackgroundColor(ContextCompat.getColor(PhCalibrateActivity.this, R.color.colorPrimary));
            btnStart.setEnabled(true);

            phView.moveTo(buffers[currentBuf]);
            phTextView.setProgress(buffers[currentBuf]);

            tvCoefficient.setVisibility(View.INVISIBLE);
            plvCoefficient.setVisibility(View.INVISIBLE);

        });
    }

    private int getAttr(@AttrRes int attrRes){
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attrRes,typedValue,true);

        return typedValue.data;
    }
}