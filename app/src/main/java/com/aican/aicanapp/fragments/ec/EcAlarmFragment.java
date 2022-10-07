package com.aican.aicanapp.fragments.ec;

import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.R;
import com.aican.aicanapp.fragments.temp.AlarmTempFragment;
import com.aican.aicanapp.specificactivities.EcActivity;
import com.aican.aicanapp.utils.AlarmConstants;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EcAlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EcAlarmFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    DatabaseReference deviceRef;
    Button startAlarm, stopAlarm;
    EditText ecValue;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EcAlarmFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EcAlarmFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EcAlarmFragment newInstance(String param1, String param2) {
        EcAlarmFragment fragment = new EcAlarmFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ec_alarm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startAlarm = view.findViewById(R.id.startAlarm);
        stopAlarm = view.findViewById(R.id.stopAlarm);
        ecValue = view.findViewById(R.id.ecValue);


        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(EcActivity.DEVICE_ID)).getReference().child("ECMETER").child(EcActivity.DEVICE_ID);
        fetchTempValue();

        stopAlarm.setEnabled(false);
        if(AlarmConstants.ecRingtone == null)
            AlarmConstants.ecRingtone = RingtoneManager.getRingtone(getContext().getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));

        if(AlarmConstants.ecRingtone.isPlaying()){
            stopAlarm.setEnabled(true);
            startAlarm.setEnabled(false);
        }

        if(AlarmConstants.thresholdECValue != null){
            ecValue.setText(""+AlarmConstants.thresholdECValue);
        }

        startAlarm.setOnClickListener(view1 -> {
            String threshHoldEC = ecValue.getText().toString();

            if(ecValue.getText().toString().isEmpty()){
                Toast.makeText(getContext(),"Please enter temperature" , Toast.LENGTH_SHORT).show();
            }else {
                Log.d("Alarm","Start alarm clicked");
                stopAlarm.setEnabled(true);
                AlarmConstants.thresholdECValue = Float.parseFloat(threshHoldEC);

                stopAlarm.setEnabled(true);
                startAlarm.setEnabled(false);
                AlarmConstants.isServiceECAvailable = true;
                new alarmBackgroundService().execute(threshHoldEC);
            }
        });

        stopAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmConstants.isServiceECAvailable = false;
                if(AlarmConstants.ecRingtone.isPlaying()) {
                    AlarmConstants.ecRingtone.stop();
                }
                stopAlarm.setEnabled(false);
                startAlarm.setEnabled(true);
            }
        });

    }

    private void fetchTempValue(){
        deviceRef.child("Data").child("EC_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ecVal = snapshot.getValue(Float.class);
                Log.d("AlarmFragment","AlarmFragment: onDataChange  "+ecVal);
                AlarmConstants.ECValue = ecVal;
                if (ecVal == null) return;

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }
    public class alarmBackgroundService extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... strings) {
            while(AlarmConstants.isServiceECAvailable){
//                if(AlarmConstants.ringtone.isPlaying()) break;
                Float threshHoldEC = Float.parseFloat(strings[0]);
                Log.d("AlarmFragment","AlarmFragment: doInBackground"+AlarmConstants.temperature+"  "+threshHoldEC);
                if(AlarmConstants.ECValue.equals(threshHoldEC)){
                    Log.d("AlarmFragment","AlarmFragment: doInBackground in if ");
                    if(!AlarmConstants.ecRingtone.isPlaying() && AlarmConstants.ecRingtone!=null)
                        AlarmConstants.ecRingtone.play();
                }else if(AlarmConstants.ecRingtone.isPlaying()){
                    AlarmConstants.ecRingtone.stop();
                }

            }

            return null;
        }
    }
}