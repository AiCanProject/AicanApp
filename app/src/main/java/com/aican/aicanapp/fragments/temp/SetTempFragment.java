package com.aican.aicanapp.fragments.temp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.dialogs.EditPhBufferDialog;
import com.aican.aicanapp.dialogs.EditSetTempDialog;
import com.aican.aicanapp.graph.ForegroundService;
import com.aican.aicanapp.service.TempService;
import com.aican.aicanapp.specificactivities.SetTempGraphActivity;
import com.aican.aicanapp.specificactivities.TemperatureActivity;
import com.aican.aicanapp.tempController.CurveSeekView;
import com.aican.aicanapp.tempController.ProgressLabelView;
import com.aican.aicanapp.utils.PlotGraphNotifier;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.timepicker.TimeFormat;
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
import java.util.Calendar;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class SetTempFragment extends Fragment implements AdapterView.OnItemSelectedListener{
    
    DatabaseReference deviceRef = null;
    ProgressLabelView currTemp;
    ProgressLabelView tempTextView;
    CurveSeekView curveSeekView;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    AppCompatButton graphBtn;
    Spinner spinner;
    Button modeBtn;
    int togTime=1;
    TextView onTime,offTime,divState;
    int setMode=0,reg=1;
    long diffTime,startTime,endTime;
    float temp = 0;
    private float progress = 150f;
    private boolean initialValue = true;
    private boolean light = false;
    TextView tvEdit;
    int valuesProgress,valTemp2,fixedTemp;
    String divStateFB="";
    boolean first=true;
    TempService tempService;
    boolean ON_CLICKED=false;
//    LinearLayout llStart, llStop, llClear, llExport;
//    CardView cv1Min, cv5Min, cv10Min, cv15Min, cvClock;
//    int skipPoints = 0;
//    int skipCount = 0;
//    ArrayList<Entry> entriesOriginal;
//    boolean isTimeOptionsVisible = false;
//    ArrayList<Entry> logs = new ArrayList<>();
//    long start = 0;
//    ForegroundService myService;
//    PlotGraphNotifier plotGraphNotifier;
//    private LineChart lineChart;
//    private boolean isLogging = false;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_temp_set, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        lineChart = view.findViewById(R.id.line_chart);

        graphBtn=view.findViewById(R.id.graphBtn);
        curveSeekView = view.findViewById(R.id.curveSeekView);
        spinner=view.findViewById(R.id.spinner);
        onTime=view.findViewById(R.id.onTime);
        offTime=view.findViewById(R.id.offTime);
        tvEdit=view.findViewById(R.id.tvEdit);
        divState=view.findViewById(R.id.divState);
        tempTextView = view.findViewById(R.id.humidityTextView);
        if (light) setLightStatusBar(curveSeekView);
        currTemp = view.findViewById(R.id.temperatureTextView);
        Button changeBtn = view.findViewById(R.id.themButton);
        modeBtn = view.findViewById(R.id.modeButton);

        startTime=0;
        endTime=0;

//        llStart = view.findViewById(R.id.llStart);
//        llStop = view.findViewById(R.id.llStop);
//        llClear = view.findViewById(R.id.llClear);
//        llExport = view.findViewById(R.id.llExport);
//        cv5Min = view.findViewById(R.id.cv5min);
//        cv1Min = view.findViewById(R.id.cv1min);
//        cv10Min = view.findViewById(R.id.cv10min);
//        cv15Min = view.findViewById(R.id.cv15min);
//        cvClock = view.findViewById(R.id.cvClock);

        currTemp.setProgress(Math.round(progress));
        tempTextView.setAnimationDuration(0);
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

//        entriesOriginal = new ArrayList<>();

//        cvClock.setOnClickListener(v -> {
//            isTimeOptionsVisible = !isTimeOptionsVisible;
//            if (isTimeOptionsVisible) {
//                showTimeOptions();
//            } else {
//                hideTimeOptions();
//            }
//        });
        graphBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(), SetTempGraphActivity.class);
                startActivity(intent);
            }
        });


        curveSeekView.setOnProgressChangeListener(new Function1<Float, Unit>() {
            @Override
            public Unit invoke(Float aFloat) {
                progress = aFloat;
                valuesProgress=Math.round(aFloat);
                tempTextView.setProgress(Math.round(aFloat));
                Log.e("progress", Integer.toString(Math.round(aFloat)));
                return null;
            }
        });

        
        tvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditSetTempDialog dialog = new EditSetTempDialog(new EditSetTempDialog.OnValueChangedListener() {
                    @Override
                    public void onValueChanged(int setTemp) {
                        valuesProgress=Math.round(setTemp);
                        tempTextView.setProgress(Math.round(setTemp));
                        curveSeekView.setProgress(seekProgCalulation(Math.round(setTemp)));
                    }
                }) ;
                dialog.show(getActivity().getSupportFragmentManager(), null);
            }
        });

        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newTemp = Math.round(curveSeekView.getProgress());
                deviceRef.child("UI").child("TEMP").child("SET_TEMP").setValue(newTemp);
                Toast.makeText(getContext(), "Temperature is updated", Toast.LENGTH_SHORT).show();
            }
        });
        modeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(setMode==0){
                    spinner.setEnabled(false);
                    setMode=1;
                    //Service
                    TempService.dataInitialize(divStateFB,fixedTemp,valTemp2,reg,setMode,valuesProgress);
                    Intent serviceIntent=new Intent(getContext(), TempService.class);
                    getContext().startService(serviceIntent);
                    modeBtn.setText("STOP");
                    if(reg==1) {
                        deviceRef.child("UI").child("TEMP").child("STATUS").setValue(1);
                    }
                    else if(reg==0){

                        deviceRef.child("UI").child("TEMP").child("STATUS").setValue(2);
                        if(!ON_CLICKED){
                            deviceRef.child("UI").child("TEMP").child("OFF_TIME").setValue(0);
                            deviceRef.child("UI").child("TEMP").child("ON_TIME").setValue(0);
                        }
                        if (togTime == 2) {
                            togTime = 0;
                            deviceRef.child("UI").child("TEMP").child("OFF_TIME").setValue(endTime);
                            deviceRef.child("UI").child("TEMP").child("ON_TIME").setValue(startTime);
//                            Toast.makeText(getContext(), ""+endTime+" "+startTime, Toast.LENGTH_SHORT).show();
                        }

                    }
                }
                else{
                    spinner.setEnabled(true);
                    setMode=0;
                    modeBtn.setText("START");
                    deviceRef.child("UI").child("TEMP").child("STATUS").setValue(0);
                    if(togTime==2){
                        togTime=0;
                        endTime=0;startTime=0;
                        deviceRef.child("UI").child("TEMP").child("OFF_TIME").setValue(endTime);
                        deviceRef.child("UI").child("TEMP").child("ON_TIME").setValue(startTime);
//                        Toast.makeText(getContext(), ""+endTime+" "+startTime, Toast.LENGTH_SHORT).show();
                    }
                    ON_CLICKED=false;
                    onTime.setText("ON Time : "+getTodayDate());
                    offTime.setText("OFF Time : "+getTodayDate());

                    //Service
                    TempService.dataInitialize("",0,0,0,0,0);
                    Intent serviceIntent=new Intent(getContext(),TempService.class);
                    getContext().stopService(serviceIntent);
                }
                Toast.makeText(getContext(), "Mode updated", Toast.LENGTH_SHORT).show();
            }
        });

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(TemperatureActivity.DEVICE_ID)).getReference()
                .child(TemperatureActivity.deviceType).child(TemperatureActivity.DEVICE_ID);

        ArrayAdapter adapter=ArrayAdapter.createFromResource(getContext(),R.array.modes,
                R.layout.color_spinner_layout);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout); //Spinner Dropdown Text
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(this);

        onTime.setText("ON Time : "+getTodayDate());
        offTime.setText("OFF Time : "+getTodayDate());

        onTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togTime=1;
                initDatePicker();
                ON_CLICKED=true;
            }
        });
        offTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (!ON_CLICKED){
                   Toast.makeText(getContext(), "Please select a On Time", Toast.LENGTH_SHORT).show();
               }
               else {
                   togTime=2;
                   initDatePicker();
               }
            }
        });

        setupListeners();
