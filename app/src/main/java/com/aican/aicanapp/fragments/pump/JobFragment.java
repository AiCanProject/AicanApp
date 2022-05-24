package com.aican.aicanapp.fragments.pump;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.aican.aicanapp.R;

import java.util.Calendar;

public class JobFragment extends Fragment {

    Spinner spin;
    CardView repeat, timer;
    TextView ph1, ph2, ph3, ph4, ph5, mv1, mv2, mv3, mv4, mv5;
    int togTime = 1;
    long diffTime, startTime, endTime;
    boolean ON_CLICKED = false;
    String valMin_final = "", valHour_final = "";
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    int on_flag = 0, off_flag = 0;
    
    public JobFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pump_job, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spin = view.findViewById(R.id.jobMode);
        repeat = view.findViewById(R.id.repeatation);
        timer = view.findViewById(R.id.timer);
        ph1 = view.findViewById(R.id.ph1);
        ph2 = view.findViewById(R.id.ph2);
        ph3 = view.findViewById(R.id.ph3);
        ph4 = view.findViewById(R.id.ph4);
        ph5 = view.findViewById(R.id.ph5);
        mv1 = view.findViewById(R.id.mv1);
        mv2 = view.findViewById(R.id.mv2);
        mv3 = view.findViewById(R.id.mv3);
        mv4 = view.findViewById(R.id.mv4);
        mv5 = view.findViewById(R.id.mv5);
        
        ph1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togTime = 1;
                on_flag = 1;
                off_flag = 1;
                initTimePicker(25, 5, 2022);
                ON_CLICKED = true;
            }
        });

        ph2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togTime = 1;
                on_flag = 2;
                off_flag = 2;
                initTimePicker(25, 5, 2022);
                ON_CLICKED = true;
            }
        });

        ph3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togTime = 1;
                on_flag = 3;
                off_flag = 3;
                initTimePicker(25, 5, 2022);
                ON_CLICKED = true;
            }
        });

        ph4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togTime = 1;
                on_flag = 4;
                off_flag = 4;
                initTimePicker(25, 5, 2022);
                ON_CLICKED = true;
            }
        });

        ph5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togTime = 1;
                on_flag = 5;
                off_flag = 5;
                initTimePicker(25, 5, 2022);
                ON_CLICKED = true;
            }
        });

        mv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_flag = 1;
                off_flag = 1;

                if (!ON_CLICKED) {
                    Toast.makeText(getContext(), "Please select a On Time", Toast.LENGTH_SHORT).show();
                } else {
                    togTime = 2;
                    initTimePicker(25, 5, 2022);
                }
            }
        });

        mv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_flag = 2;
                off_flag = 2;

                if (!ON_CLICKED) {
                    Toast.makeText(getContext(), "Please select a On Time", Toast.LENGTH_SHORT).show();
                } else {
                    togTime = 2;
                    initTimePicker(25, 5, 2022);
                }
            }
        });

        mv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_flag = 3;
                off_flag = 3;

                if (!ON_CLICKED) {
                    Toast.makeText(getContext(), "Please select a On Time", Toast.LENGTH_SHORT).show();
                } else {
                    togTime = 2;
                    initTimePicker(25, 5, 2022);
                }
            }
        });

        mv4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_flag = 4;
                off_flag = 4;

                if (!ON_CLICKED) {
                    Toast.makeText(getContext(), "Please select a On Time", Toast.LENGTH_SHORT).show();
                } else {
                    togTime = 2;
                    initTimePicker(25, 5, 2022);
                }
            }
        });

        mv5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_flag = 5;
                off_flag = 5;

                if (!ON_CLICKED) {
                    Toast.makeText(getContext(), "Please select a On Time", Toast.LENGTH_SHORT).show();
                } else {
                    togTime = 2;
                    initTimePicker(25, 5, 2022);
                }
            }
        });

        String[] spinselect = {"Repeatation","Timer"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, spinselect);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        repeat.setVisibility(View.VISIBLE);
                        timer.setVisibility(View.GONE);
                        break;
                    case 1:
                        timer.setVisibility(View.VISIBLE);
                        repeat.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(requireContext(), "Select a mode of callibration", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initTimePicker(int day, int month, int year) {
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int min) {
                String val = makeDateString(day, month, year, hour, min);
                if (togTime == 1) {
                    if (differFromNowTimeCalculate(day, month, year, hour, min)) {
                        if(on_flag == 1){
                            ph1.setText(valHour_final + ":" + valMin_final);
                        } else if (on_flag == 2){
                            ph2.setText(valHour_final + ":" + valMin_final);
                        } else if (on_flag == 3){
                            ph3.setText(valHour_final + ":" + valMin_final);
                        } else if (on_flag == 4){
                            ph4.setText(valHour_final + ":" + valMin_final);
                        } else if (on_flag == 5){
                            ph5.setText(valHour_final + ":" + valMin_final);
                        }
                        
                        startTime = diffTime;
                    } else {
                        Toast.makeText(getContext(), "Previous time not applicable", Toast.LENGTH_SHORT).show();
                    }
                } else if (togTime == 2) {
                    if (differFromNowTimeCalculate(day, month, year, hour, min)) {
                        if(on_flag == 1){
                            mv1.setText(valHour_final + ":" + valMin_final);
                        } else if (on_flag == 2){
                            mv2.setText(valHour_final + ":" + valMin_final);
                        } else if (on_flag == 3){
                            mv3.setText(valHour_final + ":" + valMin_final);
                        } else if (on_flag == 4){
                            mv4.setText(valHour_final + ":" + valMin_final);
                        } else if (on_flag == 5){
                            mv5.setText(valHour_final + ":" + valMin_final);
                        }
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
}