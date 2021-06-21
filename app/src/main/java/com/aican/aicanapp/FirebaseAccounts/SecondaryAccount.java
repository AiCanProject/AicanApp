package com.aican.aicanapp.FirebaseAccounts;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

public class SecondaryAccount {
    private static final String api = "AIzaSyBaJcbF3nZ7cPlvD4Nix410QU4y5D7SkaE";
    private static final String app = "1:475070793969:android:ecd6c5d66b88c94f948150";
    private static final String database = "https://labdevices-b2a50-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final String project = "labdevices-b2a50";

    public static FirebaseApp getInstance(Context context) {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setProjectId(project)
                .setApplicationId(app)
                .setApiKey(api)
                .setDatabaseUrl(database)
                .build();

        try {
            FirebaseApp app = FirebaseApp.initializeApp(context, options, "secondary");
            FirebaseDatabase.getInstance(app).setPersistenceEnabled(true);
        } catch (IllegalStateException e) {
            //Ignore
        }

        return FirebaseApp.getInstance("secondary");
    }
}