//        setupGraph();
    }

    private float seekProgCalulation(int n) {

        if(n<=300&&n>=293){
            return 300-n;
        }

        else if(n<=292&&n>=279){
            return 300-n+1;
        }

        else if(n<=278&&n>=265){
            return 300-n+2;
        }
        else if(n<=264&&n>=251){
            return 300-n+3;
        }
        else if(n<=250&&n>=237){
            return 300-n+4;
        }
        else if(n<=236&&n>=223){
            return 300-n+5;
        }
        else if(n<=222&&n>=209){
            return 300-n+6;
        }
        else if(n<=208&&n>=195){
            return 300-n+7;
        }
        else if(n<=194&&n>=181){
            return 300-n+8;
        }
        else if(n<=180&&n>=167){
            return 300-n+9;
        }
        else if(n<=166&&n>=153){
            return 300-n+10;
        }
        else if(n<=152&&n>=139){
            return 300-n+11;
        }
        else if(n<=138&&n>=125){
            return 300-n+12;
        }
        else if(n<=124&&n>=111){
            return 300-n+13;
        }
        else if(n<=110&&n>=97){
            return 300-n+14;
        }
        else if(n<=96&&n>=83){
            return 300-n+15;
        }
        else if(n<=82&&n>=69){
            return 300-n+16;
        }
        else if(n<=68&&n>=55){
            return 300-n+17;
        }
        else if(n<=54&&n>=41){
            return 300-n+18;
        }
        else if(n<=40&&n>=27){
            return 300-n+19;
        }
        else if(n<=26&&n>=13){
            return 300-n+20;
        }
        else{
            return 0;
        }

    }

    private String getTodayDate() {
        Calendar cal=Calendar.getInstance();
        int year=cal.get(Calendar.YEAR);
        int month=cal.get(Calendar.MONTH);
        month++;
        int day=cal.get(Calendar.DATE);
        int hour=cal.get(Calendar.HOUR_OF_DAY);
        int min=cal.get(Calendar.MINUTE);
        return makeDateString(day,month,year,hour,min);
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                  month=month+1;
                  initTimePicker(day,month,year);
            }
        };

        Calendar cal=Calendar.getInstance();
        int year=cal.get(Calendar.YEAR);
        int month=cal.get(Calendar.MONTH);
        int day=cal.get(Calendar.DATE);

        int style= AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog=new DatePickerDialog(getContext(),style,dateSetListener,year,month,day);
        datePickerDialog.show();
    }

    private void initTimePicker(int day,int month,int year) {
        TimePickerDialog.OnTimeSetListener timeSetListener=new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int min) {
                String val=makeDateString(day,month,year,hour,min);
                if(togTime==1) {
                    if(differFromNowTimeCalculate(day, month, year, hour, min)){
                        onTime.setText("ON Time : " +val);
                        startTime=diffTime;
                    }
                     else{
                        Toast.makeText(getContext(), "Previous time not applicable", Toast.LENGTH_SHORT).show();
                    }
                  }
                  else if(togTime==2){
                    if(differFromNowTimeCalculate(day,month,year,hour,min)){
                        offTime.setText("OFF Time : "+val);
                        endTime=diffTime-startTime;
                    }
                    else{
                        Toast.makeText(getContext(), "Previous time not applicable", Toast.LENGTH_SHORT).show();
                    }

                  }
            }
        };
        Calendar cal=Calendar.getInstance();
        int h=cal.get(Calendar.HOUR_OF_DAY);
        int m=cal.get(Calendar.MINUTE);

        int style= AlertDialog.THEME_HOLO_LIGHT;

        timePickerDialog=new TimePickerDialog(getContext(),style,timeSetListener,h,m, true);
        timePickerDialog.show();
    }

    private String makeDateString(int day, int month, int year,int hour,int min) {
        String valMin="",valHour="";
        if(min<10){
            valMin=("0"+String.valueOf(min));
        }
        else {
            valMin=String.valueOf( min);
        }
        if(hour<10){
            valHour=("0"+String.valueOf(hour));
        }
        else {
            valHour=String.valueOf(hour);
        }
        return day+"-"+month+"-"+year+"  "+valHour+":"+valMin;
    }

    private boolean differFromNowTimeCalculate(int day,int month,int year,int hour,int min){
        boolean bol=false;
        Calendar cal=Calendar.getInstance();
        int yearNow=cal.get(Calendar.YEAR);
        int monthNow=cal.get(Calendar.MONTH);
        monthNow++;
        int dayNow=cal.get(Calendar.DATE);
        int hourNow=cal.get(Calendar.HOUR_OF_DAY);
        int minNow=cal.get(Calendar.MINUTE);
        if(year>yearNow){
            bol=true;
        }
        else if(year==yearNow){
            if(month>monthNow){
                bol=true;

            }
            else if(month==monthNow){
                if(day>dayNow){
                    bol=true;
                }
                else if(day==dayNow){
                    if(hour>hourNow){
                        bol=true;
                    }
                    else if(hour==hourNow){
                        bol= min >= minNow;
                    }
                }
            }
        }
        diffTime=(year-yearNow)*365*24*60+(month-monthNow)*30*24*60+(day-dayNow)*24*60
                +(hour-hourNow)*60+(min-minNow);
        return bol;
    }

