package com.aican.aicanapp.webSocket;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class LocalWebSocketListener extends WebSocketListener {

    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
        super.onOpen(webSocket, response);
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        super.onClosed(webSocket, code, reason);
    }

    @Override
    public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        super.onMessage(webSocket, text);
    }

}
