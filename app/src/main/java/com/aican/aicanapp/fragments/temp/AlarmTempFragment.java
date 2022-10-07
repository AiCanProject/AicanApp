package com.aican.aicanapp.fragments.temp;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.aican.aicanapp.R;
import com.aican.aicanapp.fragments.ph.phAlarmFragment;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.specificactivities.TemperatureActivity;
import com.aican.aicanapp.utils.AlarmConstants;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class AlarmTempFragment extends Fragment {

    DatabaseReference deviceRef;
    Button startAlarm, stopAlarm;
    EditText tempValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_temp_alarm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startAlarm = view.findViewById(R.id.startAlarm);
        stopAlarm = view.findViewById(R.id.stopAlarm);
        tempValue = view.findViewById(R.id.tempValue);

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(TemperatureActivity.DEVICE_ID)).getReference().child("TEMP_CONTROLLER").child(TemperatureActivity.DEVICE_ID);
        fetchTempValue();

        stopAlarm.setEnabled(false);
        if(AlarmConstants.tempRingtone == null)
            AlarmConstants.tempRingtone = RingtoneManager.getRingtone(getContext().getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));

        if(AlarmConstants.tempRingtone.isPlaying()){
            stopAlarm.setEnabled(true);
            startAlarm.setEnabled(false);
        }

        if(AlarmConstants.thresholdTemperature != null){
            tempValue.setText(""+AlarmConstants.thresholdTemperature);
        }

        startAlarm.setOnClickListener(view1 -> {
            String threshHoldTemp = tempValue.getText().toString();

            if(tempValue.getText().toString().isEmpty()){
                Toast.makeText(getContext(),"Please enter temperature" , Toast.LENGTH_SHORT).show();
            }else {
                Log.d("Alarm","Start alarm clicked");
                stopAlarm.setEnabled(true);
                AlarmConstants.thresholdTemperature = Float.parseFloat(threshHoldTemp);

                stopAlarm.setEnabled(true);
                startAlarm.setEnabled(false);
                AlarmConstants.isServiceTempAvailable = true;
                new alarmBackgroundService().execute(threshHoldTemp);
            }
        });

        stopAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmConstants.isServiceTempAvailable = false;
                if(AlarmConstants.tempRingtone.isPlaying()) {
                    AlarmConstants.tempRingtone.stop();
                }


                stopAlarm.setEnabled(false);
                startAlarm.setEnabled(true);
            }
        });
    }

    private void fetchTempValue(){
        deviceRef.child("Data").child("TEMP1_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String tempString = snapshot.getValue(String.class);
                Log.d("AlarmFragment","AlarmFragment: onDataChange  "+tempString);
                Float temp = Float.parseFloat(tempString);
                AlarmConstants.temperature = temp;
                if (temp == null) return;

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }

    public class alarmBackgroundService extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... strings) {
            while(AlarmConstants.isServiceTempAvailable){
//                if(AlarmConstants.ringtone.isPlaying()) break;
                Float threshHoldTemp = Float.parseFloat(strings[0]);
                Log.d("AlarmFragment","AlarmFragment: doInBackground"+AlarmConstants.temperature+"  "+threshHoldTemp);
                if(AlarmConstants.temperature.equals(threshHoldTemp)){
                    Log.d("AlarmFragment","AlarmFragment: doInBackground in if ");
                    if(!AlarmConstants.tempRingtone.isPlaying() && AlarmConstants.tempRingtone!=null)
                        AlarmConstants.tempRingtone.play();
                }else if(AlarmConstants.tempRingtone.isPlaying()){
                    AlarmConstants.tempRingtone.stop();
                }

            }

            return null;
        }
    }
}
