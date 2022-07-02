package com.aican.aicanappnoncfr.Services;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aican.aicanappnoncfr.data.DatabaseHelper;
import com.aican.aicanappnoncfr.fragments.ph.phAlarmFragment;
import com.aican.aicanappnoncfr.specificactivities.PhActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class AlarmBackgroundService extends Service {

    String PhValue;
    float phForm;
    float phv = 0;
    int num;
    Ringtone ringtone;
    DatabaseReference deviceReference;
    DatabaseHelper dbHelper;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        deviceReference = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        dbHelper = new DatabaseHelper(getApplicationContext());

        ringtone = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));

        if(intent!=null){
            Bundle bundle = intent.getExtras();
            PhValue = bundle.getString("alarm");
            phv = Float.parseFloat(PhValue);
            Log.d("checkph", String.valueOf(phv));
        } else {
            Toast.makeText(getApplicationContext(), "Failed value", Toast.LENGTH_SHORT).show();
        }


        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while(true){

                            //getting the value of ph from firebase in realtime in background
                            deviceReference.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    Float ph = snapshot.getValue(Float.class);
                                    if (ph == null) return;
                                    phForm = Float.parseFloat(String.valueOf(ph));
                                }
                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                }
                            });

                            if(phForm>phv){
                                ringtone.play();
                            } else {
                                ringtone.stop();
                            }

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