//    private void setupGraph() {
//        LineDataSet lineDataSet = new LineDataSet(new ArrayList<>(), "Temp");
//
//        lineDataSet.setLineWidth(2);
//        lineDataSet.setCircleRadius(4);
//        lineDataSet.setValueTextSize(10);
//
//
//        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
//        dataSets.add(lineDataSet);
//
//        LineData data = new LineData(dataSets);
//        lineChart.setData(data);
//        lineChart.invalidate();
//        lineChart.setDrawGridBackground(true);
//        lineChart.setDrawBorders(true);
//
//        Description d = new Description();
//        d.setText("Temp Graph");
//        lineChart.setDescription(d);
//
//        llStart.setOnClickListener(v -> {
//            if (ForegroundService.isRunning()) {
//                Toast.makeText(requireContext(), "Another graph is logging", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            llStart.setVisibility(View.INVISIBLE);
//            llStop.setVisibility(View.VISIBLE);
//            llClear.setVisibility(View.INVISIBLE);
//            llExport.setVisibility(View.INVISIBLE);
//            startLogging();
//        });
//        llStop.setOnClickListener(v -> {
//            llStart.setVisibility(View.VISIBLE);
//            llStop.setVisibility(View.INVISIBLE);
//            llClear.setVisibility(View.VISIBLE);
//            llExport.setVisibility(View.VISIBLE);
//            stopLogging();
//        });
//        llClear.setOnClickListener(v -> {
//            llClear.setVisibility(View.INVISIBLE);
//            llExport.setVisibility(View.INVISIBLE);
//            clearLogs();
//        });
//        llExport.setOnClickListener(v -> {
//            exportLogs();
//        });
//        cv1Min.setOnClickListener(v -> {
//            skipPoints = (60 * 1000) / Dashboard.GRAPH_PLOT_DELAY;
//            rescaleGraph();
//        });
//        cv5Min.setOnClickListener(v -> {
//            skipPoints = (5 * 60 * 1000) / Dashboard.GRAPH_PLOT_DELAY;
//            rescaleGraph();
//        });
//        cv10Min.setOnClickListener(v -> {
//            skipPoints = (10 * 60 * 1000) / Dashboard.GRAPH_PLOT_DELAY;
//            rescaleGraph();
//        });
//        cv15Min.setOnClickListener(v -> {
//            skipPoints = (15 * 60 * 1000) / Dashboard.GRAPH_PLOT_DELAY;
//            rescaleGraph();
//        });
//    }

