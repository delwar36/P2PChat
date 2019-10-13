package com.example.wifidirectchat.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.example.wifidirectchat.Constants;
import com.example.wifidirectchat.db.MessageRepository;
import com.example.wifidirectchat.model.ChatHistoryEntity;
import com.example.wifidirectchat.view.ChatActivity;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private LiveData<List<ChatHistoryEntity>> history;
    private MessageRepository repository;

    public MainViewModel(@NonNull Application application) {
        super(application);
        MutableLiveData<Boolean> searchStatus = new MutableLiveData<>();
        repository = MessageRepository.getInstance();
        history = repository.getAllChats();
    }


    public void startSearch() {
        Intent intent = new Intent(getApplication(), ChatActivity.class);
        intent.putExtra(Constants.IS_OFFLINE, false);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(intent);
    }


    public LiveData<List<ChatHistoryEntity>> getHistory() {
        return history;
    }

    public void clearHistory() {
        repository.deleteAll();
    }
}
