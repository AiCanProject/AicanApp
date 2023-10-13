package com.aican.aicanapp.interfaces;

public interface WebSocketInit {
    void initWebSocket(String deviceID);

    void updateToggle();

    void cancelWebSocket(String deviceID);
}
