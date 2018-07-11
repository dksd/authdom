package com.authdon.dksd.authdon;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class PushService {
    private Gson gson = new GsonBuilder().create();

    private WebSocketClient client;

    public PushService() {

    }

    public void connect() throws URISyntaxException {
        client = new WebSocketClient(new URI("wss://londonary.com:8001")) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.i("WS", " open: " + handshakedata);
                User user = new User();
                user.setMsgType("RegisterUser");
                //user.setToken(UUID.randomUUID().toString());
                user.setEmail(user.getEmail());
                client.send(gson.toJson(user));
            }

            @Override
            public void onMessage(String message) {
                Log.i("WS", "msg: " + message);
                User user = gson.fromJson(message, User.class);
                if (user.getMsgType().equals("AuthenticateUser")) {
                    //Ask notification question here
                    //If approved
                    user.setMsgType("Approved");
                    //or if denied
                    user.setMsgType("Denied");
                    //user.setToken(UUID.randomUUID().toString()); //WSe should receive two tokens,
                    //one for approval and for denial.
                    user.setEmail(user.getEmail());
                    client.send(gson.toJson(user));
                }
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
