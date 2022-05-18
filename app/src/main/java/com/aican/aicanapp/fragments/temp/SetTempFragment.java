package com.aican.aicanapp.fragments.temp;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.FirebaseAccounts.DevicesAccount;
import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.graph.ForegroundService;
import com.aican.aicanapp.specificactivities.TemperatureActivity;
import com.aican.aicanapp.tempController.CurveSeekView;
import com.aican.aicanapp.utils.PlotGraphNotifier;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SetTempFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    DatabaseReference deviceRef = null, temp_value;
    CurveSeekView curveSeekView;
    LinearLayout llStart, llStop, llClear, llExport;
    CardView cv1Min, cv5Min, cv10Min, cv15Min;
    ImageView minus, plus;
    EditText temp_set;
    TextView temp1, temp2, end_time, start_time, on_time, off_time;
    int skipPoints = 0;
    public static String DEVICE_ID = null;
    int togTime = 1;
    int skipCount = 0;
    String valMin_final = "", valHour_final = "";
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    ArrayList<Entry> entriesOriginal;
    String spinner_status = "Regular";
    String status;
    float temp = 0;
    ArrayList<Entry> logs = new ArrayList<>();
    long start = 0;
    ForegroundService myService;
    PlotGraphNotifier plotGraphNotifier;
    int flag=0;
    TextView start_date, end_date, end_date_display, start_date_display;
    private final float progress = 150f;
    private LineChart lineChart;
    private boolean initialValue = true;
    private boolean isLogging = false;
    long diffTime, startTime, endTime;
    Spinner spinner_mode;
    String[] mode_array;
    ArrayList<String> list_temp;
    Button set_btn, start_btn, stop_btn, green_btn, orange_btn;
    boolean ON_CLICKED = false;

    int bar_progress= 0;
    ProgressBar progress_bar;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_temp_set, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lineChart = view.findViewById(R.id.line_chart);
        boolean light = false;
        if (light) setLightStatusBar(curveSeekView);
        set_btn = view.findViewById(R.id.set_btn);
        spinner_mode = view.findViewById(R.id.spinner_mode);
        start_btn = view.findViewById(R.id.start_btn);
        list_temp = new ArrayList<>();
        temp1 = view.findViewById(R.id.temp1);
        temp2 = view.findViewById(R.id.temp2);
        end_time = view.findViewById(R.id.end_time);
        start_time = view.findViewById(R.id.start_time);
        stop_btn = view.findViewById(R.id.stop_btn);
        on_time = view.findViewById(R.id.on_time);
        off_time = view.findViewById(R.id.off_time);
        green_btn = view.findViewById(R.id.green_btn);
        orange_btn = view.findViewById(R.id.orange_btn);
        temp_value = FirebaseDatabase.getInstance(DevicesAccount.getInstance(getContext())).getReference();

        mode_array = getResources().getStringArray(R.array.mode);

        llStart = view.findViewById(R.id.llStart);
        llStop = view.findViewById(R.id.llStop);
        llClear = view.findViewById(R.id.llClear);
        llExport = view.findViewById(R.id.llExport);
        cv5Min = view.findViewById(R.id.cv5min);
        cv1Min = view.findViewById(R.id.cv1min);
        cv10Min = view.findViewById(R.id.cv10min);
        cv15Min = view.findViewById(R.id.cv15min);

        minus = view.findViewById(R.id.minus);
        progress_bar = view.findViewById(R.id.progress_bar);
        plus = view.findViewById(R.id.plus);
        temp_set = view.findViewById(R.id.temp_set);

        start_date = view.findViewById(R.id.start_date);
        end_date = view.findViewById(R.id.end_date);
        start_date_display = view.findViewById(R.id.start_date_display);
        end_date_display = view.findViewById(R.id.end_date_display);

        updateProgressBar();

        temp_value = temp_value.child("TEMP_CONTROLLER").child(Source.deviceID);

        temp_value.child("Data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list_temp.clear();
                for(DataSnapshot data : snapshot.getChildren()){
                    list_temp.add(data.getValue().toString());
                }
                temp1.setText(list_temp.get(0));
                temp2.setText(list_temp.get(1));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        temp_value.child("UI").child("TEMP").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list_temp.clear();
                for(DataSnapshot data : snapshot.getChildren()){
                    list_temp.add(data.getValue().toString());
                }
                status = list_temp.get(4);
                if(status.equals("ON")){
                    orange_btn.setVisibility(View.INVISIBLE);
                    green_btn.setVisibility(View.VISIBLE);
                }else if(status.equals("OFF")){
                    orange_btn.setVisibility(View.VISIBLE);
                    green_btn.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start_btn.setVisibility(View.INVISIBLE);
                stop_btn.setVisibility(View.VISIBLE);

                int statee = 0;
                if(spinner_status.equals("Regular")){
                    statee = 1;
                } else if(spinner_status.equals("Timer")){
                    statee = 2;
                }

                temp_value.child("UI").child("TEMP").child("STATUS").setValue(statee).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            }
        });

        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start_btn.setVisibility(View.VISIBLE);
                stop_btn.setVisibility(View.INVISIBLE);

                temp_value.child("UI").child("TEMP").child("STATUS").setValue(0).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            }
        });

        ArrayAdapter adapter = new ArrayAdapter(getContext(),R.layout.custom_spinner_mode,mode_array);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_mode.setAdapter(adapter);

        start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togTime = 1;
                initDatePicker();
                ON_CLICKED = true;
            }
        });

        start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togTime = 1;
                initDatePicker();
                ON_CLICKED = true;
            }
        });

        end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ON_CLICKED) {
                    Toast.makeText(getContext(), "Please select a On Time", Toast.LENGTH_SHORT).show();
                } else {
                    togTime = 2;
                    initDatePicker();
                }
            }
        });

        end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ON_CLICKED) {
                    Toast.makeText(getContext(), "Please select a On Time", Toast.LENGTH_SHORT).show();
                } else {
                    togTime = 2;
                    initDatePicker();
                }
            }
        });

        spinner_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(mode_array[i].equals("Regular")){
                    spinner_status = "Regular";
                    start_time.setEnabled(false);
                    end_time.setEnabled(false);
                    start_date.setEnabled(false);
                    end_date.setEnabled(false);
                }else if(mode_array[i].equals("Timer")){
                    spinner_status = "Timer";
                    start_time.setEnabled(true);
                    end_time.setEnabled(true);
                    start_date.setEnabled(true);
                    end_date.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        entriesOriginal = new ArrayList<>();

        set_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceRef.child("Data").child("TEMP1_VAL").setValue(temp_set.getText().toString());
            }
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bar_progress -= 1;
                updateProgressBar();
            }
        });

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bar_progress += 1;
                updateProgressBar();
            }
        });

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(TemperatureActivity.DEVICE_ID)).getReference()
                .child(TemperatureActivity.deviceType).child(TemperatureActivity.DEVICE_ID);

        //setupListeners();
    }

    private void updateProgressBar(){
        progress_bar.setProgress(bar_progress);
        temp_set.setText(String.valueOf(bar_progress));
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                initTimePicker(day, month, year);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DATE);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(getContext(), style, dateSetListener, year, month, day);
        datePickerDialog.show();
    }

    private void initTimePicker(int day, int month, int year) {
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int min) {
                String val = makeDateString(day, month, year, hour, min);
                if (togTime == 1) {
                    if (differFromNowTimeCalculate(day, month, year, hour, min)) {
                        start_date_display.setText(val);
                        on_time.setText(valHour_final + ":" + valMin_final);
                        startTime = diffTime;
                    } else {
                        Toast.makeText(getContext(), "Previous time not applicable", Toast.LENGTH_SHORT).show();
                    }
                } else if (togTime == 2) {
                    if (differFromNowTimeCalculate(day, month, year, hour, min)) {
                        end_date_display.setText(val);
                        off_time.setText(valHour_final + ":" + valMin_final);
                        endTime = diffTime - startTime;
                    } else {
                        Toast.makeText(getContext(), "Previous time not applicable", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        Calendar cal = Calendar.getInstance();
        int h = cal.get(Calendar.HOUR_OF_DAY);
        int m = cal.get(Calendar.MINUTE);
        int style = AlertDialog.THEME_HOLO_LIGHT;
        timePickerDialog = new TimePickerDialog(getContext(), style, timeSetListener, h, m, true);
        timePickerDialog.show();
    }

    private boolean differFromNowTimeCalculate(int day, int month, int year, int hour, int min) {
        boolean bol = false;
        Calendar cal = Calendar.getInstance();
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH);
        monthNow++;
        int dayNow = cal.get(Calendar.DATE);
        int hourNow = cal.get(Calendar.HOUR_OF_DAY);
        int minNow = cal.get(Calendar.MINUTE);
        if (year > yearNow) {
            bol = true;
        } else if (year == yearNow) {
            if (month > monthNow) {
                bol = true;

            } else if (month == monthNow) {
                if (day > dayNow) {
                    bol = true;
                } else if (day == dayNow) {
                    if (hour > hourNow) {
                        bol = true;
                    } else if (hour == hourNow) {
                        bol = min >= minNow;
                    }
                }
            }
        }
        diffTime = (year - yearNow) * 365 * 24 * 60 + (month - monthNow) * 30 * 24 * 60 + (day - dayNow) * 24 * 60 + (hour - hourNow) * 60 + (min - minNow);
        return bol;
    }

    private String makeDateString(int day, int month, int year, int hour, int min) {
        String valMin = "", valHour = "";
        if (min < 10) {
            valMin = ("0" + String.valueOf(min));
        } else {
            valMin = String.valueOf(min);
        }
        if (hour < 10) {
            valHour = ("0" + String.valueOf(hour));
        } else {
            valHour = String.valueOf(hour);
        }
        valHour_final = valHour;
        valMin_final = valMin;
        return day + "-" + month + "-" + year;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        String currentDateString = DateFormat.getDateInstance(DateFormat.DEFAULT).format(calendar.getTime());

        if(flag == 1){
            start_date.setText(currentDateString);
        }else if(flag == 2){
            end_date.setText(currentDateString);
        }
        flag = 0;
    }


    private void setupGraph() {
        LineDataSet lineDataSet = new LineDataSet(new ArrayList<>(), "Temp");

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
        d.setText("Temp Graph");
        lineChart.setDescription(d);

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

    private void exportLogs() {
        if (!checkStoragePermission()) {
            return;
        }
        String csv = (requireContext().getExternalFilesDir(null).getAbsolutePath() + "/" + System.currentTimeMillis() + ".csv");
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(csv));

            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[]{"X", "Y"});
            for (int i = 0; i < logs.size(); i++) {
                String[] s = {String.valueOf(logs.get(i).getX()), String.valueOf(logs.get(i).getY())};
                data.add(s);
            }
            writer.writeAll(data); // data is adding to csv
            Toast.makeText(requireContext(), "Exported", Toast.LENGTH_LONG).show();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
                return false;
            }
        }
        return true;
    }

    private void clearLogs() {
        logs.clear();
        if (myService != null) {
            myService.clearEntries();
        }
    }

    private void stopLogging() {
        isLogging = false;
        if (myService != null) {
            myService.stopLogging(SetTempFragment.class);
        }
    }

    private void startLogging() {
        logs.clear();
        isLogging = true;

        Context context = requireContext();
        Intent intent = new Intent(context, ForegroundService.class);
        DatabaseReference ref = deviceRef.child("Data").child("TEMP1_VAL");
        ForegroundService.setInitials(TemperatureActivity.DEVICE_ID, ref, SetTempFragment.class, start, TemperatureActivity.deviceType + "set");
        context.startService(intent);
        context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (service instanceof ForegroundService.MyBinder) {
                    myService = ((ForegroundService.MyBinder) service).getService();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        plotGraphNotifier.stop();
    }

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

        LineDataSet lds = new LineDataSet(entries, "Temp");

        lds.setLineWidth(2);
        lds.setCircleRadius(4);
        lds.setValueTextSize(10);

        ArrayList<ILineDataSet> ds = new ArrayList<>();
        ds.add(lds);

        LineData ld = new LineData(ds);
        lineChart.setData(ld);
        lineChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();

        start = System.currentTimeMillis();

        if (ForegroundService.isMyTypeRunning(TemperatureActivity.DEVICE_ID, SetTempFragment.class, TemperatureActivity.deviceType + "set")) {
            llStart.setVisibility(View.INVISIBLE);
            llStop.setVisibility(View.VISIBLE);
            llClear.setVisibility(View.INVISIBLE);
            llExport.setVisibility(View.INVISIBLE);

            start = ForegroundService.start;

            Intent intent = new Intent(requireContext(), ForegroundService.class);
            requireContext().bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if (service instanceof ForegroundService.MyBinder) {
                        myService = ((ForegroundService.MyBinder) service).getService();
                        ArrayList<Entry> entries = myService.getEntries();
                        logs.clear();
                        logs.addAll(entries);

                        lineChart.getLineData().clearValues();

                        LineDataSet lineDataSet = new LineDataSet(logs, "Temp");

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
            }, 0);
        }

        plotGraphNotifier = new PlotGraphNotifier(Dashboard.GRAPH_PLOT_DELAY, () -> {
            if (skipCount < skipPoints) {
                skipCount++;
                return;
            }
            skipCount = 0;
            long seconds = (System.currentTimeMillis() - start) / 1000;
            LineData data = lineChart.getData();
            Entry entry = new Entry(seconds, temp);
            entriesOriginal.add(entry);
            data.addEntry(entry, 0);
            lineChart.notifyDataSetChanged();
            data.notifyDataChanged();
            lineChart.invalidate();
            if (isLogging) {
                logs.add(entry);
            }
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

    private void setupListeners() {

        deviceRef.child("Data").child("TEMP1_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Integer temp = snapshot.getValue(Integer.class);
                if (temp == null) return;
                if (initialValue) {
                    initialValue = false;
                }
                SetTempFragment.this.temp = temp;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
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
}
