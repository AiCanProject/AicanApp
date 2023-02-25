package com.aican.aicanapp.specificactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import com.aican.aicanapp.R;
import com.aican.aicanapp.wifiReceiver.WifiReceiver;

public class AvailableWifiDevices extends AppCompatActivity {
    Button btnOpenSettings, sendData;
    RecyclerView recView;
    WifiManager wifiManager;
    private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;
    WifiReceiver receiverWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_wifi_devices);
        btnOpenSettings = findViewById(R.id.btnOpenSettings);
        recView = findViewById(R.id.recView);
        sendData = findViewById(R.id.sendData);

        btnOpenSettings.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        });

        sendData.setOnClickListener(v -> {
            Intent intent = new Intent(AvailableWifiDevices.this, ConnectDeviceActivity.class);
            startActivity(intent);
        });

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
//            Toast.makeText(getApplicationContext(), "Turning WiFi ON...", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        if (ActivityCompat.checkSelfPermission(AvailableWifiDevices.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    AvailableWifiDevices.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            wifiManager.startScan();
        }

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        receiverWifi = new WifiReceiver(wifiManager, recView);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiverWifi, intentFilter);
        getWifi();
    }



    private void getWifi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Toast.makeText(AvailableWifiDevices.this, "version >= marshmallow", Toast.LENGTH_SHORT).show();
            if (ContextCompat.checkSelfPermission(AvailableWifiDevices.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(AvailableWifiDevices.this, "location turned off", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(AvailableWifiDevices.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
            } else {
//                Toast.makeText(AvailableWifiDevices.this, "location turned on", Toast.LENGTH_SHORT).show();
                wifiManager.startScan();
            }
        } else {
//            Toast.makeText(AvailableWifiDevices.this, "scanning", Toast.LENGTH_SHORT).show();
            wifiManager.startScan();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiverWifi);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(AvailableWifiDevices.this, "permission granted", Toast.LENGTH_SHORT).show();
                wifiManager.startScan();
            } else {
                Toast.makeText(AvailableWifiDevices.this, "permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
}