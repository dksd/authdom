package com.authdon.dksd.authdon;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class PushService {

    private WebSocketClient client;

    public PushService() {
    }

    public void connect() throws URISyntaxException {
        client = new WebSocketClient(new URI("wss://londonary.com:8100")) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.i("WS", " open: " + handshakedata);
            }

            @Override
            public void onMessage(String message) {
                Log.i("WS", "msg: " + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i("WS", " closed: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                Log.e("WS", " error: ", ex);
            }
        };
        client.connect();
    }

    public void send(String msg) throws URISyntaxException {
        if (client == null || !client.isOpen()) {
            connect();
        }
        client.send(msg);
    }

    public boolean isOpen() {
        if (client == null) {
            return false;
        }
        return client.isOpen();
    }
}
