package com.aican.aicanapp.specificactivities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aican.aicanapp.R;
import com.google.android.material.textfield.TextInputEditText;

import static android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;

public class EcTdsCalibrateActivity extends AppCompatActivity {

    TextInputEditText etBuffer,etTds;
    RelativeLayout startLayout, rlCoefficient;
    ImageView ivStartBtn;
    TextView tvTimer,tvCoefficient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_Light);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ec_tds_calibrate);

        Window window = getWindow();
        window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.orange));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        etBuffer = findViewById(R.id.etBuffer);
        etTds = findViewById(R.id.etTds);
        startLayout = findViewById(R.id.startLayout);
        rlCoefficient = findViewById(R.id.rlCoefficient);
        ivStartBtn = findViewById(R.id.ivStartBtn);
        tvTimer = findViewById(R.id.tvTimer);
        tvCoefficient = findViewById(R.id.tvCoefficient);


        ivStartBtn.setOnClickListener(v->{
            ivStartBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryAlpha));
            ivStartBtn.setEnabled(false);
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
                    startLayout.setVisibility(View.INVISIBLE);
                    rlCoefficient.setVisibility(View.VISIBLE);
                }
            };
            timer.start();
        });

    }
}