//    private void exportLogs() {
//        if (!checkStoragePermission()) {
//            return;
//        }
//        String csv = (requireContext().getExternalFilesDir(null).getAbsolutePath() + "/" + System.currentTimeMillis() + ".csv");
//        try {
//            CSVWriter writer = new CSVWriter(new FileWriter(csv));
//
//            List<String[]> data = new ArrayList<String[]>();
//            data.add(new String[]{"X", "Y"});
//            for (int i = 0; i < logs.size(); i++) {
//                String[] s = {String.valueOf(logs.get(i).getX()), String.valueOf(logs.get(i).getY())};
//                data.add(s);
//            }
//            writer.writeAll(data); // data is adding to csv
//            Toast.makeText(requireContext(), "Exported", Toast.LENGTH_LONG).show();
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    private boolean checkStoragePermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (requireActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
//                return false;
//            }
//        }
//        return true;
//    }

//    private void clearLogs() {
//        logs.clear();
//        if (myService != null) {
//            myService.clearEntries();
//        }
//    }

//    private void stopLogging() {
//        isLogging = false;
//        if (myService != null) {
//            myService.stopLogging(SetTempFragment.class);
//        }
//    }

//    private void startLogging() {
//        logs.clear();
//        isLogging = true;
//
//        Context context = requireContext();
//        Intent intent = new Intent(context, ForegroundService.class);
//        DatabaseReference ref = deviceRef.child("Data").child("TEMP1_VAL");
//        ForegroundService.setInitials(TemperatureActivity.DEVICE_ID, ref, SetTempFragment.class, start, TemperatureActivity.deviceType + "set");
//        context.startService(intent);
//        context.bindService(intent, new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//                if (service instanceof ForegroundService.MyBinder) {
//                    myService = ((ForegroundService.MyBinder) service).getService();
//                }
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//
//            }
//        }, 0);
//    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        plotGraphNotifier.stop();
//    }

//    private void rescaleGraph() {
//        ArrayList<Entry> entries = new ArrayList<>();
//        int count = 0;
//        for (Entry entry : entriesOriginal) {
//            if (count == 0) {
//                entries.add(entry);
//            }
//            ++count;
//            if (count >= skipPoints) {
//                count = 0;
//            }
//        }
//
//        lineChart.getLineData().clearValues();
//
//        LineDataSet lds = new LineDataSet(entries, "Temp");
//
//        lds.setLineWidth(2);
//        lds.setCircleRadius(4);
//        lds.setValueTextSize(10);
//
//
//        ArrayList<ILineDataSet> ds = new ArrayList<>();
//        ds.add(lds);
//
//        LineData ld = new LineData(ds);
//        lineChart.setData(ld);
//        lineChart.invalidate();
//    }

//    @Override
//    public void onResume() {
//        super.onResume();
//
//        start = System.currentTimeMillis();
//
//        if (ForegroundService.isMyTypeRunning(TemperatureActivity.DEVICE_ID, SetTempFragment.class, TemperatureActivity.deviceType + "set")) {
//            llStart.setVisibility(View.INVISIBLE);
//            llStop.setVisibility(View.VISIBLE);
//            llClear.setVisibility(View.INVISIBLE);
//            llExport.setVisibility(View.INVISIBLE);
//
//            start = ForegroundService.start;
//
//            Intent intent = new Intent(requireContext(), ForegroundService.class);
//            requireContext().bindService(intent, new ServiceConnection() {
//                @Override
//                public void onServiceConnected(ComponentName name, IBinder service) {
//                    if (service instanceof ForegroundService.MyBinder) {
//                        myService = ((ForegroundService.MyBinder) service).getService();
//                        ArrayList<Entry> entries = myService.getEntries();
//                        logs.clear();
//                        logs.addAll(entries);
//
//                        lineChart.getLineData().clearValues();
//
//                        LineDataSet lineDataSet = new LineDataSet(logs, "Temp");
//
//                        lineDataSet.setLineWidth(2);
//                        lineDataSet.setCircleRadius(4);
//                        lineDataSet.setValueTextSize(10);
//
//
//                        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
//                        dataSets.add(lineDataSet);
//
//                        LineData data = new LineData(dataSets);
//                        lineChart.setData(data);
//                        lineChart.invalidate();
//                    }
//                }
//
//                @Override
//                public void onServiceDisconnected(ComponentName name) {
//
//                }
//            }, 0);
//        }
//
//        plotGraphNotifier = new PlotGraphNotifier(Dashboard.GRAPH_PLOT_DELAY, () -> {
//            if (skipCount < skipPoints) {
//                skipCount++;
//                return;
//            }
//            skipCount = 0;
//            long seconds = (System.currentTimeMillis() - start) / 1000;
//            LineData data = lineChart.getData();
//            Entry entry = new Entry(seconds, temp);
//            entriesOriginal.add(entry);
//            data.addEntry(entry, 0);
//            lineChart.notifyDataSetChanged();
//            data.notifyDataChanged();
//            lineChart.invalidate();
//            if (isLogging) {
//                logs.add(entry);
//            }
//        });
//    }

//    private void showTimeOptions() {
//        cv1Min.setVisibility(View.VISIBLE);
//        cv5Min.setVisibility(View.VISIBLE);
//        cv10Min.setVisibility(View.VISIBLE);
//        cv15Min.setVisibility(View.VISIBLE);
//
//        Animation zoomIn = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_in);
//        cv1Min.startAnimation(zoomIn);
//        cv5Min.startAnimation(zoomIn);
//        cv10Min.startAnimation(zoomIn);
//        cv15Min.startAnimation(zoomIn);
//    }
//
//    private void hideTimeOptions() {
//        Animation zoomOut = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_out);
//        zoomOut.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//
//                cv1Min.setVisibility(View.INVISIBLE);
//                cv5Min.setVisibility(View.INVISIBLE);
//                cv10Min.setVisibility(View.INVISIBLE);
//                cv15Min.setVisibility(View.INVISIBLE);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//
//        cv1Min.startAnimation(zoomOut);
//        cv5Min.startAnimation(zoomOut);
//        cv10Min.startAnimation(zoomOut);
//        cv15Min.startAnimation(zoomOut);
//    }

    private void setupListeners() {

        deviceRef.child("Data").child("TEMP1_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Integer temp = snapshot.getValue(Integer.class);
                if (temp == null) return;

                currTemp.setProgress(temp);
                fixedTemp=temp;
                SetTempFragment.this.temp = temp;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        deviceRef.child("Data").child("TEMP2_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Integer temp = snapshot.getValue(Integer.class);
                if (temp == null) return;
                valTemp2=temp;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        deviceRef.child("UI").child("TEMP").child("STATE").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String val=snapshot.getValue(String.class);
                if (val == null) return;
                divState.setText("Device State is - "+val);
                if(val.toLowerCase().equals("on")){
                    divState.setTextColor(getResources().getColor(R.color.green));
                }
                else{
                    divState.setTextColor(getResources().getColor(R.color.red));
                }
                divStateFB=val;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        deviceRef.child("UI").child("TEMP").child("SET_TEMP").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer temp = snapshot.getValue(Integer.class);
                if (temp == null) return;
                if (initialValue) {
                    initialValue = false;
                    curveSeekView.setProgress(seekProgCalulation(temp));

//                    valuesProgress=temp;
                    tempTextView.setProgress(temp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position==0){
            reg=1;
            if (!first){
                modeBtn.setText("START");
                setMode=0;
            }
            onTime.setVisibility(View.GONE);
            offTime.setVisibility(View.GONE);
            deviceRef.child("UI").child("TEMP").child("STATUS").setValue(0);
        }
        else if(position==1){
            reg=0;
            ON_CLICKED=false;
            showTimeONOFFOptions();
            onTime.setText("ON Time : "+getTodayDate());
            offTime.setText("OFF Time : "+getTodayDate());
            if (!first){
                modeBtn.setText("START");
                setMode=0;
            }
            deviceRef.child("UI").child("TEMP").child("STATUS").setValue(0);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void showTimeONOFFOptions() {
        onTime.setVisibility(View.VISIBLE);
        offTime.setVisibility(View.VISIBLE);

        Animation zoomIn = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_in);
        onTime.startAnimation(zoomIn);
        offTime.startAnimation(zoomIn);
    }

    private int getAttr(@AttrRes int attrRes) {
        TypedValue typedValue = new TypedValue();
        requireActivity().getTheme().resolveAttribute(attrRes, typedValue, true);

        return typedValue.data;
    }

    private void setLightStatusBar(CurveSeekView curveSeekView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = curveSeekView.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            curveSeekView.setSystemUiVisibility(flags);
            requireActivity().getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        curveSeekView.setProgress(seekProgCalulation(150));
        tempTextView.setProgress((int)150f);
        Intent intent=new Intent(getContext(),TempService.class);
        getContext().bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                tempService= ((TempService.MyBinder) iBinder).getTempService();
                if (TempService.isShowingNotification()){
                    modeBtn.setText("STOP");
                    curveSeekView.setProgress(seekProgCalulation(TempService.getSetTemp()));
                    tempTextView.setProgress(TempService.getSetTemp());
                    setMode=TempService.getSetMode();
                    reg=TempService.getReg();
                    initialValue=false;
                    if(reg==1) {
                        spinner.setSelection(0);
                    }
                    else if(reg==0){
                        spinner.setSelection(1);
                        Log.w("selection","1");
                    }
                }
                else{
                    modeBtn.setText("START");
                    curveSeekView.setProgress(seekProgCalulation(TempService.getSetTemp()));
                    tempTextView.setProgress(TempService.getSetTemp());

                }

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        },0);
    }
}
