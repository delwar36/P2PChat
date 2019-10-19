package com.example.p2pchat.connection;

import java.io.File;

public abstract class IMessenger extends Thread {
    public abstract void send(String text, boolean isMessage);

    public abstract void DestroySocket();
}
