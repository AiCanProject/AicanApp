package com.aican.aicanappnoncfr.fragments.temp;

import android.media.Ringtone;
import android.media.RingtoneManager;
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

import com.aican.aicanappnoncfr.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class AlarmTempFragment extends Fragment {

    RadioGroup radioGroup;
    // DatabaseReference deviceRef;
    Button alarm, stopAlarm;
    Ringtone ringtone;
    String phForm;
    EditText phValue;
    RadioButton radioButton;
    float ph = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alarm_temp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        radioGroup = view.findViewById(R.id.groupradio);
        alarm = view.findViewById(R.id.startAlarm);
        stopAlarm = view.findViewById(R.id.stopAlarm);
        phValue = view.findViewById(R.id.etPhValue);

        //   deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);

       /* deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ph = snapshot.getValue(Float.class);
                if (ph == null) return;
                phForm = String.format(Locale.UK, "%.2f", ph);
                AlarmTempFragment.this.ph = ph;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });*/

//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(RadioGroup group, int checkedId)
//                    {
//                        RadioButton radioButton = (RadioButton)group.findViewById(checkedId);
//                    }
//                });

        ringtone = RingtoneManager.getRingtone(getContext().getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        if(!ringtone.isPlaying()){
            stopAlarm.setEnabled(false);
        }

        alarm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                String phVal = phValue.getText().toString();
                Float phV = Float.parseFloat(phVal);
                Float phFire = Float.parseFloat(phForm);

                if (selectedId == -1) {
                    Toast.makeText(requireContext(), "No answer has been selected", Toast.LENGTH_SHORT).show();
                }
                else {
                    radioButton = (RadioButton)radioGroup.findViewById(selectedId);

                    if(radioButton.getText().toString().equals("Greater than")){
                        if(phV > phFire){
                            ringtone.play();
                            alarm.setEnabled(false);
                            Toast.makeText(requireContext(),"Greater1", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(),"Lesser1", Toast.LENGTH_SHORT).show();
                        }
                    } else if(radioButton.getText().toString().equals("Less than")){
                        if(phV < phFire){
                            Toast.makeText(requireContext(),"Less", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(),"Great", Toast.LENGTH_SHORT).show();
                        }
                    }

                }

                if(ringtone.isPlaying()){
                    stopAlarm.setEnabled(true);
                    stopAlarm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ringtone.stop();
                            stopAlarm.setEnabled(false);
                            alarm.setEnabled(true);
                        }
                    });
                }
            }
        });
    }
}
