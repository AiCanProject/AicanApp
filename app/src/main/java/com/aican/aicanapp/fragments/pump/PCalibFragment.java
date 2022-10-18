package com.aican.aicanapp.fragments.pump;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.dataClasses.BufferData;
import com.aican.aicanapp.pumpController.HorizontalSlider;
import com.aican.aicanapp.pumpController.VerticalSlider;
import com.aican.aicanapp.specificactivities.PumpActivity;
import com.aican.aicanapp.specificactivities.PumpCalibrateActivity;
import com.aican.aicanapp.utils.FloatSeekBar;
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
    Button speedSet, calibBtn, saveBtn, setCalVal;
    SwitchCompat switchClock, switchAntiClock;
    SeekBar slider;
    LinearLayout saveLayout;
    TextView tvTimer, speedText;
    EditText calVal;
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
        tvTimer = view.findViewById(R.id.tvTimer);
        setCalVal = view.findViewById(R.id.setCalVal);
        calVal = view.findViewById(R.id.calVal);
        speedText = view.findViewById(R.id.speedText);

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PumpActivity.DEVICE_ID)).getReference()
                .child("P_PUMP").child(PumpActivity.DEVICE_ID);

        deviceRef.child("UI").child("Mode").setValue(0);

        calibBtn.setOnClickListener(v -> {
            calibLayout.setVisibility(View.GONE);
            saveLayout.setVisibility(View.VISIBLE);
            tvTimer.setVisibility(View.VISIBLE);
            calibrate();
            deviceRef.child("UI").child("Start").setValue(1);
        });

        saveBtn.setOnClickListener(v -> {
            float calVal1 = Float.parseFloat(calVal.getText().toString());
            deviceRef.child("UI").child("Cal").setValue(calVal1);

            //onBackPressed();
        });

        deviceRef.child("UI").child("Cal").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Float data = snapshot.getValue(Float.class);
                    calVal.setText(String.valueOf(data));
                    float f = data;
                    int vi = (int) f;
                    slider.setProgress(vi);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        deviceRef.child("UI").child("Speed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float spd = snapshot.getValue(Float.class);
                speedText.setText("Speed : " + spd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        setCalVal.setOnClickListener(v -> {
            float f = Float.parseFloat(calVal.getText().toString());
            int vi = (int) f;
            slider.setProgress(vi);
        });

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                calVal.setText(String.valueOf((float) progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SharedPreferences sh = getContext().getSharedPreferences("Pump_Calib", MODE_PRIVATE);
        SharedPreferences.Editor getTime = sh.edit();
        //String roleSuper = Source.userName;
        getTime.putString("CalibTime", time);
        getTime.putString("CalibDate", date);

        getTime.commit();


        SetUpListener();
    }

    private void calibrate() {

        time = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());
        date = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(new Date());


        slider.setEnabled(false);
        calVal.setEnabled(false);
        setCalVal.setEnabled(false);
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
                        slider.setEnabled(true);
                        calVal.setEnabled(true);
                        setCalVal.setEnabled(true);
                        //saveBtn.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                        saveBtn.setText("Save");
                        deviceRef.child("UI").child("LAST_CALIB_DATE").setValue(time);
                        deviceRef.child("UI").child("LAST_CALIB_TIME").setValue(date);
                        tvTimer.setVisibility(View.GONE);

                    }
                };
                runnable.run();
            }
        };
        //deviceRef.child("UI").child("CAL").child("CAL").setValue(0);
        timer.start();
    }


    private void SetUpListener() {

//        deviceRef.child("UI").child("Speed").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                int spd = snapshot.getValue(Integer.class);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });


        deviceRef.child("UI").child("Direction").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer dir = snapshot.getValue(Integer.class);
                if (dir == null) return;

                if (dir == 0) {
                    switchClock.setChecked(true);
                } else {
                    switchAntiClock.setChecked(true);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        switchClock.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) {
                switchAntiClock.setChecked(false);
            }

            deviceRef.child("UI").child("Direction").setValue(isChecked ? 0 : 1);
        });

        switchAntiClock.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) {
                switchClock.setChecked(false);
            }

            deviceRef.child("UI").child("Direction").setValue(isChecked ? 1 : 0);
        });

    }
}