package com.authdon.dksd.authdon;

public class PushService {

    SpringBootWebSocketClient client = new SpringBootWebSocketClient();

    public PushService() {
        client.setId("sub-001");
    }

    public void startListening() {
        TopicHandler handler = client.subscribe("/topic/greetings");
        handler.addListener(new StompMessageListener() {
            @Override
            public void onMessage(StompMessage message) {
                System.out.println(message.getHeader("destination") + ": " + message.getContent());
            }
        });
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
}
