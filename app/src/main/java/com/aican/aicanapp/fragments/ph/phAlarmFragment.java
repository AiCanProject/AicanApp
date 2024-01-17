package com.aican.aicanapp.fragments.ph;

import static com.aican.aicanapp.utils.Constants.SERVER_PATH;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
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
import com.aican.aicanapp.Services.alarmBackgroundService;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.utils.AlarmConstants;
import com.aican.aicanapp.utils.Constants;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class phAlarmFragment extends Fragment {

    RadioGroup radioGroup;
    DatabaseReference deviceRef;
    Button alarm, stopAlarm;
    Ringtone ringtone;
    String phForm;
    EditText phValue;
    EditText maxPhValue;
    RadioButton radioButton;
    Intent serviceIntent;
    float ph = 0;
    WebSocket webSocket1;
    JSONObject jsonData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ph_alarm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        radioGroup = view.findViewById(R.id.groupradio);
        alarm = view.findViewById(R.id.startAlarm);
        stopAlarm = view.findViewById(R.id.stopAlarm);
        phValue = view.findViewById(R.id.etPhValue);
        maxPhValue = view.findViewById(R.id.maxPhValue);

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);

        deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float ph = snapshot.getValue(Float.class);
                AlarmConstants.PH = ph;
                if (ph == null) return;
                phForm = String.format(Locale.UK, "%.2f", ph);
                phAlarmFragment.this.ph = ph;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(RadioGroup group, int checkedId)
//                    {
//                        RadioButton radioButton = (RadioButton)group.findViewById(checkedId);
//                    }
//                });

        stopAlarm.setEnabled(false);

        if (AlarmConstants.ringtone == null)
            AlarmConstants.ringtone = RingtoneManager.getRingtone(getContext().getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));

        if (AlarmConstants.ringtone.isPlaying()) {
            stopAlarm.setEnabled(true);
            alarm.setEnabled(false);
        }

        if (AlarmConstants.minPh != null && AlarmConstants.maxPh != null) {
            phValue.setText("" + AlarmConstants.minPh);
            maxPhValue.setText("" + AlarmConstants.maxPh);
        }
        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String minPH = phValue.getText().toString();
                String maxPH = maxPhValue.getText().toString();

                if (phValue.getText().toString().isEmpty() || maxPhValue.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter all values", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("Alarm", "Start alarm clicked");
                    stopAlarm.setEnabled(true);
                    AlarmConstants.maxPh = Float.parseFloat(minPH);
                    AlarmConstants.minPh = Float.parseFloat(maxPH);

                    stopAlarm.setEnabled(true);
                    alarm.setEnabled(false);
                    AlarmConstants.isServiceAvailable = true;
                    new alarmBackgroundService().execute(minPH, maxPH);
                }
            }
        });

        stopAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AlarmConstants.ringtone.isPlaying() || AlarmConstants.ringtone != null) {
                    AlarmConstants.ringtone.stop();
                }
                AlarmConstants.isServiceAvailable = false;
                stopAlarm.setEnabled(false);
                alarm.setEnabled(true);
            }
        });
    }

    public class alarmBackgroundService extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            while (AlarmConstants.isServiceAvailable) {
//                if(AlarmConstants.ringtone.isPlaying()) break;
                Float minPH = Float.parseFloat(strings[0]);
                Float maxPH = Float.parseFloat(strings[1]);
                Log.d("AlarmFragment", "AlarmFragment: doInBackground in if " + AlarmConstants.PH);
                if (AlarmConstants.PH < minPH || AlarmConstants.PH > maxPH) {
                    if (!AlarmConstants.ringtone.isPlaying() && AlarmConstants.ringtone != null)
                        AlarmConstants.ringtone.play();
                } else if (AlarmConstants.ringtone.isPlaying()) {
                    AlarmConstants.ringtone.stop();
                }

            }

            Log.d("AlarmFragment", "AlarmFragment: Service stopped " + AlarmConstants.PH);
            return null;
        }
    }

    private void initiateSocketConnection() {

        OkHttpClient client = new OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder().url(SERVER_PATH).build();
        webSocket1 = client.newWebSocket(request, new SocketListener());
    }

    private class SocketListener extends WebSocketListener {

        @Override
        public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable okhttp3.Response response) {
            super.onFailure(webSocket, t, response);
            webSocket.cancel();
            webSocket1.cancel();
            Log.e("WebSocketClosed", "onFailure " + (response != null ? response.message().toString() : null) + " " + t.getMessage());

        }

        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            super.onClosed(webSocket, code, reason);
            webSocket.cancel();
            webSocket1.cancel();
            Log.e("WebSocketClosed", "onClosed " + reason.toString());
        }

        @Override
        public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            super.onClosing(webSocket, code, reason);
            webSocket.cancel();
            webSocket1.cancel();
            Log.e("WebSocketClosed", "onClosing " + reason.toString());
        }

        @Override
        public void onOpen(WebSocket webSocket, okhttp3.Response response) {
            super.onOpen(webSocket, response);
//            webSocket1 = webSocket;

            if (webSocket1 == null) {
                webSocket.cancel();
            }

            getActivity().runOnUiThread(() -> {
//                calibrateBtn.setEnabled(true);
                Toast.makeText(getContext(),
                        "Socket Connection Successful!",
                        Toast.LENGTH_SHORT).show();

            });

            try {
                jsonData.put("SOCKET_INIT", "Successfully Initialized on phAlarmFragment");
                jsonData.put("DEVICE_ID", PhActivity.DEVICE_ID);
                webSocket.send(jsonData.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "Socket Connection Unsuccessful!",
                            Toast.LENGTH_SHORT).show();

                });
            }

        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);

            if (webSocket1 == null) {
                webSocket.cancel();
            }

            getActivity().runOnUiThread(() -> {
                try {
                    jsonData = new JSONObject(text);
                    Log.d("JSONReceived:PHFragment", "onMessage: " + text);
                    if (jsonData.has("PH_VAL") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                        float ph = Float.parseFloat(jsonData.getString("PH_VAL"));
                        String phForm = String.format(Locale.UK, "%.2f", ph);
                        AlarmConstants.PH = ph;

                    }
                    if (jsonData.has("TEMP_VAL") && jsonData.getString("DEVICE_ID").equals(PhActivity.DEVICE_ID)) {
                        String temp = jsonData.getString("TEMP_VAL");

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }

    }

    @Override
    public void onStart() {
//        initiateSocketConnection();
        if (Constants.OFFLINE_MODE) {
            initiateSocketConnection();
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        if (Constants.OFFLINE_MODE) {
            webSocket1.cancel();
        }
        super.onStop();
    }

}