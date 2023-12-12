package com.aican.aicanapp.interfaces;

import org.json.JSONObject;

public interface DeviceConnectionInfo {
    void onDisconnect(String frag, String deviceID, String message, JSONObject lastJsonData);
    void onReconnect(String frag, String deviceID, String message, JSONObject lastJsonData);
}
