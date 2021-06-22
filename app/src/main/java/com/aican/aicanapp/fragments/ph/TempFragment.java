package com.aican.aicanapp.fragments.ph;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.R;
import com.aican.aicanapp.ph.TempView;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.specificactivities.TemperatureActivity;
import com.aican.aicanapp.tempController.ProgressLabelView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;

public class TempFragment extends Fragment {

    TempView tempView;
    TextView tvTempCurr, tvTempNext;
//    ProgressLabelView plvTemp;
    DatabaseReference deviceRef;
    LineChart lineChart;

    int temp=0;

    FillGraphDataTask fillGraphDataTask;

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
        lineChart = view.findViewById(R.id.line_chart);
        tvTempCurr = view.findViewById(R.id.tvTempCurr);
        tvTempNext = view.findViewById(R.id.tvTempNext);

        tempView.setTemp(10);

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);

        setupGraph();
        setupListeners();
    }

    private void setupGraph() {
        LineDataSet lineDataSet = new LineDataSet(new ArrayList<>(),"Temperature");

        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleRadius(4);
        lineDataSet.setValueTextSize(10);


        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);

        LineData data = new LineData(dataSets);
        lineChart.setData(data);
        lineChart.invalidate();

        lineChart.setDrawGridBackground(true);
        lineChart.setDrawBorders(true);

        Description d = new Description();
        d.setText("Temperature Graph");
        lineChart.setDescription(d);
        data.setValueFormatter(new MyValueFormatter());
    }

    @Override
    public void onResume() {
        super.onResume();
        fillGraphDataTask= new FillGraphDataTask();
        fillGraphDataTask.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        fillGraphDataTask.stopRunning();
        fillGraphDataTask.cancel(true);
    }

    private void updateTemp(int temp){
        String newText = String.format(Locale.UK,"%d°C",temp);
        tvTempNext.setText(newText);

        if(getContext()!=null){
            Animation fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out);
            Animation slideInBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom);

            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    tvTempCurr.setVisibility(View.INVISIBLE);
                    TextView t = tvTempCurr;
                    tvTempCurr = tvTempNext;
                    tvTempNext = t;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            tvTempCurr.startAnimation(fadeOut);
            tvTempNext.setVisibility(View.VISIBLE);
            tvTempNext.startAnimation(slideInBottom);
        }else{
            tvTempCurr.setText(newText);
        }
    }


    private void setupListeners() {
        deviceRef.child("Data").child("TEMP_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                int temp = snapshot.getValue(Integer.class);
                tempView.setTemp(temp);
                updateTemp(temp);
                TempFragment.this.temp = temp;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private int getAttr(@AttrRes int attrRes){
        TypedValue typedValue = new TypedValue();
        requireActivity().getTheme().resolveAttribute(attrRes,typedValue,true);

        return typedValue.data;
    }

    private class MyValueFormatter extends ValueFormatter {

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return value + "℃";
        }
    }

    class FillGraphDataTask extends AsyncTask<Void, Void, Void>{

        Long start;
        boolean running=true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            start = System.currentTimeMillis();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            while (running){
                publishProgress();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            long seconds = (System.currentTimeMillis()-start)/1000;
            LineData data = lineChart.getData();
            data.addEntry(new Entry(seconds, temp), 0);
            if(data.getXMax()-data.getXMin()>60){
                lineChart.getXAxis().setAxisMinimum(data.getXMax()-60);
            }
            lineChart.notifyDataSetChanged();
            data.notifyDataChanged();
            lineChart.invalidate();
        }

        void stopRunning(){
            running=false;
        }
    }
}
