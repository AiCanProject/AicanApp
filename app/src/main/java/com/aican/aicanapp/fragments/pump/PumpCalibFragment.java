package com.aican.aicanapp.fragments.pump;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.pumpController.HorizontalSlider;
import com.aican.aicanapp.pumpController.VerticalSlider;
import com.aican.aicanapp.specificactivities.PumpActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.triggertrap.seekarc.SeekArc;

public class PumpCalibFragment extends Fragment {

    SwitchCompat switchClock;
    SwitchCompat switchAntiClock;
    //VerticalSlider speedController;
    SeekArc seekArc;
    TextView seekArcText;
    Button speedSet, startBtn;
    TextView appMode, date, time;
    DatabaseReference deviceRef;
    boolean isStarted = false;

    public PumpCalibFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pump_calib, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        speedSet = view.findViewById(R.id.speedSet);
      //  speedController = view.findViewById(R.id.speedController);
        switchClock = view.findViewById(R.id.switch1);
        switchAntiClock = view.findViewById(R.id.switch2);
        appMode = view.findViewById(R.id.appMode);
        startBtn = view.findViewById(R.id.startCalib);
        date = view.findViewById(R.id.date);
        time = view.findViewById(R.id.time);
        seekArc = view.findViewById(R.id.seekArc);
        //seekArcText = view.findViewById(R.id.seekArcText);

        startBtn.setOnClickListener(v->{
            isStarted = !isStarted;
            refreshStartBtnUI();
            if (isStarted) {
                deviceRef.child("UI").child("Start").setValue(1);
            } else {
                deviceRef.child("UI").child("Start").setValue(0);
            }

        });


        speedSet.setOnClickListener(v->{
            int speed = seekArc.getProgress();
            deviceRef.child("UI").child("Speed").setValue(speed);
        });

        SharedPreferences sh = getContext().getSharedPreferences("Pump_Calib", MODE_PRIVATE);
        String shTime = sh.getString("CalibTime", "N/A");
        String shDate = sh.getString("CalibDate", "N/A");
        date.setText(shTime);
        time.setText(shDate);


        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PumpActivity.DEVICE_ID)).getReference()
                .child("P_PUMP").child(PumpActivity.DEVICE_ID);

        SetUpListener();
    }

    private void SetUpListener() {

        deviceRef.child("UI").child("Speed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int spd = snapshot.getValue(Integer.class);
                seekArc.setProgress(spd);
                // speedController.setProgress(spd);
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
    private void refreshStartBtnUI() {
        if (isStarted) {
            startBtn.setText("STOP");
            startBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red));
        } else {
            startBtn.setText("START");
            startBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        }
    }
}