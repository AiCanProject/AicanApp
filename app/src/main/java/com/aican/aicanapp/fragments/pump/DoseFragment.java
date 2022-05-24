package com.aican.aicanapp.fragments.pump;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
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

import com.aican.aicanapp.R;
import com.aican.aicanapp.pumpController.ProgressBar;
import com.aican.aicanapp.pumpController.VerticalSlider;
import com.aican.aicanapp.specificactivities.PumpActivity;
import com.github.mikephil.charting.charts.LineChart;
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
    Button calibrateBtn, speedSet, volumeSet, startBtn;
    //ShapeableImageView startBtn;
    TextView tvStart, date, time;
    SwitchCompat switchClock, switchAntiClock;
    boolean isStarted = false;
    Integer speed;
    Integer vol;
    TextView appMode;


    ProgressBar progressBar;
    DatabaseReference deviceRef = null;
    RelativeLayout startLayout, stopLayout;
    Button btnStop;
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_pump_dose,
                container,
                false
        );
    }

    CountDownTimer timer;

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        speedSet = view.findViewById(R.id.speedSet);
        volumeSet =  view.findViewById(R.id.volumeSet);
        date = view.findViewById(R.id.date);
        time = view.findViewById(R.id.time);
        switchClock = view.findViewById(R.id.switch1);
        switchAntiClock = view.findViewById(R.id.switch2);
        startBtn = view.findViewById(R.id.startBtn);
        volController = view.findViewById(R.id.volumeController);
        speedController = view.findViewById(R.id.speedController);
        switchClock = view.findViewById(R.id.switch1);
        switchAntiClock = view.findViewById(R.id.switch2);
        appMode = view.findViewById(R.id.appMode);
        startBtn = view.findViewById(R.id.startBtn);

        //lineChart = view.findViewById(R.id.line_chart);
        //calibrateBtn = view.findViewById(R.id.calibrateBtn);
        //tvStart = view.findViewById(R.id.tvStart);
        //switchDir = view.findViewById(R.id.switchDir);
        //progressBar = view.findViewById(R.id.progressBar);
        //startLayout = view.findViewById(R.id.startLayout);
        //stopLayout = view.findViewById(R.id.stopLayout);
        //btnStop = view.findViewById(R.id.btnStop);

        volController.setProgress(0);
        speedController.setProgress(0);
        speedController.setMaxRange(150);


        /*calibrateBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PumpCalibrateActivity.class);
            intent.putExtra(Dashboard.KEY_DEVICE_ID, PumpActivity.DEVICE_ID);
            startActivity(intent);
        });
*/

        startBtn.setOnClickListener(v -> {
            isStarted = true;
            //showProgressBarLayout();
            //startProgressBar();
            deviceRef.child("UI").child("Start").setValue(1);
        });


        speedSet.setOnClickListener( v ->{
            int spee= speedController.getProgress();
            deviceRef.child("UI").child("Speed").setValue(spee);

        });

        volumeSet.setOnClickListener(v ->{
            volController.setProgress(vol);
        });
       /* btnStop.setOnClickListener(v -> {
            isStarted = false;
            hideProgressBarLayout();
            stopProgressBar();
        });
        */

        SharedPreferences sh = getContext().getSharedPreferences("Pump_Calib", MODE_PRIVATE);
        String shTime = sh.getString("CalibTime", "N/A");
        String shDate = sh.getString("CalibDate", "N/A");
        time.setText(shDate);
        date.setText(shTime);

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PumpActivity.DEVICE_ID)).getReference()
                .child("P_PUMP").child(PumpActivity.DEVICE_ID);

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

        //startLayout.startAnimation(slideOutLeft);
        //stopLayout.setVisibility(View.VISIBLE);
        //progressBar.setVisibility(View.VISIBLE);
        //stopLayout.startAnimation(slideInRight);

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
                deviceRef.child("UI").child("Start").setValue(PumpActivity.STATUS_DOSE_COMPLETED);
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
                    switchClock.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void setupListeners() {

        deviceRef.child("UI").child("Direction").get().addOnSuccessListener(snapshot -> {
            Integer dir = snapshot.getValue(Integer.class);
            if (dir == null) return;
            switchClock.setChecked(dir == 0);
            switchAntiClock.setChecked(dir == 1);

            //switchDir.setChecked(dir == 0);
        });

        deviceRef.child("UI").child("Direction").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer dir = snapshot.getValue(Integer.class);
                if (dir == null) return;
                if (dir == 0){
                    switchClock.setChecked(true);
                }else {
                    switchAntiClock.setChecked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        deviceRef.child("UI").child("Speed").get().addOnSuccessListener(snapshot -> {
            speed = snapshot.getValue(Integer.class);
            if (speed == null) return;
            speedController.setProgress(speed);
        });

        deviceRef.child("UI").child("Volume").get().addOnSuccessListener(snapshot -> {
            vol = snapshot.getValue(Integer.class);
            if (vol == null) return;
            volController.getProgress();
            volController.setProgress(vol);
        });

        deviceRef.child("UI").child("Start").get().addOnSuccessListener(snapshot -> {
            Integer status = snapshot.getValue(Integer.class);
            if (status == null) return;

            if (status == PumpActivity.STATUS_DOSE) {
                isStarted = true;
              //  showProgressBarLayout();
                //progressBar.setVisibility(View.INVISIBLE);
            }
        });

        speedController.setOnProgressChangeListener(progress -> {
            deviceRef.child("UI").child("Speed").setValue(progress);
        });

        volController.setOnProgressChangeListener(progress -> {
            deviceRef.child("UI").child("Volume").setValue(progress);
        });

       switchClock.setOnCheckedChangeListener((v, isChecked) -> {
           if (isChecked){
               switchAntiClock.setChecked(false);
           }
            deviceRef.child("UI").child("Direction").setValue(isChecked ? 0 : 1);
        });

        switchAntiClock.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked){
                switchClock.setChecked(false);
            }
            deviceRef.child("UI").child("Direction").setValue(isChecked ? 1 : 0);
        });

        deviceRef.child("UI").child("App").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int app = snapshot.getValue(Integer.class);
                if (app == 0 ){
                    appMode.setText("App Mode - OFF");
                    appMode.setTextColor(Color.RED);
                }else if (app == 1 ){
                    appMode.setText("App Mode - ON");
                    appMode.setTextColor(Color.GREEN);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
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
