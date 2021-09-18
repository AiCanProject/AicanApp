package com.aican.aicanapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.fragments.temp.SetTempFragment;
import com.aican.aicanapp.specificactivities.TemperatureActivity;

public class TempService extends Service {
    public static final String CHANNEL_ID="TemperatureServiceChannel";
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String div = intent.getStringExtra("device");
        int temp1 = intent.getIntExtra("temp1",0);
        int temp2 = intent.getIntExtra("temp2",0);
        int reg=intent.getIntExtra("reg",0);
        int mode=intent.getIntExtra("mode",0);
        createNotificationChannel();
        Intent notificationIntent = new Intent(this,Dashboard.class);
        notificationIntent.putExtra("reg",reg);
        notificationIntent.putExtra("mode",mode);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Data of Device")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Device State : "+div+"\n"
                        +"Temperature1 : "+temp1+"°C\n"
                        +"Temperature2 : "+temp2+"°C"))
                .setSmallIcon(R.drawable.notification)
                .setColor(Color.RED)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
