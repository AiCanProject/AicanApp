package com.aican.aicanapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.R;
import com.aican.aicanapp.fragments.temp.SetTempFragment;

import com.aican.aicanapp.specificactivities.TemperatureActivity;

public class TempService extends Service {
    public static final String CHANNEL_ID="TemperatureServiceChannel";
    public static int temp1=0;
    public static int temp2=0;
    public static int reg=1;
    public static int mode=0;
    public static int setTemp=0;
    public static String device="";
    private static boolean isRunning=false;

    public MyBinder myBinder=new MyBinder();
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        isRunning=true;
        createNotificationChannel();
        Intent notificationIntent = new Intent(this,Dashboard.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Data of Device")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Device State : "+device+"\n"
                        +"Temperature1 : "+temp1+"°C\n"
                        +"Temperature2 : "+temp2+"°C"))
                .setSmallIcon(R.drawable.notification)
                .setColor(Color.RED)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(2, notification);
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID,
                    "TemperatureServiceChannel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public static void dataInitialize(String Device,int Temp1,int Temp2,int Reg,int Mode,int SetTemp){
        TempService.device=Device;
        TempService.temp1=Temp1;
        TempService.temp2=Temp2;
        TempService.reg=Reg;
        TempService.mode=Mode;
        TempService.setTemp=SetTemp;
    }
    public static boolean isShowingNotification(){
        return isRunning;
    }

    public static int getSetTemp(){
        return setTemp;
    }
    public static int getSetMode(){
        return mode;
    }
    public static int getReg(){
        return reg;
    }

    public class MyBinder extends Binder{
        public TempService getTempService(){return TempService.this;};
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning=false;
        reg=1;
        mode=0;
        setTemp=0;
        temp1=0;
        temp2=0;
        device="";
    }
}
