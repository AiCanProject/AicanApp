package com.aican.aicanapp.fragments.ph;

import android.content.Intent;
import android.os.AsyncTask;
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

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.specificactivities.EcTdsCalibrateActivity;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;


public class TdsFragment extends Fragment {

    TextView tvEcCurr, tvEcNext;
    Button btnCalibrate;

    DatabaseReference deviceRef;
    LineChart lineChart;

    FillGraphDataTask fillGraphDataTask;

    int tds=0;

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
        lineChart = view.findViewById(R.id.line_chart);

        btnCalibrate.setOnClickListener(v->{
            Intent intent = new Intent(requireContext(), EcTdsCalibrateActivity.class);
            intent.putExtra(Dashboard.KEY_DEVICE_ID, PhActivity.DEVICE_ID);
            startActivity(intent);
        });

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        setupGraph();
        setupListeners();
    }

    private void setupGraph() {
        LineDataSet lineDataSet = new LineDataSet(new ArrayList<>(),"TDS");

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
        d.setText("TDS Graph");
        lineChart.setDescription(d);
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
    private void setupListeners() {
        deviceRef.child("Data").child("TDS_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                int tds = snapshot.getValue(Integer.class);
                updateValue(tds);
                TdsFragment.this.tds = tds;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }
    private void updateValue(Integer value){
        String newText = String.format(Locale.UK,"%04d",value);
        tvEcNext.setText(newText);

        if(getContext()!=null){
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
        }else{
            tvEcCurr.setText(newText);
        }
    }

    class FillGraphDataTask extends AsyncTask<Void, Void, Void> {

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
            data.addEntry(new Entry(seconds, tds), 0);

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
