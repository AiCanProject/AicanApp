package com.aican.aicanapp.fragments.ph;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.R;
import com.aican.aicanapp.specificactivities.EcTdsCalibrateActivity;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Random;


public class TdsFragment extends Fragment {

    TextView tvEcCurr, tvEcNext;
    Button btnCalibrate;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ph_tds, container, false);

    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEcCurr = view.findViewById(R.id.tvTds);
        tvEcNext = view.findViewById(R.id.tvTdsNext);
        btnCalibrate = view.findViewById(R.id.calibrateBtn);

        btnCalibrate.setOnClickListener(v->{
            Intent intent = new Intent(requireContext(), EcTdsCalibrateActivity.class);
            startActivity(intent);
        });

        CountDownTimer t = new CountDownTimer(15000,3000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateValue(new Random().nextInt(10000));
            }

            @Override
            public void onFinish() {

            }
        };
        t.start();
    }
    private void updateValue(Integer value){
        String newText = String.format(Locale.UK,"%04d",value);
        tvEcNext.setText(newText);

        Animation fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out);
        Animation slideInBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tvEcCurr.setVisibility(View.INVISIBLE);
                TextView t = tvEcCurr;
                tvEcCurr = tvEcNext;
                tvEcNext = t;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        tvEcCurr.startAnimation(fadeOut);
        tvEcNext.setVisibility(View.VISIBLE);
        tvEcNext.startAnimation(slideInBottom);
    }
}
