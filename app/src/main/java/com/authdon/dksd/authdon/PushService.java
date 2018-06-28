package com.authdon.dksd.authdon;

public class PushService {

    private SpringBootWebSocketClient client = new SpringBootWebSocketClient();

    public PushService() {
        client.setId("sub-001");
    }

    public void startListening(StompMessageListener listener) {
        TopicHandler handler = client.subscribe("/topic/greetings");
        handler.addListener(listener);
        client.connect("ws://localhost:8080/gs-guide-websocket");
        //Thread.sleep(60000L);
        //client.disconnect();
    }

    public void send(String msg) {
        if (client.isConnected()) {
            startListening();
        }
        client.send(msg);
    }

    public boolean isConnected() {
        return client.isConnected();
    }
}
