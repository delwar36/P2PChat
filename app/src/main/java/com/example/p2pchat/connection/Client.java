package com.example.p2pchat.connection;

import android.arch.lifecycle.MutableLiveData;
import android.os.Environment;
import android.util.Log;

import com.example.p2pchat.LocalDevice;
import com.example.p2pchat.db.MessageRepository;
import com.example.p2pchat.model.MessageEntity;
import com.example.p2pchat.viewmodel.ChatPageViewModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class Client extends IMessenger {
    private Socket socket;
    private String peerName;
    private String host;
    private ChatPageViewModel model;
    private MutableLiveData<Boolean> isConnected;

    public Client(String host, ChatPageViewModel model, MutableLiveData<Boolean> isConnected) {
        this.host = host;
        this.isConnected = isConnected;
        this.model = model;
    }

    @Override
    public void run() {
        this.socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, 8888), 5000);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        send(LocalDevice.getInstance().getDevice().deviceName, false);


        boolean isAddresseeSet = false;

        while (socket != null) {
            try {
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                String messageText = (String) inputStream.readObject();
                if (messageText != null && !messageText.contains("~")) {
                    if (isAddresseeSet) {

                        Date c = Calendar.getInstance().getTime();
                        MessageEntity message = new MessageEntity(messageText, c, peerName, false);
                        MessageRepository.getInstance().insert(message);
                    } else {

                        isAddresseeSet = true;
                        peerName = messageText;
                        model.setAddressee(messageText);
                        isConnected.postValue(true);
                    }
                }
                if (messageText != null && messageText.contains("~")){
                    writeToFile("Shared", messageText);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                             model.closeChat();
            }
        }
    }

    private void writeToFile(String shared, String messageText) {
        long time= System.currentTimeMillis();
        String timeMill = Long.toString(time);
        File defaultDir = Environment.getExternalStorageDirectory();
        File file = new File(defaultDir, shared+timeMill+".txt");
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(file, false);
            stream.write(messageText.getBytes());
        } catch (FileNotFoundException e) {
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }

    @Override
    public void send(final String text, final boolean isMessage) {
        new Thread() {
            @Override
            public void run() {
                if (socket == null) return;
                try {
                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

                    outputStream.writeObject(text);
                    outputStream.flush();
                    if (isMessage) {
                        Date c = Calendar.getInstance().getTime();
                        MessageEntity message = new MessageEntity(text, c, peerName, true);
                        MessageRepository.getInstance().insert(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }.start();

    }


    @Override
    public void DestroySocket() {
        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
