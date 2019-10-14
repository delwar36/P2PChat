package com.example.p2pchat.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.p2pchat.WiFiDirectBroadcastReceiver;
import com.example.p2pchat.connection.Client;
import com.example.p2pchat.connection.IMessenger;
import com.example.p2pchat.connection.Server;
import com.example.p2pchat.connection.WIFIDirectConnections;
import com.example.p2pchat.db.MessageRepository;
import com.example.p2pchat.model.MessageEntity;

import java.net.InetAddress;
import java.util.List;

public class ChatPageViewModel extends AndroidViewModel {
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private Application app;
    private WiFiDirectBroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;
    private WIFIDirectConnections connections;
    private IMessenger messenger;
    private String addressee;
    private MessageRepository repository;


    // Folds off when we find a diva (Observer shows chat page)
    private MutableLiveData<Boolean> chatIsReady;

    // is automatically removed from the database when a new message is added to the database with the corresponding name
    private LiveData<List<MessageEntity>> messageList;

    // Shuts off when a new contact divisor appears
    private MutableLiveData<List<WifiP2pDevice>> peerList;

    // closes when one of the divas closes the chat (the web server delivers the appropriate message)
    private MutableLiveData<Boolean> chatClosed;

    private boolean isConnected = false;

    public ChatPageViewModel(@NonNull final Application application) {
        super(application);
        app = application;
        wifiP2pManager = (WifiP2pManager) app.getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(app.getApplicationContext(), app.getMainLooper(), null);
        WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if (!info.groupFormed) return;
                if (isConnected) return;
                isConnected = true;

                Log.d("new connection", info.toString());
                final InetAddress address = info.groupOwnerAddress;
                if (info.isGroupOwner) {
                    Server server = new Server(ChatPageViewModel.this, chatIsReady);
                    server.start();
                    messenger = server;
                } else {
                    Client client = new Client(address.getHostAddress(), ChatPageViewModel.this, chatIsReady);
                    client.start();
                    messenger = client;
                }
                Toast.makeText(application, "The connection to the device is firmly established", Toast.LENGTH_SHORT).show();
            }
        };


        WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                Log.e("new peer", peers.toString());

                if (connections != null) {
                    if (!connections.updateDeviceList(peers)) return;
                    if (connections.getDeviceCount() > 0 && !isConnected) {
                        peerList.postValue(connections.getDeviceList());
                    }
                }
            }
        };
        broadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, peerListListener, connectionInfoListener);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        connections = new WIFIDirectConnections();
        repository = MessageRepository.getInstance();
        chatIsReady = new MutableLiveData<>();
        messageList = new MutableLiveData<>();
        peerList = new MutableLiveData<>();
        chatClosed = new MutableLiveData<>();
        registerReceiver();
    }

    public void setAddressee(String addressee) {
        this.addressee = addressee;
        messageList = repository.getAllMessages(this.addressee);
    }

    public String getAddressee() {
        return addressee;
    }

    public MutableLiveData<Boolean> chatIsReady() {
        return chatIsReady;
    }

    public LiveData<List<MessageEntity>> getMessageList() {
        return messageList;
    }

    public MutableLiveData<List<WifiP2pDevice>> getPeerList() {
        return peerList;
    }

    public MutableLiveData<Boolean> getChatClosed() {
        return chatClosed;
    }


    public void registerReceiver() {
        app.getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);
    }


    public void unregisterBroadcast() {
        app.getApplicationContext().unregisterReceiver(broadcastReceiver);
    }

    public void startSearch() {
        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("", "success peer discovery");
            }

            @Override
            public void onFailure(int reason) {
                Log.d("", "fail peer discovery");
            }
        });
    }

    public void connectToPeer(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("", "connection success");
            }

            @Override
            public void onFailure(int reason) {
                Log.d("", "connection fail");
            }
        });
    }

    public void sendMessage(String text) {
        messenger.send(text, true);
    }

    public void closeChat() {
        if (wifiP2pManager != null && channel != null) {
            wifiP2pManager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && wifiP2pManager != null && channel != null
                            && group.isGroupOwner()) {
                        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d("groupRemoveSuccess", "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d("groupRemoveFail", "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }
            });
        }


        if (messenger != null) {
            messenger.DestroySocket();
        }
        if(isConnected)
            chatClosed.postValue(true);
    }

    public void deleteChat() {
        repository.deleteAllFrom(addressee);
    }

}
