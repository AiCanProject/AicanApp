package com.aican.aicanappnoncfr.fragments.pump;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aican.aicanappnoncfr.R;
import com.aican.aicanappnoncfr.pumpController.HorizontalSlider;
import com.aican.aicanappnoncfr.pumpController.VerticalSlider;
import com.aican.aicanappnoncfr.specificactivities.PumpActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PCalibFragment extends Fragment {

    LinearLayout calibLayout;
    Button speedSet, calibBtn, saveBtn;
    SwitchCompat switchClock, switchAntiClock;
    HorizontalSlider slider;
    RelativeLayout saveLayout;
    VerticalSlider speedController;
    TextView tvTimer;
    DatabaseReference deviceRef;
    String time, date;

    public PCalibFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_p_calib, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        saveLayout = view.findViewById(R.id.saveLayout);
        calibLayout = view.findViewById(R.id.calibLayout);
        calibBtn = view.findViewById(R.id.calibBtn);
        saveBtn = view.findViewById(R.id.savBtn);
        slider = view.findViewById(R.id.slider);
        speedSet = view.findViewById(R.id.speedSet);
        switchClock = view.findViewById(R.id.switch1);
        switchAntiClock = view.findViewById(R.id.switch2);
        speedController = view.findViewById(R.id.speedController);
        tvTimer = view.findViewById(R.id.tvTimer);

        calibBtn.setOnClickListener(v->{
            calibLayout.setVisibility(View.GONE);
            saveLayout.setVisibility(View.VISIBLE);
            calibrate();
            deviceRef.child("UI").child("Start").setValue(1);
        });

        saveBtn.setOnClickListener(v -> {
            int calVal = slider.getProgress();
            deviceRef.child("UI").child("Cal").setValue(calVal);
            //onBackPressed();
        });

        SharedPreferences sh = getContext().getSharedPreferences("Pump_Calib", MODE_PRIVATE);
        SharedPreferences.Editor getTime = sh.edit();
        //String roleSuper = Source.userName;
        getTime.putString("CalibTime", time);
        getTime.putString("CalibDate", date);

        getTime.commit();

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PumpActivity.DEVICE_ID)).getReference()
                .child("P_PUMP").child(PumpActivity.DEVICE_ID);

        SetUpListener();
    }

    private void calibrate() {

        time = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());
        date = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(new Date());

        slider.setDisabled(true);
        CountDownTimer timer = new CountDownTimer(45000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                millisUntilFinished /= 1000;
                int min = (int) millisUntilFinished / 60;
                int sec = (int) millisUntilFinished % 60;
                String time = String.format(Locale.UK, "%02d:%02d", min, sec);
                tvTimer.setText(time);
            }

            final Handler handler = new Handler();
            Runnable runnable;

            @Override
            public void onFinish() {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        slider.setDisabled(false);
                        //saveBtn.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                        saveBtn.setText("Save");
                    }
                };
                runnable.run();
            }
        };
        //deviceRef.child("UI").child("CAL").child("CAL").setValue(0);
        timer.start();
    }



    private void SetUpListener() {

        deviceRef.child("UI").child("Speed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int spd = snapshot.getValue(Integer.class);
                speedController.setProgress(spd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
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

    }
}