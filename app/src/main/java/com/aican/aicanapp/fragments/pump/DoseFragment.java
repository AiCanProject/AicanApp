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

public class DoseFragment extends Fragment {

    VerticalSlider volController, speedController;
    LineChart lineChart;
    Button calibrateBtn;
    ShapeableImageView startBtn;
    TextView tvStart;
    SwitchCompat switchDir;
    boolean isStarted = false;

    DatabaseReference deviceRef = null;

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

        volController.setProgress(0);
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
                if (mode == 0) {
                    setupListeners();
                } else {
                    isStarted = false;
                    refreshStartBtnUI();
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

        deviceRef.child("UI").child("MODE").child("DOSE").child("DIR").get().addOnSuccessListener(snapshot -> {
            Integer dir = snapshot.getValue(Integer.class);
            if (dir == null) return;

            switchDir.setChecked(dir == 0);
        });

        deviceRef.child("UI").child("MODE").child("DOSE").child("SPEED").get().addOnSuccessListener(snapshot -> {
            Integer speed = snapshot.getValue(Integer.class);
            if (speed == null) return;

            speedController.setProgress(speed);
        });

        deviceRef.child("UI").child("MODE").child("DOSE").child("VOL").get().addOnSuccessListener(snapshot -> {
            Integer vol = snapshot.getValue(Integer.class);
            if (vol == null) return;

            volController.setProgress(vol);
        });

        speedController.setOnProgressChangeListener(progress -> {
            deviceRef.child("UI").child("MODE").child("DOSE").child("SPEED").setValue(progress);
        });

        volController.setOnProgressChangeListener(progress -> {
            deviceRef.child("UI").child("MODE").child("DOSE").child("VOL").setValue(progress);
        });

        switchDir.setOnCheckedChangeListener((v, isChecked) -> {
            deviceRef.child("UI").child("MODE").child("DOSE").child("DIR").setValue(isChecked ? 0 : 1);
        });
    }

    private void refreshStartBtnUI() {
        if (isStarted) {
            tvStart.setText("STOP");
            startBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red));
        } else {
            tvStart.setText("START");
            startBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        }
    }

}
