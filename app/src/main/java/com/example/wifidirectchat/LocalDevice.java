package com.example.wifidirectchat;

import android.net.wifi.p2p.WifiP2pDevice;


public class LocalDevice {
    private WifiP2pDevice device;
    private static final LocalDevice instance = new LocalDevice();

    public static LocalDevice getInstance() {
        return instance;
    }

    private LocalDevice() {
        device = new WifiP2pDevice();
    }

    void setDevice(WifiP2pDevice device) {
        this.device = device;
    }

    public WifiP2pDevice getDevice() {
        return device;
    }
}
