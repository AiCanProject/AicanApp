package com.aican.aicanapp.fragments.ph;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.R;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Random;

public class EcFragment extends Fragment {

    TextView tvEcCurr, tvEcNext;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ph_ec, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEcCurr = view.findViewById(R.id.tvEc);
        tvEcNext = view.findViewById(R.id.tvEcNext);

        CountDownTimer t = new CountDownTimer(15000,3000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateValue(new Random().nextFloat()*100);
            }

            @Override
            public void onFinish() {

            }
        };
        t.start();
    }

    private void updateValue(Float value){
        String newText = String.format(Locale.UK,"%02.2f",value);
        if(value<10){
            newText = "0"+newText;
        }
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
