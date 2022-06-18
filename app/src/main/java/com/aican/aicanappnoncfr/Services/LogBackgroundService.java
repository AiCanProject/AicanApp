package com.aican.aicanappnoncfr.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aican.aicanappnoncfr.Source;
import com.aican.aicanappnoncfr.data.DatabaseHelper;
import com.aican.aicanappnoncfr.fragments.ph.phLogFragment;
import com.aican.aicanappnoncfr.specificactivities.PhActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogBackgroundService extends Service {

    DatabaseReference deviceReference;
    String ph, temp, mv, date, time, batchnum, arnum, compound_name;
    Integer hold, counter;
    DatabaseHelper dbHelper;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        deviceReference = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        dbHelper = new DatabaseHelper(getApplicationContext());

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            deviceReference.child("Data").child("HOLD").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int Hold = snapshot.getValue(Integer.class);
                                    hold = Hold;

                                    if (hold == 1) {
                                        Log.d("service", "Value is Stable");
                                        fetch_logs();

                                        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                        time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                                        fetch_logs();

                                        if (ph == null || temp == null || mv == null) {
                                            Toast.makeText(getApplicationContext(), "Fetching Data", Toast.LENGTH_SHORT).show();
                                        } else {
                                            dbHelper.print_insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name);
                                            dbHelper.insert_log_data(date, time, ph, temp, batchnum, arnum, compound_name);
                                            deviceReference.child("Data").child("HOLD").setValue(0);
                                            Toast.makeText(getApplicationContext(), "BG Service data", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Log.d("notstable", "Wait for ph to be stable");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });


                            try {

                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).start();
        return super.onStartCommand(intent, flags, startId);
    }

    private void fetch_logs() {

        deviceReference.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float p = snapshot.getValue(Float.class);
                ph = String.format(Locale.UK, "%.2f", p);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceReference.child("Data").child("TEMP_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float temper = snapshot.getValue(Float.class);
                temp = String.format(Locale.UK, "%.2f", temper);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceReference.child("Data").child("COMPOUND_NAME").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                compound_name = (String) snapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceReference.child("Data").child("BATCH_NUMBER").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                batchnum = (String) snapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceReference.child("Data").child("AR_NUMBER").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                arnum = (String) snapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        deviceReference.child("Data").child("EC_VAL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Float p = snapshot.getValue(Float.class);
                mv = String.format(Locale.UK, "%.2f", p);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
