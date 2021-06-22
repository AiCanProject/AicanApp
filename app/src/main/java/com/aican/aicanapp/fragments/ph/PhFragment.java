package com.aican.aicanapp.fragments.ph;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.MainActivity;
import com.aican.aicanapp.R;
import com.aican.aicanapp.graph.ForegroundService;
import com.aican.aicanapp.ph.PhView;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.specificactivities.PhCalibrateActivity;
import com.aican.aicanapp.tempController.ProgressLabelView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVWriter;

import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class PhFragment extends Fragment {
    private static final String TAG = "PhFragment";
    PhView phView;
//    ProgressLabelView phTextView;
    Button calibrateBtn;
    TextView tvPhCurr, tvPhNext;
    LineChart lineChart;

    DatabaseReference deviceRef;
    LinearLayout llStart, llStop, llClear, llExport;

    float ph = 0;
    FillGraphDataTask fillGraphDataTask;


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
//        phTextView = view.findViewById(R.id.phTextView);
        calibrateBtn = view.findViewById(R.id.calibrateBtn);
        tvPhCurr = view.findViewById(R.id.tvPhCurr);
        tvPhNext = view.findViewById(R.id.tvPhNext);
        lineChart = view.findViewById(R.id.line_chart);
        llStart = view.findViewById(R.id.llStart);
        llStop = view.findViewById(R.id.llStop);
        llClear = view.findViewById(R.id.llClear);
        llExport = view.findViewById(R.id.llExport);
        

//        phTextView.setAnimationDuration(0);
//        phTextView.setProgress(7);
//        phTextView.setAnimationDuration(800);

        phView.setCurrentPh(7);

//        phTextView.setTextColor(getAttr(R.attr.primaryTextColor));

        calibrateBtn.setOnClickListener(v->{
            Intent intent= new Intent(requireContext(), PhCalibrateActivity.class);
            intent.putExtra(Dashboard.KEY_DEVICE_ID, PhActivity.DEVICE_ID);
            startActivity(intent);
        });

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        setupGraph();
        setupListeners();
    }

    private void setupGraph() {
        LineDataSet lineDataSet = new LineDataSet(new ArrayList<>(),"pH");

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
        d.setText("pH Graph");
        lineChart.setDescription(d);

        llStart.setOnClickListener(v->{
            llStart.setVisibility(View.INVISIBLE);
            llStop.setVisibility(View.VISIBLE);
            llClear.setVisibility(View.INVISIBLE);
            llExport.setVisibility(View.INVISIBLE);
            startLogging();
        });
        llStop.setOnClickListener(v->{
            llStart.setVisibility(View.VISIBLE);
            llStop.setVisibility(View.INVISIBLE);
            llClear.setVisibility(View.VISIBLE);
            llExport.setVisibility(View.VISIBLE);
            stopLogging();
        });
        llClear.setOnClickListener(v->{
            llClear.setVisibility(View.INVISIBLE);
            llExport.setVisibility(View.INVISIBLE);
            clearLogs();
        });
        llExport.setOnClickListener(v->{
            exportLogs();
        });
    }

    private void exportLogs() {
        if(!checkStoragePermission()){
            return;
        }
        String csv = (requireContext().getExternalFilesDir(null).getAbsolutePath() + "/"+System.currentTimeMillis()+".csv");
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(csv));

            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[]{"X", "Y"});
            for(int i=0; i<logs.size(); i++)
            {
                String[] s = {String.valueOf(logs.get(i).getX()), String.valueOf(logs.get(i).getY())};
                data.add(s);
            }
            writer.writeAll(data); // data is adding to csv
            Toast.makeText(requireContext(),"Exported",Toast.LENGTH_LONG).show();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requireActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
                return false;
            }
        }
        return true;
    }

    private void clearLogs() {
        logs.clear();
    }

    private void stopLogging() {
        isLogging = false;
    }

    ArrayList<Entry> logs = new ArrayList<>();
    private boolean isLogging = false;
    private void startLogging() {
//        Context context = requireContext();
//        Intent intent = new Intent(context, ForegroundService.class);
//        ForegroundService.enqueueWork(context, intent);
        logs.clear();
        isLogging = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        fillGraphDataTask= new FillGraphDataTask();
        fillGraphDataTask.execute();
//        Intent intent = new Intent(requireContext(), ForegroundService.class);
//        if()
    }

    @Override
    public void onPause() {
        super.onPause();
        fillGraphDataTask.stopRunning();
        fillGraphDataTask.cancel(true);
    }

    private void setupListeners() {
        deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ph = snapshot.getValue(Float.class);
                if(ph==null) return;
                phView.moveTo(ph);
                updatePh(ph);
                PhFragment.this.ph = ph;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void updatePh(float ph){
        String newText = String.format(Locale.UK,"%.2f",ph);
        tvPhNext.setText(newText);

        if(getContext()!=null){
            Animation fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out);
            Animation slideInBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom);

            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    tvPhCurr.setVisibility(View.INVISIBLE);
                    TextView t = tvPhCurr;
                    tvPhCurr = tvPhNext;
                    tvPhNext = t;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            tvPhCurr.startAnimation(fadeOut);
            tvPhNext.setVisibility(View.VISIBLE);
            tvPhNext.startAnimation(slideInBottom);
        }else{
            tvPhCurr.setText(newText);
        }
    }

    private int getAttr(@AttrRes int attrRes){
        TypedValue typedValue = new TypedValue();
        requireActivity().getTheme().resolveAttribute(attrRes,typedValue,true);

        return typedValue.data;
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
            Entry entry = new Entry(seconds, ph);
            data.addEntry(entry, 0);
            lineChart.notifyDataSetChanged();
            data.notifyDataChanged();
            lineChart.invalidate();
            if(isLogging){
                logs.add(entry);
            }
        }

        void stopRunning(){
            running=false;
        }
    }

}
