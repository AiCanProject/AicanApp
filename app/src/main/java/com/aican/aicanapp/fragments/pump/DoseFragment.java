package com.aican.aicanapp.fragments.pump;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.pumpController.ProgressBar;
import com.aican.aicanapp.pumpController.VerticalSlider;
import com.aican.aicanapp.specificactivities.PumpActivity;
import com.aican.aicanapp.specificactivities.PumpCalibrateActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class DoseFragment extends Fragment {

    VerticalSlider volController, speedController;
    LineChart lineChart;
    Button calibrateBtn;
    ShapeableImageView startBtn;
    TextView tvStart, appMode;
    SwitchCompat switchDir;
    boolean isStarted = false;

    ProgressBar progressBar;
    DatabaseReference deviceRef = null;
    RelativeLayout startLayout, stopLayout;
    Button btnStop;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_dose,
                container,
                false
        );
    }

    CountDownTimer timer;

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        volController = view.findViewById(R.id.volController);
        speedController = view.findViewById(R.id.speedController);
        lineChart = view.findViewById(R.id.line_chart);
        calibrateBtn = view.findViewById(R.id.calibrateBtn);
        startBtn = view.findViewById(R.id.ivStartBtn);
        tvStart = view.findViewById(R.id.tvStart);
        switchDir = view.findViewById(R.id.switchDir);
        progressBar = view.findViewById(R.id.progressBar);
        startLayout = view.findViewById(R.id.startLayout);
        stopLayout = view.findViewById(R.id.stopLayout);
        btnStop = view.findViewById(R.id.btnStop);
        appMode = view.findViewById(R.id.appMode);

        volController.setProgress((int) 0.0);
        speedController.setProgress((int) 0.0);

        speedController.setMaxRange((int) 60.0);

        calibrateBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PumpCalibrateActivity.class);
            intent.putExtra(Dashboard.KEY_DEVICE_ID, PumpActivity.DEVICE_ID);
            startActivity(intent);

            deviceRef.child("UI").child("Mode").setValue(2);
        });

        startBtn.setOnClickListener(v -> {
            isStarted = true;
            showProgressBarLayout();
            startProgressBar();
            deviceRef.child("UI").child("Start").setValue(1);
        });

        btnStop.setOnClickListener(v -> {
            isStarted = false;
            hideProgressBarLayout();
            stopProgressBar();
            deviceRef.child("UI").child("Start").setValue(0);
        });

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PumpActivity.DEVICE_ID)).getReference()
                .child("P_PUMP").child(PumpActivity.DEVICE_ID);

        deviceRef.child("UI").child("Mode").setValue(0);

        checkModeAndSetListeners();
    }

    private void showProgressBarLayout() {
        Animation slideOutLeft = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_left);
        Animation slideInRight = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right);

        slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        startLayout.startAnimation(slideOutLeft);
        stopLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        stopLayout.startAnimation(slideInRight);

    }

    private void hideProgressBarLayout() {
        Animation slideOutRight = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_right);
        Animation slideInLeft = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_left);

        slideOutRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                stopLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        stopLayout.startAnimation(slideOutRight);
        startLayout.setVisibility(View.VISIBLE);
        startLayout.startAnimation(slideInLeft);
    }

    private void startProgressBar() {

        float speed = speedController.getProgress();
        float vol = speedController.getProgress();

        long duration = (int) (vol * 60 * 1000 / speed);

        timer = new CountDownTimer(duration, 1000) {
            int progress = 0;

            @Override
            public void onTick(long millisUntilFinished) {
                int prevProgress = progress;
                progress = (int) ((duration - millisUntilFinished) * 100 / duration);

                if (progress != prevProgress) {
                    progressBar.setProgress(progress);
                }
            }

            @Override
            public void onFinish() {
                progressBar.setProgress(100);
                deviceRef.child("UI").child("Start").setValue(0);
            }
        };
        timer.start();
    }

    private void stopProgressBar() {
        if (timer != null) {
            timer.cancel();
        }
        progressBar.setProgress(0);
        deviceRef.child("UI").child("Start").setValue(PumpActivity.STATUS_OFF);

    }

    private void checkModeAndSetListeners() {

        deviceRef.child("UI").child("Mode").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Integer mode = snapshot.getValue(Integer.class);
                if (mode == null) {
                    return;
                }
                if (mode == 0) {
                    setupListeners();
                } else {
                    isStarted = false;
//                    refreshStartBtnUI();
                    volController.setProgress(0);
                    speedController.setProgress(0);
                    switchDir.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void setupListeners() {


        deviceRef.child("UI").child("App").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int app = snapshot.getValue(Integer.class);
                if (app == 0) {
                    appMode.setText("App Mode is - OFF");
                    appMode.setTextColor(Color.RED);
                }else if (app == 1) {
                    appMode.setText("App Mode is - ON");
                    appMode.setTextColor(Color.GREEN);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        deviceRef.child("UI").child("Direction").get().addOnSuccessListener(snapshot -> {
            Integer dir = snapshot.getValue(Integer.class);
            if (dir == null) return;

            switchDir.setChecked(dir == 0);
        });

        deviceRef.child("UI").child("Speed").get().addOnSuccessListener(snapshot -> {
            Integer speed = snapshot.getValue(Integer.class);
            if (speed == null) return;
            speedController.setProgress(speed);
        });

        deviceRef.child("UI").child("Volume").get().addOnSuccessListener(snapshot -> {
            Integer vol = snapshot.getValue(Integer.class);
            if (vol == null) return;

            volController.setProgress(vol);
        });

        deviceRef.child("UI").child("Start").get().addOnSuccessListener(snapshot -> {
            Integer status = snapshot.getValue(Integer.class);
            if (status == null) return;

            if (status == PumpActivity.STATUS_DOSE) {
                isStarted = true;
                showProgressBarLayout();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

        speedController.setOnProgressChangeListener(progress -> {
            deviceRef.child("UI").child("Speed").setValue(progress);
        });

        volController.setOnProgressChangeListener(progress -> {
            deviceRef.child("UI").child("Volume").setValue(progress);
        });

        switchDir.setOnCheckedChangeListener((v, isChecked) -> {
            deviceRef.child("UI").child("Direction").setValue(isChecked ? 0 : 1);
        });
    }

//    private void refreshStartBtnUI() {
//        if (isStarted) {
//            tvStart.setText("STOP");
//            startBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red));
//        } else {
//            tvStart.setText("START");
//            startBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
//        }
//    }

}
