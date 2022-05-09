package com.aican.aicanappnoncfr.FirebaseAccounts;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

public class PrimaryAccount {
    private static final String api = "AIzaSyCclyBT0kF5pNxRoMbd2bstvOunh4Fjcbg";
    private static final String app = "1:172941175818:android:baba92bbc5bf73f3d6c3f3";
    private static final String database = "https://labappusers-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final String project = "labappusers";

    public static FirebaseApp getInstance(Context context) {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setProjectId(project)
                .setApplicationId(app)
                .setApiKey(api)
                .setDatabaseUrl(database)
                .build();

        try {
            FirebaseApp app = FirebaseApp.initializeApp(context, options, "primary");
            FirebaseDatabase.getInstance(app).setPersistenceEnabled(true);

        } catch (IllegalStateException e) {
            //Ignore
        }

        return FirebaseApp.getInstance("primary");
    }
}
