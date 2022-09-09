package com.aican.aicanapp.wifiReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WifiReceiver extends BroadcastReceiver {
    WifiManager wifiManager;
    StringBuilder sb;
    RecyclerView wifiDeviceList;

    public WifiReceiver(WifiManager wifiManager, RecyclerView wifiDeviceList) {
        this.wifiManager = wifiManager;
        this.wifiDeviceList = wifiDeviceList;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            sb = new StringBuilder();
            List<ScanResult> wifiList = wifiManager.getScanResults();

            Toast.makeText(context, sb, Toast.LENGTH_SHORT).show();
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);

            wifiDeviceList.setHasFixedSize(true);
            wifiDeviceList.setLayoutManager(layoutManager);
            wifiAdapter arrayAdapter = new wifiAdapter(context, wifiList);
            arrayAdapter.notifyDataSetChanged();
            wifiDeviceList.setAdapter(arrayAdapter);

        }
    }
}