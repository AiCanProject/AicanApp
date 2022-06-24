package com.aican.aicanappnoncfr.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class AlarmBackgroundService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while(true){

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
