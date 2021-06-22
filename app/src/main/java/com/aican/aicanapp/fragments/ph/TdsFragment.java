package com.aican.aicanapp.fragments.ph;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.graph.ForegroundService;
import com.aican.aicanapp.specificactivities.EcTdsCalibrateActivity;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.utils.PlotGraphNotifier;
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
import com.opencsv.CSVWriter;

import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class TdsFragment extends Fragment {

    TextView tvEcCurr, tvEcNext;
    Button btnCalibrate;

    DatabaseReference deviceRef;
    LineChart lineChart;
    LinearLayout llStart, llStop, llClear, llExport;


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

        llStart = view.findViewById(R.id.llStart);
        llStop = view.findViewById(R.id.llStop);
        llClear = view.findViewById(R.id.llClear);
        llExport = view.findViewById(R.id.llExport);

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

        llStart.setOnClickListener(v->{
            if(ForegroundService.isRunning()){
                Toast.makeText(requireContext(), "Another graph is logging", Toast.LENGTH_SHORT).show();
                return;
            }
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
        if(myService!=null){
            myService.clearEntries();
        }
    }

    private void stopLogging() {
        isLogging = false;
        if (myService != null) {
            myService.stopLogging(TdsFragment.class);
        }
    }

    ArrayList<Entry> logs = new ArrayList<>();
    private boolean isLogging = false;
    long start = 0;


    ForegroundService myService;
    PlotGraphNotifier plotGraphNotifier;

    private void startLogging() {
        logs.clear();
        isLogging = true;

        Context context = requireContext();
        Intent intent = new Intent(context, ForegroundService.class);
        DatabaseReference ref = deviceRef.child("Data").child("TDS_VAL");
        ForegroundService.setInitials(PhActivity.DEVICE_ID, ref, TdsFragment.class, start, "tds");
        requireActivity().startService(intent);
        requireActivity().bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if(service instanceof ForegroundService.MyBinder){
                    myService = ((ForegroundService.MyBinder) service).getService();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        start = System.currentTimeMillis();
        if(ForegroundService.isMyTypeRunning(PhActivity.DEVICE_ID, TdsFragment.class, "tds")){
            llStart.setVisibility(View.INVISIBLE);
            llStop.setVisibility(View.VISIBLE);
            llClear.setVisibility(View.INVISIBLE);
            llExport.setVisibility(View.INVISIBLE);

            start = ForegroundService.start;
            Intent intent = new Intent(requireContext(), ForegroundService.class);
            requireActivity().bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if(service instanceof ForegroundService.MyBinder){
                        myService = ((ForegroundService.MyBinder) service).getService();
                        ArrayList<Entry> entries=myService.getEntries();
                        logs.clear();
                        logs.addAll(entries);

                        lineChart.getLineData().clearValues();

                        LineDataSet lineDataSet = new LineDataSet(logs,"TDS");

                        lineDataSet.setLineWidth(2);
                        lineDataSet.setCircleRadius(4);
                        lineDataSet.setValueTextSize(10);


                        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                        dataSets.add(lineDataSet);

                        LineData data = new LineData(dataSets);
                        lineChart.setData(data);
                        lineChart.invalidate();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            },0);
        }

        plotGraphNotifier = new PlotGraphNotifier(Dashboard.GRAPH_PLOT_DELAY, () -> {
            long seconds = (System.currentTimeMillis() - start) / 1000;
            LineData data = lineChart.getData();
            Entry entry = new Entry(seconds, tds);
            data.addEntry(entry, 0);
            lineChart.notifyDataSetChanged();
            data.notifyDataChanged();
            lineChart.invalidate();
            if (isLogging) {
                logs.add(entry);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        plotGraphNotifier.stop();
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


}
