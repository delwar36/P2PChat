package com.example.wifidirectchat.connection;

public abstract class IMessenger extends Thread {
    public abstract void send(String text, boolean isMessage);

    public abstract void DestroySocket();
}
