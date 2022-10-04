package com.aican.aicanapp.fragments.pump;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.aican.aicanapp.R;
import com.aican.aicanapp.specificactivities.PumpActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class JobFragment extends Fragment {

    Spinner spin;
    CardView repeat, timer;
    TextView on_time_1, on_time_2, on_time_3, on_time_4, on_time_5;
    TextView off_time_1,off_time_2,off_time_3,off_time_4,off_time_5;
    TextView speed_1,speed_2,speed_3,speed_4,speed_5;
    TextView volume_1, volume_2,volume_3,volume_4,volume_5;
    int togTime = 1,speedTextNumber=0,volumeTextNumber=0;
    long diffTime, startTime, endTime;
    boolean ON_CLICKED = false;
    String valMin_final = "", valHour_final = "";
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    DatabaseReference deviceRef = null;
    EditText etSpeed,etVol, etTime, etName;
    int on_flag = 0, off_flag = 0;
    Button startBtnRep,stopBtnRep,startBtnTimer,stopBtnTimer;
    private int direction = 0;
    SwitchCompat clockwiseSwitch,antiClockwiseSwitch;
    int speedVol = 0;
    
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
        on_time_1 = view.findViewById(R.id.on_time_1);
        on_time_2 = view.findViewById(R.id.on_time_2);
        on_time_3 = view.findViewById(R.id.on_time_3);
        on_time_4 = view.findViewById(R.id.on_time_4);
        on_time_5 = view.findViewById(R.id.on_time_5);
        off_time_1 = view.findViewById(R.id.off_time_1);
        off_time_2 = view.findViewById(R.id.off_time_2);
        off_time_3 = view.findViewById(R.id.off_time_3);
        off_time_4 = view.findViewById(R.id.off_time_4);
        off_time_5 = view.findViewById(R.id.off_time_5);
        speed_1 = view.findViewById(R.id.speed_1);
        speed_2 = view.findViewById(R.id.speed_2);
        speed_3 = view.findViewById(R.id.speed_3);
        speed_4 = view.findViewById(R.id.speed_4);
        speed_5 = view.findViewById(R.id.speed_5);
        volume_1 = view.findViewById(R.id.volume_1);
        volume_2 = view.findViewById(R.id.volume_2);
        volume_3 = view.findViewById(R.id.volume_3);
        volume_4 = view.findViewById(R.id.volume_4);
        volume_5 = view.findViewById(R.id.volume_5);

        etName = view.findViewById(R.id.etName);
        etSpeed = view.findViewById(R.id.etSpeed);
        etTime = view.findViewById(R.id.etTime);
        etVol = view.findViewById(R.id.etVol);
        startBtnRep = view.findViewById(R.id.startBtnRep);
        stopBtnRep = view.findViewById(R.id.stopBtnRep);
        clockwiseSwitch = view.findViewById(R.id.clockwiseSwitch);
        antiClockwiseSwitch = view.findViewById(R.id.antiClockwiseSwitch);

        startBtnTimer = view.findViewById(R.id.startBtnTimer);
        stopBtnTimer = view.findViewById(R.id.stopBtnTimer);

//        Current date time and mtnth
        Calendar c= Calendar.getInstance();
        int cyear = c.get(Calendar.YEAR);
        int cmonth = c.get(Calendar.MONTH)+1;
        int cday = c.get(Calendar.DAY_OF_MONTH);

        Log.d("DateTime", "onViewCreated: "+cyear+" "+cmonth+" "+cday);
        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PumpActivity.DEVICE_ID)).getReference()
                .child("P_PUMP").child(PumpActivity.DEVICE_ID);
        
        on_time_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togTime = 1;
                on_flag = 1;
                off_flag = 1;
                initTimePicker(cday, cmonth, cyear);
                ON_CLICKED = true;
            }
        });

        on_time_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togTime = 1;
                on_flag = 2;
                off_flag = 2;
                initTimePicker(cday, cmonth, cyear);
                ON_CLICKED = true;
            }
        });

        on_time_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togTime = 1;
                on_flag = 3;
                off_flag = 3;
                initTimePicker(cday, cmonth, cyear);
                ON_CLICKED = true;
            }
        });

        on_time_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togTime = 1;
                on_flag = 4;
                off_flag = 4;
                initTimePicker(cday, cmonth, cyear);
                ON_CLICKED = true;
            }
        });

        on_time_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togTime = 1;
                on_flag = 5;
                off_flag = 5;
                initTimePicker(cday, cmonth, cyear);
                ON_CLICKED = true;
            }
        });

        off_time_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_flag = 1;
                off_flag = 1;

                if (!ON_CLICKED) {
                    Toast.makeText(getContext(), "Please select a On Time", Toast.LENGTH_SHORT).show();
                } else {
                    togTime = 2;
                    initTimePicker(cday, cmonth, cyear);
                }
            }
        });

        off_time_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_flag = 2;
                off_flag = 2;

                if (!ON_CLICKED) {
                    Toast.makeText(getContext(), "Please select a On Time", Toast.LENGTH_SHORT).show();
                } else {
                    togTime = 2;
                    initTimePicker(cday, cmonth, cyear);
                }
            }
        });

        off_time_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_flag = 3;
                off_flag = 3;

                if (!ON_CLICKED) {
                    Toast.makeText(getContext(), "Please select a On Time", Toast.LENGTH_SHORT).show();
                } else {
                    togTime = 2;
                    initTimePicker(cday, cmonth, cyear);
                }
            }
        });

        off_time_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_flag = 4;
                off_flag = 4;

                if (!ON_CLICKED) {
                    Toast.makeText(getContext(), "Please select a On Time", Toast.LENGTH_SHORT).show();
                } else {
                    togTime = 2;
                    initTimePicker(cday, cmonth, cyear);
                }
            }
        });

        off_time_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                on_flag = 5;
                off_flag = 5;

                if (!ON_CLICKED) {
                    Toast.makeText(getContext(), "Please select a On Time", Toast.LENGTH_SHORT).show();
                } else {
                    togTime = 2;
                    initTimePicker(cday, cmonth, cyear);
                }
            }
        });

        speed_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speedTextNumber = 1;
                speedVol = 1;
                selectSpeedVolumeDialog();
            }
        });

        speed_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speedTextNumber = 2;
                speedVol = 1;
                selectSpeedVolumeDialog();
            }
        });

        speed_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speedTextNumber = 3;
                speedVol = 1;
                selectSpeedVolumeDialog();
            }
        });

        speed_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speedTextNumber = 4;
                speedVol = 1;
                selectSpeedVolumeDialog();
            }
        });

        speed_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speedTextNumber = 5;
                speedVol = 1;
                selectSpeedVolumeDialog();
            }
        });

        volume_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                volumeTextNumber = 1;
                speedVol = 2;
                selectSpeedVolumeDialog();
            }
        });

        volume_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                volumeTextNumber = 2;
                speedVol = 2;
                selectSpeedVolumeDialog();
            }
        });

        volume_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                volumeTextNumber = 3;
                speedVol = 2;
                selectSpeedVolumeDialog();
            }
        });

        volume_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                volumeTextNumber = 4;
                speedVol = 2;
                selectSpeedVolumeDialog();
            }
        });

        volume_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                volumeTextNumber = 5;
                speedVol = 2;
                selectSpeedVolumeDialog();
            }
        });

        startBtnTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceRef.child("UI").child("Start").setValue(1);
                stopBtnTimer.setVisibility(View.VISIBLE);
                startBtnTimer.setVisibility(View.GONE);
                selectSpeedVolumeDialog();
            }
        });

        stopBtnTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceRef.child("UI").child("Start").setValue(0);
                stopBtnTimer.setVisibility(View.GONE);
                startBtnTimer.setVisibility(View.VISIBLE);
                selectSpeedVolumeDialog();
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

        startBtnRep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateValuesOnFirebase();
            }
        });

        stopBtnRep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetValuesOnFirebase();
            }
        });

        clockwiseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (clockwiseSwitch.isChecked()){
                    direction = 0;
                    if(antiClockwiseSwitch.isChecked()){
                        antiClockwiseSwitch.setChecked(false);
                    }
                }else{
                    antiClockwiseSwitch.setChecked(true);
                    direction = 1;
                }
            }
        });

        antiClockwiseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(antiClockwiseSwitch.isChecked()){
                    direction = 1;
                    if(clockwiseSwitch.isChecked()){
                        clockwiseSwitch.setChecked(false);
                    }
                }else{
                    clockwiseSwitch.setChecked(true);
                    direction = 0;
                }
            }
        });
    }

    private void selectSpeedVolumeDialog(){
        final Dialog dialog = new Dialog(getContext());
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.set_speed_volume_dialog_layout);
        dialog.show();

        Button cancelBtn, speedVolSetBtn;
        EditText value;
        TextView setSpeedVolText;

        cancelBtn = dialog.findViewById(R.id.cancelBtn);
        speedVolSetBtn = dialog.findViewById(R.id.set_speed_vol);
        value = dialog.findViewById(R.id.setSpeedVol);
        setSpeedVolText = (TextView) dialog.findViewById(R.id.setSpeedVolText);

        if(speedVol ==1){
            setSpeedVolText.setText(R.string.set_Speed);
            speedVolSetBtn.setText(R.string.set_Speed);
        }else if(speedVol == 2){
            setSpeedVolText.setText(R.string.set_volume);
            speedVolSetBtn.setText(R.string.set_volume);
        }

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        speedVolSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!value.getText().toString().isEmpty()) {
                    if (speedVol == 1) {
                        int speed = Integer.parseInt(value.getText().toString());

                        if (speedTextNumber == 1) {
                            speed_1.setText(""+speed);
                            deviceRef.child("UI").child("SPEED_1").setValue(speed);
                        }else if(speedTextNumber == 2){
                            speed_2.setText(""+speed);
                            deviceRef.child("UI").child("SPEED_2").setValue(speed);
                        }else if(speedTextNumber == 3){
                            speed_3.setText(""+speed);
                            deviceRef.child("UI").child("SPEED_3").setValue(speed);
                        }else if(speedTextNumber == 4){
                            speed_4.setText(""+speed);
                            deviceRef.child("UI").child("SPEED_4").setValue(speed);
                        }else if(speedTextNumber == 5){
                            speed_5.setText(""+speed);
                            deviceRef.child("UI").child("SPEED_5").setValue(speed);
                        }
                    }else if(speedVol == 2){
                        int volume = Integer.parseInt(value.getText().toString());

                        if (volumeTextNumber == 1) {
                            volume_1.setText(""+volume);
                            deviceRef.child("UI").child("VOLUME_1").setValue(volume);
                        }else if(volumeTextNumber == 2){
                            volume_2.setText(""+volume);
                            deviceRef.child("UI").child("VOLUME_2").setValue(volume);
                        }else if(volumeTextNumber == 3){
                            volume_3.setText(""+volume);
                            deviceRef.child("UI").child("VOLUME_3").setValue(volume);
                        }else if(volumeTextNumber == 4){
                            volume_4.setText(""+volume);
                            deviceRef.child("UI").child("VOLUME_4").setValue(volume);
                        }else if(volumeTextNumber == 5){
                            volume_5.setText(""+volume);
                            deviceRef.child("UI").child("VOLUME_5").setValue(volume);
                        }
                    }
                    dialog.dismiss();
                }else{
                    Toast.makeText(getContext(),"Please Enter value",Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void resetValuesOnFirebase() {
        deviceRef.child("UI").child("Speed").setValue(0);
        deviceRef.child("UI").child("Volume").setValue(0);
        deviceRef.child("UI").child("interval").setValue(0);
        deviceRef.child("UI").child("repetition").setValue(0);
        deviceRef.child("UI").child("Start").setValue(0);
        deviceRef.child("UI").child("Direction").setValue(0);
        stopBtnRep.setVisibility(View.GONE);
        startBtnRep.setVisibility(View.VISIBLE);
    }

    private void updateValuesOnFirebase() {
        if(etVol.getText().toString().isEmpty() || etTime.getText().toString().isEmpty() || etSpeed.getText().toString().isEmpty() || etName.getText().toString().isEmpty()){
            Toast.makeText(getContext(), "Please enter all values", Toast.LENGTH_SHORT).show();
        }else{
            deviceRef.child("UI").child("Speed").setValue(Integer.parseInt(etSpeed.getText().toString()));
            deviceRef.child("UI").child("Volume").setValue(Integer.parseInt(etVol.getText().toString()));
            deviceRef.child("UI").child("interval").setValue(Integer.parseInt(etTime.getText().toString()));
            deviceRef.child("UI").child("repetition").setValue(Integer.parseInt(etName.getText().toString()));
            deviceRef.child("UI").child("Start").setValue(1);
            deviceRef.child("UI").child("Direction").setValue(direction);
            stopBtnRep.setVisibility(View.VISIBLE);
            startBtnRep.setVisibility(View.GONE);
        }
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
                            on_time_1.setText(valHour_final + ":" + valMin_final);
                            deviceRef.child("UI").child("TIMER_ON_1").setValue(valHour_final + ":" + valMin_final);
                        } else if (on_flag == 2){
                            on_time_2.setText(valHour_final + ":" + valMin_final);
                            deviceRef.child("UI").child("TIMER_ON_2").setValue(valHour_final + ":" + valMin_final);
                        } else if (on_flag == 3){
                            on_time_3.setText(valHour_final + ":" + valMin_final);
                            deviceRef.child("UI").child("TIMER_ON_3").setValue(valHour_final + ":" + valMin_final);
                        } else if (on_flag == 4){
                            on_time_4.setText(valHour_final + ":" + valMin_final);
                            deviceRef.child("UI").child("TIMER_ON_4").setValue(valHour_final + ":" + valMin_final);
                        } else if (on_flag == 5){
                            on_time_5.setText(valHour_final + ":" + valMin_final);
                            deviceRef.child("UI").child("TIMER_ON_5").setValue(valHour_final + ":" + valMin_final);
                        }
                        
                        startTime = diffTime;
                    } else {
                        Toast.makeText(getContext(), "Previous time not applicable", Toast.LENGTH_SHORT).show();
                    }
                } else if (togTime == 2) {
                    if (differFromNowTimeCalculate(day, month, year, hour, min)) {
                        if(on_flag == 1){
                            off_time_1.setText(valHour_final + ":" + valMin_final);
                            deviceRef.child("UI").child("TIMER_OFF_1").setValue(valHour_final + ":" + valMin_final);
                        } else if (on_flag == 2){
                            off_time_2.setText(valHour_final + ":" + valMin_final);
                            deviceRef.child("UI").child("TIMER_OFF_2").setValue(valHour_final + ":" + valMin_final);
                        } else if (on_flag == 3){
                            off_time_3.setText(valHour_final + ":" + valMin_final);
                            deviceRef.child("UI").child("TIMER_OFF_3").setValue(valHour_final + ":" + valMin_final);
                        } else if (on_flag == 4){
                            off_time_4.setText(valHour_final + ":" + valMin_final);
                            deviceRef.child("UI").child("TIMER_OFF_4").setValue(valHour_final + ":" + valMin_final);
                        } else if (on_flag == 5){
                            off_time_5.setText(valHour_final + ":" + valMin_final);
                            deviceRef.child("UI").child("TIMER_OFF_5").setValue(valHour_final + ":" + valMin_final);
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