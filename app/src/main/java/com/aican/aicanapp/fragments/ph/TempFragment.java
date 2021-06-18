package com.aican.aicanapp.fragments.ph;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.R;
import com.aican.aicanapp.ph.TempView;
import com.aican.aicanapp.tempController.ProgressLabelView;

import org.jetbrains.annotations.NotNull;

public class TempFragment extends Fragment {

    TempView tempView;
    ProgressLabelView plvTemp;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ph_temp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tempView = view.findViewById(R.id.tempView);
        plvTemp = view.findViewById(R.id.plvTemp);

        plvTemp.setAnimationDuration(0);
        plvTemp.setProgress(10);
        plvTemp.setAnimationDuration(800);

        tempView.setTemp(10);

        plvTemp.setTextColor(getAttr(R.attr.primaryTextColor));

    }

    private int getAttr(@AttrRes int attrRes){
        TypedValue typedValue = new TypedValue();
        requireActivity().getTheme().resolveAttribute(attrRes,typedValue,true);

        return typedValue.data;
    }
}
