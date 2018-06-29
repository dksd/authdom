package com.authdon.dksd.authdon;

public class PushService {

    private SpringBootWebSocketClient client = new SpringBootWebSocketClient();

    public PushService() {
        client.setId("sub-001");
    }

    public void connect(StompMessageListener listener) {
        TopicHandler handler = client.subscribe("/topic/greetings");
        handler.addListener(listener);
        client.connect("wss://localhost/gs-guide-websocket");
    }

    public void send(String msg, StompMessageListener listener) {
        if (client.isConnected()) {
            connect(listener);
        }
        client.send(msg);
    }

    public boolean isConnected() {
        return client.isConnected();
    }
}
