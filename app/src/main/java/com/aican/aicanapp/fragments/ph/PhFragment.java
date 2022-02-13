package com.aican.aicanapp.fragments.ph;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.adapters.LogAdapter;
import com.aican.aicanapp.dataClasses.phData;
import com.aican.aicanapp.dialogs.SelectCalibrationPointsDialog;
import com.aican.aicanapp.graph.ForegroundService;
import com.aican.aicanapp.ph.PhView;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.utils.DecimalValueFormatter;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhFragment extends Fragment {
    private static final String TAG = "PhFragment";
    PhView phView;
    Button calibrateBtn, logBtn;
    TextView tvPhCurr, tvPhNext, tvTempCurr, tvTempNext, tvEcCurr, batter, slopeVal, offsetVal;
    LineChart lineChart;
    RecyclerView recyclerView;

    TableLayout stk;
    DatabaseReference deviceRef;
    LinearLayout llStart, llStop, llClear, llExport;
    CardView cv1Min, cv5Min, cv10Min, cv15Min, cvClock;

    float ph = 0;
    int skipPoints = 0;
    int skipCount = 0;

    ArrayList<Entry> entriesOriginal;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ph_main, container, false);
    }

    boolean isTimeOptionsVisible = false;

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEcCurr = view.findViewById(R.id.tvEcCurr);
        tvTempCurr = view.findViewById(R.id.tvTempCurr);
        tvTempNext = view.findViewById(R.id.tvTempNext);
        //stk =     view.findViewById(R.id.table_main);
        logBtn = view.findViewById(R.id.logBtn);
        recyclerView = view.findViewById(R.id.tableRecycler);
        batter = view.findViewById(R.id.batteryPer);
        slopeVal = view.findViewById(R.id.slopeVal);
        offsetVal = view.findViewById(R.id.offsetVal);


        phView = view.findViewById(R.id.phView);
        calibrateBtn = view.findViewById(R.id.calibrateBtn);
        tvPhCurr = view.findViewById(R.id.tvPhCurr);
        tvPhNext = view.findViewById(R.id.tvPhNext);
        lineChart = view.findViewById(R.id.line_chart);
        llStart = view.findViewById(R.id.llStart);
        llStop = view.findViewById(R.id.llStop);
        llClear = view.findViewById(R.id.llClear);
        llExport = view.findViewById(R.id.llExport);
        cv5Min = view.findViewById(R.id.cv5min);
        cv1Min = view.findViewById(R.id.cv1min);
        cv10Min = view.findViewById(R.id.cv10min);
        cv15Min = view.findViewById(R.id.cv15min);
        cvClock = view.findViewById(R.id.cvClock);

        phData phData = new phData();
        phView.setCurrentPh(7);
        entriesOriginal = new ArrayList<>();
        ArrayList<phData> list = new ArrayList<>();


        logBtn.setOnClickListener(v -> {
            String currentTime = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault()).format(new Date());
            phData.setDate(currentTime);

            deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Float p = snapshot.getValue(Float.class);
                    String ph = String.format(Locale.UK, "%.2f", p );

                    phData.setpH(ph);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

            deviceRef.child("Data").child("EC_VAL").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Float mv = snapshot.getValue(Float.class);
                    String m = String.format(Locale.UK, "%.2f", mv );
                    phData.setmV(m);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

            LogAdapter adapter = new LogAdapter(list);

            list.add(phData);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        });



        //setup recyclerview log



        calibrateBtn.setOnClickListener(v -> {
            SelectCalibrationPointsDialog dialog = new SelectCalibrationPointsDialog();
            dialog.show(getParentFragmentManager(), null);
        });
        cvClock.setOnClickListener(v -> {
            isTimeOptionsVisible = !isTimeOptionsVisible;
            if (isTimeOptionsVisible) {
                showTimeOptions();
            } else {
                hideTimeOptions();
            }
        });

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        setupGraph();
        setupListeners();
    }

    /*
    public void init() {
        TableRow tbrow0 = new TableRow(requireContext());
        TextView tv0 = new TextView(requireContext());
        tv0.setText("pH");
        tv0.setTextColor(Color.BLACK);
        tv0.setPadding(12,0,20,10);
        tv0.setTextSize(32);

        tbrow0.addView(tv0);
        TextView tv1 = new TextView(requireContext());
        tv1.setText("mV");
        tv1.setTextColor(Color.BLACK);

        tv1.setPadding(20,0,12,10);
        tv1.setTextSize(32);
        tbrow0.addView(tv1);

        stk.addView(tbrow0);
        for (int i = 0; i < 5; i++) {
            TableRow tbrow = new TableRow(requireContext());
            TextView t1v = new TextView(requireContext());

            t1v.setText( "3"+ i);
            t1v.setPadding(12,0,20,10);
            t1v.setTextSize(28);
            t1v.setTextColor(Color.BLACK);
            t1v.setGravity(Gravity.CENTER);
            tbrow.addView(t1v);
            TextView t2v = new TextView(requireContext());
            t2v.setText(Float.toString(updatePh(ph)));
            t2v.setTextColor(Color.BLACK);
            t2v.setGravity(Gravity.CENTER);

            t2v.setPadding(20,0,12,10);
            t2v.setTextSize(28);
            tbrow.addView(t2v);

            stk.addView(tbrow);
        }

    }
*/

    private void rescaleGraph() {
        ArrayList<Entry> entries = new ArrayList<>();
        int count = 0;
        for (Entry entry : entriesOriginal) {
            if (count == 0) {
                entries.add(entry);
            }
            ++count;
            if (count >= skipPoints) {
                count = 0;
            }
        }

        lineChart.getLineData().clearValues();

        LineDataSet lds = new LineDataSet(entries, "pH");

        lds.setLineWidth(2);
        lds.setCircleRadius(4);
        lds.setValueTextSize(10);


        ArrayList<ILineDataSet> ds = new ArrayList<>();
        ds.add(lds);

        LineData ld = new LineData(ds);
        lineChart.setData(ld);
        lineChart.invalidate();
    }

    private void exportLogs() {
        if (!checkStoragePermission()) {
            return;
        }
        String csv = (requireContext().getExternalFilesDir(null).getAbsolutePath() + "/" + System.currentTimeMillis() + ".csv");
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
            myService.stopLogging(PhFragment.class);
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
        DatabaseReference ref = deviceRef.child("Data").child("PH_VAL");

        ForegroundService.setInitials(PhActivity.DEVICE_ID, ref, PhFragment.class, start, "ph");
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
        if(ForegroundService.isMyTypeRunning(PhActivity.DEVICE_ID, PhFragment.class, "ph")){
            llStart.setVisibility(View.INVISIBLE);
            llStop.setVisibility(View.VISIBLE);
            llClear.setVisibility(View.INVISIBLE);
            llExport.setVisibility(View.INVISIBLE);

            start = ForegroundService.start;
            Intent intent = new Intent(requireContext(), ForegroundService.class);
            requireActivity().bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if(service instanceof ForegroundService.MyBinder) {
                        myService = ((ForegroundService.MyBinder) service).getService();
                        ArrayList<Entry> entries = myService.getEntries();
                        logs.clear();
                        logs.addAll(entries);
                        entriesOriginal.clear();
                        entriesOriginal.addAll(entries);

                        lineChart.getLineData().clearValues();

                        LineDataSet lineDataSet = new LineDataSet(logs, "pH");

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
            if (ph < 0 || ph > 14) {
                return;
            }
            if (skipCount < skipPoints) {
                skipCount++;
                return;
            }
            skipCount = 0;
            long seconds = (System.currentTimeMillis() - start) / 1000;
            LineData data = lineChart.getData();
            Entry entry = new Entry(seconds, ph);
            data.addEntry(entry, 0);
            entriesOriginal.add(entry);
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

        deviceRef.child("Data").child("TEMP_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String temp = snapshot.getValue(Integer.class).toString();
                tvTempCurr.setText(temp + "°C");
                //updatePh(temp);
                //PhFragment.this.ph = temp;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        deviceRef.child("Data").child("EC_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String ec = snapshot.getValue(Integer.class).toString();
                tvEcCurr.setText(ec);
                //updatePh(temp);
                //PhFragment.this.ph = temp;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        deviceRef.child("Data").child("HOLD").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float hold = snapshot.getValue(Float.class);
                if(hold == 1) {
                    tvPhNext.setTextColor(Color.GREEN);
                    tvPhCurr.setTextColor(Color.GREEN);
                }
                else{
                    tvPhCurr.setTextColor(Color.parseColor("#433A7F"));
                    tvPhCurr.setTextColor(Color.parseColor("#433A7F"));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        deviceRef.child("Data").child("BATTERY").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String batt = snapshot.getValue(Integer.class).toString();
                batter.setText(batt + "%");


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        deviceRef.child("Data").child("SLOPE").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String slope = snapshot.getValue(Integer.class).toString();
                slopeVal.setText(slope + "%");


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        deviceRef.child("Data").child("OFFSET").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String offset = snapshot.getValue(Integer.class).toString();
                offsetVal.setText(offset);


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });



    }

    private void updatePh(float ph) {
        String newText;
        if (ph < 0 || ph > 14) {
            newText = "--";
        } else {
            newText = String.format(Locale.UK, "%.2f", ph);
        }
        tvPhNext.setText(newText);

        if (getContext() != null) {
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

    private int getAttr(@AttrRes int attrRes) {
        TypedValue typedValue = new TypedValue();
        requireActivity().getTheme().resolveAttribute(attrRes, typedValue, true);

        return typedValue.data;
    }

    private void setupGraph() {
        LineDataSet lineDataSet = new LineDataSet(new ArrayList<>(), "pH");

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

        data.setValueFormatter(new DecimalValueFormatter());

        llStart.setOnClickListener(v -> {
            if (ForegroundService.isRunning()) {
                Toast.makeText(requireContext(), "Another graph is logging", Toast.LENGTH_SHORT).show();
                return;
            }
            llStart.setVisibility(View.INVISIBLE);
            llStop.setVisibility(View.VISIBLE);
            llClear.setVisibility(View.INVISIBLE);
            llExport.setVisibility(View.INVISIBLE);
            startLogging();
        });
        llStop.setOnClickListener(v -> {
            llStart.setVisibility(View.VISIBLE);
            llStop.setVisibility(View.INVISIBLE);
            llClear.setVisibility(View.VISIBLE);
            llExport.setVisibility(View.VISIBLE);
            stopLogging();
        });
        llClear.setOnClickListener(v -> {
            llClear.setVisibility(View.INVISIBLE);
            llExport.setVisibility(View.INVISIBLE);
            clearLogs();
        });
        llExport.setOnClickListener(v -> {
            exportLogs();
        });

        cv1Min.setOnClickListener(v -> {
            skipPoints = (60 * 1000) / Dashboard.GRAPH_PLOT_DELAY;
            rescaleGraph();
        });
        cv5Min.setOnClickListener(v -> {
            skipPoints = (5 * 60 * 1000) / Dashboard.GRAPH_PLOT_DELAY;
            rescaleGraph();
        });
        cv10Min.setOnClickListener(v -> {
            skipPoints = (10 * 60 * 1000) / Dashboard.GRAPH_PLOT_DELAY;
            rescaleGraph();
        });
        cv15Min.setOnClickListener(v -> {
            skipPoints = (15 * 60 * 1000) / Dashboard.GRAPH_PLOT_DELAY;
            rescaleGraph();
        });
    }

    private void showTimeOptions() {
        cv1Min.setVisibility(View.VISIBLE);
        cv5Min.setVisibility(View.VISIBLE);
        cv10Min.setVisibility(View.VISIBLE);
        cv15Min.setVisibility(View.VISIBLE);

        Animation zoomIn = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_in);
        cv1Min.startAnimation(zoomIn);
        cv5Min.startAnimation(zoomIn);
        cv10Min.startAnimation(zoomIn);
        cv15Min.startAnimation(zoomIn);
    }

    private void hideTimeOptions() {
        Animation zoomOut = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_out);
        zoomOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                cv1Min.setVisibility(View.INVISIBLE);
                cv5Min.setVisibility(View.INVISIBLE);
                cv10Min.setVisibility(View.INVISIBLE);
                cv15Min.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        cv1Min.startAnimation(zoomOut);
        cv5Min.startAnimation(zoomOut);
        cv10Min.startAnimation(zoomOut);
        cv15Min.startAnimation(zoomOut);
    }

}
