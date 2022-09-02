package com.aican.aicanapp.Services;

import static androidx.camera.core.CameraX.getContext;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.utils.AlarmConstants;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class alarmBackgroundService extends Service {
    DatabaseReference deviceRef;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        deviceRef = FirebaseDatabase.getInstance(FirebaseApp.getInstance(PhActivity.DEVICE_ID)).getReference().child("PHMETER").child(PhActivity.DEVICE_ID);
        Log.d("Alarm","Background service is running");

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Alarm","Background service is running");
                        while(true){
                            try{
                                deviceRef.child("Data").child("PH_VAL").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Float ph = snapshot.getValue(Float.class);
                                        if (ph == null) return;

                                        if(ph> AlarmConstants.maxPh || ph<AlarmConstants.minPh){
                                            AlarmConstants.ringtone.play();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }catch (Exception e){
                                Log.e("Service",""+e.getMessage());
                            }
                        }
                    }
                }
        );

        return START_STICKY;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Alarm","Background service is destroyed");

    }
}
