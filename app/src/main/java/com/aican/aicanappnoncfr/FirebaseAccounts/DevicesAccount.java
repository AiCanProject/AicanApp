package com.aican.aicanappnoncfr.FirebaseAccounts;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

public class DevicesAccount {
    private static final String api = "AIzaSyBCMSq_W19pumvrgSbDYFSWRkE2GxNFOVo";
    private static final String app = "1:817311803017:android:e2604e56c2843e85e98858";
    private static final String database = "https://labdevices-8c9a5-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final String project = "labdevices-8c9a5";

    public static FirebaseApp getInstance(Context context) {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setProjectId(project)
                .setApplicationId(app)
                .setApiKey(api)
                .setDatabaseUrl(database)
                .build();

        try {
            FirebaseApp app = FirebaseApp.initializeApp(context, options, "devicesacc");
            FirebaseDatabase.getInstance(app).setPersistenceEnabled(true);

        } catch (IllegalStateException e) {
            //Ignore
        }

        return FirebaseApp.getInstance("devicesacc");
    }
}
