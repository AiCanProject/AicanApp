package com.aican.aicanapp.graph;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import com.aican.aicanapp.MainActivity;
import com.aican.aicanapp.utils.PlotGraphNotifier;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;


public class ForegroundService extends Service {
    private static final String TAG = "ForegroundService";
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    private static final int JOB_ID = 1;
    MyBinder myBinder = new MyBinder();

//    public static void enqueueWork(Context context, Intent intent) {
//        Log.e("In", "EnqueueWork");
////        enqueueWork(context, ForegroundService.class, JOB_ID, intent);
//    }

    @Override
    public void onCreate() {
        Log.e("In", "onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("In", "onStartCommand");
        onHandleWork(intent);
        return START_NOT_STICKY;
    }

    protected void onHandleWork(@NonNull Intent intent) {
        Log.e("In", "onHandleWork");

        Log.e("In", "Service Start");
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        startLogging();

        Log.e("In", "Service End");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.e("In", "OnDestroy");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        return myBinder;
    }

    public void clearEntries() {
        entries.clear();
    }

    public class MyBinder extends Binder {
        public ForegroundService getService() {
            return ForegroundService.this;
        }
    }


    private static boolean isRunning = false;
    private static Class<?> currentClass = null;
    private static String type = null;
    private static String deviceId = null;
    private static DatabaseReference currentRef = null;
    float value=0.0F;
    private ArrayList<Entry> entries = new ArrayList<>();
//    FillGraphDataTask fillGraphDataTask;
//    OnEntryAddedCallback onEntryAddedCallback;

//    public void setOnEntryAddedCallback(OnEntryAddedCallback callback){
//        this.onEntryAddedCallback = callback;
//    }
//
//    public void removeOnEntryAddedCallback(){
//        onEntryAddedCallback = null;
//    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
            Float newVal = snapshot.getValue(Float.class);
            if(newVal == null) return;
            value = newVal;
        }

        @Override
        public void onCancelled(@NonNull @NotNull DatabaseError error) {

        }
    };

    public static void setInitials(String deviceId, DatabaseReference ref, Class<?> c, @Nullable String type){
        ForegroundService.deviceId = deviceId;
        currentClass = c;
        ForegroundService.type = type;
        currentRef = ref;
    }

    PlotGraphNotifier plotGraphNotifier;
    private void startLogging(){
        if(isRunning) return;
        isRunning = true;
        value = 0F;
        entries.clear();
//        fillGraphDataTask = new FillGraphDataTask();
        currentRef.addValueEventListener(valueEventListener);
//        fillGraphDataTask.execute();
        long start = System.currentTimeMillis();
        plotGraphNotifier = new PlotGraphNotifier(2000, () -> {
            long seconds = (System.currentTimeMillis()-start)/1000;
            Entry entry = new Entry(seconds, value);
            entries.add(entry);
            Log.d(TAG, "on entry: ");
        });
    }

    public ArrayList<Entry> stopLogging(Class<?> c){
        if(currentClass != c){
            return null;
        }

        if(currentRef!=null){
            currentRef.removeEventListener(valueEventListener);
        }
        plotGraphNotifier.stop();
//        fillGraphDataTask.stopRunning();
//        fillGraphDataTask.cancel(true);
//        removeOnEntryAddedCallback();
        isRunning = false;
        stopSelf();
        return entries;
    }

    public static boolean isRunning(){
        return isRunning;
    }
    public static boolean isMyClassRunning(String deviceId, Class<?> c){
        if(isRunning && currentClass == c && deviceId.equals(ForegroundService.deviceId)){
            return true;
        }
        return false;
    }
    public static boolean isMyTypeRunning(String deviceId, Class<?> c, String type){
        return isMyClassRunning(deviceId, c) && ForegroundService.type.equals(type);
    }
    public ArrayList<Entry> getEntries(){
        return entries;
    }

    class FillGraphDataTask extends AsyncTask<Void, Void, Void> {

        Long start;
        boolean running=true;
//        OnEntryAddedCallback onEntryAddedCallback;

//        public FillGraphDataTask(OnEntryAddedCallback onEntryAddedCallback) {
//            this.onEntryAddedCallback = onEntryAddedCallback;
//        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            start = System.currentTimeMillis();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground: ");
            while (running){
                publishProgress();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            long seconds = (System.currentTimeMillis()-start)/1000;
            Entry entry = new Entry(seconds, value);
            entries.add(entry);
            Log.d(TAG, "onProgressUpdate: "+entry.toString());
//            if(onEntryAddedCallback!=null)
//                onEntryAddedCallback.onEntryAdded(entry);
//            LineData data = lineChart.getData();
//            data.addEntry(new Entry(seconds, ph), 0);
//            lineChart.notifyDataSetChanged();
//            data.notifyDataChanged();
//            lineChart.invalidate();
        }

        void stopRunning(){
            running=false;
        }
    }
}