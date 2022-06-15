package com.aican.aicanappnoncfr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.aican.aicanappnoncfr.Dashboard.*;
import com.aican.aicanappnoncfr.FirebaseAccounts.DeviceAccount;
import com.aican.aicanappnoncfr.FirebaseAccounts.SecondaryAccount;
import com.aican.aicanappnoncfr.Services.LogBackgroundService;
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





}