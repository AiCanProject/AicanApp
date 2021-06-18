package com.aican.aicanapp.fragments.ph;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.R;
import com.aican.aicanapp.ph.PhView;
import com.aican.aicanapp.specificactivities.PhCalibrateActivity;
import com.aican.aicanapp.tempController.ProgressLabelView;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class PhFragment extends Fragment {

    PhView phView;
    ProgressLabelView phTextView;
    Button calibrateBtn;


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ph_main,container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        phView = view.findViewById(R.id.phView);
        phTextView = view.findViewById(R.id.phTextView);
        calibrateBtn = view.findViewById(R.id.calibrateBtn);

        phTextView.setAnimationDuration(0);
        phTextView.setProgress(7);
        phTextView.setAnimationDuration(800);

        phView.setCurrentPh(7);

        phTextView.setTextColor(getAttr(R.attr.primaryTextColor));

        calibrateBtn.setOnClickListener(v->{
            Intent intent= new Intent(requireContext(), PhCalibrateActivity.class);
            startActivity(intent);
        });
    }

    private int getAttr(@AttrRes int attrRes){
        TypedValue typedValue = new TypedValue();
        requireActivity().getTheme().resolveAttribute(attrRes,typedValue,true);

        return typedValue.data;
    }
}
