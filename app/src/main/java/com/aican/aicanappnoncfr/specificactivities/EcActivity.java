package com.aican.aicanappnoncfr.specificactivities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.aican.aicanappnoncfr.R;

public class EcActivity extends AppCompatActivity {

    public static final String DEVICE_TYPE_KEY = "device_type";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ec);
    }
}