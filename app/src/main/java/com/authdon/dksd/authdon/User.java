package com.authdon.dksd.authdon;


public class User {
    private String msgType;
    private String email;
    private String token;

    public String getMsgType() {
        return msgType;
    }

    public String getEmail() {
        return email;
    }

    public void setToken(String uuid) {
        this.token = uuid;
    }

    public String getToken() {
        return token;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMsgType(String msgtype) {
        this.msgType = msgtype;
    }
}
