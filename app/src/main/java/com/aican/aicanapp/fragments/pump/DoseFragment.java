package com.aican.aicanapp.fragments.pump;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.R;
import com.aican.aicanapp.pumpController.VerticalSlider;
import com.aican.aicanapp.specificactivities.PumpCalibrateActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.material.imageview.ShapeableImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DoseFragment extends Fragment {

    VerticalSlider volController, speedController;
    LineChart lineChart;
    Button calibrateBtn;
    ShapeableImageView startBtn;
    TextView tvStart;
    boolean isStarted = false;
    
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

        volController.setProgress(200);
        speedController.setProgress(250);

//        volController.setOnProgressChangeListener(progress -> {
////            newVol.setProgress(progress);
//        });
//        speedController.setOnProgressChangeListener(progress -> {
////            newSpeed.setProgress(progress);
//        });

        calibrateBtn.setOnClickListener(v->{
//            volController.setProgress(volController.getProgress());
//            speedController.setProgress(speedController.getProgress());
            startActivity(
                    new Intent(requireContext(), PumpCalibrateActivity.class)
            );
        });

        startBtn.setOnClickListener(v->{
            isStarted=!isStarted;
            refreshStartBtnUI();
        });

        //....................................................Graph............................................................................
        LineDataSet lineDataSet = new LineDataSet(dataPoints(),"Temperature 1");
        LineDataSet lineDataSet1 = new LineDataSet(dataPoints1(),"Temperature 2");

        lineDataSet1.setColors(Color.RED);
        lineDataSet1.setLineWidth(2);
        lineDataSet1.setCircleRadius(4);
        lineDataSet1.setValueTextSize(10);

        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleRadius(4);
        lineDataSet.setValueTextSize(10);


        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);
        dataSets.add(lineDataSet1);

        LineData data = new LineData(dataSets);
        lineChart.setData(data);
        lineChart.invalidate();

        lineChart.setDrawGridBackground(true);
        lineChart.setDrawBorders(true);

        data.setValueFormatter(new MyValueFormatter());

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

    private int getAttr(@AttrRes int attrRes){
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(attrRes,typedValue,true);

        return typedValue.data;
    }

    public ArrayList<Entry> dataPoints (){
        ArrayList<Entry> dataPointsList = new ArrayList<>();
        dataPointsList.add(new Entry(0,20));
        dataPointsList.add(new Entry(1,10));
        dataPointsList.add(new Entry(2,5));
        dataPointsList.add(new Entry(3,12));
        dataPointsList.add(new Entry(4,18));

        return dataPointsList;
    }

    public ArrayList<Entry> dataPoints1 (){
        ArrayList<Entry> dataPointsList = new ArrayList<>();
        dataPointsList.add(new Entry(0,5));
        dataPointsList.add(new Entry(1,16));
        dataPointsList.add(new Entry(2,5));
        dataPointsList.add(new Entry(3,5));
        dataPointsList.add(new Entry(4,1));
        dataPointsList.add(new Entry(5,11));

        return dataPointsList;
    }

    private class MyValueFormatter extends ValueFormatter {

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return value+"";
        }
    }
}
