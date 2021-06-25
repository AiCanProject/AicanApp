package com.aican.aicanapp.fragments.pump;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.R;
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

public class PumpFragment extends Fragment {

    VerticalSlider speedController;
    LineChart lineChart;
    SwitchCompat switchDir;
    Button calibrateBtn;
    ShapeableImageView startBtn;
    TextView tvStart;
    boolean isStarted = false;

    DatabaseReference deviceRef = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pump, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        speedController = view.findViewById(R.id.speedController);
        lineChart = view.findViewById(R.id.line_chart);
        calibrateBtn = view.findViewById(R.id.calibrateBtn);
        startBtn = view.findViewById(R.id.ivStartBtn);
        tvStart = view.findViewById(R.id.tvStart);
        switchDir = view.findViewById(R.id.switchDir);


        speedController.setProgress(0);
        calibrateBtn.setOnClickListener(v -> {
            startActivity(
                    new Intent(requireContext(), PumpCalibrateActivity.class)
            );
        });

        startBtn.setOnClickListener(v -> {
            isStarted = !isStarted;
            refreshStartBtnUI();
        });
        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PumpActivity.DEVICE_ID)).getReference()
                .child("P_PUMP").child(PumpActivity.DEVICE_ID);

        checkModeAndSetListeners();
    }

    private void checkModeAndSetListeners() {
        deviceRef.child("UI").child("MODE").child("MODE_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Integer mode = snapshot.getValue(Integer.class);
                if (mode == null) {
                    return;
                }
                if (mode == 1) {
                    setupListeners();
                } else {
                    isStarted = false;
                    refreshStartBtnUI();
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

        deviceRef.child("UI").child("MODE").child("PUMP").child("DIR").get().addOnSuccessListener(snapshot -> {
            Integer dir = snapshot.getValue(Integer.class);
            if (dir == null) return;

            switchDir.setChecked(dir == 0);
        });

        deviceRef.child("UI").child("MODE").child("PUMP").child("SPEED").get().addOnSuccessListener(snapshot -> {
            Integer speed = snapshot.getValue(Integer.class);
            if (speed == null) return;

            speedController.setProgress(speed);
        });

        speedController.setOnProgressChangeListener(progress -> {
            deviceRef.child("UI").child("MODE").child("PUMP").child("SPEED").setValue(progress);
        });

        switchDir.setOnCheckedChangeListener((v, isChecked) -> {
            deviceRef.child("UI").child("MODE").child("PUMP").child("DIR").setValue(isChecked ? 0 : 1);
        });
    }

    private void refreshStartBtnUI() {
        if(isStarted){
            tvStart.setText("STOP");
            startBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red));
        }else{
            tvStart.setText("START");
            startBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        }
    }
}
