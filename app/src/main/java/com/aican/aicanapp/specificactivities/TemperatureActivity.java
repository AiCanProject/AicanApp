package com.aican.aicanapp.specificactivities;

import androidx.annotation.AttrRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.text.HtmlCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aican.aicanapp.R;
import com.aican.aicanapp.tempController.CurveSeekView;
import com.aican.aicanapp.tempController.ProgressLabelView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;

public class TemperatureActivity extends AppCompatActivity {

    private boolean light = true;
    private float progress = 150f;
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        setTheme(R.style.AppTheme_Light);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temp_activity);

        String id = getIntent().getStringExtra("deviceId");

        lineChart = findViewById(R.id.line_chart);
        CurveSeekView curveSeekView = findViewById(R.id.curveSeekView);
        ProgressLabelView tempTextView = findViewById(R.id.humidityTextView);
        if(light) setLightStatusBar(curveSeekView);
        ProgressLabelView currTemp = findViewById(R.id.temperatureTextView);
        Button changeBtn = findViewById(R.id.themButton);

        currTemp.setProgress(Math.round(progress));
        tempTextView.setAnimationDuration(0);
        curveSeekView.setProgress(progress);
        tempTextView.setProgress((int)progress);
        tempTextView.setAnimationDuration(800);

        currTemp.setTextColor(getAttr(R.attr.primaryTextColor));
        tempTextView.setTextColor(getAttr(R.attr.primaryTextColor));
        curveSeekView.setBackgroundShadowColor(getAttr(R.attr.backgroundColor1));
        curveSeekView.setSelectedLabelColor(getAttr(R.attr.selectedLabelColor));
        curveSeekView.setLabelColor(getAttr(R.attr.labelColor));
        curveSeekView.setScaleColor(getAttr(R.attr.scaleColor));
        curveSeekView.setSliderColor(getAttr(R.attr.sliderColor));
        curveSeekView.setSliderIconColor(getAttr(R.attr.sliderIconColor));
        curveSeekView.setFirstGradientColor(getAttr(R.attr.firstGradientColor));
        curveSeekView.setSecondGradientColor(getAttr(R.attr.secondGradientColor));
        changeBtn.setBackgroundColor(getAttr(R.attr.warningTextColor));


        curveSeekView.setOnProgressChangeListener(new Function1<Float, Unit>() {
            @Override
            public Unit invoke(Float aFloat) {
                progress = aFloat;
                tempTextView.setProgress(Math.round(aFloat));
                Log.e("progress",Integer.toString(Math.round(aFloat)));
                return null;
            }
        });

        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currTemp.setProgress(Math.round(curveSeekView.getProgress()));

            }
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

    private void setLightStatusBar(CurveSeekView curveSeekView) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int flags = curveSeekView.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            curveSeekView.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    private int getAttr(@AttrRes int attrRes){
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attrRes,typedValue,true);

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
            return value + "℃";
        }
    }

}