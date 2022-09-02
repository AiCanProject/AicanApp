package com.aican.aicanapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.aican.aicanapp.Dashboard.*;
import com.aican.aicanapp.FirebaseAccounts.DeviceAccount;
import com.aican.aicanapp.FirebaseAccounts.SecondaryAccount;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private Button login, register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        login = findViewById(R.id.main_login_btn);
//        register = findViewById(R.id.main_register_btn);

        // Login button on click listener
//        login.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                checkLogin();
//            }
//        });

        DatabaseReference ref = FirebaseDatabase.getInstance(SecondaryAccount.getInstance(this)).getReference();
        ref.child("DEVICE1").get().addOnSuccessListener(dataSnapShot->{
            ref.child("EPT2001").setValue(dataSnapShot.getValue());
//            DeviceAccount account = dataSnapShot.getValue(DeviceAccount.class);
//            initialiseFirebaseForDevice("DEVICE1", account);
//            DatabaseReference ref = FirebaseDatabase.getInstance(FirebaseApp.getInstance("DEVICE1")).getReference().child("PHMETER");
//            ref.child("DEVICE1").get().addOnSuccessListener(data->{
//                ref.child("EPT2001").setValue(data.getValue());
//            });
        });

    }
    // Go to DashBoard
    public void checkLogin(){
        Intent toDashBoard = new Intent(MainActivity.this, Dashboard.class);
        startActivity(toDashBoard);
    }

    private void initialiseFirebaseForDevice(String deviceId, DeviceAccount deviceAccount) {
        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setApiKey(deviceAccount.api)
                .setApplicationId(deviceAccount.app)
                .setDatabaseUrl(deviceAccount.database)
                .setProjectId(deviceAccount.project)
                .build();

        try {
            FirebaseApp app = FirebaseApp.initializeApp(this, firebaseOptions, deviceId);
            FirebaseDatabase.getInstance(app).setPersistenceEnabled(true);
        } catch (IllegalStateException e) {
            //Ignore
        }
    }

}