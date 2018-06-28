package com.authdon.dksd.authdon;

public interface StompMessageListener {
    void onMessage(StompMessage message);
}