package com.aican.aicanapp.specificactivities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.ProbeScan.ProbeScanner;
import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.fragments.ph.PhCalibFragment;
import com.aican.aicanapp.fragments.ph.PhCalibFragmentNew;
import com.aican.aicanapp.fragments.ph.PhFragment;
import com.aican.aicanapp.fragments.ph.phAlarmFragment;
import com.aican.aicanapp.fragments.ph.phGraphFragment;
import com.aican.aicanapp.fragments.ph.phLogFragment;
import com.aican.aicanapp.interfaces.DeviceConnectionInfo;
import com.aican.aicanapp.utils.Constants;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PhActivity extends AppCompatActivity implements View.OnClickListener, DeviceConnectionInfo {

    TextView ph, calibrate, log, graph, alarm, tabItemPh, tabItemCalib;
    DatabaseReference deviceRef;

    boolean connectedWebsocket = false;

    Switch offlineModeSwitch;

    PhFragment phFragment = new PhFragment();
    PhCalibFragment phCalibFragment = new PhCalibFragment();
    phLogFragment phLogFragment = new phLogFragment();
    phGraphFragment phGraphFragment = new phGraphFragment();
    phAlarmFragment phAlarmFragment = new phAlarmFragment();
    PhCalibFragmentNew phCalibFragmentNew = new PhCalibFragmentNew(this);
    DatabaseHelper databaseHelper;

    public static String DEVICE_ID = null;

    TextView offlineMode, onlineMode, notice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setTheme(R.style.AppTheme_Light);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ph);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.btnColor));

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        DEVICE_ID = getIntent().getStringExtra(Dashboard.KEY_DEVICE_ID);
        if (DEVICE_ID == null) {
            throw new RuntimeException();
        }

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);

        deviceRef.child("Data").child("AUTOLOG").setValue(0);

        deviceRef.child("Data").child("AUTOLOG").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Source.auto_log = snapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
        databaseHelper = new DatabaseHelper(PhActivity.this);

        offlineModeSwitch = findViewById(R.id.offlineModeSwitch);
        ph = findViewById(R.id.item1);
        calibrate = findViewById(R.id.item2);
        log = findViewById(R.id.item3);
        graph = findViewById(R.id.item4);
        alarm = findViewById(R.id.item5);
        offlineMode = findViewById(R.id.offlineMode);
        onlineMode = findViewById(R.id.onlineMode);
        notice = findViewById(R.id.notice);
        tabItemPh = findViewById(R.id.tabItemP);
        tabItemCalib = findViewById(R.id.select2);

        ph.setOnClickListener(this);
        calibrate.setOnClickListener(this);
        log.setOnClickListener(this);
        graph.setOnClickListener(this);
        alarm.setOnClickListener(this);

        onlineMode.setVisibility(View.VISIBLE);
        offlineMode.setVisibility(View.GONE);
        notice.setVisibility(View.GONE);

        if (Constants.OFFLINE_DATA && Constants.OFFLINE_MODE) {
            onlineMode.setVisibility(View.GONE);
            offlineMode.setVisibility(View.VISIBLE);
        } else {
            onlineMode.setVisibility(View.VISIBLE);
            offlineMode.setVisibility(View.GONE);

            if (Constants.OFFLINE_MODE) {
                notice.setVisibility(View.GONE);
            }
            if (Constants.OFFLINE_DATA) {
                notice.setVisibility(View.VISIBLE);
                notice.setText("Device is not connected");
                onlineMode.setVisibility(View.GONE);
                offlineMode.setVisibility(View.VISIBLE);
            }
        }


        String i = getIntent().getStringExtra("refreshCalib");
        if (i != null) {
            if (i.equals("y")) {
                tabItemPh.setBackground(getResources().getDrawable(R.drawable.backselect1));
                tabItemPh.setVisibility(View.INVISIBLE);
                TextView select2 = findViewById(R.id.select2);
                select2.setBackground(getResources().getDrawable(R.drawable.back_select2));

                loadFragments(phCalibFragmentNew);
                calibrate.setTextColor(Color.WHITE);
                ph.setTextColor(Color.parseColor("#FF24003A"));
                log.setTextColor(Color.parseColor("#FF24003A"));
                graph.setTextColor(Color.parseColor("#FF24003A"));
                alarm.setTextColor(Color.parseColor("#FF24003A"));
//                Toast.makeText(this, "" + calibrate.getWidth(), Toast.LENGTH_SHORT).show();
//                int size = calibrate.getWidth();
//                tabItemPh.animate().x(size).setDuration(100);

            }
        } else {
            loadFragments(phFragment);
        }


        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        deviceRef.child("PH_MODE").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @com.google.firebase.database.annotations.NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    PhCalibFragment.PH_MODE = snapshot.getValue(String.class);
                } else {
                    deviceRef.child("PH_MODE").setValue("both");
                    PhCalibFragment.PH_MODE = "both";
                }
            }

            @Override
            public void onCancelled(@NonNull @com.google.firebase.database.annotations.NotNull DatabaseError error) {
            }
        });

        if (Constants.OFFLINE_DATA) {

            offlineModeSwitch.setChecked(true);
            offlineModeSwitch.setText("Disconnect");

            offlineModeSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (offlineModeSwitch.isChecked()) {
                        offlineModeSwitch.setText("Reconnect");

                        if (Source.activeFragment == 1) {
                            phCalibFragmentNew.receiveDataFromPhActivity("Connect", PhActivity.DEVICE_ID, lastJsonData);
                        }
                    } else {
                        offlineModeSwitch.setText("Disconnect");

                        if (Source.activeFragment == 1) {

                            phCalibFragmentNew.receiveDataFromPhActivity("Disconnect", PhActivity.DEVICE_ID, lastJsonData);
                        }
                    }
                }
            });
        }

        offlineModeSwitch.setVisibility(View.GONE);

    }

    @Override
    public void onClick(View view) {
        if (Source.auto_log == 0 && !Source.calibratingNow) {
            TextView select2 = findViewById(R.id.select2);
            select2.setBackground(getResources().getDrawable(R.drawable.backselect1));
            select2.setVisibility(View.INVISIBLE);
            tabItemPh.setVisibility(View.VISIBLE);
            tabItemPh.setBackground(getResources().getDrawable(R.drawable.back_select2));

            if (view.getId() == R.id.item1) {
                offlineModeSwitch.setVisibility(View.GONE);
                tabItemPh.animate().x(0).setDuration(100);
                loadFragments(phFragment);
                ph.setTextColor(Color.WHITE);
                calibrate.setTextColor(Color.parseColor("#FF24003A"));
                log.setTextColor(Color.parseColor("#FF24003A"));
                graph.setTextColor(Color.parseColor("#FF24003A"));
                alarm.setTextColor(Color.parseColor("#FF24003A"));
            } else if (view.getId() == R.id.item2) {
                offlineModeSwitch.setVisibility(View.VISIBLE);

//                loadFragments(phCalibFragment);
                loadFragments(phCalibFragmentNew);

                calibrate.setTextColor(Color.WHITE);
                ph.setTextColor(Color.parseColor("#FF24003A"));
                log.setTextColor(Color.parseColor("#FF24003A"));
                graph.setTextColor(Color.parseColor("#FF24003A"));
                alarm.setTextColor(Color.parseColor("#FF24003A"));
                int size = calibrate.getWidth();
                tabItemPh.animate().x(size).setDuration(100);

            } else if (view.getId() == R.id.item3) {
                offlineModeSwitch.setVisibility(View.GONE);

                loadFragments(phLogFragment);
                log.setTextColor(Color.WHITE);
                ph.setTextColor(Color.parseColor("#FF24003A"));
                calibrate.setTextColor(Color.parseColor("#FF24003A"));
                graph.setTextColor(Color.parseColor("#FF24003A"));
                alarm.setTextColor(Color.parseColor("#FF24003A"));
                int size = calibrate.getWidth() * 2;
                tabItemPh.animate().x(size).setDuration(100);

            } else if (view.getId() == R.id.item4) {
                offlineModeSwitch.setVisibility(View.GONE);

                loadFragments(phGraphFragment);

                graph.setTextColor(Color.WHITE);
                ph.setTextColor(Color.parseColor("#FF24003A"));
                calibrate.setTextColor(Color.parseColor("#FF24003A"));
                log.setTextColor(Color.parseColor("#FF24003A"));
                alarm.setTextColor(Color.parseColor("#FF24003A"));
                int size = calibrate.getWidth() * 3;
                tabItemPh.animate().x(size).setDuration(100);

            } else if (view.getId() == R.id.item5) {
                loadFragments(phAlarmFragment);

                alarm.setTextColor(Color.WHITE);
                ph.setTextColor(Color.parseColor("#FF24003A"));
                calibrate.setTextColor(Color.parseColor("#FF24003A"));
                graph.setTextColor(Color.parseColor("#FF24003A"));
                log.setTextColor(Color.parseColor("#FF24003A"));
                int size = calibrate.getWidth() * 4;
                tabItemPh.animate().x(size).setDuration(100);
            } else if (view.getId() == R.id.cLProbes) {
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                databaseHelper.insert_action_data(time, date, "Probe Scanner : " + Source.logUserName, "", "", "", "", PhActivity.DEVICE_ID);

                Intent intent = new Intent(PhActivity.this, ProbeScanner.class);
                intent.putExtra("activity", "PhFragment");
//            intent.addFlags()
                startActivity(intent);
            }
        } else {
            Toast.makeText(this, "You cannot change fragment while calibrating / logging", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (Source.auto_log == 0 && !Source.calibratingNow) {
//            Intent intent = new Intent(PhActivity.this, Dashboard.class);
//            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "You cannot change fragment while logging / calibrating", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean loadFragments(Fragment fragment) {
        if (fragment != null) {
            Log.d("navigation", "loadFragments: Frag is loaded");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, fragment)
                    .addToBackStack(null)
                    .commit();

            deviceRef.child("UI").child("PH").child("PH_CAL").child("CAL").setValue(0);

            return true;
        }
        return false;
    }

    String frag = "na";
    JSONObject lastJsonData;

    @Override
    public void onDisconnect(String frag, String deviceID, String message, JSONObject lastJsonData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                offlineModeSwitch.setChecked(false);
                offlineModeSwitch.setText("Reconnect");

            }
        });
        this.frag = frag;
        this.lastJsonData = lastJsonData;
    }

    @Override
    public void onReconnect(String frag, String deviceID, String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                offlineModeSwitch.setChecked(true);
                offlineModeSwitch.setText("Disconnect");
            }
        });
        this.frag = frag;

    }
}